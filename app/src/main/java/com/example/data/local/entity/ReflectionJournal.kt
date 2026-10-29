package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Cognitive Reflection Journal
 * Entidade de persistência para as reflexões cognitivas guiadas pelo Kudu.
 * Conectada ao Kudula Local Session Engine (KLSE) e preparada para sincronização.
 */
@Entity(tableName = "reflection_journals")
data class ReflectionJournal(
    @PrimaryKey
    val uuid: String = UUID.randomUUID().toString(),
    val text: String,
    val mood: String, // "Radiante", "Focado", "Estratégico", "Pensativo", "Cansado"
    val energy: Int, // 1 a 100
    val confidence: Int, // 1 a 100
    val tags: List<String>,
    val promptQuestion: String,
    val sessionId: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val synced: Boolean = false
)
