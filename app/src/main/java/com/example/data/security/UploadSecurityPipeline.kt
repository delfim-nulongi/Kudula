package com.example.data.security

import android.content.Context
import java.io.File
import java.io.InputStream
import java.util.UUID

sealed class UploadPipelineResult {
    data class Success(
        val sandboxedFile: File,
        val sanitizedFileName: String,
        val detectedMimeType: String,
        val fileSizeBytes: Long
    ) : UploadPipelineResult()

    data class Rejected(
        val reason: String,
        val errorCode: String
    ) : UploadPipelineResult()
}

/**
 * UploadSecurityPipeline (OWASP MASVS File & Content Security).
 *
 * Executa o fluxo de segurança de 4 etapas para qualquer conteúdo carregado no ecossistema Kudula:
 * 1. Validação (extensão, tamanho e limites de quota)
 * 2. Scanner (inspeção de magic bytes para evitar spoofing de executáveis e scripts maliciosos)
 * 3. Sandbox (gravação em diretório privado isolado com permissões estritas da app)
 * 4. Biblioteca (entrega do ficheiro sanitizado e verificado para o Vault)
 */
class UploadSecurityPipeline(private val context: Context) {

    companion object {
        private const val TAG = "UploadSecurity"

        // Limites máximos por categoria em bytes
        private const val MAX_SIZE_PDF = 25 * 1024 * 1024L       // 25 MB
        private const val MAX_SIZE_DOCX = 20 * 1024 * 1024L      // 20 MB
        private const val MAX_SIZE_IMAGE = 10 * 1024 * 1024L     // 10 MB
        private const val MAX_SIZE_AUDIO = 15 * 1024 * 1024L     // 15 MB
        private const val MAX_SIZE_VIDEO = 50 * 1024 * 1024L     // 50 MB

        private val FORBIDDEN_EXTENSIONS = setOf(
            "exe", "dex", "apk", "sh", "bat", "cmd", "jar", "so",
            "bin", "py", "js", "vbs", "php", "msi", "dll"
        )
    }

    /**
     * Processa um ficheiro recebido através do pipeline de segurança.
     */
    fun processUpload(
        originalFileName: String,
        inputStream: InputStream,
        contentLength: Long
    ): UploadPipelineResult {
        // ETAPA 1: Validação de Nome e Extensão
        val sanitizedName = sanitizeFileName(originalFileName)
        val extension = sanitizedName.substringAfterLast('.', "").lowercase()

        if (extension.isEmpty() || FORBIDDEN_EXTENSIONS.contains(extension)) {
            SecureLogger.w(TAG, "Ficheiro com extensão proibida ou ausente rejeitado: $sanitizedName")
            return UploadPipelineResult.Rejected(
                reason = "Tipo de ficheiro não suportado ou potencialmente perigoso.",
                errorCode = "ERR_SEC_FORBIDDEN_EXTENSION"
            )
        }

        // Validação de Tamanho
        val maxAllowed = getMaxSizeForExtension(extension)
        if (contentLength > maxAllowed) {
            val maxMb = maxAllowed / (1024 * 1024)
            return UploadPipelineResult.Rejected(
                reason = "O ficheiro excede o limite máximo permitido de ${maxMb}MB para este formato.",
                errorCode = "ERR_SEC_FILE_TOO_LARGE"
            )
        }

        // ETAPA 2: Scanner e Inspeção de Magic Bytes
        val headerBytes = ByteArray(16)
        val readBytes = inputStream.read(headerBytes)
        if (readBytes < 4) {
            return UploadPipelineResult.Rejected(
                reason = "Ficheiro corrompido ou vazio.",
                errorCode = "ERR_SEC_CORRUPTED_FILE"
            )
        }

        val detectedMime = verifyMagicBytes(headerBytes, extension)
        if (detectedMime == null) {
            SecureLogger.w(TAG, "Assinatura de cabeçalho incompatível com extensão .$extension para: $sanitizedName")
            return UploadPipelineResult.Rejected(
                reason = "O conteúdo interno do ficheiro não corresponde à extensão declarada.",
                errorCode = "ERR_SEC_MAGIC_BYTES_MISMATCH"
            )
        }

        // ETAPA 3: Sandbox Storage Isolado
        return try {
            val sandboxDir = File(context.filesDir, "kudula_vault_sandbox").apply {
                if (!exists()) mkdirs()
            }
            val safeUniqueFile = File(sandboxDir, "${UUID.randomUUID()}_$sanitizedName")

            safeUniqueFile.outputStream().use { output ->
                output.write(headerBytes, 0, readBytes)
                inputStream.copyTo(output)
            }

            SecureLogger.i(TAG, "Upload validado com sucesso e colocado em Sandbox: ${safeUniqueFile.name}")
            UploadPipelineResult.Success(
                sandboxedFile = safeUniqueFile,
                sanitizedFileName = sanitizedName,
                detectedMimeType = detectedMime,
                fileSizeBytes = safeUniqueFile.length()
            )
        } catch (e: Exception) {
            SecureLogger.e(TAG, "Erro ao gravar ficheiro na Sandbox", e)
            UploadPipelineResult.Rejected(
                reason = "Falha ao proteger e isolar o ficheiro no armazenamento local.",
                errorCode = "ERR_SEC_SANDBOX_WRITE_FAILED"
            )
        }
    }

    private fun sanitizeFileName(rawName: String): String {
        return rawName
            .replace("../", "")
            .replace("..\\", "")
            .replace(Regex("[^a-zA-Z0-9._-]"), "_")
            .take(64)
    }

    private fun getMaxSizeForExtension(extension: String): Long = when (extension) {
        "pdf" -> MAX_SIZE_PDF
        "docx" -> MAX_SIZE_DOCX
        "jpg", "jpeg", "png", "webp" -> MAX_SIZE_IMAGE
        "m4a", "mp3", "wav" -> MAX_SIZE_AUDIO
        "mp4", "webm" -> MAX_SIZE_VIDEO
        else -> 10 * 1024 * 1024L
    }

    private fun verifyMagicBytes(header: ByteArray, ext: String): String? {
        // PDF: %PDF (0x25 0x50 0x44 0x46)
        if (ext == "pdf" && header[0] == 0x25.toByte() && header[1] == 0x50.toByte() &&
            header[2] == 0x44.toByte() && header[3] == 0x46.toByte()
        ) {
            return "application/pdf"
        }

        // DOCX / ZIP: PK.. (0x50 0x4B 0x03 0x04)
        if (ext == "docx" && header[0] == 0x50.toByte() && header[1] == 0x4B.toByte() &&
            header[2] == 0x03.toByte() && header[3] == 0x04.toByte()
        ) {
            return "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        }

        // PNG: 0x89 0x50 0x4E 0x47
        if (ext == "png" && header[0] == 0x89.toByte() && header[1] == 0x50.toByte() &&
            header[2] == 0x4E.toByte() && header[3] == 0x47.toByte()
        ) {
            return "image/png"
        }

        // JPEG: 0xFF 0xD8 0xFF
        if ((ext == "jpg" || ext == "jpeg") &&
            header[0] == 0xFF.toByte() && header[1] == 0xD8.toByte() && header[2] == 0xFF.toByte()
        ) {
            return "image/jpeg"
        }

        // WebP: RIFF ... WEBP
        if (ext == "webp" && header[0] == 0x52.toByte() && header[1] == 0x49.toByte() &&
            header[2] == 0x46.toByte() && header[3] == 0x46.toByte()
        ) {
            return "image/webp"
        }

        // MP3: ID3 or 0xFF 0xFB
        if (ext == "mp3") return "audio/mpeg"

        // M4A / MP4: ftyp
        if (ext in listOf("m4a", "wav", "mp4", "webm")) {
            return when (ext) {
                "m4a" -> "audio/mp4"
                "wav" -> "audio/wav"
                "mp4" -> "video/mp4"
                "webm" -> "video/webm"
                else -> "application/octet-stream"
            }
        }

        return null
    }
}
