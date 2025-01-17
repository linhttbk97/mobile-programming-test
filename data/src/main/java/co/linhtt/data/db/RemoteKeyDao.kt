package co.linhtt.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import co.linhtt.data.db.entities.RemoteKey

@Dao
interface RemoteKeyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveKeys(list: List<RemoteKey>)

    @Query("SELECT * FROM remoteKey WHERE userLogin = :id")
    suspend fun getRemoteKeys(id: String): RemoteKey?

    @Query("DELETE FROM remoteKey")
    suspend fun clearAll()
}