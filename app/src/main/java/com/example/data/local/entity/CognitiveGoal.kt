package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Cognitive Growth Engine
 * Metas cognitivas diárias com rastreamento visual, streak e persistência local via Room.
 */
@Entity(tableName = "cognitive_goals")
data class CognitiveGoal(
    @PrimaryKey
    val uuid: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val targetValue: Int,
    val currentValue: Int = 0,
    val unit: String, // "min", "conceito", "revisão", "desafio", "capítulo"
    val isCompleted: Boolean = false,
    val streakDays: Int = 0,
    val dateDay: String, // formato "YYYY-MM-DD"
    val synced: Boolean = false
) {
    val progressFraction: Float
        get() = if (targetValue > 0) (currentValue.toFloat() / targetValue.toFloat()).coerceIn(0f, 1f) else 0f
}
