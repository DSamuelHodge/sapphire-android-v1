package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.AppFilterEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppFilterDao {
    @Query("SELECT * FROM app_filters ORDER BY appName ASC")
    fun getAllFiltersFlow(): Flow<List<AppFilterEntity>>

    @Query("SELECT * FROM app_filters WHERE packageName = :pkg LIMIT 1")
    suspend fun getFilterForPackage(pkg: String): AppFilterEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(filter: AppFilterEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(filters: List<AppFilterEntity>)

    @Query("UPDATE app_filters SET isEnabled = :isEnabled WHERE packageName = :packageName")
    suspend fun setAppEnabled(packageName: String, isEnabled: Boolean)

    @Query("DELETE FROM app_filters WHERE packageName = :packageName")
    suspend fun deleteFilter(packageName: String)
}
