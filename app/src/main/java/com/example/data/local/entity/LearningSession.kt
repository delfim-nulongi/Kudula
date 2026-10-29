package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Kudula Local Session Engine (KLSE)
 * Entidade de persistência local para sessões cognitivas offline.
 * Preparada para futura sincronização com Firebase/PostgreSQL.
 */
@Entity(tableName = "learning_sessions")
data class LearningSession(
    @PrimaryKey
    val uuid: String = UUID.randomUUID().toString(),
    val concept: String,
    val sourceType: String, // PDF, YOUTUBE, QUIZ, CAMERA, CHAPTER, REVISION, FLASHCARD
    val studyMinutes: Int,
    val ikcPartial: Int,
    val ipaPartial: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val synced: Boolean = false,
    val remoteId: String? = null,
    val notes: String = "",
    val lastModified: Long = System.currentTimeMillis()
)
