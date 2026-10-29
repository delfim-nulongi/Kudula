package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.ReflectionJournal
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object para as reflexões diárias cognitivas.
 */
@Dao
interface ReflectionJournalDao {

    @Query("SELECT * FROM reflection_journals ORDER BY timestamp DESC")
    fun getAllReflections(): Flow<List<ReflectionJournal>>

    @Query("SELECT * FROM reflection_journals WHERE uuid = :uuid LIMIT 1")
    fun getReflectionByUuid(uuid: String): Flow<ReflectionJournal?>

    @Query("SELECT * FROM reflection_journals WHERE synced = 0 ORDER BY timestamp ASC")
    fun getUnsyncedReflections(): Flow<List<ReflectionJournal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReflection(reflection: ReflectionJournal)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReflections(reflections: List<ReflectionJournal>)

    @Update
    suspend fun updateReflection(reflection: ReflectionJournal)

    @Delete
    suspend fun deleteReflection(reflection: ReflectionJournal)

    @Query("UPDATE reflection_journals SET synced = 1 WHERE uuid = :uuid")
    suspend fun markAsSynced(uuid: String)
}
