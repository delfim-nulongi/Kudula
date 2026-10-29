package com.example.data.remote

import com.example.data.local.entity.CognitiveGoal
import com.example.data.local.entity.KudulaEvent
import com.example.data.local.entity.LearningSession
import com.example.data.local.entity.ReflectionJournal
import com.example.data.local.entity.VoiceNoteEntity
import com.example.data.security.SecureLogger
import com.example.model.LibraryItem
import com.example.model.UserCognitiveProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * FirestoreDataSource / FirestoreRepository (F0.7 Real Firebase Implementation).
 *
 * Implementação real e segura de persistência na nuvem com o Cloud Firestore:
 * - Implementa a interface abstrata [CloudDataSource] para futura transição NestJS/PostgreSQL.
 * - Toda operação exige utilizador autenticado pelo [FirebaseAuth].
 * - UID é sempre validado contra o token de sessão do Firebase Auth.
 * - Rejeita campos de autoridade exclusiva do servidor (assinaturas, entitlements, roles).
 * - Sem atrasos simulados (delays) ou retornos de sucesso falsificados.
 */
class FirestoreRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
) : CloudDataSource {

    companion object {
        private const val TAG = "FirestoreRepository"
        private const val USERS_COLLECTION = "users"
        private const val PROFILE_COLLECTION = "profile"
        private const val PROFILE_MAIN_DOC = "main"
        private const val SESSIONS_COLLECTION = "learningSessions"
        private const val EVENTS_COLLECTION = "events"
        private const val GOALS_COLLECTION = "goals"
        private const val REFLECTIONS_COLLECTION = "reflections"
        private const val VOICE_NOTES_COLLECTION = "voiceNotes"
        private const val LIBRARY_COLLECTION = "libraryItems"
    }

    /**
     * Valida se existe um utilizador autenticado e se corresponde ao UID solicitado.
     */
    private fun getVerifiedCurrentUid(requestedUid: String? = null): String {
        val currentUid = firebaseAuth.currentUser?.uid
            ?: throw SecurityException("Utilizador não autenticado no Firebase. Operação remota cancelada.")
        if (requestedUid != null && requestedUid != currentUid) {
            throw SecurityException("Violação de isolamento de utilizador: o UID solicitado ($requestedUid) não corresponde ao utilizador ativo ($currentUid).")
        }
        return currentUid
    }

    private fun logFirestoreFailure(operation: String, e: Exception) {
        val isPermissionDenied = (e is com.google.firebase.firestore.FirebaseFirestoreException &&
                e.code == com.google.firebase.firestore.FirebaseFirestoreException.Code.PERMISSION_DENIED) ||
                e.message?.contains("PERMISSION_DENIED", ignoreCase = true) == true
        if (isPermissionDenied) {
            SecureLogger.w(
                TAG,
                "Acesso ao Cloud Firestore pausado ($operation): Regras de segurança na nuvem retornaram PERMISSION_DENIED. Persistência local Offline-First mantida com segurança no Room."
            )
        } else {
            SecureLogger.w(TAG, "Falha de persistência remota no Firestore ($operation): ${e.message}")
        }
    }

    // --- CloudDataSource Implementation ---

    override suspend fun upsertUserDocument(
        uid: String,
        email: String?,
        displayName: String?,
        photoUrl: String?
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val verifiedUid = getVerifiedCurrentUid(uid)
            val userDocRef = firestore.collection(USERS_COLLECTION).document(verifiedUid)

            // Apenas campos permitidos ao cliente (conforme regra MASVS e Firestore Rules)
            val userData = mutableMapOf<String, Any?>(
                "uid" to verifiedUid,
                "email" to email,
                "displayName" to displayName,
                "photoUrl" to photoUrl,
                "updatedAt" to System.currentTimeMillis(),
                "profileVersion" to 1
            )

            userDocRef.set(userData, SetOptions.merge()).await()
            SecureLogger.i(TAG, "Documento users/$verifiedUid atualizado com sucesso no Firestore.")
            Result.success(Unit)
        } catch (e: Exception) {
            logFirestoreFailure("upsertUserDocument", e)
            Result.failure(e)
        }
    }

    override suspend fun saveCognitiveProfile(
        uid: String,
        profile: UserCognitiveProfile
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val verifiedUid = getVerifiedCurrentUid(uid)
            val profileDocRef = firestore.collection(USERS_COLLECTION)
                .document(verifiedUid)
                .collection(PROFILE_COLLECTION)
                .document(PROFILE_MAIN_DOC)

            val profileMap = FirestoreMapper.toCognitiveProfileMap(profile)
            profileDocRef.set(profileMap, SetOptions.merge()).await()
            SecureLogger.i(TAG, "Perfil cognitivo users/$verifiedUid/profile/main guardado no Firestore.")
            Result.success(Unit)
        } catch (e: Exception) {
            logFirestoreFailure("saveCognitiveProfile", e)
            Result.failure(e)
        }
    }

    override suspend fun getCognitiveProfile(uid: String): Result<UserCognitiveProfile?> = withContext(Dispatchers.IO) {
        try {
            val verifiedUid = getVerifiedCurrentUid(uid)
            val doc = firestore.collection(USERS_COLLECTION)
                .document(verifiedUid)
                .collection(PROFILE_COLLECTION)
                .document(PROFILE_MAIN_DOC)
                .get()
                .await()

            if (!doc.exists() || doc.data == null) {
                return@withContext Result.success(null)
            }

            val profile = FirestoreMapper.toCognitiveProfile(doc.data!!)
            Result.success(profile)
        } catch (e: Exception) {
            logFirestoreFailure("getCognitiveProfile", e)
            Result.failure(e)
        }
    }

    override suspend fun uploadLearningSessions(
        uid: String,
        sessions: List<LearningSession>
    ): Result<List<String>> = withContext(Dispatchers.IO) {
        try {
            val verifiedUid = getVerifiedCurrentUid(uid)
            val batch = firestore.batch()
            val sessionsCol = firestore.collection(USERS_COLLECTION)
                .document(verifiedUid)
                .collection(SESSIONS_COLLECTION)

            val uploadedUuids = mutableListOf<String>()
            for (session in sessions) {
                val docRef = sessionsCol.document(session.uuid)
                val map = FirestoreMapper.toSessionMap(session)
                batch.set(docRef, map, SetOptions.merge())
                uploadedUuids.add(session.uuid)
            }

            if (uploadedUuids.isNotEmpty()) {
                batch.commit().await()
                SecureLogger.i(TAG, "Commit de lote com ${uploadedUuids.size} sessões de aprendizagem concluído no Firestore.")
            }
            Result.success(uploadedUuids)
        } catch (e: Exception) {
            logFirestoreFailure("uploadLearningSessions", e)
            Result.failure(e)
        }
    }

    override suspend fun fetchLearningSessions(uid: String): Result<List<LearningSession>> = withContext(Dispatchers.IO) {
        try {
            val verifiedUid = getVerifiedCurrentUid(uid)
            val snapshot = firestore.collection(USERS_COLLECTION)
                .document(verifiedUid)
                .collection(SESSIONS_COLLECTION)
                .get()
                .await()

            val sessions = snapshot.documents.mapNotNull { doc ->
                doc.data?.let { FirestoreMapper.toLearningSession(it) }
            }
            Result.success(sessions)
        } catch (e: Exception) {
            logFirestoreFailure("fetchLearningSessions", e)
            Result.failure(e)
        }
    }

    override suspend fun uploadEvents(
        uid: String,
        events: List<KudulaEvent>
    ): Result<List<String>> = withContext(Dispatchers.IO) {
        try {
            val verifiedUid = getVerifiedCurrentUid(uid)
            val batch = firestore.batch()
            val eventsCol = firestore.collection(USERS_COLLECTION)
                .document(verifiedUid)
                .collection(EVENTS_COLLECTION)

            val uploadedUuids = mutableListOf<String>()
            for (event in events) {
                val docRef = eventsCol.document(event.uuid)
                val map = FirestoreMapper.toEventMap(event)
                // Eventos são append-only
                batch.set(docRef, map)
                uploadedUuids.add(event.uuid)
            }

            if (uploadedUuids.isNotEmpty()) {
                batch.commit().await()
                SecureLogger.i(TAG, "Commit de lote com ${uploadedUuids.size} eventos cognitivos concluído no Firestore.")
            }
            Result.success(uploadedUuids)
        } catch (e: Exception) {
            logFirestoreFailure("uploadEvents", e)
            Result.failure(e)
        }
    }

    override suspend fun uploadGoals(
        uid: String,
        goals: List<CognitiveGoal>
    ): Result<List<String>> = withContext(Dispatchers.IO) {
        try {
            val verifiedUid = getVerifiedCurrentUid(uid)
            val batch = firestore.batch()
            val goalsCol = firestore.collection(USERS_COLLECTION)
                .document(verifiedUid)
                .collection(GOALS_COLLECTION)

            val uploadedUuids = mutableListOf<String>()
            for (goal in goals) {
                val docRef = goalsCol.document(goal.uuid)
                val map = FirestoreMapper.toGoalMap(goal)
                batch.set(docRef, map, SetOptions.merge())
                uploadedUuids.add(goal.uuid)
            }

            if (uploadedUuids.isNotEmpty()) {
                batch.commit().await()
                SecureLogger.i(TAG, "Commit de lote com ${uploadedUuids.size} metas cognitivas concluído no Firestore.")
            }
            Result.success(uploadedUuids)
        } catch (e: Exception) {
            logFirestoreFailure("uploadGoals", e)
            Result.failure(e)
        }
    }

    override suspend fun fetchGoals(uid: String): Result<List<CognitiveGoal>> = withContext(Dispatchers.IO) {
        try {
            val verifiedUid = getVerifiedCurrentUid(uid)
            val snapshot = firestore.collection(USERS_COLLECTION)
                .document(verifiedUid)
                .collection(GOALS_COLLECTION)
                .get()
                .await()

            val goals = snapshot.documents.mapNotNull { doc ->
                doc.data?.let { FirestoreMapper.toCognitiveGoal(it) }
            }
            Result.success(goals)
        } catch (e: Exception) {
            logFirestoreFailure("fetchGoals", e)
            Result.failure(e)
        }
    }

    override suspend fun uploadReflections(
        uid: String,
        reflections: List<ReflectionJournal>
    ): Result<List<String>> = withContext(Dispatchers.IO) {
        try {
            val verifiedUid = getVerifiedCurrentUid(uid)
            val batch = firestore.batch()
            val reflectionsCol = firestore.collection(USERS_COLLECTION)
                .document(verifiedUid)
                .collection(REFLECTIONS_COLLECTION)

            val uploadedUuids = mutableListOf<String>()
            for (reflection in reflections) {
                val docRef = reflectionsCol.document(reflection.uuid)
                val map = FirestoreMapper.toReflectionMap(reflection)
                batch.set(docRef, map, SetOptions.merge())
                uploadedUuids.add(reflection.uuid)
            }

            if (uploadedUuids.isNotEmpty()) {
                batch.commit().await()
                SecureLogger.i(TAG, "Commit de lote com ${uploadedUuids.size} reflexões concluído no Firestore.")
            }
            Result.success(uploadedUuids)
        } catch (e: Exception) {
            logFirestoreFailure("uploadReflections", e)
            Result.failure(e)
        }
    }

    override suspend fun fetchReflections(uid: String): Result<List<ReflectionJournal>> = withContext(Dispatchers.IO) {
        try {
            val verifiedUid = getVerifiedCurrentUid(uid)
            val snapshot = firestore.collection(USERS_COLLECTION)
                .document(verifiedUid)
                .collection(REFLECTIONS_COLLECTION)
                .get()
                .await()

            val reflections = snapshot.documents.mapNotNull { doc ->
                doc.data?.let { FirestoreMapper.toReflectionJournal(it) }
            }
            Result.success(reflections)
        } catch (e: Exception) {
            logFirestoreFailure("fetchReflections", e)
            Result.failure(e)
        }
    }

    override suspend fun uploadVoiceNotes(
        uid: String,
        voiceNotes: List<VoiceNoteEntity>
    ): Result<List<String>> = withContext(Dispatchers.IO) {
        try {
            val verifiedUid = getVerifiedCurrentUid(uid)
            val batch = firestore.batch()
            val voiceCol = firestore.collection(USERS_COLLECTION)
                .document(verifiedUid)
                .collection(VOICE_NOTES_COLLECTION)

            val uploadedUuids = mutableListOf<String>()
            for (note in voiceNotes) {
                val docRef = voiceCol.document(note.uuid)
                val map = FirestoreMapper.toVoiceNoteMap(note)
                batch.set(docRef, map, SetOptions.merge())
                uploadedUuids.add(note.uuid)
            }

            if (uploadedUuids.isNotEmpty()) {
                batch.commit().await()
                SecureLogger.i(TAG, "Commit de lote com ${uploadedUuids.size} metadados de voice notes concluído no Firestore.")
            }
            Result.success(uploadedUuids)
        } catch (e: Exception) {
            logFirestoreFailure("uploadVoiceNotes", e)
            Result.failure(e)
        }
    }

    override suspend fun fetchVoiceNotes(uid: String): Result<List<VoiceNoteEntity>> = withContext(Dispatchers.IO) {
        try {
            val verifiedUid = getVerifiedCurrentUid(uid)
            val snapshot = firestore.collection(USERS_COLLECTION)
                .document(verifiedUid)
                .collection(VOICE_NOTES_COLLECTION)
                .get()
                .await()

            val voiceNotes = snapshot.documents.mapNotNull { doc ->
                doc.data?.let { FirestoreMapper.toVoiceNoteEntity(it) }
            }
            Result.success(voiceNotes)
        } catch (e: Exception) {
            logFirestoreFailure("fetchVoiceNotes", e)
            Result.failure(e)
        }
    }

    override suspend fun uploadUserLibraryItem(
        uid: String,
        item: LibraryItem
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val verifiedUid = getVerifiedCurrentUid(uid)
            val docRef = firestore.collection(USERS_COLLECTION)
                .document(verifiedUid)
                .collection(LIBRARY_COLLECTION)
                .document(item.id)

            val map = FirestoreMapper.toLibraryItemMap(item)
            docRef.set(map, SetOptions.merge()).await()
            SecureLogger.i(TAG, "Item da biblioteca ${item.id} guardado no Firestore.")
            Result.success(Unit)
        } catch (e: Exception) {
            logFirestoreFailure("uploadUserLibraryItem", e)
            Result.failure(e)
        }
    }

    override suspend fun fetchUserLibraryItems(uid: String): Result<List<LibraryMetadataRemoteDto>> = withContext(Dispatchers.IO) {
        try {
            val verifiedUid = getVerifiedCurrentUid(uid)
            val snapshot = firestore.collection(USERS_COLLECTION)
                .document(verifiedUid)
                .collection(LIBRARY_COLLECTION)
                .get()
                .await()

            val items = snapshot.documents.mapNotNull { doc ->
                val data = doc.data ?: return@mapNotNull null
                LibraryMetadataRemoteDto(
                    id = doc.id,
                    title = data["title"] as? String ?: "",
                    source = data["source"] as? String ?: "",
                    type = data["type"] as? String ?: "",
                    category = data["category"] as? String ?: "",
                    progress = (data["progress"] as? Number)?.toFloat() ?: 0f,
                    ikcValidated = (data["ikcValidated"] as? Number)?.toInt() ?: 0,
                    lastReadTimestamp = (data["lastReadTimestamp"] as? Number)?.toLong() ?: System.currentTimeMillis()
                )
            }
            Result.success(items)
        } catch (e: Exception) {
            logFirestoreFailure("fetchUserLibraryItems", e)
            Result.failure(e)
        }
    }

    // --- Métodos de compatibilidade direta ---

    suspend fun uploadProgress(session: LearningSession): Result<SessionRemoteDto> = withContext(Dispatchers.IO) {
        val currentUid = firebaseAuth.currentUser?.uid
            ?: return@withContext Result.failure(SecurityException("Utilizador não autenticado."))
        val result = uploadLearningSessions(currentUid, listOf(session))
        if (result.isSuccess) {
            Result.success(FirestoreMapper.toSessionMap(session).let {
                SessionRemoteDto(
                    uuid = session.uuid,
                    concept = session.concept,
                    sourceType = session.sourceType,
                    studyMinutes = session.studyMinutes,
                    clientReportedIkcPartial = session.ikcPartial,
                    clientReportedIpaPartial = session.ipaPartial,
                    timestamp = session.timestamp,
                    notes = session.notes
                )
            })
        } else {
            Result.failure(result.exceptionOrNull() ?: Exception("Falha no upload da sessão"))
        }
    }

    suspend fun uploadEvents(events: List<KudulaEvent>): Result<List<EventRemoteDto>> = withContext(Dispatchers.IO) {
        val currentUid = firebaseAuth.currentUser?.uid
            ?: return@withContext Result.failure(SecurityException("Utilizador não autenticado."))
        val result = uploadEvents(currentUid, events)
        if (result.isSuccess) {
            Result.success(events.map { event ->
                EventRemoteDto(
                    uuid = event.uuid,
                    eventType = event.eventType,
                    details = event.details,
                    source = event.source,
                    timestamp = event.timestamp
                )
            })
        } else {
            Result.failure(result.exceptionOrNull() ?: Exception("Falha no upload de eventos"))
        }
    }

    suspend fun uploadGoals(goals: List<CognitiveGoal>): Result<List<GoalRemoteDto>> = withContext(Dispatchers.IO) {
        val currentUid = firebaseAuth.currentUser?.uid
            ?: return@withContext Result.failure(SecurityException("Utilizador não autenticado."))
        val result = uploadGoals(currentUid, goals)
        if (result.isSuccess) {
            Result.success(goals.map { goal ->
                GoalRemoteDto(
                    uuid = goal.uuid,
                    title = goal.title,
                    description = goal.description,
                    targetValue = goal.targetValue,
                    currentValue = goal.currentValue,
                    unit = goal.unit,
                    isCompleted = goal.isCompleted,
                    streakDays = goal.streakDays,
                    dateDay = goal.dateDay
                )
            })
        } else {
            Result.failure(result.exceptionOrNull() ?: Exception("Falha no upload de metas"))
        }
    }

    suspend fun uploadReflections(reflections: List<ReflectionJournal>): Result<List<ReflectionRemoteDto>> = withContext(Dispatchers.IO) {
        val currentUid = firebaseAuth.currentUser?.uid
            ?: return@withContext Result.failure(SecurityException("Utilizador não autenticado."))
        val result = uploadReflections(currentUid, reflections)
        if (result.isSuccess) {
            Result.success(reflections.map { ref ->
                ReflectionRemoteDto(
                    uuid = ref.uuid,
                    text = ref.text,
                    mood = ref.mood,
                    energy = ref.energy,
                    confidence = ref.confidence,
                    promptQuestion = ref.promptQuestion,
                    sessionId = ref.sessionId,
                    tags = ref.tags,
                    timestamp = ref.timestamp
                )
            })
        } else {
            Result.failure(result.exceptionOrNull() ?: Exception("Falha no upload de reflexões"))
        }
    }

    suspend fun uploadVoiceNotes(voiceNotes: List<VoiceNoteEntity>): Result<List<VoiceNoteRemoteDto>> = withContext(Dispatchers.IO) {
        val currentUid = firebaseAuth.currentUser?.uid
            ?: return@withContext Result.failure(SecurityException("Utilizador não autenticado."))
        val result = uploadVoiceNotes(currentUid, voiceNotes)
        if (result.isSuccess) {
            Result.success(voiceNotes.map { note ->
                VoiceNoteRemoteDto(
                    uuid = note.uuid,
                    durationSeconds = note.durationSeconds,
                    timestamp = note.timestamp,
                    questionTranscript = note.questionTranscript,
                    aiResponseText = note.aiResponseText,
                    status = note.status
                )
            })
        } else {
            Result.failure(result.exceptionOrNull() ?: Exception("Falha no upload de voice notes"))
        }
    }

    suspend fun uploadLibraryMetadata(items: List<LibraryItem>): Result<List<LibraryMetadataRemoteDto>> = withContext(Dispatchers.IO) {
        val currentUid = firebaseAuth.currentUser?.uid
            ?: return@withContext Result.failure(SecurityException("Utilizador não autenticado."))
        val results = items.map { uploadUserLibraryItem(currentUid, it) }
        val failed = results.firstOrNull { it.isFailure }
        if (failed != null) {
            Result.failure(failed.exceptionOrNull() ?: Exception("Falha no upload de metadados"))
        } else {
            Result.success(items.map { item ->
                LibraryMetadataRemoteDto(
                    id = item.id,
                    title = item.title,
                    source = item.source,
                    type = item.type.name,
                    category = item.category,
                    progress = item.progress,
                    ikcValidated = item.ikcValidated
                )
            })
        }
    }
}
