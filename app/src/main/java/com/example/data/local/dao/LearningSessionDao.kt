package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.LearningSession
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object para as sessões cognitivas locais (KLSE).
 */
@Dao
interface LearningSessionDao {

    @Query("SELECT * FROM learning_sessions ORDER BY timestamp DESC")
    fun getAllSessions(): Flow<List<LearningSession>>

    @Query("SELECT * FROM learning_sessions WHERE synced = 0 ORDER BY timestamp ASC")
    fun getUnsyncedSessions(): Flow<List<LearningSession>>

    @Query("SELECT * FROM learning_sessions WHERE uuid = :uuid LIMIT 1")
    fun getSessionByUuid(uuid: String): Flow<LearningSession?>

    @Query("SELECT SUM(studyMinutes) FROM learning_sessions")
    fun getTotalStudyMinutes(): Flow<Int?>

    @Query("SELECT COUNT(*) FROM learning_sessions")
    fun getSessionCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: LearningSession)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSessions(sessions: List<LearningSession>)

    @Update
    suspend fun updateSession(session: LearningSession)

    @Delete
    suspend fun deleteSession(session: LearningSession)

    @Query("UPDATE learning_sessions SET synced = 1 WHERE uuid = :uuid")
    suspend fun markAsSynced(uuid: String)

    @Query("UPDATE learning_sessions SET synced = 1")
    suspend fun markAllAsSynced()
}
