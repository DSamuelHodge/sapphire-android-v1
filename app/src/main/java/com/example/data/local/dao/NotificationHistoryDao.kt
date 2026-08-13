package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.NotificationHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationHistoryDao {
    @Query("SELECT * FROM notification_history ORDER BY timestamp DESC LIMIT 100")
    fun getRecentHistoryFlow(): Flow<List<NotificationHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(history: NotificationHistoryEntity): Long

    @Query("DELETE FROM notification_history")
    suspend fun clearAll()

    @Query("DELETE FROM notification_history WHERE id = :id")
    suspend fun deleteById(id: Long)
}
