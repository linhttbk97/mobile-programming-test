package co.linhtt.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import co.linhtt.data.db.entities.QueryEntity

@Dao
interface QueryDao {
    @Insert(onConflict = androidx.room.OnConflictStrategy.REPLACE)
    suspend fun saveQuery(queryEntity: QueryEntity)

    @Query("SELECT * FROM QueryEntity WHERE `query` = :query")
    suspend fun getByQuery(query: String): QueryEntity?

    @Query("DELETE FROM QueryEntity")
    suspend fun clearAll()
}