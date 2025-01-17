package co.linhtt.data.remote

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import co.linhtt.data.db.UserDatabase
import co.linhtt.data.db.entities.QueryEntity
import co.linhtt.data.db.entities.RemoteKey
import co.linhtt.data.db.entities.UserEntity
import javax.inject.Inject

@OptIn(ExperimentalPagingApi::class)
class UserRemoteMediator @Inject constructor(
    private val githubUserService: GithubUserService,
    private val database: UserDatabase
) : RemoteMediator<Int, UserEntity>() {
    companion object {
        const val GITHUB_USER_STARTING_PAGE_INDEX = 1
        const val ITEM_PER_PAGE = 20
        const val USERS_QUERY_CACHE_EXPIRED_TIME = 5 * 60 * 1000L // 5 minutes
    }

    override suspend fun initialize(): InitializeAction {
        val shouldFetchFirstPage = shouldFetchFromNetworkForPage(GITHUB_USER_STARTING_PAGE_INDEX)

        // If we have a cached query result, then we don't need to fetch the data from the network
        return if (shouldFetchFirstPage) {
            InitializeAction.LAUNCH_INITIAL_REFRESH
        } else {
            InitializeAction.SKIP_INITIAL_REFRESH
        }
    }

    override suspend fun load(
        loadType: LoadType, state: PagingState<Int, UserEntity>
    ): MediatorResult {
        val page = when (loadType) {
            // If refresh is called, then we start from the first page
            LoadType.REFRESH -> GITHUB_USER_STARTING_PAGE_INDEX
            // don't need to handle prepend case because we only load the first page on refresh
            LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)

            LoadType.APPEND -> {
                // Get the remote key of the last item that was retrieved
                // if remote key is null, then we can't request more data
                val remoteKeys = getRemoteKeyForLastItem(state)
                val nextKey = remoteKeys?.nextKey ?: return MediatorResult.Success(
                    endOfPaginationReached = remoteKeys != null
                )
                nextKey
            }
        }
        val shouldFetchThisPage = shouldFetchFromNetworkForPage(page)
        if (shouldFetchThisPage.not()) {
            return MediatorResult.Success(endOfPaginationReached = false)
        }
        try {
            val users = githubUserService.getUsers(page * ITEM_PER_PAGE, ITEM_PER_PAGE)
            val endOfPaginationReached =
                users.isEmpty()// if users empty, then we have reached the end of the pagination
            database.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    database.userDao().clearAll()
                    database.remoteKeyDao().clearAll()
                }
                val prevKey = if (page == GITHUB_USER_STARTING_PAGE_INDEX) null else page - 1
                val nextKey = if (endOfPaginationReached) null else page + 1
                val userEntities = mutableListOf<UserEntity>()
                val keys = users.map {
                    userEntities.add(it.toUserEntity())
                    RemoteKey(it.login, prevKey, nextKey)
                }
                database.remoteKeyDao().saveKeys(keys)
                database.userDao().saveUsers(userEntities)
                database.queryDao()
                    .saveQuery(QueryEntity(createUsersQueryKey(page), System.currentTimeMillis()))
            }
            return MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
        } catch (ex: Exception) {
            ex.printStackTrace()
            return MediatorResult.Error(ex)
        }
    }

    private suspend fun getRemoteKeyForLastItem(state: PagingState<Int, UserEntity>): RemoteKey? {
        // Get the last page that was retrieved, that contained items.
        // From that last page, get the last item
        return state.pages.lastOrNull() { it.data.isNotEmpty() }?.data?.lastOrNull()
            ?.let { githubUser ->
                // Get the remote keys of the last item retrieved
                database.remoteKeyDao().getRemoteKeys(githubUser.login)
            }
    }

    private fun createUsersQueryKey(page: Int) = "users/since=0&page=$page"

    private suspend fun shouldFetchFromNetworkForPage(page: Int): Boolean {
        val query = createUsersQueryKey(page)
        val queryEntity = database.queryDao().getByQuery(query)
        return queryEntity == null || System.currentTimeMillis() - queryEntity.lastUpdatedAt > USERS_QUERY_CACHE_EXPIRED_TIME
    }
}