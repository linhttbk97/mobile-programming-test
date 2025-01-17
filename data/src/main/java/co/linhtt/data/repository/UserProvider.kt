package co.linhtt.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import co.linhtt.data.db.UserDatabase
import co.linhtt.data.db.entities.UserDetailEntity
import co.linhtt.data.remote.GithubUserService
import co.linhtt.data.remote.UserRemoteMediator
import co.linhtt.data.remote.UserRemoteMediator.Companion.ITEM_PER_PAGE
import co.linhtt.domain.model.GithubUser
import co.linhtt.domain.model.GithubUserDetails
import co.linhtt.domain.model.LoadResourceResult
import co.linhtt.domain.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UserProvider @Inject constructor(
    private val database: UserDatabase,
    private val remoteMediator: UserRemoteMediator,
    private val githubUserService: GithubUserService
) : UserRepository {
    companion object {
        const val CACHED_USER_DETAILS_EXPIRATION_TIME = 5 * 60 * 1000L // 5 minutes
    }

    @OptIn(ExperimentalPagingApi::class)
    override fun getUsers(): Flow<PagingData<GithubUser>> {
        return Pager(config = PagingConfig(pageSize = ITEM_PER_PAGE, enablePlaceholders = false),
            remoteMediator = remoteMediator,
            pagingSourceFactory = { database.userDao().getUsers() }).flow.map { pagingData ->
            pagingData.map { userEntity ->
                GithubUser(
                    login = userEntity.login,
                    avatarUrl = userEntity.avatarUrl,
                    htmlUrl = userEntity.htmlUrl
                )
            }
        }
    }

    /**
     * Get user details by login
     * If user details is cached, return cached data immediately
     * If user details is not cached or expired, fetch from remote then cache it
     * If fetch from remote fail, return error
     */
    override fun getUserByLogin(login: String): Flow<LoadResourceResult<GithubUserDetails>> =
        callbackFlow {
            val userDetailFromLocal = withContext(Dispatchers.IO) {
                database.userDao().getUserDetailByLogin(login)
            }
            if (userDetailFromLocal != null) {
                trySend(LoadResourceResult.Success(userDetailFromLocal.run {
                    GithubUserDetails(
                        login = login,
                        avatarUrl = avatarUrl,
                        htmlUrl = htmlUrl,
                        name = name,
                        followers = followers,
                        following = following,
                        location = location
                    )
                }))
            } else {
                trySend(LoadResourceResult.Loading)
            }
            // if user detail is not cached or expired, then fetch from remote
            if (userDetailFromLocal == null || System.currentTimeMillis() - userDetailFromLocal.lastUpdatedAt > CACHED_USER_DETAILS_EXPIRATION_TIME) {
                try {
                    val userDetailFromRemote = withContext(Dispatchers.IO) {
                        githubUserService.getUserDetails(login)
                    }
                    withContext(Dispatchers.IO) {
                        database.userDao().saveUserDetails(userDetailFromRemote.run {
                            UserDetailEntity(
                                login = login,
                                name = name,
                                followers = followers,
                                following = following,
                                location = location,
                                avatarUrl = avatar_url,
                                htmlUrl = html_url,
                                lastUpdatedAt = System.currentTimeMillis(),
                            )
                        })
                    }
                    trySend(LoadResourceResult.Success(userDetailFromRemote.run {
                        GithubUserDetails(
                            login = login,
                            avatarUrl = avatar_url,
                            htmlUrl = html_url,
                            name = name,
                            followers = followers,
                            following = following,
                            location = location
                        )
                    }))
                } catch (ex: Exception) {
                    // user details is not cached and fetch from remote fail -> return error
                    if (userDetailFromLocal == null) {
                        trySend(LoadResourceResult.Error("Fail to load user details: ${ex.message}"))
                    }
                }
            }
            awaitClose()
        }
}