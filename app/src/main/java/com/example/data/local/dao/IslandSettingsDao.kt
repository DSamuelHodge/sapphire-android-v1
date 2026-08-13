package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.IslandSettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface IslandSettingsDao {
    @Query("SELECT * FROM island_settings WHERE id = 1")
    fun getSettingsFlow(): Flow<IslandSettingsEntity?>

    @Query("SELECT * FROM island_settings WHERE id = 1")
    suspend fun getSettings(): IslandSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(settings: IslandSettingsEntity)

    @Update
    suspend fun update(settings: IslandSettingsEntity)
}
