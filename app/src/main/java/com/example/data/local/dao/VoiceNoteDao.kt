package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.VoiceNoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VoiceNoteDao {

    @Query("SELECT * FROM voice_notes ORDER BY timestamp DESC")
    fun getAll(): Flow<List<VoiceNoteEntity>>

    @Query("SELECT * FROM voice_notes WHERE id = :id")
    suspend fun getById(id: Long): VoiceNoteEntity?

    @Query("SELECT * FROM voice_notes WHERE synced = 0")
    suspend fun getPendingSync(): List<VoiceNoteEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(voiceNote: VoiceNoteEntity): Long

    @Update
    suspend fun update(voiceNote: VoiceNoteEntity)

    @Query("UPDATE voice_notes SET synced = 1 WHERE uuid = :uuid")
    suspend fun markSynced(uuid: String)

    @Delete
    suspend fun delete(voiceNote: VoiceNoteEntity)

    @Query("DELETE FROM voice_notes WHERE id = :id")
    suspend fun deleteById(id: Long)
}
