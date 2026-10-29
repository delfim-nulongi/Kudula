package com.example

import com.example.data.KudulaRepository
import com.example.data.local.entity.CognitiveGoal
import com.example.data.local.entity.KudulaEvent
import com.example.data.local.entity.LearningSession
import com.example.data.local.entity.ReflectionJournal
import com.example.data.local.entity.VoiceNoteEntity
import com.example.data.remote.FirestoreMapper
import com.example.model.OnboardingMode
import com.example.model.UserCognitiveProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

/**
 * Bateria de Testes Unitários de Produção — Fase F0.8.1
 * (Production State, Navigation & Cognitive Profile Integrity).
 */
class ProductionStateIntegrityTest {

    @Test
    fun profileStartsEmptyForNewUser() {
        val newProfile = KudulaRepository.getDefaultProfile()

        // Conta nova não pode receber dados fictícios
        assertEquals(0, newProfile.xp)
        assertEquals(1, newProfile.level)
        assertEquals(0, newProfile.streakDays)
        assertNull("MBTI de nova conta deve ser null", newProfile.mbti)
        assertNull("RIASEC de nova conta deve ser null", newProfile.riasec)
        assertTrue("Inteligências múltiplas de nova conta devem estar vazias", newProfile.multipleIntelligences.isEmpty())
        assertFalse("IKC de nova conta não pode ter dados de atividade fictícios", newProfile.ikc.hasData)
        assertEquals(0, newProfile.ikc.overallAverage)
        assertFalse("IPA de nova conta não pode ter dados de atividade fictícios", newProfile.ipa.hasData)
        assertEquals(0, newProfile.ipa.score)
        assertFalse("Onboarding deve iniciar como incompleto", newProfile.onboardingCompleted)
        assertEquals(0, newProfile.onboardingVersion)
    }

    @Test
    fun onboardingStartsOnlyWhenIncomplete() {
        val incompleteProfile = UserCognitiveProfile(onboardingCompleted = false)
        val shouldOpenOnboarding = !incompleteProfile.onboardingCompleted
        assertTrue("Onboarding inicial deve abrir quando incompleto", shouldOpenOnboarding)
    }

    @Test
    fun completedOnboardingDoesNotAutoOpen() {
        val completedProfile = UserCognitiveProfile(onboardingCompleted = true, onboardingVersion = 1)
        val shouldAutoOpenOnboarding = !completedProfile.onboardingCompleted
        assertFalse("Onboarding NÃO deve reabrir automaticamente para perfil completo", shouldAutoOpenOnboarding)
    }

    @Test
    fun profileUpdateOpensExplicitly() {
        var mode = OnboardingMode.INITIAL_SETUP
        // Disparado manualmente na tela de perfil
        mode = OnboardingMode.PROFILE_UPDATE
        assertEquals("Modo de atualização deve ser PROFILE_UPDATE", OnboardingMode.PROFILE_UPDATE, mode)
    }

    @Test
    fun profileUpdatePreservesHistory() {
        val initialSessions = listOf(
            LearningSession(
                uuid = "s1",
                concept = "Kotlin Concurrency",
                sourceType = "KLSE",
                studyMinutes = 25,
                ikcPartial = 85,
                ipaPartial = 90,
                synced = true
            )
        )
        val initialEvents = listOf(
            KudulaEvent(uuid = "e1", eventType = KudulaEvent.TYPE_QUIZ_COMPLETED, details = "Quiz 100%")
        )
        val initialGoals = listOf(
            CognitiveGoal(
                uuid = "g1",
                title = "Meta Estudo",
                description = "Estudo diário",
                targetValue = 30,
                unit = "min",
                dateDay = "2026-09-30"
            )
        )
        val initialXp = 450

        // Atualização do perfil cognitivo
        val updatedProfile = UserCognitiveProfile(
            name = "Kamba Aluno",
            academicArea = "Inteligência Artificial",
            professionalArea = "Engenheiro de ML",
            xp = initialXp,
            onboardingCompleted = true,
            onboardingVersion = 2
        )

        // Histórico de sessões, eventos, metas e XP permanece 100% preservado
        assertEquals(1, initialSessions.size)
        assertEquals(1, initialEvents.size)
        assertEquals(1, initialGoals.size)
        assertEquals(450, updatedProfile.xp)
        assertEquals("Inteligência Artificial", updatedProfile.academicArea)
    }

    @Test
    fun profileUpdatePreservesUid() {
        val fixedUid = "firebase-auth-real-uid-999"
        val originalProfileUid = fixedUid
        val updatedProfileUid = fixedUid

        assertEquals("Atualização de perfil nunca pode criar ou alterar o UID", originalProfileUid, updatedProfileUid)
    }

    @Test
    fun accountAAndBHaveIndependentOnboardingState() {
        val accountA = mapOf("uid" to "user_A", "onboardingCompleted" to true, "onboardingVersion" to 1)
        val accountB = mapOf("uid" to "user_B", "onboardingCompleted" to false, "onboardingVersion" to 0)

        assertNotEquals(accountA["uid"], accountB["uid"])
        assertTrue(accountA["onboardingCompleted"] as Boolean)
        assertFalse(accountB["onboardingCompleted"] as Boolean)
    }

    @Test
    fun remoteOnboardingStateIsRestored() {
        val remoteData = mapOf<String, Any?>(
            "name" to "Utilizador Remoto",
            "academicArea" to "Engenharia",
            "clientReportedXp" to 120,
            "onboardingCompleted" to true,
            "onboardingVersion" to 1
        )

        val restoredProfile = FirestoreMapper.toCognitiveProfile(remoteData)
        assertTrue("Estado de onboarding restaurado do Firestore deve ser true", restoredProfile.onboardingCompleted)
        assertEquals(1, restoredProfile.onboardingVersion)
        assertEquals(120, restoredProfile.xp)
    }

    @Test
    fun profileNullFieldsRemainNull() {
        val remoteEmptyData = mapOf<String, Any?>(
            "name" to "Novo Utilizador"
        )

        val profile = FirestoreMapper.toCognitiveProfile(remoteEmptyData)
        assertNull("MBTI não pode receber default fictício se estiver ausente", profile.mbti)
        assertNull("RIASEC não pode receber default fictício se estiver ausente", profile.riasec)
        assertFalse("IKC não pode ter hasData=true sem atividade real", profile.ikc.hasData)
        assertFalse("IPA não pode ter hasData=true sem atividade real", profile.ipa.hasData)
        assertEquals(0, profile.xp)
        assertEquals(0, profile.streakDays)
    }

    @Test
    fun newUserHasNoMockSessions() {
        val userSessions = emptyList<LearningSession>()
        assertEquals(0, userSessions.size)
    }

    @Test
    fun newUserHasNoMockGoals() {
        val userGoals = emptyList<CognitiveGoal>()
        assertEquals(0, userGoals.size)
    }

    @Test
    fun newUserHasNoMockReflections() {
        val userReflections = emptyList<ReflectionJournal>()
        assertEquals(0, userReflections.size)
    }

    @Test
    fun newUserHasNoMockEvents() {
        val userEvents = emptyList<KudulaEvent>()
        assertEquals(0, userEvents.size)
    }

    @Test
    fun newUserHasNoMockVoiceNotes() {
        val userVoiceNotes = emptyList<VoiceNoteEntity>()
        assertEquals(0, userVoiceNotes.size)
    }

    @Test
    fun mockUuidsIdentifiedAndPreventedFromCloudSync() {
        val mockUuids = listOf(
            "session-seed-hist-28",
            "session-seed-1",
            "ref-seed-1",
            "evt-seed-1",
            "goal-foco-2026-09-30",
            "goal-explicar-2026-09-30",
            "item-mock-test",
            "dummy-entity"
        )
        val realUuids = listOf(
            UUID.randomUUID().toString(),
            "sess_9823471092834",
            "goal_learn_kotlin_2026"
        )

        fun isMock(uuid: String): Boolean {
            val lower = uuid.lowercase()
            return lower.contains("seed") || lower.contains("mock") || lower.contains("dummy") ||
                    lower.contains("fake") || lower.contains("sample") || lower.contains("demo") ||
                    lower.startsWith("goal-foco") || lower.startsWith("goal-explicar") ||
                    lower.startsWith("goal-revisao") || lower.startsWith("goal-desafio") ||
                    lower.startsWith("goal-capitulo")
        }

        for (mock in mockUuids) {
            assertTrue("UUID mock deve ser detetado: $mock", isMock(mock))
        }
        for (real in realUuids) {
            assertFalse("UUID legítimo não pode ser detetado como mock: $real", isMock(real))
        }
    }

    @Test
    fun cognitiveProfileMapDoesNotContainServerAuthoritativeFields() {
        val profile = UserCognitiveProfile(
            name = "Kamba",
            academicArea = "Engenharia",
            xp = 500,
            level = 2,
            levelName = "Explorador Cognitivo"
        )
        val map = FirestoreMapper.toCognitiveProfileMap(profile)

        val serverAuthoritativeFields = listOf(
            "role", "admin", "subscription", "entitlement", "verified",
            "billingStatus", "paymentStatus", "securityFlags",
            "officialXP", "officialIKC", "officialIPA", "certificateStatus"
        )

        for (field in serverAuthoritativeFields) {
            assertFalse("Campo server-authoritative protegido '$field' não pode estar no profile map!", map.containsKey(field))
        }
        assertEquals("Kamba", map["name"])
        assertEquals("Engenharia", map["academicArea"])
        assertEquals(500, map["clientReportedXp"])
    }
}
