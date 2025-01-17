package co.linhtt.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class RemoteKey (
    @PrimaryKey
    val userLogin:String,
    val prevKey:Int?,
    val nextKey:Int?
)