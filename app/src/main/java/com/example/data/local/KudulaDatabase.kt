package com.example.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.data.local.dao.AnonymousSessionDao
import com.example.data.local.dao.ChatMessageDao
import com.example.data.local.dao.CognitiveGoalDao
import com.example.data.local.dao.KudulaEventDao
import com.example.data.local.dao.LearningSessionDao
import com.example.data.local.dao.LibraryItemDao
import com.example.data.local.dao.ReflectionJournalDao
import com.example.data.local.dao.VoiceNoteDao
import com.example.data.local.entity.AnonymousSession
import com.example.data.local.entity.ChatMessageEntity
import com.example.data.local.entity.CognitiveGoal
import com.example.data.local.entity.KudulaEvent
import com.example.data.local.entity.LearningSession
import com.example.data.local.entity.LibraryItemEntity
import com.example.data.local.entity.ReflectionJournal
import com.example.data.local.entity.VoiceNoteEntity

/**
 * Base de dados local Room da Kudula.
 *
 * Preserva as tabelas existentes e acrescenta o histórico
 * persistente da Biblioteca Inteligente.
 */
@Database(
    entities = [
        AnonymousSession::class,
        LearningSession::class,
        KudulaEvent::class,
        CognitiveGoal::class,
        ReflectionJournal::class,
        VoiceNoteEntity::class,
        ChatMessageEntity::class,
        LibraryItemEntity::class
    ],
    version = 6,
    exportSchema = false
)
@TypeConverters(KudulaTypeConverters::class)
abstract class KudulaDatabase : RoomDatabase() {

    abstract fun anonymousSessionDao(): AnonymousSessionDao

    abstract fun learningSessionDao(): LearningSessionDao

    abstract fun kudulaEventDao(): KudulaEventDao

    abstract fun cognitiveGoalDao(): CognitiveGoalDao

    abstract fun reflectionJournalDao(): ReflectionJournalDao

    abstract fun voiceNoteDao(): VoiceNoteDao

    abstract fun chatMessageDao(): ChatMessageDao

    abstract fun libraryItemDao(): LibraryItemDao

    companion object {
        fun getInstance(
            context: android.content.Context
        ): KudulaDatabase =
            KudulaDatabaseProvider.getDatabase(context)
    }
}
