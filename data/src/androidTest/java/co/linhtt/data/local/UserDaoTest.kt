package co.linhtt.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import co.linhtt.data.db.UserDao
import co.linhtt.data.db.UserDatabase
import co.linhtt.data.db.entities.UserDetailEntity
import co.linhtt.data.db.entities.UserEntity
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UserDaoTest {
    private lateinit var database: UserDatabase
    private lateinit var userDao: UserDao

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            UserDatabase::class.java
        ).allowMainThreadQueries().build()
        userDao = database.userDao()
    }

    @Test
    fun insertUser_returnUserByLogin() = runBlocking {
        val user = UserEntity("test1", "avatar1.jpg", "test1.html")
        val user2 = UserEntity("test2", "avatar2.jpg", "test2.html")
        userDao.saveUsers(
            listOf(
                user,
                user2
            )
        )
        val testUserFromDB = userDao.getUserByLogin("test1").firstOrNull()
        Assert.assertEquals(user.login, testUserFromDB?.login)
        Assert.assertEquals(user.htmlUrl, testUserFromDB?.htmlUrl)
        Assert.assertEquals(user.avatarUrl, testUserFromDB?.avatarUrl)
    }

    @Test
    fun saveUserDetails_returnUserDetailsByLogin() = runBlocking {
        val userDetail = UserDetailEntity("test1", "David", 1, "VI", 1,"test1.html","avatar1.jpg", System.currentTimeMillis())
        userDao.saveUserDetails(userDetail)
        val testUserDetailFromDB = userDao.getUserDetailByLogin("test1")
        Assert.assertEquals(userDetail.login, testUserDetailFromDB?.login)
        Assert.assertEquals(userDetail.htmlUrl, testUserDetailFromDB?.htmlUrl)
        Assert.assertEquals(userDetail.avatarUrl, testUserDetailFromDB?.avatarUrl)
    }
}