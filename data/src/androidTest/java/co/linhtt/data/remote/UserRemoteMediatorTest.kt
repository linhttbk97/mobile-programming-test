package co.linhtt.data.remote

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingConfig
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import co.linhtt.data.db.UserDatabase
import co.linhtt.data.db.entities.UserEntity
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class UserRemoteMediatorTest {
    private lateinit var database: UserDatabase
    private val githubUserService: GithubUserService = mockk()
    private lateinit var userRemoteMediator: UserRemoteMediator

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            UserDatabase::class.java
        ).allowMainThreadQueries().build()
        userRemoteMediator = UserRemoteMediator(githubUserService, database)
    }

    @OptIn(ExperimentalPagingApi::class)
    @Test
    fun testFirstPageReturnInvalid_returnPageResultSuccess() = runBlocking {
        coEvery { githubUserService.getUsers(any(), any()) } returns emptyList()
        val pagingState = PagingState<Int, UserEntity>(
            listOf(),
            null,
            PagingConfig(20),
            10
        )
        val result = userRemoteMediator.load(LoadType.REFRESH, pagingState)
        Assert.assertTrue(result is RemoteMediator.MediatorResult.Success && result.endOfPaginationReached)
    }

    @OptIn(ExperimentalPagingApi::class)
    @Test
    fun testFirstPageThrowException_returnPageResultError() = runBlocking {
        coEvery { githubUserService.getUsers(any(), any()) } throws Exception("error due to test")
        val pagingState = PagingState<Int, UserEntity>(
            listOf(),
            null,
            PagingConfig(20),
            10
        )
        val result = userRemoteMediator.load(LoadType.REFRESH, pagingState)
        Assert.assertTrue(result is RemoteMediator.MediatorResult.Error && result.throwable.cause?.message == "error due to test")
    }

    @OptIn(ExperimentalPagingApi::class)
    @Test
    fun testFirstPageReturnList_returnPageResultSuccess() = runBlocking {
        val usersForPage = (0..19).map {
            GithubUser("test${it}", "avatar of $it", "html of $it")
        }
        coEvery { githubUserService.getUsers(any(), any()) } returns usersForPage
        val pagingState = PagingState<Int, UserEntity>(
            listOf(),
            null,
            PagingConfig(20),
            10
        )
        val result = userRemoteMediator.load(LoadType.REFRESH, pagingState)
        Assert.assertTrue(result is RemoteMediator.MediatorResult.Success && result.endOfPaginationReached.not())
        val firstUser = database.userDao().getUserByLogin("test0").firstOrNull()
        val userByLogin = usersForPage.first { it.login == "test0" }
        Assert.assertEquals(firstUser?.login, userByLogin.login)
        Assert.assertEquals(firstUser?.avatarUrl, userByLogin.avatar_url)
        Assert.assertEquals(firstUser?.htmlUrl, userByLogin.html_url)
    }

    @OptIn(ExperimentalPagingApi::class)
    @Test
    fun testLoadPageAgainBefore5Minutes_returnDoNotCallNetwork() = runBlocking {
        val usersForPage = (0..19).map {
            GithubUser("test${it}", "avatar of $it", "html of $it")
        }
        coEvery { githubUserService.getUsers(any(), any()) } returns usersForPage
        val pagingState = PagingState<Int, UserEntity>(
            listOf(),
            null,
            PagingConfig(20),
            10
        )
        val result = userRemoteMediator.load(LoadType.REFRESH, pagingState)
        Assert.assertTrue(result is RemoteMediator.MediatorResult.Success && result.endOfPaginationReached.not())
        coVerify(exactly = 1) { githubUserService.getUsers(any(), any()) }
        // clear called count of githubUserService.getUsers
        clearMocks(githubUserService)
        val resultCalledSecondTime = userRemoteMediator.load(LoadType.REFRESH, pagingState)
        Assert.assertTrue(resultCalledSecondTime is RemoteMediator.MediatorResult.Success)
        coVerify(exactly = 0) { githubUserService.getUsers(any(), any()) }
    }

    @After
    fun tearDown() {
        database.clearAllTables()
    }


}