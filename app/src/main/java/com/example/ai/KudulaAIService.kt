package com.example.ai

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.google.firebase.Firebase
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.content
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.Locale
import java.util.zip.ZipInputStream

/**
 * Erro da Biblioteca Inteligente com mensagem compreensível.
 */
class LibraryFileException(message: String) : Exception(message)

/**
 * Conteúdo preparado para análise.
 */
sealed class LibraryPayload {

    class Binary(
        val bytes: ByteArray,
        val mimeType: String,
        val kind: String
    ) : LibraryPayload()

    class TextContent(
        val text: String,
        val kind: String,
        val truncated: Boolean
    ) : LibraryPayload()

    class YouTube(
        val url: String
    ) : LibraryPayload()
}

/**
 * Normaliza links comuns do YouTube.
 */
fun normalizeYouTubeUrl(raw: String): String? {
    val text = raw.trim()
    if (text.isEmpty()) return null

    val match = Regex(
        """(?:v=|youtu\.be/|/shorts/|/embed/)([A-Za-z0-9_-]{11})"""
    ).find(text) ?: return null

    return "https://www.youtube.com/watch?v=${match.groupValues[1]}"
}

/**
 * Converte erros técnicos em mensagens claras.
 */
fun libraryErrorMessage(e: Throwable): String {
    if (e is LibraryFileException) {
        return e.message ?: "Não foi possível processar o ficheiro."
    }

    val text = (e.message ?: "").lowercase(Locale.ROOT)

    return when {
        e is UnknownHostException ||
                e is SocketTimeoutException ||
                e is ConnectException ->
            "Sem ligação à internet. Verifica a tua rede e tenta novamente."

        "quota" in text ||
                "429" in text ||
                "resource_exhausted" in text ||
                "rate limit" in text ->
            "O limite de utilização da IA foi atingido. Aguarda um pouco e tenta novamente."

        "413" in text ||
                "too large" in text ||
                ("payload" in text && "size" in text) ->
            "O ficheiro é demasiado grande. Usa um ficheiro mais pequeno."

        "safety" in text || "blocked" in text ->
            "A IA bloqueou este conteúdo pelos filtros de segurança."

        "api key" in text ||
                "permission" in text ||
                "403" in text ||
                "not enabled" in text ->
            "O serviço de IA não está autorizado. Verifica a configuração do Firebase AI Logic."

        "404" in text ||
                ("model" in text && "not found" in text) ->
            "O modelo de IA não está disponível. Verifica o nome e a configuração do modelo."

        else ->
            "Não foi possível processar o conteúdo. Tenta novamente. Se o problema continuar, verifica a ligação e os registos de erro."
    }
}

class KudulaAIService(
    @Suppress("unused")
    private val contentResolver: ContentResolver? = null
) {

    private val model: GenerativeModel by lazy {
        Firebase.ai(
            backend = GenerativeBackend.googleAI()
        ).generativeModel("gemini-3.8-flash")
    }

    /**
     * Mantém o streaming usado pelo Tutor Kudu.
     */
    fun generateTextStream(prompt: String): Flow<String> {
        return model.generateContentStream(prompt)
            .mapNotNull { chunk ->
                chunk.text?.takeIf { it.isNotEmpty() }
            }
    }

    // ---------------------------------------------------------
    // PREPARAÇÃO DO FICHEIRO
    // ---------------------------------------------------------

    suspend fun prepareFile(
        context: Context,
        uri: Uri
    ): LibraryPayload = withContext(Dispatchers.IO) {

        val fileName = queryFileName(context, uri)
        val mimeType = resolveMimeType(context, uri, fileName)

        when (mimeType) {

            DOCX_MIME -> {
                val text = extractDocxText(context, uri)
                buildTextPayload(text, "documento Word")
            }

            "text/plain" -> {
                val bytes = openStream(context, uri).use {
                    readLimited(
                        it,
                        MAX_TEXT_FILE_BYTES,
                        "O ficheiro de texto excede o limite de 2 MB."
                    )
                }

                buildTextPayload(
                    bytes.toString(Charsets.UTF_8),
                    "ficheiro de texto"
                )
            }

            "application/msword" -> {
                throw LibraryFileException(
                    "Os ficheiros Word antigos (.doc) não são suportados. " +
                            "Guarda o documento como .docx, TXT ou PDF."
                )
            }

            "application/pdf" -> {
                /*
                 * Extrai o texto real do PDF.
                 * O resultado TextContent permite ao ViewModel
                 * preencher originalText para o espaço de leitura.
                 */
                val extractedText = extractPdfText(context, uri)

                buildTextPayload(
                    extractedText,
                    "documento PDF"
                )
            }

            in IMAGE_MIME_TYPES -> {
                val bytes = readBinaryFile(context, uri)

                LibraryPayload.Binary(
                    bytes = bytes,
                    mimeType = mimeType,
                    kind = "imagem"
                )
            }

            in AUDIO_MIME_TYPES -> {
                val bytes = readBinaryFile(context, uri)

                LibraryPayload.Binary(
                    bytes = bytes,
                    mimeType = mimeType,
                    kind = "áudio"
                )
            }

            in VIDEO_MIME_TYPES -> {
                val bytes = readBinaryFile(context, uri)

                LibraryPayload.Binary(
                    bytes = bytes,
                    mimeType = mimeType,
                    kind = "vídeo"
                )
            }

            "video/x-matroska" -> {
                throw LibraryFileException(
                    "Os vídeos MKV não são suportados. " +
                            "Converte o vídeo para MP4 e tenta novamente."
                )
            }

            else -> {
                throw LibraryFileException(
                    "Formato não suportado: ${fileName.ifBlank { mimeType }}. " +
                            "Formatos previstos: PDF, DOCX, TXT, PNG, JPG, WEBP, " +
                            "MP3, M4A, WAV, AAC, MP4, MOV e WEBM."
                )
            }
        }
    }

    /**
     * Extrai texto de um PDF que contenha texto digital.
     *
     * PDFs digitalizados como imagens precisam de OCR,
     * que não é implementado por esta função.
     */
    private fun extractPdfText(
        context: Context,
        uri: Uri
    ): String {

        PDFBoxResourceLoader.init(context.applicationContext)

        openStream(context, uri).use { input ->
            PDDocument.load(input).use { document ->

                if (document.isEncrypted) {
                    throw LibraryFileException(
                        "Este PDF está protegido por palavra-passe. " +
                                "Desbloqueia-o e tenta novamente."
                    )
                }

                val stripper = PDFTextStripper().apply {
                    sortByPosition = true
                }

                val text = stripper.getText(document).trim()

                if (text.isBlank()) {
                    throw LibraryFileException(
                        "Não foi possível extrair texto deste PDF. " +
                                "Pode ser um documento digitalizado como imagens. " +
                                "Para esse tipo de documento, a Kudula precisará de OCR."
                    )
                }

                return text
            }
        }
    }

    /**
     * Lê ficheiros binários respeitando o limite configurado.
     */
    private fun readBinaryFile(
        context: Context,
        uri: Uri
    ): ByteArray {
        val declaredSize = queryFileSize(context, uri)

        if (declaredSize > MAX_INLINE_BYTES) {
            throw LibraryFileException(tooBigMessage(declaredSize))
        }

        val bytes = openStream(context, uri).use {
            readLimited(
                it,
                MAX_INLINE_BYTES,
                tooBigMessage(-1L)
            )
        }

        if (bytes.isEmpty()) {
            throw LibraryFileException("O ficheiro selecionado está vazio.")
        }

        return bytes
    }

    // ---------------------------------------------------------
    // ANÁLISE PELA IA
    // ---------------------------------------------------------

    suspend fun analyzePayload(
        payload: LibraryPayload,
        title: String,
        category: String
    ): String = withContext(Dispatchers.IO) {

        val response = when (payload) {

            is LibraryPayload.Binary -> {
                model.generateContent(
                    content {
                        inlineData(
                            payload.bytes,
                            payload.mimeType
                        )

                        text(
                            buildLibraryPrompt(
                                kind = payload.kind,
                                title = title,
                                category = category,
                                extraNote = ""
                            )
                        )
                    }
                )
            }

            is LibraryPayload.TextContent -> {
                val note = if (payload.truncated) {
                    "O texto foi limitado aos primeiros " +
                            "$MAX_TEXT_CHARS caracteres. " +
                            "A análise pode não abranger o documento completo."
                } else {
                    ""
                }

                model.generateContent(
                    content {
                        text(
                            buildLibraryPrompt(
                                kind = payload.kind,
                                title = title,
                                category = category,
                                extraNote = note
                            )
                        )

                        text(
                            "CONTEÚDO REAL DO FICHEIRO:\n${payload.text}"
                        )
                    }
                )
            }

            is LibraryPayload.YouTube -> {
                model.generateContent(
                    content {
                        fileData(
                            payload.url,
                            "video/mp4"
                        )

                        text(
                            buildLibraryPrompt(
                                kind = "vídeo do YouTube",
                                title = title,
                                category = category,
                                extraNote = ""
                            )
                        )
                    }
                )
            }
        }

        response.text
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?: throw LibraryFileException(
                "A IA não devolveu uma análise. Tenta novamente."
            )
    }

    // ---------------------------------------------------------
    // INSTRUÇÕES DA ANÁLISE
    // ---------------------------------------------------------

    private fun buildLibraryPrompt(
        kind: String,
        title: String,
        category: String,
        extraNote: String
    ): String {

        val kindInstruction = when {
            kind.contains("imagem", ignoreCase = true) ->
                "Interpreta o texto visível e descreve os diagramas. " +
                        "Indica o que não for legível."

            kind.contains("áudio", ignoreCase = true) ->
                "Resume o conteúdo audível e indica as partes inaudíveis."

            kind.contains("YouTube", ignoreCase = true) ->
                "Analisa o vídeo indicado apenas na medida em que o conteúdo estiver acessível."

            kind.contains("vídeo", ignoreCase = true) ->
                "Analisa o que é dito e mostrado, indicando as limitações."

            kind == "PDF" || kind == "documento PDF" ->
                "Analisa o texto extraído do PDF. " +
                        "Não afirmes que interpretaste imagens ou páginas " +
                        "que não estejam representadas no texto fornecido."

            else ->
                "Analisa o conteúdo real fornecido."
        }

        return """
            És o Kudu, tutor de aprendizagem da Kudula.

            Analisa o conteúdo fornecido.
            Tipo de material: $kind
            Título indicado pelo estudante: $title
            Categoria: $category

            $extraNote

            $kindInstruction

            Escreve em português natural, claro e adequado a estudantes angolanos.
            Não infantilizes o estudante.
            Não inventes informações ausentes do material.

            Usa texto simples, sem tabelas, sem LaTeX e sem formatação Markdown.
            Escreve cada título de secção em maiúsculas, numa linha própria.

            Organiza a resposta nestas secções:

            RESUMO GERAL
            Resume as ideias centrais do conteúdo disponível.

            IDEIAS E CONCEITOS PRINCIPAIS
            Identifica e explica os conceitos importantes.

            EXPLICAÇÃO SIMPLES DOS CONCEITOS MAIS DIFÍCEIS
            Explica de forma acessível, sem perder o significado.

            EXEMPLOS PRÁTICOS
            Dá exemplos coerentes com o material. Se o material não permitir exemplos,
            declara essa limitação.

            PERGUNTAS PARA TESTAR A COMPREENSÃO
            Cria cinco perguntas baseadas no conteúdo disponível.

            FLASHCARDS
            Cria cinco cartões. Cada cartão deve ocupar uma única linha e seguir exatamente:
            FLASHCARD: pergunta || resposta

            LIMITAÇÕES
            Indica páginas, secções, texto, som ou imagens que não foram interpretados,
            bem como qualquer informação em falta.

            Regras obrigatórias:
            - Distingue o que está no material das explicações acrescentadas.
            - Não inventes factos, citações, páginas ou conclusões.
            - Não declares que o documento foi lido na íntegra se isso não foi confirmado.
            - Se não conseguires interpretar o conteúdo, explica claramente a limitação.
        """.trimIndent()
    }

    // ---------------------------------------------------------
    // PREPARAÇÃO DE TEXTO
    // ---------------------------------------------------------

    private fun buildTextPayload(
        rawText: String,
        kind: String
    ): LibraryPayload.TextContent {

        val text = rawText.trim()

        if (text.isEmpty()) {
            throw LibraryFileException(
                "Não foi possível extrair texto deste ficheiro. " +
                        "Confirma que contém texto legível e não apenas imagens."
            )
        }

        val truncated = text.length > MAX_TEXT_CHARS

        return LibraryPayload.TextContent(
            text = if (truncated) text.take(MAX_TEXT_CHARS) else text,
            kind = kind,
            truncated = truncated
        )
    }

    private fun openStream(
        context: Context,
        uri: Uri
    ): InputStream {
        return context.contentResolver.openInputStream(uri)
            ?: throw LibraryFileException(
                "Não foi possível abrir o ficheiro selecionado."
            )
    }

    private fun readLimited(
        stream: InputStream,
        maxBytes: Int,
        tooBigText: String
    ): ByteArray {

        val output = ByteArrayOutputStream()
        val buffer = ByteArray(8192)
        var total = 0

        while (true) {
            val count = stream.read(buffer)

            if (count == -1) break

            if (total > maxBytes - count) {
                throw LibraryFileException(tooBigText)
            }

            total += count
            output.write(buffer, 0, count)
        }

        return output.toByteArray()
    }

    // ---------------------------------------------------------
    // EXTRAÇÃO DE TEXTO DOCX
    // ---------------------------------------------------------

    private fun extractDocxText(
        context: Context,
        uri: Uri
    ): String {

        openStream(context, uri).use { input ->
            ZipInputStream(input).use { zip ->

                var entry = zip.nextEntry

                while (entry != null) {

                    if (entry.name == "word/document.xml") {

                        val xml = readLimited(
                            zip,
                            MAX_DOCX_XML_BYTES,
                            "O documento Word é demasiado grande para ser lido."
                        ).toString(Charsets.UTF_8)

                        return docxXmlToPlainText(xml)
                    }

                    zip.closeEntry()
                    entry = zip.nextEntry
                }
            }
        }

        throw LibraryFileException(
            "Não foi possível ler o conteúdo deste DOCX. " +
                    "O ficheiro pode estar danificado ou não ser um documento Word válido."
        )
    }

    /**
     * Conversão simples do XML Word para texto.
     * Não preserva integralmente tabelas, imagens ou formatação.
     */
    private fun docxXmlToPlainText(xml: String): String {

        val withLineBreaks = xml
            .replace(Regex("</w:p\\s*>"), "\n")
            .replace(Regex("<w:tab\\s*/>"), "\t")
            .replace(Regex("<w:br\\s*/>"), "\n")
            .replace(Regex("</w:tc\\s*>"), "\t")

        val withoutTags = withLineBreaks
            .replace(Regex("<[^>]+>"), "")

        return decodeXmlEntities(withoutTags)
            .replace(Regex("[ \\t]+\\n"), "\n")
            .replace(Regex("\\n[ \\t]+"), "\n")
            .replace(Regex("\\n{3,}"), "\n\n")
            .trim()
    }

    private fun decodeXmlEntities(value: String): String {
        return value
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&apos;", "'")
            .replace("&amp;", "&")
    }

    // ---------------------------------------------------------
    // INFORMAÇÕES DO FICHEIRO
    // ---------------------------------------------------------

    private fun queryFileName(
        context: Context,
        uri: Uri
    ): String {

        if (uri.scheme == "content") {
            try {
                context.contentResolver.query(
                    uri,
                    arrayOf(OpenableColumns.DISPLAY_NAME),
                    null,
                    null,
                    null
                )?.use { cursor ->

                    val index = cursor.getColumnIndex(
                        OpenableColumns.DISPLAY_NAME
                    )

                    if (index >= 0 && cursor.moveToFirst()) {
                        val name = cursor.getString(index)
                        if (!name.isNullOrBlank()) return name
                    }
                }
            } catch (_: Exception) {
                // Continua para tentar obter o nome através do URI.
            }
        }

        return uri.lastPathSegment
            ?.substringAfterLast('/')
            .orEmpty()
    }

    private fun queryFileSize(
        context: Context,
        uri: Uri
    ): Long {

        if (uri.scheme == "file") {
            return try {
                File(uri.path.orEmpty()).length()
            } catch (_: Exception) {
                -1L
            }
        }

        return try {
            context.contentResolver.query(
                uri,
                arrayOf(OpenableColumns.SIZE),
                null,
                null,
                null
            )?.use { cursor ->

                val index = cursor.getColumnIndex(OpenableColumns.SIZE)

                if (index >= 0 && cursor.moveToFirst() && !cursor.isNull(index)) {
                    cursor.getLong(index)
                } else {
                    -1L
                }
            } ?: -1L
        } catch (_: Exception) {
            -1L
        }
    }

    /**
     * Identifica o tipo usando a extensão do nome e, em seguida,
     * o MIME fornecido pelo Android.
     */
    private fun resolveMimeType(
        context: Context,
        uri: Uri,
        fileName: String
    ): String {

        val extension = fileName
            .substringAfterLast('.', "")
            .lowercase(Locale.ROOT)

        val byExtension = when (extension) {
            "pdf" -> "application/pdf"
            "docx" -> DOCX_MIME
            "doc" -> "application/msword"
            "txt" -> "text/plain"

            "png" -> "image/png"
            "jpg", "jpeg" -> "image/jpeg"
            "webp" -> "image/webp"

            "mp3" -> "audio/mpeg"
            "m4a" -> "audio/mp4"
            "wav" -> "audio/wav"
            "aac" -> "audio/aac"
            "flac" -> "audio/flac"
            "opus" -> "audio/opus"

            "mp4" -> "video/mp4"
            "mov" -> "video/quicktime"
            "webm" -> "video/webm"
            "mpeg", "mpg" -> "video/mpeg"
            "3gp" -> "video/3gpp"
            "wmv" -> "video/x-ms-wmv"
            "flv" -> "video/x-flv"
            "mkv" -> "video/x-matroska"

            else -> null
        }

        if (byExtension != null) return byExtension

        val systemType = (
                context.contentResolver.getType(uri) ?: ""
                ).lowercase(Locale.ROOT)

        return when (systemType) {
            "audio/x-m4a", "audio/m4a" -> "audio/mp4"
            "audio/x-wav", "audio/wave" -> "audio/wav"
            "image/jpg" -> "image/jpeg"
            else -> systemType
        }
    }

    private fun tooBigMessage(sizeBytes: Long): String {

        val sizeText = if (sizeBytes > 0) {
            " (${String.format(
                Locale.getDefault(),
                "%.1f",
                sizeBytes / (1024.0 * 1024.0)
            )} MB)"
        } else {
            ""
        }

        return "O ficheiro$sizeText excede o limite atual de 14 MB. " +
                "Usa um ficheiro mais pequeno ou divide o documento."
    }

    private companion object {

        const val DOCX_MIME =
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"

        const val MAX_INLINE_BYTES = 14 * 1024 * 1024
        const val MAX_TEXT_FILE_BYTES = 2 * 1024 * 1024
        const val MAX_DOCX_XML_BYTES = 30 * 1024 * 1024
        const val MAX_TEXT_CHARS = 200_000

        val IMAGE_MIME_TYPES = setOf(
            "image/png",
            "image/jpeg",
            "image/webp"
        )

        val AUDIO_MIME_TYPES = setOf(
            "audio/aac",
            "audio/flac",
            "audio/mp3",
            "audio/mpeg",
            "audio/mp4",
            "audio/opus",
            "audio/wav",
            "audio/webm"
        )

        val VIDEO_MIME_TYPES = setOf(
            "video/x-flv",
            "video/quicktime",
            "video/mpeg",
            "video/mpg",
            "video/mp4",
            "video/webm",
            "video/wmv",
            "video/x-ms-wmv",
            "video/3gpp"
        )
    }
}