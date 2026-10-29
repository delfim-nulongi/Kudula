package com.example.data.remote

import com.example.data.local.entity.CognitiveGoal
import com.example.data.local.entity.KudulaEvent
import com.example.data.local.entity.LearningSession
import com.example.data.local.entity.ReflectionJournal
import com.example.data.local.entity.VoiceNoteEntity
import com.example.model.IKCDimensions
import com.example.model.IPAScore
import com.example.model.KDAPLevel
import com.example.model.LibraryItem
import com.example.model.LibraryItemType
import com.example.model.ReadingVoiceConfig
import com.example.model.UserCognitiveProfile
import com.google.firebase.Timestamp
import java.util.Date

/**
 * DTOs estruturados para comunicação com o Firebase Firestore e Backend NestJS (F0.7).
 *
 * Princípios de segurança e arquitetura:
 * - O campo 'synced' permanece exclusivamente no banco de dados local Room.
 * - Métricas cognitivas reportadas pelo cliente são marcadas como 'clientReported'.
 * - Nenhum segredo, token, senha ou dado sensível é mapeado para transmissão remota.
 */
data class SessionRemoteDto(
    val uuid: String = "",
    val concept: String = "",
    val sourceType: String = "",
    val studyMinutes: Int = 0,
    val clientReportedIkcPartial: Int = 0,
    val clientReportedIpaPartial: Int = 0,
    val timestamp: Long = 0L,
    val notes: String = "",
    val schemaVersion: Int = 1,
    val updatedAt: Long = System.currentTimeMillis()
)

data class EventRemoteDto(
    val uuid: String = "",
    val eventType: String = "",
    val details: String = "",
    val source: String = "LOCAL",
    val timestamp: Long = 0L,
    val schemaVersion: Int = 1,
    val createdAt: Long = System.currentTimeMillis()
)

data class GoalRemoteDto(
    val uuid: String = "",
    val title: String = "",
    val description: String = "",
    val targetValue: Int = 0,
    val currentValue: Int = 0,
    val unit: String = "min",
    val isCompleted: Boolean = false,
    val streakDays: Int = 0,
    val dateDay: String = "",
    val schemaVersion: Int = 1,
    val updatedAt: Long = System.currentTimeMillis()
)

data class ReflectionRemoteDto(
    val uuid: String = "",
    val text: String = "",
    val mood: String = "",
    val energy: Int = 0,
    val confidence: Int = 0,
    val promptQuestion: String = "",
    val sessionId: String? = null,
    val tags: List<String> = emptyList(),
    val timestamp: Long = 0L,
    val schemaVersion: Int = 1,
    val updatedAt: Long = System.currentTimeMillis()
)

data class VoiceNoteRemoteDto(
    val uuid: String = "",
    val durationSeconds: Int = 0,
    val timestamp: Long = 0L,
    val questionTranscript: String = "",
    val aiResponseText: String = "",
    val status: String = "",
    val schemaVersion: Int = 1,
    val updatedAt: Long = System.currentTimeMillis()
)

data class LibraryMetadataRemoteDto(
    val id: String = "",
    val title: String = "",
    val source: String = "",
    val type: String = "",
    val category: String = "",
    val progress: Float = 0f,
    val ikcValidated: Int = 0,
    val lastReadTimestamp: Long = System.currentTimeMillis(),
    val schemaVersion: Int = 1
)

object FirestoreMapper {

    // --- Entity to Map / DTO ---

    fun toSessionMap(session: LearningSession): Map<String, Any?> = mapOf(
        "uuid" to session.uuid,
        "concept" to session.concept,
        "sourceType" to session.sourceType,
        "studyMinutes" to session.studyMinutes,
        "clientReportedIkcPartial" to session.ikcPartial,
        "clientReportedIpaPartial" to session.ipaPartial,
        "timestamp" to session.timestamp,
        "notes" to session.notes,
        "schemaVersion" to 1,
        "updatedAt" to System.currentTimeMillis()
    )

    fun toEventMap(event: KudulaEvent): Map<String, Any?> = mapOf(
        "uuid" to event.uuid,
        "eventType" to event.eventType,
        "details" to event.details,
        "source" to event.source,
        "timestamp" to event.timestamp,
        "schemaVersion" to 1,
        "createdAt" to System.currentTimeMillis()
    )

    fun toGoalMap(goal: CognitiveGoal): Map<String, Any?> = mapOf(
        "uuid" to goal.uuid,
        "title" to goal.title,
        "description" to goal.description,
        "targetValue" to goal.targetValue,
        "currentValue" to goal.currentValue,
        "unit" to goal.unit,
        "isCompleted" to goal.isCompleted,
        "streakDays" to goal.streakDays,
        "dateDay" to goal.dateDay,
        "schemaVersion" to 1,
        "updatedAt" to System.currentTimeMillis()
    )

    fun toReflectionMap(reflection: ReflectionJournal): Map<String, Any?> = mapOf(
        "uuid" to reflection.uuid,
        "text" to reflection.text,
        "mood" to reflection.mood,
        "energy" to reflection.energy,
        "confidence" to reflection.confidence,
        "tags" to reflection.tags,
        "promptQuestion" to reflection.promptQuestion,
        "sessionId" to reflection.sessionId,
        "timestamp" to reflection.timestamp,
        "schemaVersion" to 1,
        "updatedAt" to System.currentTimeMillis()
    )

    fun toVoiceNoteMap(voiceNote: VoiceNoteEntity): Map<String, Any?> = mapOf(
        "uuid" to voiceNote.uuid,
        "durationSeconds" to voiceNote.durationSeconds,
        "timestamp" to voiceNote.timestamp,
        "questionTranscript" to voiceNote.questionTranscript,
        "aiResponseText" to voiceNote.aiResponseText,
        "status" to voiceNote.status,
        "schemaVersion" to 1,
        "updatedAt" to System.currentTimeMillis()
    )

    fun toLibraryItemMap(item: LibraryItem): Map<String, Any?> = mapOf(
        "id" to item.id,
        "title" to item.title,
        "source" to item.source,
        "type" to item.type.name,
        "category" to item.category,
        "progress" to item.progress,
        "ikcValidated" to item.ikcValidated,
        "lastReadTimestamp" to System.currentTimeMillis(),
        "schemaVersion" to 1
    )

    fun toCognitiveProfileMap(profile: UserCognitiveProfile): Map<String, Any?> = mapOf(
        "name" to profile.name,
        "location" to profile.location,
        "academicArea" to profile.academicArea,
        "professionalArea" to profile.professionalArea,
        "primaryGoal" to profile.primaryGoal,
        "studyStyle" to profile.studyStyle,
        "preferredExplanation" to profile.preferredExplanation,
        "careerAspiration" to profile.careerAspiration,
        "mbti" to profile.mbti,
        "riasec" to profile.riasec,
        "riasecScores" to profile.riasecScores,
        "multipleIntelligences" to profile.multipleIntelligences,
        "tutorCustomName" to profile.tutorCustomName,
        "companionName" to profile.companionName,
        "dailyGoalMinutes" to profile.dailyGoalMinutes,
        "dailyRhythm" to profile.dailyRhythm,
        "learningStyle" to profile.learningStyle,
        "mainInterests" to profile.mainInterests,
        "careerDirection" to profile.careerDirection,
        "tutorPersonality" to profile.tutorPersonality,
        "preferredFormat" to profile.preferredFormat,
        "voiceConfig" to mapOf(
            "voiceType" to profile.voiceConfig.voiceType,
            "gender" to profile.voiceConfig.gender,
            "rhythm" to profile.voiceConfig.rhythm,
            "speed" to profile.voiceConfig.speed
        ),
        "kdapLevel" to profile.kdapLevel.name,
        // Métricas tratadas como telemetria do cliente (não oficiais até validação de backend)
        "clientReportedXp" to profile.xp,
        "clientReportedLevel" to profile.level,
        "clientReportedLevelName" to profile.levelName,
        "clientReportedStreak" to profile.streakDays,
        "clientReportedPanicFreezesAvoided" to profile.panicFreezesAvoided,
        "clientReportedAuthenticMasteryRate" to profile.authenticMasteryRate,
        "clientReportedIkcAverage" to profile.ikc.overallAverage,
        "clientReportedIpaScore" to profile.ipa.score,
        "onboardingCompleted" to profile.onboardingCompleted,
        "onboardingVersion" to profile.onboardingVersion,
        "updatedAt" to System.currentTimeMillis(),
        "profileVersion" to 1
    )

    // --- Firestore Map / Document to Entity (Reconciliation) ---

    fun toLearningSession(data: Map<String, Any?>): LearningSession? {
        val uuid = data["uuid"] as? String ?: return null
        val concept = data["concept"] as? String ?: "Sessão Cognitiva"
        val sourceType = data["sourceType"] as? String ?: "KLSE"
        val studyMinutes = (data["studyMinutes"] as? Number)?.toInt() ?: 10
        val ikcPartial = (data["clientReportedIkcPartial"] as? Number)?.toInt() ?: 80
        val ipaPartial = (data["clientReportedIpaPartial"] as? Number)?.toInt() ?: 85
        val timestamp = (data["timestamp"] as? Number)?.toLong() ?: System.currentTimeMillis()
        val notes = data["notes"] as? String ?: ""
        return LearningSession(
            uuid = uuid,
            concept = concept,
            sourceType = sourceType,
            studyMinutes = studyMinutes,
            ikcPartial = ikcPartial,
            ipaPartial = ipaPartial,
            timestamp = timestamp,
            synced = true,
            notes = notes
        )
    }

    fun toCognitiveGoal(data: Map<String, Any?>): CognitiveGoal? {
        val uuid = data["uuid"] as? String ?: return null
        val title = data["title"] as? String ?: "Meta"
        val description = data["description"] as? String ?: ""
        val targetValue = (data["targetValue"] as? Number)?.toInt() ?: 20
        val currentValue = (data["currentValue"] as? Number)?.toInt() ?: 0
        val unit = data["unit"] as? String ?: "min"
        val isCompleted = data["isCompleted"] as? Boolean ?: false
        val streakDays = (data["streakDays"] as? Number)?.toInt() ?: 0
        val dateDay = data["dateDay"] as? String ?: ""
        return CognitiveGoal(
            uuid = uuid,
            title = title,
            description = description,
            targetValue = targetValue,
            currentValue = currentValue,
            unit = unit,
            isCompleted = isCompleted,
            streakDays = streakDays,
            dateDay = dateDay,
            synced = true
        )
    }

    fun toReflectionJournal(data: Map<String, Any?>): ReflectionJournal? {
        val uuid = data["uuid"] as? String ?: return null
        val text = data["text"] as? String ?: return null
        val mood = data["mood"] as? String ?: "Focado"
        val energy = (data["energy"] as? Number)?.toInt() ?: 80
        val confidence = (data["confidence"] as? Number)?.toInt() ?: 80
        @Suppress("UNCHECKED_CAST")
        val tags = (data["tags"] as? List<String>) ?: emptyList()
        val promptQuestion = data["promptQuestion"] as? String ?: ""
        val sessionId = data["sessionId"] as? String
        val timestamp = (data["timestamp"] as? Number)?.toLong() ?: System.currentTimeMillis()
        return ReflectionJournal(
            uuid = uuid,
            text = text,
            mood = mood,
            energy = energy,
            confidence = confidence,
            tags = tags,
            promptQuestion = promptQuestion,
            sessionId = sessionId,
            timestamp = timestamp,
            synced = true
        )
    }

    fun toVoiceNoteEntity(data: Map<String, Any?>): VoiceNoteEntity? {
        val uuid = data["uuid"] as? String ?: return null
        val duration = (data["durationSeconds"] as? Number)?.toInt() ?: 0
        val timestamp = (data["timestamp"] as? Number)?.toLong() ?: System.currentTimeMillis()
        val transcript = data["questionTranscript"] as? String ?: ""
        val response = data["aiResponseText"] as? String ?: ""
        val status = data["status"] as? String ?: VoiceNoteEntity.STATUS_ANSWERED
        return VoiceNoteEntity(
            uuid = uuid,
            durationSeconds = duration,
            timestamp = timestamp,
            audioPath = "", // Binários locais nunca são transmitidos para o Firestore
            questionTranscript = transcript,
            aiResponseText = response,
            status = status,
            synced = true
        )
    }

    fun toCognitiveProfile(data: Map<String, Any?>): UserCognitiveProfile {
        val name = data["name"] as? String ?: ""
        val location = data["location"] as? String ?: ""
        val academicArea = data["academicArea"] as? String ?: ""
        val professionalArea = data["professionalArea"] as? String ?: ""
        val primaryGoal = data["primaryGoal"] as? String ?: ""
        val studyStyle = data["studyStyle"] as? String ?: ""
        val preferredExplanation = data["preferredExplanation"] as? String ?: ""
        val careerAspiration = data["careerAspiration"] as? String ?: ""
        val mbti = data["mbti"] as? String
        val riasec = data["riasec"] as? String
        val tutorName = data["tutorCustomName"] as? String ?: "Mentor Kai"
        val companionName = data["companionName"] as? String ?: "Kudu"
        val dailyGoal = (data["dailyGoalMinutes"] as? Number)?.toInt() ?: 20
        val dailyRhythm = data["dailyRhythm"] as? String ?: "30 min/dia"
        val learningStyle = data["learningStyle"] as? String ?: ""
        @Suppress("UNCHECKED_CAST")
        val mainInterests = (data["mainInterests"] as? List<String>) ?: emptyList()
        val careerDirection = data["careerDirection"] as? String ?: ""
        @Suppress("UNCHECKED_CAST")
        val riasecScores = (data["riasecScores"] as? List<String>) ?: emptyList()
        @Suppress("UNCHECKED_CAST")
        val intelligences = (data["multipleIntelligences"] as? List<String>) ?: emptyList()
        val personality = data["tutorPersonality"] as? String ?: "Coach"
        val preferredFormat = data["preferredFormat"] as? String ?: "Áudio + Síntese"

        val kdapString = data["kdapLevel"] as? String ?: "SCAFFOLDING"
        val kdap = try { KDAPLevel.valueOf(kdapString) } catch (e: Exception) { KDAPLevel.SCAFFOLDING }

        val xp = (data["clientReportedXp"] as? Number)?.toInt() ?: 0
        val level = (data["clientReportedLevel"] as? Number)?.toInt() ?: 1
        val levelName = data["clientReportedLevelName"] as? String ?: "Iniciante Cognitivo"
        val streak = (data["clientReportedStreak"] as? Number)?.toInt() ?: 0
        val onboardingCompleted = (data["onboardingCompleted"] as? Boolean) ?: false
        val onboardingVersion = (data["onboardingVersion"] as? Number)?.toInt() ?: 0

        val ikcAvg = (data["clientReportedIkcAverage"] as? Number)?.toInt() ?: 0
        val ikc = if (ikcAvg > 0) {
            IKCDimensions(
                compreensao = ikcAvg,
                retencao = ikcAvg,
                aplicacao = ikcAvg,
                explicacao = ikcAvg,
                confianca = ikcAvg,
                velocidade = ikcAvg,
                evolucao = ikcAvg,
                hasData = true
            )
        } else IKCDimensions(hasData = false)

        val ipaScore = (data["clientReportedIpaScore"] as? Number)?.toInt() ?: 0
        val ipa = if (ipaScore > 0) {
            IPAScore(score = ipaScore, hasData = true)
        } else IPAScore(hasData = false)

        return UserCognitiveProfile(
            name = name,
            location = location,
            academicArea = academicArea,
            professionalArea = professionalArea,
            primaryGoal = primaryGoal,
            studyStyle = studyStyle,
            preferredExplanation = preferredExplanation,
            careerAspiration = careerAspiration,
            ikc = ikc,
            ipa = ipa,
            xp = xp,
            level = level,
            levelName = levelName,
            streakDays = streak,
            kdapLevel = kdap,
            mbti = mbti,
            riasec = riasec,
            tutorCustomName = tutorName,
            companionName = companionName,
            dailyGoalMinutes = dailyGoal,
            dailyRhythm = dailyRhythm,
            learningStyle = learningStyle,
            mainInterests = mainInterests,
            careerDirection = careerDirection,
            riasecScores = riasecScores,
            multipleIntelligences = intelligences,
            tutorPersonality = personality,
            preferredFormat = preferredFormat,
            onboardingCompleted = onboardingCompleted,
            onboardingVersion = onboardingVersion
        )
    }
}
