package co.linhtt.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import co.linhtt.data.db.entities.QueryEntity
import co.linhtt.data.db.entities.RemoteKey
import co.linhtt.data.db.entities.UserDetailEntity
import co.linhtt.data.db.entities.UserEntity

@Database(entities = [UserEntity::class, RemoteKey::class, UserDetailEntity::class,QueryEntity::class], version = 1)
abstract class UserDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun remoteKeyDao(): RemoteKeyDao
    abstract fun queryDao(): QueryDao
}