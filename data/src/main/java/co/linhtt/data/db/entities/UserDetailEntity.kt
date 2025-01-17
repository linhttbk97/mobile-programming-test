package co.linhtt.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
// relation to user
@Entity(
    tableName = "UserDetailEntity",
)
data class UserDetailEntity(
    @PrimaryKey val login: String,
    val name: String?,
    val followers: Int,
    val location: String?,
    val following: Int,
    val htmlUrl: String,
    val avatarUrl: String,
    val lastUpdatedAt:Long
)
