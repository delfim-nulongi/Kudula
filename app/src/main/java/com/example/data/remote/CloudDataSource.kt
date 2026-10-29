package com.example.data.remote

import com.example.data.local.entity.CognitiveGoal
import com.example.data.local.entity.KudulaEvent
import com.example.data.local.entity.LearningSession
import com.example.data.local.entity.ReflectionJournal
import com.example.data.local.entity.VoiceNoteEntity
import com.example.model.LibraryItem
import com.example.model.UserCognitiveProfile

/**
 * Contrato abstrato para a camada remota de nuvem do Kudula (F0.7).
 *
 * Desacopla completamente a lógica de persistência e sincronização de dados remotos
 * da implementação tecnológica concreta (Firebase Firestore hoje, NestJS / PostgreSQL amanhã).
 * Garante portabilidade limpa para Android, iOS, Desktop e Web.
 */
interface CloudDataSource {

    /**
     * Regista ou atualiza o documento base do utilizador em users/{uid}.
     * Apenas campos permitidos ao cliente são persistidos.
     */
    suspend fun upsertUserDocument(
        uid: String,
        email: String?,
        displayName: String?,
        photoUrl: String? = null
    ): Result<Unit>

    /**
     * Salva ou atualiza o perfil cognitivo completo em users/{uid}/profile/main.
     */
    suspend fun saveCognitiveProfile(
        uid: String,
        profile: UserCognitiveProfile
    ): Result<Unit>

    /**
     * Obtém o perfil cognitivo remoto mais recente do utilizador.
     */
    suspend fun getCognitiveProfile(uid: String): Result<UserCognitiveProfile?>

    /**
     * Upload de sessões de aprendizagem do KLSE para users/{uid}/learningSessions/{uuid}.
     */
    suspend fun uploadLearningSessions(
        uid: String,
        sessions: List<LearningSession>
    ): Result<List<String>>

    /**
     * Leitura remota de sessões de aprendizagem do utilizador para reconciliação local.
     */
    suspend fun fetchLearningSessions(uid: String): Result<List<LearningSession>>

    /**
     * Upload append-only de eventos cognitivos para users/{uid}/events/{uuid}.
     */
    suspend fun uploadEvents(
        uid: String,
        events: List<KudulaEvent>
    ): Result<List<String>>

    /**
     * Upload de metas cognitivas para users/{uid}/goals/{uuid}.
     */
    suspend fun uploadGoals(
        uid: String,
        goals: List<CognitiveGoal>
    ): Result<List<String>>

    /**
     * Leitura remota de metas cognitivas do utilizador.
     */
    suspend fun fetchGoals(uid: String): Result<List<CognitiveGoal>>

    /**
     * Upload de diários de reflexão guiados para users/{uid}/reflections/{uuid}.
     */
    suspend fun uploadReflections(
        uid: String,
        reflections: List<ReflectionJournal>
    ): Result<List<String>>

    /**
     * Leitura remota de diários de reflexão do utilizador.
     */
    suspend fun fetchReflections(uid: String): Result<List<ReflectionJournal>>

    /**
     * Upload de metadados de notas de voz curtas para users/{uid}/voiceNotes/{uuid}.
     * Ficheiros binários de áudio nunca são enviados para o Firestore.
     */
    suspend fun uploadVoiceNotes(
        uid: String,
        voiceNotes: List<VoiceNoteEntity>
    ): Result<List<String>>

    /**
     * Leitura remota de metadados de notas de voz do utilizador.
     */
    suspend fun fetchVoiceNotes(uid: String): Result<List<VoiceNoteEntity>>

    /**
     * Upload de item adicionado pelo utilizador à sua biblioteca privada em users/{uid}/libraryItems/{itemId}.
     */
    suspend fun uploadUserLibraryItem(
        uid: String,
        item: LibraryItem
    ): Result<Unit>

    /**
     * Leitura de itens da biblioteca do utilizador.
     */
    suspend fun fetchUserLibraryItems(uid: String): Result<List<LibraryMetadataRemoteDto>>
}
