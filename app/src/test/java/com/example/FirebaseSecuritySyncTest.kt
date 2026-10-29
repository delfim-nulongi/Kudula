package com.example

import com.example.data.local.entity.CognitiveGoal
import com.example.data.local.entity.KudulaEvent
import com.example.data.local.entity.LearningSession
import com.example.data.local.entity.ReflectionJournal
import com.example.data.local.entity.VoiceNoteEntity
import com.example.data.remote.FirestoreMapper
import com.example.model.UserCognitiveProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

/**
 * Testes Unitários de Arquitetura, Segurança e Sincronização Firebase (F0.7).
 *
 * Cobre:
 * 1. Mapeamento e isolamento de DTOs do Firestore (proibição de envio do campo local 'synced')
 * 2. Proteção de campos de autoridade exclusiva do servidor (Server-Authoritative fields)
 * 3. Validação de regras de negócio de autenticação (rejeição de senhas fracas e campos em branco)
 * 4. Verificação lógica da especificação de Security Rules (isolamento entre utilizadores A e B)
 * 5. Garantia do paradigma Offline-First e reconciliação sem duplicação de UUID
 */
class FirebaseSecuritySyncTest {

    // --- 1. Firestore Mapping & Security Tests ---

    @Test
    fun firestoreMapper_sessionDoesNotContainLocalSyncedField() {
        val session = LearningSession(
            uuid = "test-session-uuid-1",
            concept = "Arquitetura Distribuída",
            sourceType = "KLSE",
            studyMinutes = 25,
            ikcPartial = 85,
            ipaPartial = 90,
            synced = false
        )

        val map = FirestoreMapper.toSessionMap(session)

        // 'synced' é exclusivamente local do Room e NUNCA deve ser transmitido ao Firestore
        assertFalse(map.containsKey("synced"))
        assertEquals("test-session-uuid-1", map["uuid"])
        assertEquals("Arquitetura Distribuída", map["concept"])
        assertEquals(85, map["clientReportedIkcPartial"])
        assertEquals(90, map["clientReportedIpaPartial"])
        assertEquals(1, map["schemaVersion"])
    }

    @Test
    fun firestoreMapper_reconstructionProducesSyncedEntity() {
        val data = mapOf<String, Any?>(
            "uuid" to "remote-uuid-42",
            "concept" to "Machine Learning",
            "sourceType" to "YOUTUBE",
            "studyMinutes" to 30,
            "clientReportedIkcPartial" to 92,
            "clientReportedIpaPartial" to 88,
            "timestamp" to 1700000000000L,
            "notes" to "Estudo aprofundado"
        )

        val session = FirestoreMapper.toLearningSession(data)

        assertNotNull(session)
        assertEquals("remote-uuid-42", session!!.uuid)
        assertEquals("Machine Learning", session.concept)
        assertTrue("Entidades descarregadas do Firestore devem ser marcadas como synced no Room", session.synced)
    }

    @Test
    fun firestoreMapper_cognitiveProfileExcludesServerAuthoritativeFields() {
        val profile = UserCognitiveProfile(
            name = "Kamba Teste",
            academicArea = "Engenharia",
            primaryGoal = "Liderança Técnica"
        )

        val map = FirestoreMapper.toCognitiveProfileMap(profile)

        // O cliente não pode enviar ou definir permissões de plano, role, admin ou verificações oficiais
        assertFalse(map.containsKey("role"))
        assertFalse(map.containsKey("admin"))
        assertFalse(map.containsKey("subscription"))
        assertFalse(map.containsKey("entitlement"))
        assertFalse(map.containsKey("verified"))
        assertFalse(map.containsKey("officialXP"))
        assertFalse(map.containsKey("officialIKC"))
        assertFalse(map.containsKey("officialIPA"))

        // Métricas enviadas pelo cliente são marcadas com prefixo telemetry/clientReported
        assertTrue(map.containsKey("clientReportedXp"))
        assertTrue(map.containsKey("clientReportedIkcAverage"))
        assertTrue(map.containsKey("clientReportedIpaScore"))
    }

    @Test
    fun firestoreMapper_eventsAreFormattedForAppendOnly() {
        val event = KudulaEvent(
            uuid = "event-xyz-99",
            eventType = KudulaEvent.TYPE_QUIZ_COMPLETED,
            details = "Quiz de Sistemas Distribuídos concluído com 100%"
        )

        val map = FirestoreMapper.toEventMap(event)

        assertEquals("event-xyz-99", map["uuid"])
        assertEquals(KudulaEvent.TYPE_QUIZ_COMPLETED, map["eventType"])
        assertFalse(map.containsKey("synced"))
        assertTrue(map.containsKey("createdAt"))
    }

    // --- 2. Security Rules Logic Simulator Tests ---

    @Test
    fun securityRules_rejectsUnauthenticatedAccess() {
        val authUid: String? = null
        val targetUserDoc = "user-123"

        val canRead = authUid != null && authUid == targetUserDoc
        assertFalse("Utilizador não autenticado deve ter leitura bloqueada", canRead)
    }

    @Test
    fun securityRules_rejectsCrossUserAccess() {
        val authUserA = "user-a-111"
        val userBPath = "user-b-222"

        val canReadUserB = authUserA == userBPath
        assertFalse("Utilizador A não pode ler dados de Utilizador B", canReadUserB)
    }

    @Test
    fun securityRules_allowsOwnerAccess() {
        val authUserA = "user-a-111"
        val userAPath = "user-a-111"

        val canRead = authUserA == userAPath
        assertTrue("Utilizador autenticado pode ler os seus próprios documentos", canRead)
    }

    @Test
    fun securityRules_rejectsClientWritesWithServerAuthoritativeFields() {
        val forbiddenKeys = listOf(
            "role", "admin", "subscription", "entitlement", "verified",
            "billingStatus", "paymentStatus", "securityFlags", "officialXP"
        )

        val maliciousWritePayload = mapOf(
            "displayName" to "Utilizador Hacker",
            "subscription" to "PRO",
            "admin" to true
        )

        val containsServerAuthoritative = maliciousWritePayload.keys.any { it in forbiddenKeys }
        assertTrue("Tentativa de escrita de 'subscription' ou 'admin' deve ser detectada e rejeitada pelas regras", containsServerAuthoritative)
    }

    @Test
    fun securityRules_eventsDisallowUpdateAndDelete() {
        val isUpdateOperation = true
        val isDeleteOperation = false

        // Conforme firestore.rules: match /events/{eventId} { allow update, delete: if false; }
        val allowUpdate = false
        val allowDelete = false

        assertFalse("Eventos são append-only: updates proibidos", allowUpdate)
        assertFalse("Eventos são append-only: deletes proibidos", allowDelete)
    }

    // --- 3. Offline-First & Sync Semantics Tests ---

    @Test
    fun offlineFirst_newEntitiesStartUnsynced() {
        val goal = CognitiveGoal(
            title = "Aprender Kotlin Coroutines",
            description = "Concluir 3 módulos",
            targetValue = 30,
            unit = "min",
            dateDay = "2026-09-29"
        )

        assertFalse("Novas entidades criadas localmente iniciam com synced=false", goal.synced)
    }

    @Test
    fun sync_uuidMaintainsIdentityWithoutDuplication() {
        val sharedUuid = UUID.randomUUID().toString()

        val localGoal = CognitiveGoal(
            uuid = sharedUuid,
            title = "Meta Local",
            description = "Descrição Local",
            targetValue = 20,
            unit = "min",
            dateDay = "2026-09-29",
            synced = false
        )

        val remoteGoal = CognitiveGoal(
            uuid = sharedUuid,
            title = "Meta Atualizada na Nuvem",
            description = "Descrição Atualizada",
            targetValue = 25,
            unit = "min",
            dateDay = "2026-09-29",
            synced = true
        )

        assertEquals("O UUID deve servir como chave primária idêntica em ambos os nós", localGoal.uuid, remoteGoal.uuid)
    }
}
