package co.linhtt.data.db

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import co.linhtt.data.db.entities.UserDetailEntity
import co.linhtt.data.db.entities.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUsers(users: List<UserEntity>)

    @Query("SELECT * FROM UserEntity WHERE login = :login")
    fun getUserByLogin(login: String): Flow<UserEntity?>

    @Query("SELECT * FROM UserEntity")
    fun getUsers(): PagingSource<Int, UserEntity>

    @Query("DELETE FROM UserEntity")
    suspend fun clearAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUserDetails(userDetailEntity: UserDetailEntity)

    @Query("SELECT * FROM UserDetailEntity WHERE UserDetailEntity.login = :login")
    suspend fun getUserDetailByLogin(login: String): UserDetailEntity?
}