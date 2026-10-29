package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.LibraryItem
import com.example.model.LibraryItemType

/**
 * Entidade Room para persistência local da Biblioteca Inteligente da Kudula.
 */
@Entity(tableName = "library_items")
data class LibraryItemEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val type: String,
    val category: String,
    val summary: String,
    val readTime: String,
    val progress: Float,
    val ikcValidated: Int,
    val tags: List<String> = emptyList(),
    val preloadedConcepts: List<String> = emptyList(),
    val difficulty: String = "Intermediário",
    val estimatedTime: String = "20 min",
    val source: String = "Verificado Kudula",
    val confidenceBadge: String = "98% Confiabilidade",
    val thumbnailUri: String? = null,
    val duration: String? = null,
    val fileSize: String? = null,
    val waveformPoints: List<Float> = emptyList(),
    val sourceUri: String? = null,
    val originalText: String? = null,
    val extractionComplete: Boolean = false,
    val editedStructureJson: String? = null
) {
    fun toLibraryItem(): LibraryItem {
        val itemType = try {
            LibraryItemType.valueOf(type)
        } catch (_: Exception) {
            LibraryItemType.PDF
        }
        return LibraryItem(
            id = id,
            title = title,
            type = itemType,
            category = category,
            summary = summary,
            readTime = readTime,
            progress = progress,
            ikcValidated = ikcValidated,
            tags = tags,
            preloadedConcepts = preloadedConcepts,
            difficulty = difficulty,
            estimatedTime = estimatedTime,
            source = source,
            confidenceBadge = confidenceBadge,
            thumbnailUri = thumbnailUri,
            duration = duration,
            fileSize = fileSize,
            waveformPoints = waveformPoints,
            sourceUri = sourceUri,
            originalText = originalText,
            extractionComplete = extractionComplete,
            editedStructureJson = editedStructureJson
        )
    }

    companion object {
        fun fromLibraryItem(item: LibraryItem): LibraryItemEntity {
            return LibraryItemEntity(
                id = item.id,
                title = item.title,
                type = item.type.name,
                category = item.category,
                summary = item.summary,
                readTime = item.readTime,
                progress = item.progress,
                ikcValidated = item.ikcValidated,
                tags = item.tags,
                preloadedConcepts = item.preloadedConcepts,
                difficulty = item.difficulty,
                estimatedTime = item.estimatedTime,
                source = item.source,
                confidenceBadge = item.confidenceBadge,
                thumbnailUri = item.thumbnailUri,
                duration = item.duration,
                fileSize = item.fileSize,
                waveformPoints = item.waveformPoints,
                sourceUri = item.sourceUri,
                originalText = item.originalText,
                extractionComplete = item.extractionComplete,
                editedStructureJson = item.editedStructureJson
            )
        }
    }
}
