package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.AnonymousSession
import kotlinx.coroutines.flow.Flow

@Dao
interface AnonymousSessionDao {

    @Query("SELECT * FROM anonymous_sessions ORDER BY createdAt DESC LIMIT 1")
    fun getActiveSession(): Flow<AnonymousSession?>

    @Query("SELECT * FROM anonymous_sessions ORDER BY createdAt DESC LIMIT 1")
    suspend fun getActiveSessionSync(): AnonymousSession?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: AnonymousSession)

    @Update
    suspend fun updateSession(session: AnonymousSession)

    @Query("UPDATE anonymous_sessions SET onboardingCompleted = 1 WHERE sessionId = :sessionId")
    suspend fun markOnboardingCompleted(sessionId: String)

    @Query("UPDATE anonymous_sessions SET firstWinUnlocked = 1 WHERE sessionId = :sessionId")
    suspend fun markFirstWinUnlocked(sessionId: String)

    @Query("UPDATE anonymous_sessions SET firebaseUid = :firebaseUid, linkedEmail = :email, synced = 1 WHERE sessionId = :sessionId")
    suspend fun linkAccount(sessionId: String, firebaseUid: String, email: String?)
}
