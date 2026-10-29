package com.example.ui.util

import android.graphics.Bitmap
import android.graphics.Matrix
import java.io.ByteArrayOutputStream

/**
 * Utilitário de processamento para captura de documentos via câmara.
 * Aplica compressão automática e correção de rotação EXIF para otimização cognitiva.
 */
object BitmapCompressor {

    /**
     * Redimensiona e comprime um Bitmap mantendo o ratio e legibilidade ideal de texto.
     */
    fun compressAndOptimize(
        source: Bitmap,
        maxDimension: Int = 1920,
        quality: Int = 85
    ): Bitmap {
        val width = source.width
        val height = source.height

        val scale = if (width > maxDimension || height > maxDimension) {
            val max = maxOf(width, height)
            maxDimension.toFloat() / max.toFloat()
        } else {
            1.0f
        }

        val matrix = Matrix()
        if (scale < 1.0f) {
            matrix.postScale(scale, scale)
        }

        val scaledBitmap = if (scale < 1.0f) {
            Bitmap.createBitmap(source, 0, 0, width, height, matrix, true)
        } else {
            source
        }

        // Compress to JPEG bytes and re-decode if needed, or return memory-optimized bitmap
        val outputStream = ByteArrayOutputStream()
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)

        return scaledBitmap
    }

    /**
     * Aplica rotação necessária para correção de orientação.
     */
    fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
        if (degrees == 0f) return bitmap
        val matrix = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
}
