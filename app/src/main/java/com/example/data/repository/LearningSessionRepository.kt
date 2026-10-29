package com.example.data.repository

import com.example.data.local.dao.LearningSessionDao
import com.example.data.local.entity.LearningSession
import kotlinx.coroutines.flow.Flow
import java.util.UUID

/**
 * Repositório para o Kudula Local Session Engine (KLSE).
 * Isola o acesso ao banco Room e oferece abstrações prontas para sincronização.
 */
class LearningSessionRepository(private val dao: LearningSessionDao) {

    val allSessions: Flow<List<LearningSession>> = dao.getAllSessions()
    val unsyncedSessions: Flow<List<LearningSession>> = dao.getUnsyncedSessions()
    val totalStudyMinutes: Flow<Int?> = dao.getTotalStudyMinutes()
    val sessionCount: Flow<Int> = dao.getSessionCount()

    fun getSessionByUuid(uuid: String): Flow<LearningSession?> = dao.getSessionByUuid(uuid)

    suspend fun recordSession(
        concept: String,
        sourceType: String,
        studyMinutes: Int,
        ikcPartial: Int,
        ipaPartial: Int,
        notes: String = ""
    ): LearningSession {
        val session = LearningSession(
            uuid = UUID.randomUUID().toString(),
            concept = concept,
            sourceType = sourceType,
            studyMinutes = studyMinutes,
            ikcPartial = ikcPartial,
            ipaPartial = ipaPartial,
            timestamp = System.currentTimeMillis(),
            synced = false,
            notes = notes
        )
        dao.insertSession(session)
        return session
    }

    suspend fun insertSession(session: LearningSession) {
        dao.insertSession(session)
    }

    suspend fun markAsSynced(uuid: String) {
        dao.markAsSynced(uuid)
    }

    suspend fun markAllAsSynced() {
        dao.markAllAsSynced()
    }

    suspend fun deleteSession(session: LearningSession) {
        dao.deleteSession(session)
    }

    suspend fun seedInitialSessionsIfEmpty() {
        // Reservado estritamente para testes; contas de produção começam 100% limpas
    }
}
