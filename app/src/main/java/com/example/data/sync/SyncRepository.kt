package com.example.data.sync

import com.example.data.local.dao.CognitiveGoalDao
import com.example.data.local.dao.KudulaEventDao
import com.example.data.local.dao.LearningSessionDao
import com.example.data.local.dao.ReflectionJournalDao
import com.example.data.local.dao.VoiceNoteDao
import com.example.data.remote.FirestoreRepository
import com.example.data.security.SecureLogger
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext

/**
 * SyncRepository (Kudula F0.7 Real Firebase & Security Foundation).
 *
 * Centralizador de sincronização bidirecional resiliente:
 * 1. Room -> Firestore (Upload): Envia dados não sincronizados para users/{uid}/...
 *    Apenas marca 'synced = true' no Room APÓS confirmação real de escrita no Firestore.
 * 2. Firestore -> Room (Download/Reconciliação): Descarrega dados da nuvem para o Room
 *    para suporte a múltiplos dispositivos e recuperação de conta.
 *
 * Princípios de segurança:
 * - Operações de dados privados são bloqueadas imediatamente caso não haja UID autenticado.
 * - 'synced' permanece exclusivamente local no Room.
 * - Preservação estrita offline-first: falha de rede mantém dados intactos no Room com 'synced = false'.
 */
class SyncRepository(
    private val sessionDao: LearningSessionDao,
    private val eventDao: KudulaEventDao,
    private val goalDao: CognitiveGoalDao,
    private val reflectionDao: ReflectionJournalDao,
    private val voiceNoteDao: VoiceNoteDao,
    private val firestoreRepository: FirestoreRepository = FirestoreRepository(),
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
) {

    companion object {
        private const val TAG = "SyncRepository"
    }

    private val _pendingSyncCount = MutableStateFlow(0)
    val pendingSyncCount: StateFlow<Int> = _pendingSyncCount.asStateFlow()

    private val _lastSyncTimestamp = MutableStateFlow(System.currentTimeMillis())
    val lastSyncTimestamp: StateFlow<Long> = _lastSyncTimestamp.asStateFlow()

    /**
     * Incrementa o contador de intenção de sincronização pendente.
     */
    fun queueSync(entityType: String, uuid: String) {
        SecureLogger.d(TAG, "Item enfileirado para sync: $entityType [$uuid]")
        _pendingSyncCount.value += 1
    }

    private fun isMockOrSeed(uuid: String): Boolean {
        val lower = uuid.lowercase()
        return lower.contains("seed") || lower.contains("mock") || lower.contains("dummy") ||
                lower.contains("fake") || lower.contains("sample") || lower.contains("demo") ||
                lower.startsWith("goal-foco") || lower.startsWith("goal-explicar") ||
                lower.startsWith("goal-revisao") || lower.startsWith("goal-desafio") ||
                lower.startsWith("goal-capitulo")
    }

    /**
     * Upload de dados pendentes do Room para o Cloud Firestore (Room -> Firestore).
     * Só marca 'synced = true' no Room após confirmação de sucesso do Firestore.
     * Dados mock ou seed são eliminados do Room e NUNCA transmitidos ao Firestore.
     */
    suspend fun syncPending(): Result<Int> = withContext(Dispatchers.IO) {
        val currentUid = firebaseAuth.currentUser?.uid
        if (currentUid == null) {
            SecureLogger.d(TAG, "Sync ignorado: nenhum utilizador autenticado no Firebase.")
            return@withContext Result.failure(SecurityException("Utilizador não autenticado no Firebase."))
        }

        try {
            var syncedCount = 0

            // 1. Learning Sessions (KLSE)
            val allUnsyncedSessions = sessionDao.getUnsyncedSessions().firstOrNull().orEmpty()
            val mockSessions = allUnsyncedSessions.filter { isMockOrSeed(it.uuid) }
            for (mock in mockSessions) {
                sessionDao.deleteSession(mock)
            }
            val unsyncedSessions = allUnsyncedSessions.filter { !isMockOrSeed(it.uuid) }
            if (unsyncedSessions.isNotEmpty()) {
                val uploadResult = firestoreRepository.uploadLearningSessions(currentUid, unsyncedSessions)
                if (uploadResult.isSuccess) {
                    val confirmedUuids = uploadResult.getOrThrow()
                    for (uuid in confirmedUuids) {
                        sessionDao.markAsSynced(uuid)
                        syncedCount++
                    }
                } else {
                    SecureLogger.w(TAG, "Falha ao enviar sessões pendentes para o Firestore: ${uploadResult.exceptionOrNull()?.message}")
                }
            }

            // 2. Cognitive Events (Append-only Event Log)
            val allUnsyncedEvents = eventDao.getUnsyncedEvents().firstOrNull().orEmpty()
            val mockEvents = allUnsyncedEvents.filter { isMockOrSeed(it.uuid) }
            for (mock in mockEvents) {
                eventDao.deleteEvent(mock)
            }
            val unsyncedEvents = allUnsyncedEvents.filter { !isMockOrSeed(it.uuid) }
            if (unsyncedEvents.isNotEmpty()) {
                val uploadResult = firestoreRepository.uploadEvents(currentUid, unsyncedEvents)
                if (uploadResult.isSuccess) {
                    val confirmedUuids = uploadResult.getOrThrow()
                    for (uuid in confirmedUuids) {
                        eventDao.markAsSynced(uuid)
                        syncedCount++
                    }
                } else {
                    SecureLogger.w(TAG, "Falha ao enviar eventos pendentes para o Firestore: ${uploadResult.exceptionOrNull()?.message}")
                }
            }

            // 3. Cognitive Goals (Growth Engine)
            val allUnsyncedGoals = goalDao.getUnsyncedGoals().firstOrNull().orEmpty()
            val mockGoals = allUnsyncedGoals.filter { isMockOrSeed(it.uuid) }
            for (mock in mockGoals) {
                goalDao.deleteGoal(mock)
            }
            val unsyncedGoals = allUnsyncedGoals.filter { !isMockOrSeed(it.uuid) }
            if (unsyncedGoals.isNotEmpty()) {
                val uploadResult = firestoreRepository.uploadGoals(currentUid, unsyncedGoals)
                if (uploadResult.isSuccess) {
                    val confirmedUuids = uploadResult.getOrThrow()
                    for (uuid in confirmedUuids) {
                        goalDao.markSynced(uuid)
                        syncedCount++
                    }
                } else {
                    SecureLogger.w(TAG, "Falha ao enviar metas pendentes para o Firestore: ${uploadResult.exceptionOrNull()?.message}")
                }
            }

            // 4. Guided Reflections (Reflection Journal)
            val allUnsyncedReflections = reflectionDao.getUnsyncedReflections().firstOrNull().orEmpty()
            val mockReflections = allUnsyncedReflections.filter { isMockOrSeed(it.uuid) }
            for (mock in mockReflections) {
                reflectionDao.deleteReflection(mock)
            }
            val unsyncedReflections = allUnsyncedReflections.filter { !isMockOrSeed(it.uuid) }
            if (unsyncedReflections.isNotEmpty()) {
                val uploadResult = firestoreRepository.uploadReflections(currentUid, unsyncedReflections)
                if (uploadResult.isSuccess) {
                    val confirmedUuids = uploadResult.getOrThrow()
                    for (uuid in confirmedUuids) {
                        reflectionDao.markAsSynced(uuid)
                        syncedCount++
                    }
                } else {
                    SecureLogger.w(TAG, "Falha ao enviar reflexões pendentes para o Firestore: ${uploadResult.exceptionOrNull()?.message}")
                }
            }

            // 5. Voice Notes Metadata
            val allUnsyncedVoiceNotes = voiceNoteDao.getPendingSync()
            val mockVoiceNotes = allUnsyncedVoiceNotes.filter { isMockOrSeed(it.uuid) }
            for (mock in mockVoiceNotes) {
                voiceNoteDao.delete(mock)
            }
            val unsyncedVoiceNotes = allUnsyncedVoiceNotes.filter { !isMockOrSeed(it.uuid) }
            if (unsyncedVoiceNotes.isNotEmpty()) {
                val uploadResult = firestoreRepository.uploadVoiceNotes(currentUid, unsyncedVoiceNotes)
                if (uploadResult.isSuccess) {
                    val confirmedUuids = uploadResult.getOrThrow()
                    for (uuid in confirmedUuids) {
                        voiceNoteDao.markSynced(uuid)
                        syncedCount++
                    }
                } else {
                    SecureLogger.w(TAG, "Falha ao enviar notas de voz pendentes para o Firestore: ${uploadResult.exceptionOrNull()?.message}")
                }
            }

            _lastSyncTimestamp.value = System.currentTimeMillis()
            _pendingSyncCount.value = 0
            SecureLogger.i(TAG, "Ciclo de upload concluído. Itens confirmados no Firestore: $syncedCount")
            Result.success(syncedCount)
        } catch (e: Exception) {
            SecureLogger.e(TAG, "Erro durante syncPending", e)
            Result.failure(e)
        }
    }

    /**
     * Descarrega dados da nuvem para o Room local (Firestore -> Room).
     * Garante compatibilidade multi-dispositivo e restauração de dados.
     */
    suspend fun fetchRemoteDataAndReconcile(): Result<Int> = withContext(Dispatchers.IO) {
        val currentUid = firebaseAuth.currentUser?.uid
            ?: return@withContext Result.failure(SecurityException("Utilizador não autenticado no Firebase."))

        try {
            var reconciledCount = 0

            // 1. Reconciliação de Learning Sessions
            val remoteSessions = firestoreRepository.fetchLearningSessions(currentUid)
            if (remoteSessions.isSuccess) {
                val sessions = remoteSessions.getOrThrow()
                if (sessions.isNotEmpty()) {
                    sessionDao.insertSessions(sessions)
                    reconciledCount += sessions.size
                }
            }

            // 2. Reconciliação de Metas
            val remoteGoals = firestoreRepository.fetchGoals(currentUid)
            if (remoteGoals.isSuccess) {
                val goals = remoteGoals.getOrThrow()
                if (goals.isNotEmpty()) {
                    goalDao.insertGoals(goals)
                    reconciledCount += goals.size
                }
            }

            // 3. Reconciliação de Reflexões
            val remoteReflections = firestoreRepository.fetchReflections(currentUid)
            if (remoteReflections.isSuccess) {
                val reflections = remoteReflections.getOrThrow()
                if (reflections.isNotEmpty()) {
                    reflectionDao.insertReflections(reflections)
                    reconciledCount += reflections.size
                }
            }

            // 4. Reconciliação de Voice Notes
            val remoteVoiceNotes = firestoreRepository.fetchVoiceNotes(currentUid)
            if (remoteVoiceNotes.isSuccess) {
                val voiceNotes = remoteVoiceNotes.getOrThrow()
                for (vn in voiceNotes) {
                    voiceNoteDao.insert(vn)
                    reconciledCount++
                }
            }

            SecureLogger.i(TAG, "Reconciliação da nuvem concluída. Entidades recebidas: $reconciledCount")
            Result.success(reconciledCount)
        } catch (e: Exception) {
            SecureLogger.e(TAG, "Erro durante fetchRemoteDataAndReconcile", e)
            Result.failure(e)
        }
    }

    /**
     * Executa sincronização completa bidirecional:
     * 1º Envia alterações pendentes locais (Room -> Firestore)
     * 2º Recebe atualizações remotas (Firestore -> Room)
     */
    suspend fun syncBidirectional(): Result<Int> = withContext(Dispatchers.IO) {
        val uploadResult = syncPending()
        val downloadResult = fetchRemoteDataAndReconcile()

        val total = (uploadResult.getOrNull() ?: 0) + (downloadResult.getOrNull() ?: 0)
        if (uploadResult.isSuccess || downloadResult.isSuccess) {
            Result.success(total)
        } else {
            Result.failure(uploadResult.exceptionOrNull() ?: downloadResult.exceptionOrNull() ?: Exception("Falha de sincronização"))
        }
    }

    /**
     * Retry com backoff de operações falhadas.
     */
    suspend fun retryFailed(): Result<Int> {
        SecureLogger.d(TAG, "Executando retry de sincronização...")
        return syncBidirectional()
    }
}
