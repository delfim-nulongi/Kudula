package com.example.data.local

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Singleton Provider para a base de dados local da Kudula.
 *
 * As migrações explícitas preservam os dados existentes.
 * Não utilizar migrações destrutivas.
 */
object KudulaDatabaseProvider {

    @Volatile
    private var instance: KudulaDatabase? = null

    /**
     * Migração 1 -> 2:
     * adiciona a tabela reflection_journals.
     */
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `reflection_journals` (
                    `uuid` TEXT NOT NULL PRIMARY KEY,
                    `text` TEXT NOT NULL,
                    `mood` TEXT NOT NULL,
                    `energy` INTEGER NOT NULL,
                    `confidence` INTEGER NOT NULL,
                    `tags` TEXT NOT NULL,
                    `promptQuestion` TEXT NOT NULL,
                    `sessionId` TEXT,
                    `timestamp` INTEGER NOT NULL,
                    `synced` INTEGER NOT NULL DEFAULT 0
                )
                """.trimIndent()
            )
        }
    }

    /**
     * Migração 2 -> 3:
     * adiciona a tabela voice_notes.
     */
    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `voice_notes` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `uuid` TEXT NOT NULL,
                    `durationSeconds` INTEGER NOT NULL,
                    `timestamp` INTEGER NOT NULL,
                    `audioPath` TEXT NOT NULL,
                    `questionTranscript` TEXT NOT NULL,
                    `aiResponseText` TEXT NOT NULL,
                    `status` TEXT NOT NULL,
                    `synced` INTEGER NOT NULL DEFAULT 0
                )
                """.trimIndent()
            )
        }
    }

    /**
     * Migração 3 -> 4:
     * adiciona o histórico local do Tutor Kudu.
     */
    val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {

            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `chat_messages` (
                    `id` TEXT NOT NULL PRIMARY KEY,
                    `isUser` INTEGER NOT NULL,
                    `text` TEXT NOT NULL,
                    `timestamp` TEXT NOT NULL,
                    `timestampMillis` INTEGER NOT NULL,
                    `suggestedPrompt` TEXT NOT NULL,
                    `analogyConcept` TEXT,
                    `socraticFollowUp` TEXT
                )
                """.trimIndent()
            )

            db.execSQL(
                """
                CREATE INDEX IF NOT EXISTS
                `index_chat_messages_timestampMillis`
                ON `chat_messages` (`timestampMillis`)
                """.trimIndent()
            )
        }
    }

    /**
     * Migração 4 -> 5:
     * adiciona a tabela library_items para persistência local da Biblioteca Inteligente.
     */
    val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `library_items` (
                    `id` TEXT NOT NULL PRIMARY KEY,
                    `title` TEXT NOT NULL,
                    `type` TEXT NOT NULL,
                    `category` TEXT NOT NULL,
                    `summary` TEXT NOT NULL,
                    `readTime` TEXT NOT NULL,
                    `progress` REAL NOT NULL,
                    `ikcValidated` INTEGER NOT NULL,
                    `tags` TEXT NOT NULL,
                    `preloadedConcepts` TEXT NOT NULL,
                    `difficulty` TEXT NOT NULL,
                    `estimatedTime` TEXT NOT NULL,
                    `source` TEXT NOT NULL,
                    `confidenceBadge` TEXT NOT NULL,
                    `thumbnailUri` TEXT,
                    `duration` TEXT,
                    `fileSize` TEXT,
                    `waveformPoints` TEXT NOT NULL,
                    `sourceUri` TEXT,
                    `originalText` TEXT,
                    `extractionComplete` INTEGER NOT NULL DEFAULT 0
                )
                """.trimIndent()
            )
        }
    }

    /**
     * Migração 5 -> 6:
     * adiciona a coluna editedStructureJson para o Modo de Edição e Controlo Personalizável da Estrutura de Leitura.
     */
    val MIGRATION_5_6 = object : Migration(5, 6) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                ALTER TABLE `library_items`
                ADD COLUMN `editedStructureJson` TEXT DEFAULT NULL
                """.trimIndent()
            )
        }
    }

    fun getDatabase(context: Context): KudulaDatabase {
        return instance ?: synchronized(this) {
            instance ?: buildDatabase(
                context.applicationContext
            ).also {
                instance = it
            }
        }
    }

    private fun buildDatabase(context: Context): KudulaDatabase {
        return Room.databaseBuilder(
            context,
            KudulaDatabase::class.java,
            "kudula_cognitive_os.db"
        )
            .addMigrations(
                MIGRATION_1_2,
                MIGRATION_2_3,
                MIGRATION_3_4,
                MIGRATION_4_5,
                MIGRATION_5_6
            )
            .addCallback(object : RoomDatabase.Callback() {

                override fun onOpen(db: SupportSQLiteDatabase) {
                    super.onOpen(db)

                    try {
                        db.execSQL(
                            """
                            DELETE FROM learning_sessions
                            WHERE uuid LIKE '%seed%'
                               OR uuid LIKE '%mock%'
                               OR uuid LIKE '%demo%'
                               OR uuid LIKE '%fake%'
                            """.trimIndent()
                        )

                        db.execSQL(
                            """
                            DELETE FROM kudula_events
                            WHERE uuid LIKE '%seed%'
                               OR uuid LIKE '%mock%'
                               OR uuid LIKE '%demo%'
                               OR uuid LIKE '%fake%'
                            """.trimIndent()
                        )

                        db.execSQL(
                            """
                            DELETE FROM reflection_journals
                            WHERE uuid LIKE '%seed%'
                               OR uuid LIKE '%mock%'
                               OR uuid LIKE '%demo%'
                               OR uuid LIKE '%fake%'
                            """.trimIndent()
                        )

                        db.execSQL(
                            """
                            DELETE FROM cognitive_goals
                            WHERE uuid LIKE '%seed%'
                               OR uuid LIKE '%mock%'
                               OR uuid LIKE '%demo%'
                               OR uuid LIKE '%fake%'
                               OR uuid LIKE 'goal-foco%'
                               OR uuid LIKE 'goal-explicar%'
                               OR uuid LIKE 'goal-revisao%'
                               OR uuid LIKE 'goal-desafio%'
                               OR uuid LIKE 'goal-capitulo%'
                            """.trimIndent()
                        )

                    } catch (e: Exception) {
                        android.util.Log.e(
                            "KudulaDatabase",
                            "Erro na limpeza de dados de demonstração",
                            e
                        )
                    }
                }
            })
            .build()
    }
}
