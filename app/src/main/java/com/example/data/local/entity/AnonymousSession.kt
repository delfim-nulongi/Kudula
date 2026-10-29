package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Kudula Anonymous Session (F0.6)
 * Permite que todo novo utilizador entre imediatamente na experiência sem login obrigatório.
 * Todos os dados e telemetria gerados são salvaguardados offline via KLSE.
 * Preparada para sincronização futura: Anonymous Session → KLSE → Firebase → NestJS → PostgreSQL.
 */
@Entity(tableName = "anonymous_sessions")
data class AnonymousSession(
    @PrimaryKey
    val sessionId: String = UUID.randomUUID().toString(),
    val createdAt: Long = System.currentTimeMillis(),
    val deviceId: String = "kudula-device-${UUID.randomUUID().toString().take(8)}",
    val language: String = "pt_AO",
    val country: String? = "Angola",
    val onboardingCompleted: Boolean = false,
    val synced: Boolean = false,
    val firebaseUid: String? = null,
    val linkedEmail: String? = null,
    val firstWinUnlocked: Boolean = false
)
