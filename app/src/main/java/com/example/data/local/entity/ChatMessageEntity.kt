package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "chat_messages",
    indices = [Index(value = ["timestampMillis"])]
)
data class ChatMessageEntity(
    @PrimaryKey
    val id: String,

    val isUser: Boolean,
    val text: String,
    val timestamp: String,
    val timestampMillis: Long,

    val suggestedPrompt: List<String> = emptyList(),
    val analogyConcept: String? = null,
    val socraticFollowUp: String? = null
)