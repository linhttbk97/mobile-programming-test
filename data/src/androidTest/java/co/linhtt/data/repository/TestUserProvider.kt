package co.linhtt.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import co.linhtt.data.db.UserDatabase
import co.linhtt.data.remote.GithubUser
import co.linhtt.data.remote.GithubUserDetails
import co.linhtt.data.remote.GithubUserService
import co.linhtt.data.remote.UserRemoteMediator
import co.linhtt.domain.model.LoadResourceResult
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class TestUserProvider {
    private lateinit var database: UserDatabase
    private val githubUserService: GithubUserService = mockk()
    private lateinit var userRemoteMediator: UserRemoteMediator
    private lateinit var userProvider: UserProvider

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(), UserDatabase::class.java
        ).allowMainThreadQueries().build()
        userRemoteMediator = UserRemoteMediator(githubUserService, database)
        userProvider = UserProvider(database, userRemoteMediator, githubUserService)
    }

    @Test
    fun testGetUserDetailsByLogin_returnUserNotFound() = runBlocking {
        coEvery { githubUserService.getUserDetails(any()) } throws Exception("User not found")
        val userDetailState = userProvider.getUserByLogin("test").take(2).toList()
        assert(userDetailState.first() is LoadResourceResult.Loading)
        assert(userDetailState.last() is LoadResourceResult.Error)
    }

    @Test
    fun testGetUserDetailsByLogin_returnUserDetails() = runBlocking {
        val mockUserFromRemote = GithubUserDetails(
            login = "test",
            avatar_url = "avatar.jpg",
            html_url = "test.html",
            name = "David",
            followers = 1,
            location = "VI",
            following = 2
        )
        coEvery { githubUserService.getUserDetails(any()) } returns mockUserFromRemote
        val userDetailState = userProvider.getUserByLogin("test").take(2).toList()
        assert(userDetailState.first() is LoadResourceResult.Loading)
        val result = userDetailState.last()
        assert(result is LoadResourceResult.Success)
        val userDetails = (result as LoadResourceResult.Success).data
        assert(userDetails.login == mockUserFromRemote.login)
        assert(userDetails.avatarUrl == mockUserFromRemote.avatar_url)
        assert(userDetails.htmlUrl == mockUserFromRemote.html_url)
        assert(userDetails.name == mockUserFromRemote.name)
        assert(userDetails.followers == mockUserFromRemote.followers)
        assert(userDetails.location == mockUserFromRemote.location)
        assert(userDetails.following == mockUserFromRemote.following)
    }

    @OptIn(ExperimentalPagingApi::class)
    @Test
    fun testGetUserDetailsByLogin_returnUserDetailsFromLocal() = runBlocking {
        val mockUserFromRemote = GithubUserDetails(
            login = "test",
            avatar_url = "avatar.jpg",
            html_url = "test.html",
            name = "David",
            followers = 1,
            location = "VI",
            following = 2
        )
        coEvery { githubUserService.getUserDetails(any()) } returns mockUserFromRemote
        // perform load user details for the first time ->
        userProvider.getUserByLogin("test").take(2).toList()
        // the second time should return user details from local
        val userDetailState = userProvider.getUserByLogin("test").firstOrNull()
        assert(userDetailState is LoadResourceResult.Success)
        val userDetails = (userDetailState as LoadResourceResult.Success).data
        assert(userDetails.login == mockUserFromRemote.login)
        assert(userDetails.avatarUrl == mockUserFromRemote.avatar_url)
        assert(userDetails.htmlUrl == mockUserFromRemote.html_url)
        assert(userDetails.name == mockUserFromRemote.name)
        assert(userDetails.followers == mockUserFromRemote.followers)
        assert(userDetails.location == mockUserFromRemote.location)
        assert(userDetails.following == mockUserFromRemote.following)
    }
}