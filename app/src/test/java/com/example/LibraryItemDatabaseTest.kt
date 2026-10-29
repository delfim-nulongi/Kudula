package com.example

import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.KudulaDatabase
import com.example.data.local.KudulaDatabaseProvider
import com.example.data.local.KudulaTypeConverters
import com.example.data.local.entity.LibraryItemEntity
import com.example.model.LibraryItem
import com.example.model.LibraryItemType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.IOException

@RunWith(RobolectricTestRunner::class)
class LibraryItemDatabaseTest {

    private lateinit var database: KudulaDatabase

    @Before
    fun createDb() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            KudulaDatabase::class.java
        ).allowMainThreadQueries().build()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        database.close()
    }

    @Test
    fun saveAndRecoverLibraryItem() = runBlocking {
        val item = LibraryItem(
            id = "lib-test-1",
            title = "Manual de Inteligência Artificial em Angola",
            type = LibraryItemType.PDF,
            category = "Tecnologia",
            summary = "Resumo detalhado sobre infraestrutura de IA local",
            readTime = "15 min",
            progress = 0.5f,
            ikcValidated = 80,
            tags = listOf("IA", "Angola", "Kudula"),
            preloadedConcepts = listOf("Modelos de Linguagem", "KCLS"),
            sourceUri = "content://media/external/files/123",
            originalText = "Conteúdo original do PDF extraído com sucesso.",
            extractionComplete = true
        )

        val entity = LibraryItemEntity.fromLibraryItem(item)
        database.libraryItemDao().insert(entity)

        val retrievedEntity = database.libraryItemDao().getById("lib-test-1")
        assertNotNull(retrievedEntity)

        val retrievedItem = retrievedEntity!!.toLibraryItem()
        assertEquals("lib-test-1", retrievedItem.id)
        assertEquals("Manual de Inteligência Artificial em Angola", retrievedItem.title)
        assertEquals(LibraryItemType.PDF, retrievedItem.type)
        assertEquals(listOf("IA", "Angola", "Kudula"), retrievedItem.tags)
        assertEquals(listOf("Modelos de Linguagem", "KCLS"), retrievedItem.preloadedConcepts)
        assertEquals("Conteúdo original do PDF extraído com sucesso.", retrievedItem.originalText)
        assertTrue(retrievedItem.extractionComplete)
    }

    @Test
    fun saveAndRecoverEditedStructureJson() = runBlocking {
        val sampleJson = """
            [
              {
                "id": 1,
                "isAiAnalysis": false,
                "chapterBadge": "CAP. 1",
                "title": "Fotossíntese Editada",
                "subtitle": "Visão geral",
                "readTime": "10 min de leitura",
                "sections": [
                  {
                    "title": "1.1 Reações Luminosas",
                    "paragraphs": ["Parágrafo 1 editado pelo utilizador."]
                  }
                ]
              }
            ]
        """.trimIndent()

        val item = LibraryItem(
            id = "lib-edited-struct",
            title = "Livro Editado",
            type = LibraryItemType.PDF,
            category = "Biologia",
            summary = "Resumo",
            readTime = "10 min",
            progress = 0f,
            ikcValidated = 100,
            originalText = "Texto original",
            extractionComplete = true,
            editedStructureJson = sampleJson
        )

        database.libraryItemDao().insert(LibraryItemEntity.fromLibraryItem(item))

        val retrieved = database.libraryItemDao().getById("lib-edited-struct")?.toLibraryItem()
        assertNotNull(retrieved)
        assertEquals(sampleJson, retrieved!!.editedStructureJson)
        assertEquals("Texto original", retrieved.originalText)
    }

    @Test
    fun preserveExtensiveOriginalTextAndSummary() = runBlocking {
        val largeText = "A".repeat(500_000)
        val largeSummary = "B".repeat(50_000)

        val item = LibraryItem(
            id = "lib-large-doc",
            title = "Documento Extenso",
            type = LibraryItemType.DOCX,
            category = "Investigação",
            summary = largeSummary,
            readTime = "60 min",
            progress = 0.1f,
            ikcValidated = 95,
            originalText = largeText,
            extractionComplete = true
        )

        database.libraryItemDao().insert(LibraryItemEntity.fromLibraryItem(item))

        val retrieved = database.libraryItemDao().getById("lib-large-doc")?.toLibraryItem()
        assertNotNull(retrieved)
        assertEquals(500_000, retrieved!!.originalText?.length)
        assertEquals(50_000, retrieved.summary.length)
    }

    @Test
    fun updateDocumentWithoutLosingOriginalText() = runBlocking {
        val initialItem = LibraryItem(
            id = "lib-update-test",
            title = "Tese Académica",
            type = LibraryItemType.PDF,
            category = "Educação",
            summary = "A processar com a Kudula AI...",
            readTime = "20 min",
            progress = 0.0f,
            ikcValidated = 0,
            originalText = "Texto da tese extraído na primeira etapa.",
            extractionComplete = true
        )

        database.libraryItemDao().insert(LibraryItemEntity.fromLibraryItem(initialItem))

        val updatedItem = initialItem.copy(
            summary = "Análise final da tese concluída com 3 conceitos chave."
        )

        database.libraryItemDao().insert(LibraryItemEntity.fromLibraryItem(updatedItem))

        val allItems = database.libraryItemDao().observeAll().first()
        assertEquals(1, allItems.size)

        val result = allItems.first().toLibraryItem()
        assertEquals("Análise final da tese concluída com 3 conceitos chave.", result.summary)
        assertEquals("Texto da tese extraído na primeira etapa.", result.originalText)
        assertTrue(result.extractionComplete)
    }

    @Test
    fun restoreEmptyLibraryCorrectly() = runBlocking {
        val emptyItems = database.libraryItemDao().observeAll().first()
        assertTrue("A biblioteca deve estar inicialmente vazia", emptyItems.isEmpty())
    }

    @Test
    fun typeConvertersHandleSpecialCharactersAndLegacyFormat() {
        val converters = KudulaTypeConverters()

        val tagsWithSpecialChars = listOf("Engenharia || Software", "IA & Saúde", "C++ / Rust")
        val jsonConverted = converters.fromStringList(tagsWithSpecialChars)
        val restoredList = converters.toStringList(jsonConverted)

        assertEquals(tagsWithSpecialChars, restoredList)

        val legacyString = "Tag1||Tag2||Tag3"
        val restoredLegacy = converters.toStringList(legacyString)
        assertEquals(listOf("Tag1", "Tag2", "Tag3"), restoredLegacy)
    }

    @Test
    fun migrationFromVersion5To6AddsEditedStructureColumn() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val sqliteFactory = FrameworkSQLiteOpenHelperFactory()
        val config = SupportSQLiteOpenHelper.Configuration.builder(context)
            .name(null)
            .callback(object : SupportSQLiteOpenHelper.Callback(5) {
                override fun onCreate(db: SupportSQLiteDatabase) {
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

                override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {}
            })
            .build()

        val dbV5 = sqliteFactory.create(config).writableDatabase

        dbV5.execSQL(
            """
            INSERT INTO library_items (id, title, type, category, summary, readTime, progress, ikcValidated, tags, preloadedConcepts, difficulty, estimatedTime, source, confidenceBadge, waveformPoints, extractionComplete)
            VALUES ('item-v5', 'Livro v5', 'PDF', 'Geral', 'Resumo', '10 min', 0, 0, '[]', '[]', 'Intermediário', '20 min', 'Kudula', '98%', '', 1)
            """.trimIndent()
        )

        // Migrar para versão 6
        KudulaDatabaseProvider.MIGRATION_5_6.migrate(dbV5)

        val cursor = dbV5.query("SELECT id, title, editedStructureJson FROM library_items WHERE id = 'item-v5'")
        assertTrue(cursor.moveToFirst())
        assertEquals("Livro v5", cursor.getString(cursor.getColumnIndexOrThrow("title")))
        assertNull(cursor.getString(cursor.getColumnIndexOrThrow("editedStructureJson")))
        cursor.close()

        dbV5.close()
    }
}
