package com.example.data.repository

import com.example.data.local.dao.ReflectionJournalDao
import com.example.data.local.entity.ReflectionJournal
import kotlinx.coroutines.flow.Flow
import java.util.UUID

/**
 * Repositório para o Cognitive Reflection Journal.
 */
class ReflectionRepository(private val dao: ReflectionJournalDao) {

    val allReflections: Flow<List<ReflectionJournal>> = dao.getAllReflections()
    val unsyncedReflections: Flow<List<ReflectionJournal>> = dao.getUnsyncedReflections()

    fun getReflectionByUuid(uuid: String): Flow<ReflectionJournal?> = dao.getReflectionByUuid(uuid)

    suspend fun saveReflection(
        text: String,
        mood: String,
        energy: Int,
        confidence: Int,
        tags: List<String>,
        promptQuestion: String,
        sessionId: String? = null
    ): ReflectionJournal {
        val reflection = ReflectionJournal(
            uuid = UUID.randomUUID().toString(),
            text = text,
            mood = mood,
            energy = energy,
            confidence = confidence,
            tags = tags,
            promptQuestion = promptQuestion,
            sessionId = sessionId,
            timestamp = System.currentTimeMillis(),
            synced = false
        )
        dao.insertReflection(reflection)
        return reflection
    }

    suspend fun deleteReflection(reflection: ReflectionJournal) {
        dao.deleteReflection(reflection)
    }

    suspend fun markAsSynced(uuid: String) {
        dao.markAsSynced(uuid)
    }

    suspend fun seedInitialReflectionsIfEmpty() {
        // Reservado estritamente para testes; contas de produção começam 100% vazias
    }
}
