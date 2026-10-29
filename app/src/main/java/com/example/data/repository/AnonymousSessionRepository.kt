package com.example.data.repository

import com.example.data.local.dao.AnonymousSessionDao
import com.example.data.local.entity.AnonymousSession
import kotlinx.coroutines.flow.Flow
import java.util.UUID

/**
 * Repositório para gestão da Sessão Anónima (Anonymous Session) do utilizador.
 * Garante entrada instantânea no ecossistema sem atrito de autenticação inicial.
 */
class AnonymousSessionRepository(private val dao: AnonymousSessionDao) {

    val activeSession: Flow<AnonymousSession?> = dao.getActiveSession()

    suspend fun getOrCreateSession(): AnonymousSession {
        val existing = dao.getActiveSessionSync()
        if (existing != null) return existing

        val newSession = AnonymousSession(
            sessionId = UUID.randomUUID().toString(),
            createdAt = System.currentTimeMillis(),
            deviceId = "kudula-${UUID.randomUUID().toString().take(8)}",
            language = "pt_AO",
            country = "Angola",
            onboardingCompleted = false,
            synced = false
        )
        dao.insertSession(newSession)
        return newSession
    }

    suspend fun markOnboardingCompleted(sessionId: String) {
        dao.markOnboardingCompleted(sessionId)
    }

    suspend fun markFirstWinUnlocked(sessionId: String) {
        dao.markFirstWinUnlocked(sessionId)
    }

    suspend fun linkAccount(sessionId: String, firebaseUid: String, email: String?) {
        dao.linkAccount(sessionId, firebaseUid, email)
    }
}
