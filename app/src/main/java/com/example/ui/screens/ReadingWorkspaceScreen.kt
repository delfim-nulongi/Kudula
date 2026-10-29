package com.example.ui.screens

import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.EventLogger
import com.example.model.LibraryItem
import com.example.model.ReadingVoiceConfig
import com.example.ui.components.KudulaEmptyStateCard
import com.example.ui.theme.*
import com.example.ui.util.LocalScreenWindowInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.util.Locale

/**
 * Guarda posição de leitura por ITEM (livro/documento).
 * (Em memória. Idealmente, persistir em Room/DataStore — mas esta mudança já corrige o bug entre livros.)
 */
object ReadingPositionStore {
    private val positions = mutableMapOf<String, Pair<Int, Int>>() // itemId -> (chapterIndex, paragraphIndex)

    fun getPosition(itemId: String?): Pair<Int, Int> {
        if (itemId.isNullOrBlank()) return 0 to 0
        return positions[itemId] ?: (0 to 0)
    }

    fun savePosition(itemId: String?, chapterIndex: Int, paragraphIndex: Int) {
        if (itemId.isNullOrBlank()) return
        positions[itemId] = chapterIndex to paragraphIndex
    }
}

data class ReadingSection(
    val title: String = "",
    val paragraphs: List<String>
)

data class ReadingChapter(
    val id: Int,
    val isAiAnalysis: Boolean = false,
    val chapterBadge: String,
    val title: String,
    val subtitle: String,
    val readTime: String,
    val sections: List<ReadingSection>,
    val highlights: List<ReadingHighlight> = emptyList()
) {
    val paragraphs: List<String>
        get() = sections.flatMap { it.paragraphs }
}

data class ReadingHighlight(
    val key: String,
    val title: String,
    val quote: String,
    val aiInsight: String
)

private val UNNUMBERED_CHAPTER_KEYWORDS = mapOf(
    "prefácio" to "PREFÁCIO",
    "prefacio" to "PREFÁCIO",
    "foreword" to "PREFÁCIO",
    "introdução" to "INTRODUÇÃO",
    "introducao" to "INTRODUÇÃO",
    "introduction" to "INTRODUÇÃO",
    "conclusão" to "CONCLUSÃO",
    "conclusao" to "CONCLUSÃO",
    "conclusion" to "CONCLUSÃO",
    "considerações finais" to "CONCLUSÃO",
    "consideracoes finais" to "CONCLUSÃO",
    "apêndice" to "APÊNDICE",
    "apendice" to "APÊNDICE",
    "appendix" to "APÊNDICE",
    "anexo" to "ANEXO",
    "annex" to "ANEXO",
    "posfácio" to "POSFÁCIO",
    "posfacio" to "POSFÁCIO",
    "epilogue" to "POSFÁCIO",
    "agradecimentos" to "AGRADECIMENTOS",
    "acknowledgments" to "AGRADECIMENTOS",
    "sobre o autor" to "AUTOR",
    "sobre os autores" to "AUTORES",
    "referências" to "REFERÊNCIAS",
    "referencias" to "REFERÊNCIAS",
    "referências bibliográficas" to "REFERÊNCIAS"
)

private data class ChapterHeaderMatch(
    val index: Int,
    val badge: String,
    val title: String,
    val rawHeadingLine: String
)

/**
 * Desserializa a estrutura de capítulos guardada pelo utilizador em JSON.
 */
private fun parseEditedStructureJson(jsonStr: String): List<ReadingChapter> {
    val result = mutableListOf<ReadingChapter>()
    try {
        val array = JSONArray(jsonStr)
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            val id = obj.optInt("id", i)
            val isAiAnalysis = obj.optBoolean("isAiAnalysis", false)
            val badge = obj.optString("chapterBadge", "CAP. ${i + 1}")
            val title = obj.optString("title", "Capítulo $i")
            val subtitle = obj.optString("subtitle", "")
            val readTime = obj.optString("readTime", "5 min de leitura")

            val sections = mutableListOf<ReadingSection>()
            val secArray = obj.optJSONArray("sections")
            if (secArray != null) {
                for (j in 0 until secArray.length()) {
                    val secObj = secArray.getJSONObject(j)
                    val secTitle = secObj.optString("title", "")
                    val paraArray = secObj.optJSONArray("paragraphs")
                    val paragraphs = mutableListOf<String>()
                    if (paraArray != null) {
                        for (k in 0 until paraArray.length()) {
                            paragraphs.add(paraArray.getString(k))
                        }
                    }
                    sections.add(ReadingSection(secTitle, paragraphs))
                }
            }

            result.add(
                ReadingChapter(
                    id = id,
                    isAiAnalysis = isAiAnalysis,
                    chapterBadge = badge,
                    title = title,
                    subtitle = subtitle,
                    readTime = readTime,
                    sections = sections
                )
            )
        }
    } catch (e: Exception) {
        android.util.Log.e("ReadingWorkspace", "Erro ao desserializar estrutura editada", e)
    }
    return result
}

/**
 * Encontra a zona provável do Sumário/Índice para ser ignorada na deteção de capítulos.
 * Retorna IntRange de caracteres (start..endExclusive-1) ou null.
 */
private fun findTocRange(text: String): IntRange? {
    val lower = text.lowercase(Locale.ROOT)
    val tocStart = sequenceOf("sumário", "sumario", "índice", "indice")
        .mapNotNull { kw -> lower.indexOf(kw).takeIf { it != -1 } }
        .minOrNull()

    // Evitar falsos positivos (sumário muito longe no texto)
    if (tocStart == null || tocStart > 20_000) return null

    val tocEndExclusive = (tocStart + 4000).coerceAtMost(text.length)
    if (tocEndExclusive <= tocStart) return null

    return tocStart until tocEndExclusive
}

/**
 * Deteta os títulos declarados no Sumário do livro quando presente nas primeiras páginas.
 */
private fun extractTocTitles(text: String): Set<String> {
    val tocRange = findTocRange(text) ?: return emptySet()
    val snippet = text.substring(tocRange.first, tocRange.last + 1)

    val result = mutableSetOf<String>()
    val explicitCapRegex = Regex("""(?i)^(?:capítulo|capitulo|cap\.|chapter)\s*\d+.*""")

    for (line in snippet.lines()) {
        val clean = line.replace(Regex("""[\.\_\-\s]{2,}\s*\d+$"""), "").trim()
        if (clean.isBlank() || clean.length > 100) continue

        val lowerClean = clean.lowercase(Locale.ROOT)
        if (
            lowerClean.equals("sumário", ignoreCase = true) ||
            lowerClean.equals("sumario", ignoreCase = true) ||
            lowerClean.equals("índice", ignoreCase = true) ||
            lowerClean.equals("indice", ignoreCase = true)
        ) continue

        if (explicitCapRegex.matches(clean)) {
            result.add(clean.lowercase(Locale.ROOT))
        } else {
            val unnumberedKey = UNNUMBERED_CHAPTER_KEYWORDS.keys.firstOrNull { kw ->
                lowerClean == kw || lowerClean.startsWith("$kw:") || lowerClean.startsWith("$kw -")
            }
            if (unnumberedKey != null) {
                result.add(clean.lowercase(Locale.ROOT))
            }
        }
    }

    return result
}

/**
 * Identifica capítulos principais de Nível 1 (numerados ou não-numerados),
 * ignorando sumário/índice, listas numeradas no corpo do texto, subsecções (1.1, 1.2),
 * números de página e ruído de rodapé.
 *
 * Inclui melhoria: se "Capítulo X" vier sozinho numa linha, tenta capturar o título na linha seguinte.
 */
private fun parseRealChapters(text: String): List<ReadingChapter> {
    val lines = text.lines()
    val headerMatches = mutableListOf<ChapterHeaderMatch>()

    val explicitChapterRegex = Regex(
        """^(?:capítulo|capitulo|cap\.|chapter)\s*(\d+|[ivxlcdm]+)[\s:.\-—]*(.*)$""",
        RegexOption.IGNORE_CASE
    )

    val tocTitles = extractTocTitles(text)
    val tocRange = findTocRange(text)

    var charIndexOffset = 0
    var numberedCounter = 1

    fun isInsideToc(lineStart: Int): Boolean {
        return tocRange != null && lineStart in tocRange
    }

    for ((lineIdx, line) in lines.withIndex()) {
        val lineStart = charIndexOffset
        val trimmed = line.trim()
        charIndexOffset += line.length + 1

        // 1) Ignora completamente a zona do Sumário/Índice
        if (isInsideToc(lineStart)) continue

        if (trimmed.isBlank() || trimmed.length > 120) continue

        // 2) Filtro de ruído: números de página, cabeçalhos/rodapés comuns
        if (
            trimmed.matches(Regex("""^(?:página|pág\.|page)\s*\d+$""", RegexOption.IGNORE_CASE)) ||
            trimmed.matches(Regex("""^\d{1,4}$"""))
        ) continue

        val cleanLine = trimmed.replace(Regex("^#+\\s*"), "").trim()
        val lowerLine = cleanLine.lowercase(Locale.ROOT)

        // 3) Capítulos não-numerados principais
        val unnumberedKey = UNNUMBERED_CHAPTER_KEYWORDS.entries.firstOrNull { (kw, _) ->
            lowerLine == kw ||
                    lowerLine.startsWith("$kw:") ||
                    lowerLine.startsWith("$kw -") ||
                    lowerLine.startsWith("$kw —")
        }

        if (unnumberedKey != null) {
            val badge = unnumberedKey.value
            val titleText = cleanLine
                .replace(
                    Regex(
                        """^(?:prefácio|prefacio|introdução|introducao|conclusão|conclusao|apêndice|apendice|posfácio|posfacio|agradecimentos|sobre o autor|sobre os autores|referências|referencias)[\s:.\-—]*""",
                        RegexOption.IGNORE_CASE
                    ),
                    ""
                )
                .trim()
                .ifBlank { badge.lowercase(Locale.ROOT).replaceFirstChar { it.uppercase() } }

            headerMatches.add(
                ChapterHeaderMatch(
                    index = lineStart,
                    badge = badge,
                    title = titleText,
                    rawHeadingLine = cleanLine
                )
            )
            continue
        }

        // 4) Capítulos numerados principais ("Capítulo 1: ...", "Cap. 2")
        val explicitMatch = explicitChapterRegex.find(cleanLine)
        if (explicitMatch != null) {
            val numStr = explicitMatch.groupValues[1].uppercase(Locale.ROOT)
            var rawTitle = explicitMatch.groupValues[2].trim()
                .replace(Regex("""^\d+\.\s*"""), "")
                .trim()

            // Melhoria: se não há título na mesma linha, tentar linha seguinte não-vazia (fora do TOC)
            if (rawTitle.isBlank()) {
                val nextNonBlank = lines.drop(lineIdx + 1)
                    .take(10) // limite defensivo
                    .firstOrNull { it.trim().isNotBlank() }
                    ?.trim()

                if (!nextNonBlank.isNullOrBlank()) {
                    // Evitar capturar linhas que são só números ou ruído
                    val ok =
                        nextNonBlank.length in 3..120 &&
                                !nextNonBlank.matches(Regex("""^\d{1,4}$""")) &&
                                !nextNonBlank.lowercase(Locale.ROOT).startsWith("sumário") &&
                                !nextNonBlank.lowercase(Locale.ROOT).startsWith("índice")
                    if (ok) rawTitle = nextNonBlank
                }
            }

            val titleText = rawTitle.ifBlank { "Capítulo $numStr" }

            headerMatches.add(
                ChapterHeaderMatch(
                    index = lineStart,
                    badge = "CAP. $numStr",
                    title = titleText,
                    rawHeadingLine = cleanLine
                )
            )
            numberedCounter++
            continue
        }

        // 5) Correspondência com o Sumário extraído (fora do TOC): ajuda quando o corpo do livro usa apenas títulos
        if (tocTitles.isNotEmpty() && lowerLine in tocTitles) {
            headerMatches.add(
                ChapterHeaderMatch(
                    index = lineStart,
                    badge = "CAP. $numberedCounter",
                    title = cleanLine,
                    rawHeadingLine = cleanLine
                )
            )
            numberedCounter++
            continue
        }

        // 6) Markdown `# Title` (Nível 1 apenas)
        if (line.startsWith("# ") && !line.startsWith("## ")) {
            val titleText = cleanLine.replace(Regex("""^\d+\.\s*"""), "").trim()
            headerMatches.add(
                ChapterHeaderMatch(
                    index = lineStart,
                    badge = "CAP. $numberedCounter",
                    title = titleText.ifBlank { "Capítulo $numberedCounter" },
                    rawHeadingLine = cleanLine
                )
            )
            numberedCounter++
        }
    }

    // Fallback: sem capítulos detetados — dividir por parágrafos e/ou partes
    if (headerMatches.isEmpty()) {
        val rawParagraphs = text.split(Regex("\n\\s*\n"))
            .map { it.trim() }
            .filter { it.isNotBlank() }

        if (rawParagraphs.isEmpty()) return emptyList()

        if (rawParagraphs.size <= 12) {
            return listOf(
                ReadingChapter(
                    id = 1,
                    isAiAnalysis = false,
                    chapterBadge = "CAP. 1",
                    title = "Conteúdo Integral do Documento",
                    subtitle = rawParagraphs.first().take(80) + "...",
                    readTime = "${maxOf(1, (text.length / 900) + 1)} min de leitura",
                    sections = listOf(ReadingSection(title = "", paragraphs = rawParagraphs))
                )
            )
        }

        val chunks = rawParagraphs.chunked(15)
        return chunks.mapIndexed { index, chunk ->
            val firstSnippet = chunk.first().take(60).trim()
            ReadingChapter(
                id = index + 1,
                isAiAnalysis = false,
                chapterBadge = "CAP. ${index + 1}",
                title = "Parte ${index + 1}: $firstSnippet...",
                subtitle = "Leitura contínua da secção ${index + 1}",
                readTime = "${maxOf(1, (chunk.sumOf { it.length } / 900) + 1)} min de leitura",
                sections = listOf(ReadingSection(title = "", paragraphs = chunk))
            )
        }
    }

    val chapters = mutableListOf<ReadingChapter>()

    for (i in headerMatches.indices) {
        val currentHeader = headerMatches[i]
        val startIndex = currentHeader.index
        val endIndex = if (i + 1 < headerMatches.size) headerMatches[i + 1].index else text.length

        val chapterText = text.substring(startIndex, endIndex).trim()
        val sections = parseChapterSubsections(chapterText, currentHeader.title, currentHeader.rawHeadingLine)

        val firstParaSnippet = sections.firstOrNull()?.paragraphs?.firstOrNull()?.take(90)?.let { "$it..." }
            ?: "Secção do documento"

        chapters.add(
            ReadingChapter(
                id = i + 1,
                isAiAnalysis = false,
                chapterBadge = currentHeader.badge,
                title = currentHeader.title,
                subtitle = firstParaSnippet,
                readTime = "${maxOf(1, (chapterText.length / 900) + 1)} min de leitura",
                sections = if (sections.isNotEmpty()) sections else listOf(ReadingSection("", listOf(chapterText)))
            )
        )
    }

    // Proteção extra: remover/ignorar capítulos “fantasma” (muito curtos e iguais ao título)
    fun isGhostChapter(ch: ReadingChapter): Boolean {
        val pars = ch.paragraphs.map { it.trim() }.filter { it.isNotBlank() }
        if (pars.isEmpty()) return true
        if (pars.size == 1) {
            val p0 = pars.first()
            if (p0.equals(ch.title.trim(), ignoreCase = true)) return true
            if (p0.equals(ch.chapterBadge.trim(), ignoreCase = true)) return true
            if (p0.length < 120) return true
        }
        val chars = pars.sumOf { it.length }
        return chars < 150 && pars.size <= 1
    }

    val merged = mutableListOf<ReadingChapter>()
    for (ch in chapters) {
        if (isGhostChapter(ch) && merged.isNotEmpty()) {
            // ignora este cartão fantasma
            continue
        }
        merged.add(ch)
    }

    return merged
}

/**
 * Agrupa o conteúdo de um capítulo nas suas subsecções internas (ex: 2.1, 2.2)
 * e remove repetição do título no início do texto sem reintroduzir o cabeçalho.
 */
private fun parseChapterSubsections(
    chapterText: String,
    chapterTitle: String,
    rawHeadingLine: String
): List<ReadingSection> {
    val subSectionRegex = Regex("""(?m)^(\d{1,2}\.\d{1,2}(?:\.\d{1,2})?\s+[^\n]+|##\s+[^\n]+)""")
    val matches = subSectionRegex.findAll(chapterText).toList()

    fun sanitizeParagraphs(rawParagraphs: List<String>): List<String> {
        val result = mutableListOf<String>()
        for ((idx, para) in rawParagraphs.map { it.trim() }.withIndex()) {
            if (para.isBlank()) continue

            // Apenas no parágrafo 0: se corresponder ao título, remove-o
            if (idx == 0) {
                if (para.equals(chapterTitle, ignoreCase = true) || para.equals(rawHeadingLine, ignoreCase = true)) {
                    continue
                }
                if (para.startsWith(rawHeadingLine, ignoreCase = true) && para.length > rawHeadingLine.length) {
                    val remaining = para.substring(rawHeadingLine.length).trim()
                    if (remaining.isNotBlank()) result.add(remaining)
                    continue
                }
                if (para.startsWith(chapterTitle, ignoreCase = true) && para.length > chapterTitle.length) {
                    val remaining = para.substring(chapterTitle.length).trim()
                    if (remaining.isNotBlank()) result.add(remaining)
                    continue
                }
            }

            result.add(para)
        }
        return result
    }

    // Caso sem subsecções: dividir em parágrafos e garantir fallback sem duplicação do cabeçalho
    if (matches.isEmpty()) {
        val rawParagraphs = chapterText.split(Regex("\n\\s*\n"))
        val sanitized = sanitizeParagraphs(rawParagraphs)

        val safeParagraphs = when {
            sanitized.isNotEmpty() -> sanitized
            chapterText.isBlank() -> listOf("Conteúdo não disponível para esta secção.")
            else -> {
                val withoutFirstLine = chapterText.lineSequence().drop(1).joinToString("\n").trim()
                if (withoutFirstLine.isNotBlank()) listOf(withoutFirstLine)
                else listOf("Este capítulo contém apenas o título — sem corpo de texto extraído.")
            }
        }

        return listOf(ReadingSection(title = "", paragraphs = safeParagraphs))
    }

    val sections = mutableListOf<ReadingSection>()

    // 1) Texto do capítulo antes da primeira subsecção
    val firstSubIndex = matches.first().range.first
    if (firstSubIndex > 0) {
        val introText = chapterText.substring(0, firstSubIndex).trim()
        val rawIntroParagraphs = introText.split(Regex("\n\\s*\n"))
        val sanitizedIntro = sanitizeParagraphs(rawIntroParagraphs)
        if (sanitizedIntro.isNotEmpty()) {
            sections.add(ReadingSection(title = "", paragraphs = sanitizedIntro))
        }
    }

    // 2) Subsecções
    for (i in matches.indices) {
        val currentMatch = matches[i]
        val startIndex = currentMatch.range.first
        val endIndex = if (i + 1 < matches.size) matches[i + 1].range.first else chapterText.length

        val sectionText = chapterText.substring(startIndex, endIndex).trim()
        val lines = sectionText.lines().map { it.trim() }.filter { it.isNotBlank() }

        val rawHeading = lines.firstOrNull() ?: ""
        val cleanHeading = rawHeading.replace(Regex("^#+\\s*"), "").trim()

        val bodyText = if (lines.size > 1) {
            val firstBreak = sectionText.indexOf('\n')
            if (firstBreak >= 0) sectionText.substring(firstBreak).trim() else ""
        } else {
            ""
        }

        val rawBodyParagraphs = bodyText.split(Regex("\n\\s*\n"))
        val sanitizedBody = sanitizeParagraphs(rawBodyParagraphs)

        sections.add(
            ReadingSection(
                title = cleanHeading,
                paragraphs = if (sanitizedBody.isNotEmpty()) sanitizedBody else listOf(sectionText)
            )
        )
    }

    return sections
}

/**
 * Converte o texto extraído em capítulos e secções reais.
 * O cartão 0 é dedicado exclusivamente à Análise IA do documento.
 */
private fun buildChaptersFromDocument(
    originalText: String,
    aiSummary: String?
): List<ReadingChapter> {
    val chapters = mutableListOf<ReadingChapter>()

    // Cartão 0: Análise IA
    val summaryText = aiSummary?.trim().orEmpty()
    if (summaryText.isNotBlank()) {
        val analysisParagraphs = summaryText
            .split(Regex("\n\\s*\n"))
            .map { it.trim() }
            .filter { it.isNotBlank() }

        chapters.add(
            ReadingChapter(
                id = 0,
                isAiAnalysis = true,
                chapterBadge = "ANÁLISE IA",
                title = "Análise Inteligente (IA)",
                subtitle = "Visão global, conceitos fundamentais e síntese do Kudu",
                readTime = "${maxOf(1, (summaryText.length / 800) + 1)} min de leitura",
                sections = listOf(
                    ReadingSection(
                        title = "",
                        paragraphs = if (analysisParagraphs.isNotEmpty()) analysisParagraphs else listOf(summaryText)
                    )
                )
            )
        )
    }

    // Capítulos reais extraídos do texto
    val cleanText = originalText
        .replace("\r\n", "\n")
        .replace('\r', '\n')
        .trim()

    if (cleanText.isNotBlank()) {
        val realChapters = parseRealChapters(cleanText)
        chapters.addAll(realChapters)
    } else if (chapters.isEmpty()) {
        chapters.add(
            ReadingChapter(
                id = 0,
                isAiAnalysis = false,
                chapterBadge = "SEM TEXTO",
                title = "Texto ainda não disponível",
                subtitle = "Aguardando extração do documento original",
                readTime = "—",
                sections = listOf(
                    ReadingSection(
                        title = "",
                        paragraphs = listOf(
                            "Ainda não foi possível obter o texto original deste documento. " +
                                    "Volta à Biblioteca Inteligente e confirma se a extração do conteúdo foi concluída."
                        )
                    )
                )
            )
        )
    }

    return chapters
}

/**
 * Split seguro para TTS (evita limites de ~4000 chars por utterance).
 */
private fun splitForTts(text: String, maxLen: Int = 3800): List<String> {
    val t = text.trim()
    if (t.length <= maxLen) return listOf(t)

    val sentences = t.split(Regex("(?<=[.!?])\\s+"))
    if (sentences.size <= 1) {
        // fallback bruto
        return t.chunked(maxLen)
    }

    val chunks = mutableListOf<String>()
    val current = StringBuilder()

    for (s in sentences) {
        val sentence = s.trim()
        if (sentence.isBlank()) continue

        if (current.length + sentence.length + 1 > maxLen) {
            if (current.isNotEmpty()) chunks.add(current.toString().trim())
            current.clear()
        }
        current.append(sentence).append(' ')
    }

    if (current.isNotEmpty()) chunks.add(current.toString().trim())
    return chunks.ifEmpty { listOf(t.take(maxLen)) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadingWorkspaceScreen(
    item: LibraryItem?,
    analysis: String? = null,
    fontSize: Int,
    themeMode: String,
    onFontSizeChange: (Int) -> Unit,
    onThemeModeChange: (String) -> Unit,
    onBack: () -> Unit,
    onAskTutor: (String) -> Unit,
    voiceConfig: ReadingVoiceConfig = ReadingVoiceConfig(),
    onVoiceConfigChange: (ReadingVoiceConfig) -> Unit = {},
    onSaveEditedStructure: (String, List<ReadingChapter>) -> Unit = { _, _ -> },
    onResetStructureToAi: (String) -> Unit = {},
    // (9) Persistência via callback (no-op por default para não quebrar)
    onSaveHighlight: (itemId: String, chapterId: Int, paragraphIndex: Int, text: String) -> Unit = { _, _, _, _ -> },
    onSaveNote: (itemId: String, chapterId: Int, note: String) -> Unit = { _, _, _ -> },
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val windowInfo = LocalScreenWindowInfo.current
    val isNarrow = windowInfo.isNarrowCompact

    val verticalListState = rememberLazyListState()
    val chapterRowState = rememberLazyListState()

    var isEditMode by remember { mutableStateOf(false) }
    var editingChapterIndex by remember { mutableStateOf<Int?>(null) }
    var showEditChapterDialog by remember { mutableStateOf(false) }
    var showAddChapterDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf<Int?>(null) }
    var showRestoreAiConfirmDialog by remember { mutableStateOf(false) }

    // (4) posição por item
    val (savedChap, savedPara) = remember(item?.id) { ReadingPositionStore.getPosition(item?.id) }
    var selectedChapterIndex by remember(item?.id) { mutableIntStateOf(savedChap) }
    var activeParagraphIndex by remember(item?.id) { mutableIntStateOf(savedPara) }

    var selectedHighlight by remember { mutableStateOf<String?>(null) }

    // Notas por capítulo (sessão). Persistência real via callback.
    val notesByChapterId = remember(item?.id) { mutableStateMapOf<Int, String>() }
    var userNotes by remember(item?.id) { mutableStateOf("") }
    var showNotesSheet by remember { mutableStateOf(false) }

    var showVoiceTunerSheet by remember { mutableStateOf(false) }
    var showQuizDialog by remember { mutableStateOf(false) }

    var isAudioPlaying by remember { mutableStateOf(false) }
    var isTtsReady by remember { mutableStateOf(false) }
    var ttsError by remember { mutableStateOf<String?>(null) }
    var feedbackMessage by remember { mutableStateOf<String?>(null) }
    var quizAnswered by remember { mutableStateOf(false) }

    // (5) parsing async + loading real
    var chapters by remember(item?.id) { mutableStateOf<List<ReadingChapter>>(emptyList()) }
    var isParsingDocument by remember(item?.id) { mutableStateOf(false) }

    val textToSpeech = remember { mutableStateOf<TextToSpeech?>(null) }

    // Tema de fundo/texto
    val bgColor = when (themeMode) {
        "Sepia" -> Color(0xFFFBF0D9)
        "Light" -> Color.White
        else -> KudulaNavyDark
    }

    val textColor = when (themeMode) {
        "Sepia" -> Color(0xFF433422)
        "Light" -> Color(0xFF0F172A)
        else -> Color(0xFFF1F5F9)
    }

    val secondaryTextColor = when (themeMode) {
        "Sepia" -> Color(0xFF786146)
        "Light" -> Color(0xFF475569)
        else -> TextSecondary
    }

    // Parsing em background
    LaunchedEffect(item?.id, item?.originalText, item?.editedStructureJson, analysis) {
        if (item == null) {
            chapters = emptyList()
            return@LaunchedEffect
        }

        isParsingDocument = true
        isAudioPlaying = false
        textToSpeech.value?.stop()

        val result = withContext(Dispatchers.Default) {
            if (!item.editedStructureJson.isNullOrBlank()) {
                val edited = parseEditedStructureJson(item.editedStructureJson!!)
                if (edited.isNotEmpty()) edited
                else buildChaptersFromDocument(item.originalText.orEmpty(), analysis ?: item.summary)
            } else {
                buildChaptersFromDocument(item.originalText.orEmpty(), analysis ?: item.summary)
            }
        }

        chapters = result
        isParsingDocument = false
    }

    val safeChapterIndex = selectedChapterIndex.coerceIn(0, (chapters.size - 1).coerceAtLeast(0))
    val selectedChapter = chapters.getOrElse(safeChapterIndex) {
        chapters.firstOrNull() ?: ReadingChapter(
            id = 0,
            isAiAnalysis = false,
            chapterBadge = "VAZIO",
            title = "Documento Vazio",
            subtitle = "",
            readTime = "—",
            sections = emptyList()
        )
    }

    val realChapters = remember(chapters) { chapters.filter { !it.isAiAnalysis } }
    val realChapterCount = realChapters.size

    // Clamp do índice quando chapters mudam
    LaunchedEffect(chapters.size) {
        if (selectedChapterIndex != safeChapterIndex) selectedChapterIndex = safeChapterIndex
        val maxPara = (selectedChapter.paragraphs.size - 1).coerceAtLeast(0)
        if (activeParagraphIndex > maxPara) activeParagraphIndex = maxPara
    }

    // (8) Auto-scroll do carrossel para o capítulo selecionado
    LaunchedEffect(safeChapterIndex, chapters.size) {
        if (chapters.isNotEmpty()) {
            chapterRowState.animateScrollToItem(
                index = safeChapterIndex.coerceIn(0, chapters.lastIndex),
                scrollOffset = -40
            )
        }
    }

    val currentParagraph = selectedChapter.paragraphs.getOrNull(activeParagraphIndex).orEmpty()

    // Persistir posição por item (em memória)
    LaunchedEffect(item?.id, safeChapterIndex, activeParagraphIndex) {
        ReadingPositionStore.savePosition(item?.id, safeChapterIndex, activeParagraphIndex)
    }

    LaunchedEffect(item?.id) {
        item?.let { EventLogger.logPdfOpened(it.title) }
    }

    // TTS setup
    DisposableEffect(context) {
        var engine: TextToSpeech? = null

        engine = TextToSpeech(context.applicationContext) { status ->
            val tts = engine

            if (status == TextToSpeech.SUCCESS && tts != null) {
                val languageResult = tts.setLanguage(Locale.forLanguageTag("pt-PT"))

                if (
                    languageResult != TextToSpeech.LANG_MISSING_DATA &&
                    languageResult != TextToSpeech.LANG_NOT_SUPPORTED
                ) {
                    isTtsReady = true
                    ttsError = null
                } else {
                    isTtsReady = false
                    ttsError = "Não há uma voz portuguesa disponível nas definições do sistema."
                }

                tts.setOnUtteranceProgressListener(
                    object : UtteranceProgressListener() {
                        override fun onStart(utteranceId: String?) = Unit

                        override fun onError(utteranceId: String?) {
                            isAudioPlaying = false
                            feedbackMessage = "Não foi possível reproduzir este trecho."
                        }

                        override fun onDone(utteranceId: String?) {
                            if (!isAudioPlaying) return
                            val id = utteranceId ?: return

                            // utteranceId: kudula_{itemKey}_{chapIdx}_{paraIdx}_{chunkIdx}_{isLast}
                            val parts = id.split('_')
                            if (parts.size < 6) return

                            val chapIdx = parts.getOrNull(2)?.toIntOrNull() ?: return
                            val paraIdx = parts.getOrNull(3)?.toIntOrNull() ?: return
                            val isLast = parts.getOrNull(5) == "1"

                            // Ignorar callbacks de utterances antigas
                            if (chapIdx != selectedChapterIndex || paraIdx != activeParagraphIndex) return

                            // Avança apenas quando terminar o ÚLTIMO chunk
                            if (!isLast) return

                            val lastParagraphIndex = selectedChapter.paragraphs.lastIndex

                            if (activeParagraphIndex < lastParagraphIndex) {
                                activeParagraphIndex += 1
                            } else if (safeChapterIndex < chapters.lastIndex) {
                                selectedChapterIndex = safeChapterIndex + 1
                                activeParagraphIndex = 0
                            } else {
                                isAudioPlaying = false
                                feedbackMessage = "Leitura concluída."
                            }
                        }
                    }
                )
            } else {
                isTtsReady = false
                ttsError = "Não foi possível iniciar a síntese de voz."
            }
        }

        textToSpeech.value = engine

        onDispose {
            engine?.stop()
            engine?.shutdown()
            textToSpeech.value = null
        }
    }

    // (7) TTS speak com fragmentação e fila de chunks
    LaunchedEffect(
        isAudioPlaying,
        isTtsReady,
        item?.id,
        safeChapterIndex,
        activeParagraphIndex,
        voiceConfig.speed
    ) {
        val tts = textToSpeech.value
        if (tts == null) return@LaunchedEffect

        if (!isAudioPlaying || !isTtsReady) {
            tts.stop()
            return@LaunchedEffect
        }

        val toRead = currentParagraph.trim()
        if (toRead.isBlank()) {
            isAudioPlaying = false
            return@LaunchedEffect
        }

        tts.setSpeechRate(voiceConfig.speed.coerceIn(0.5f, 2.0f))

        val chunks = splitForTts(toRead, maxLen = 3800)
        val itemKey = item?.id?.hashCode()?.toString() ?: "0"

        chunks.forEachIndexed { chunkIdx, chunk ->
            val isLast = if (chunkIdx == chunks.lastIndex) 1 else 0
            val utteranceId = "kudula_${itemKey}_${safeChapterIndex}_${activeParagraphIndex}_${chunkIdx}_${isLast}"

            val queueMode = if (chunkIdx == 0) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
            val result = tts.speak(chunk, queueMode, null, utteranceId)

            if (result == TextToSpeech.ERROR) {
                isAudioPlaying = false
                feedbackMessage = "Erro na leitura por voz."
                return@LaunchedEffect
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bgColor)
            .testTag("reading_workspace_screen")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // A. TopAppBar
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = item?.title ?: "Espaço Imersivo de Leitura",
                            style = if (isNarrow) MaterialTheme.typography.titleSmall else MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = textColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (selectedChapter.isAiAnalysis) {
                                "Análise IA • Visão Global • ${selectedChapter.readTime}"
                            } else {
                                val currentRealIndex = realChapters.indexOf(selectedChapter) + 1
                                val safeRealIndex = currentRealIndex.coerceAtLeast(1)
                                "Capítulo $safeRealIndex de $realChapterCount • ${selectedChapter.readTime}"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = KudulaCyanGlow,
                            fontSize = if (isNarrow) 10.sp else 11.sp
                        )
                    }
                },
                navigationIcon = {
                    Surface(
                        onClick = {
                            isAudioPlaying = false
                            textToSpeech.value?.stop()
                            onBack()
                        },
                        shape = CircleShape,
                        color = Color.Transparent,
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Box(
                            modifier = Modifier.size(36.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "Voltar",
                                tint = textColor
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            isEditMode = !isEditMode
                            feedbackMessage = if (isEditMode) "Modo de Edição ativado." else "Modo de Edição concluído."
                        },
                        enabled = item != null && !isParsingDocument
                    ) {
                        Icon(
                            imageVector = if (isEditMode) Icons.Default.Check else Icons.Default.Edit,
                            contentDescription = if (isEditMode) "Concluir Edição" else "Modo de Edição",
                            tint = if (isEditMode) KudulaCyanGlow else textColor
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (themeMode == "Dark") KudulaNavyContainer else Color.Black.copy(alpha = 0.06f),
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = {
                                    val nextSize = when {
                                        fontSize > 18 -> 18
                                        fontSize > 14 -> 14
                                        else -> 14
                                    }
                                    onFontSizeChange(nextSize)
                                },
                                enabled = fontSize > 14,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Text(
                                    "A-",
                                    color = if (fontSize > 14) textColor else TextSecondary.copy(alpha = 0.4f),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                            IconButton(
                                onClick = {
                                    val nextSize = when {
                                        fontSize < 18 -> 18
                                        fontSize < 22 -> 22
                                        else -> 22
                                    }
                                    onFontSizeChange(nextSize)
                                },
                                enabled = fontSize < 22,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Text(
                                    "A+",
                                    color = if (fontSize < 22) textColor else TextSecondary.copy(alpha = 0.4f),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }

                    Surface(
                        onClick = {
                            val nextTheme = when (themeMode) {
                                "Dark" -> "Sepia"
                                "Sepia" -> "Light"
                                else -> "Dark"
                            }
                            onThemeModeChange(nextTheme)
                        },
                        shape = CircleShape,
                        color = if (themeMode == "Dark") KudulaNavyContainer else Color.Black.copy(alpha = 0.06f),
                        modifier = Modifier.size(34.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (themeMode == "Dark") Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = "Tema de leitura",
                                tint = textColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    IconButton(onClick = { showVoiceTunerSheet = true }, enabled = item != null) {
                        Icon(
                            Icons.Default.Tune,
                            contentDescription = "Configurações da voz",
                            tint = textColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = bgColor)
            )

            feedbackMessage?.let { message ->
                Surface(
                    color = KudulaNavyContainer,
                    border = BorderStroke(
                        1.dp,
                        Brush.horizontalGradient(listOf(KudulaCyanGlow, KudulaElectricBlue))
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = message,
                            style = MaterialTheme.typography.bodySmall,
                            color = KudulaCyanGlow,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { feedbackMessage = null },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Fechar mensagem",
                                tint = TextTertiary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            // Corpo
            when {
                item == null -> {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        KudulaEmptyStateCard(
                            icon = Icons.Default.MenuBook,
                            badge = "Leitor KDAP",
                            title = "Nenhum material selecionado para leitura",
                            description = "Seleciona um material na Biblioteca Inteligente para começar a ler.",
                            actionText = "Ir para Biblioteca",
                            actionIcon = Icons.Default.AutoStories,
                            onActionClick = onBack,
                            testTag = "reading_empty_card"
                        )
                    }
                }

                isParsingDocument -> {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        KudulaProcessingIndicator(
                            title = "A estruturar o teu livro…",
                            description = "A Kudula está a organizar capítulos e parágrafos para navegação fluida."
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        state = verticalListState,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        contentPadding = PaddingValues(top = 8.dp, bottom = 120.dp)
                    ) {
                        // B. Cabeçalho “Capítulos do documento”
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 2.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.BookmarkBorder,
                                            contentDescription = null,
                                            tint = if (isEditMode) KudulaElectricBlue else KudulaCyanGlow,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (isEditMode) "MODO DE EDIÇÃO DE ESTRUTURA" else "CAPÍTULOS DO DOCUMENTO",
                                            color = if (isEditMode) KudulaElectricBlue else KudulaCyanGlow,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            letterSpacing = 0.8.sp
                                        )
                                    }

                                    Surface(
                                        onClick = { isEditMode = !isEditMode },
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isEditMode) KudulaElectricBlue else Color.Transparent,
                                        border = BorderStroke(1.dp, if (isEditMode) KudulaCyanGlow else BorderSubtle)
                                    ) {
                                        Text(
                                            text = if (isEditMode) "Concluir" else "Editar estrutura",
                                            color = if (isEditMode) Color.White else TextSecondary,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                if (isEditMode) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = { showAddChapterDialog = true },
                                            colors = ButtonDefaults.buttonColors(containerColor = KudulaNavyContainer),
                                            border = BorderStroke(1.dp, BorderSubtle),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(
                                                Icons.Default.Add,
                                                contentDescription = null,
                                                modifier = Modifier.size(14.dp),
                                                tint = KudulaCyanGlow
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Novo Capítulo", fontSize = 11.sp, color = Color.White)
                                        }

                                        Button(
                                            onClick = { showRestoreAiConfirmDialog = true },
                                            colors = ButtonDefaults.buttonColors(containerColor = KudulaNavyContainer),
                                            border = BorderStroke(1.dp, BorderSubtle),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(
                                                Icons.Default.Restore,
                                                contentDescription = null,
                                                modifier = Modifier.size(14.dp),
                                                tint = KudulaVioletLight
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Restaurar IA", fontSize = 11.sp, color = Color.White)
                                        }
                                    }
                                }
                            }
                        }

                        // Cartões Horizontais de Capítulos (com auto-scroll)
                        item {
                            LazyRow(
                                state = chapterRowState,
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                itemsIndexed(chapters) { index, chapter ->
                                    val isSelected = index == safeChapterIndex

                                    Surface(
                                        onClick = {
                                            isAudioPlaying = false
                                            textToSpeech.value?.stop()
                                            selectedChapterIndex = index
                                            activeParagraphIndex = 0
                                            EventLogger.logChapterRead(chapter.title)
                                        },
                                        shape = RoundedCornerShape(16.dp),
                                        color = if (isSelected) Color(0xFF0F2A4A) else KudulaNavyContainer,
                                        border = BorderStroke(
                                            if (isSelected) 2.dp else 1.dp,
                                            if (isSelected) KudulaElectricBlue else BorderSubtle
                                        ),
                                        modifier = Modifier.width(220.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(14.dp),
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Surface(
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = if (chapter.isAiAnalysis) {
                                                        KudulaVioletLight.copy(alpha = 0.25f)
                                                    } else if (isSelected) {
                                                        KudulaElectricBlue.copy(alpha = 0.35f)
                                                    } else {
                                                        Color.White.copy(alpha = 0.08f)
                                                    }
                                                ) {
                                                    Text(
                                                        text = chapter.chapterBadge,
                                                        color = if (chapter.isAiAnalysis) KudulaVioletLight else if (isSelected) KudulaCyanGlow else textColor,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 11.sp,
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                    )
                                                }

                                                Text(
                                                    text = chapter.readTime,
                                                    color = TextSecondary,
                                                    fontSize = 10.sp
                                                )
                                            }

                                            Text(
                                                text = chapter.title,
                                                color = textColor,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis,
                                                lineHeight = 17.sp
                                            )

                                            if (isEditMode) {
                                                Divider(color = BorderSubtle, modifier = Modifier.padding(vertical = 4.dp))
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    IconButton(
                                                        onClick = {
                                                            if (index > 0) {
                                                                val mutable = chapters.toMutableList()
                                                                val itemToMove = mutable.removeAt(index)
                                                                mutable.add(index - 1, itemToMove)
                                                                chapters = mutable
                                                                selectedChapterIndex = index - 1
                                                                onSaveEditedStructure(item.id, mutable)
                                                            }
                                                        },
                                                        enabled = index > 0,
                                                        modifier = Modifier.size(28.dp)
                                                    ) {
                                                        Icon(
                                                            Icons.Default.ArrowLeft,
                                                            contentDescription = "Mover Esquerda",
                                                            tint = if (index > 0) KudulaCyanGlow else TextSecondary.copy(alpha = 0.3f)
                                                        )
                                                    }

                                                    IconButton(
                                                        onClick = {
                                                            editingChapterIndex = index
                                                            showEditChapterDialog = true
                                                        },
                                                        modifier = Modifier.size(28.dp)
                                                    ) {
                                                        Icon(Icons.Default.Edit, contentDescription = "Editar", tint = Color.White)
                                                    }

                                                    IconButton(
                                                        onClick = { showDeleteConfirmDialog = index },
                                                        modifier = Modifier.size(28.dp)
                                                    ) {
                                                        Icon(Icons.Default.Delete, contentDescription = "Remover", tint = Color(0xFFEF4444))
                                                    }

                                                    IconButton(
                                                        onClick = {
                                                            if (index < chapters.lastIndex) {
                                                                val mutable = chapters.toMutableList()
                                                                val itemToMove = mutable.removeAt(index)
                                                                mutable.add(index + 1, itemToMove)
                                                                chapters = mutable
                                                                selectedChapterIndex = index + 1
                                                                onSaveEditedStructure(item.id, mutable)
                                                            }
                                                        },
                                                        enabled = index < chapters.lastIndex,
                                                        modifier = Modifier.size(28.dp)
                                                    ) {
                                                        Icon(
                                                            Icons.Default.ArrowRight,
                                                            contentDescription = "Mover Direita",
                                                            tint = if (index < chapters.lastIndex) KudulaCyanGlow else TextSecondary.copy(alpha = 0.3f)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // C. Conteúdo do capítulo selecionado (6) “lazy de verdade”: cada parágrafo é um item
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = selectedChapter.title,
                                    color = textColor,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )

                                val shouldShowSubtitle =
                                    selectedChapter.subtitle.isNotBlank() &&
                                            !selectedChapter.subtitle.trim().equals(selectedChapter.title.trim(), ignoreCase = true) &&
                                            !selectedChapter.title.trim().startsWith(
                                                selectedChapter.subtitle.trim().removeSuffix("..."),
                                                ignoreCase = true
                                            )

                                if (shouldShowSubtitle) {
                                    Text(
                                        text = selectedChapter.subtitle,
                                        color = secondaryTextColor,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }

                        var paragraphBaseIndex = 0
                        selectedChapter.sections.forEach { section ->
                            if (section.title.isNotBlank()) {
                                item(key = "sec_${selectedChapter.id}_${section.title.hashCode()}") {
                                    Text(
                                        text = section.title,
                                        color = KudulaCyanGlow,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            val sectionStart = paragraphBaseIndex
                            itemsIndexed(
                                items = section.paragraphs,
                                key = { idx, para -> "p_${selectedChapter.id}_${section.title.hashCode()}_${sectionStart + idx}_${para.hashCode()}" }
                            ) { idxInSection, paragraph ->
                                val paragraphIdx = sectionStart + idxInSection
                                val isActive = paragraphIdx == activeParagraphIndex

                                Surface(
                                    // (3) usar onClick do Surface (evita ripple “bolhas” fora do card)
                                    onClick = {
                                        isAudioPlaying = false
                                        textToSpeech.value?.stop()
                                        activeParagraphIndex = paragraphIdx
                                        feedbackMessage = "Parágrafo ${paragraphIdx + 1} selecionado."
                                    },
                                    shape = RoundedCornerShape(14.dp),
                                    color = if (themeMode == "Dark") KudulaNavyContainer else Color(0x10000000),
                                    border = BorderStroke(
                                        if (isActive) 1.5.dp else 1.dp,
                                        if (isActive) KudulaCyanGlow.copy(alpha = 0.65f) else BorderSubtle
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = if (selectedChapter.isAiAnalysis) "Análise de Conteúdo" else "Parágrafo ${paragraphIdx + 1}",
                                            color = KudulaCyanGlow,
                                            style = MaterialTheme.typography.labelSmall
                                        )

                                        Text(
                                            text = paragraph,
                                            color = textColor,
                                            fontSize = fontSize.sp,
                                            lineHeight = (fontSize * 1.6f).sp,
                                            textAlign = TextAlign.Justify
                                        )
                                    }
                                }
                            }

                            paragraphBaseIndex += section.paragraphs.size
                        }
                    }
                }
            }
        }

        // E. Barra Inferior “sticky”
        val actionsEnabled = item != null && !isParsingDocument

        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .testTag("sticky_learning_bar"),
            color = if (themeMode == "Dark") {
                KudulaNavySurface.copy(alpha = 0.98f)
            } else {
                Color(0xFFF8FAFC)
            },
            tonalElevation = 10.dp,
            border = BorderStroke(1.dp, BorderSubtle)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StickyBarActionItem(
                    icon = Icons.Default.Lightbulb,
                    label = "Explicar",
                    enabled = actionsEnabled,
                    onClick = {
                        val targetText = currentParagraph.ifBlank {
                            selectedChapter.paragraphs.joinToString("\n\n")
                        }
                        onAskTutor(
                            "Kudu, explica com clareza o conteúdo da secção \"${selectedChapter.title}\" " +
                                    "do documento \"${item?.title.orEmpty()}\".\n\n" +
                                    "Texto para explicação:\n$targetText"
                        )
                    }
                )

                StickyBarActionItem(
                    icon = if (isAudioPlaying) Icons.Default.PauseCircle else Icons.Default.PlayCircle,
                    label = if (isAudioPlaying) "Parar" else "Áudio",
                    enabled = actionsEnabled,
                    tint = if (isAudioPlaying) KudulaCyanGlow else TextSecondary,
                    onClick = {
                        if (!isTtsReady) {
                            feedbackMessage = ttsError ?: "A voz ainda está a ser preparada. Tenta novamente."
                        } else if (currentParagraph.isBlank()) {
                            feedbackMessage = "Não existe texto neste parágrafo para ler."
                        } else {
                            isAudioPlaying = !isAudioPlaying
                            if (!isAudioPlaying) {
                                textToSpeech.value?.stop()
                                feedbackMessage = "Leitura por voz parada."
                            } else {
                                feedbackMessage = "A ler o texto da secção selecionada."
                            }
                        }
                    }
                )

                StickyBarActionItem(
                    icon = Icons.Default.Quiz,
                    label = "Quiz",
                    enabled = actionsEnabled,
                    onClick = {
                        quizAnswered = false
                        showQuizDialog = true
                    }
                )

                StickyBarActionItem(
                    icon = Icons.Default.BorderColor,
                    label = "Destacar",
                    enabled = actionsEnabled,
                    onClick = {
                        if (currentParagraph.isNotBlank() && item != null) {
                            selectedHighlight = currentParagraph
                            onSaveHighlight(item.id, selectedChapter.id, activeParagraphIndex, currentParagraph)
                            feedbackMessage = "Trecho destacado."
                        } else {
                            feedbackMessage = "Seleciona primeiro um parágrafo."
                        }
                    }
                )

                StickyBarActionItem(
                    icon = Icons.Default.BookmarkAdd,
                    label = "Insight",
                    enabled = actionsEnabled,
                    onClick = {
                        // Carregar nota do capítulo atual
                        userNotes = notesByChapterId[selectedChapter.id].orEmpty()
                        showNotesSheet = true
                    }
                )
            }
        }
    }

    // --- DIÁLOGOS DO MODO DE EDIÇÃO ---

    if (showEditChapterDialog && editingChapterIndex != null) {
        val targetIdx = editingChapterIndex!!
        val chapterToEdit = chapters.getOrNull(targetIdx)
        if (chapterToEdit != null && item != null) {
            var newBadge by remember { mutableStateOf(chapterToEdit.chapterBadge) }
            var newTitle by remember { mutableStateOf(chapterToEdit.title) }

            AlertDialog(
                onDismissRequest = { showEditChapterDialog = false },
                containerColor = KudulaNavySurface,
                title = { Text("Editar Capítulo", color = Color.White, fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Identificador / Badge (ex: CAP. 1, PREFÁCIO):", color = TextSecondary, fontSize = 12.sp)
                        OutlinedTextField(
                            value = newBadge,
                            onValueChange = { newBadge = it },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )

                        Text("Título do Capítulo:", color = TextSecondary, fontSize = 12.sp)
                        OutlinedTextField(
                            value = newTitle,
                            onValueChange = { newTitle = it },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val mutable = chapters.toMutableList()
                            val updated = chapterToEdit.copy(chapterBadge = newBadge, title = newTitle)
                            mutable[targetIdx] = updated
                            chapters = mutable
                            showEditChapterDialog = false
                            onSaveEditedStructure(item.id, mutable)
                            feedbackMessage = "Capítulo atualizado e guardado."
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = KudulaElectricBlue)
                    ) {
                        Text("Guardar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEditChapterDialog = false }) {
                        Text("Cancelar", color = TextSecondary)
                    }
                }
            )
        }
    }

    if (showAddChapterDialog && item != null) {
        var addBadge by remember { mutableStateOf("CAP. ${chapters.size}") }
        var addTitle by remember { mutableStateOf("") }
        var addContent by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddChapterDialog = false },
            containerColor = KudulaNavySurface,
            title = { Text("Adicionar Novo Capítulo", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Identificador:", color = TextSecondary, fontSize = 12.sp)
                    OutlinedTextField(
                        value = addBadge,
                        onValueChange = { addBadge = it },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    Text("Título:", color = TextSecondary, fontSize = 12.sp)
                    OutlinedTextField(
                        value = addTitle,
                        onValueChange = { addTitle = it },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    Text("Conteúdo do Capítulo:", color = TextSecondary, fontSize = 12.sp)
                    OutlinedTextField(
                        value = addContent,
                        onValueChange = { addContent = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val newChap = ReadingChapter(
                            id = chapters.size + 1,
                            isAiAnalysis = false,
                            chapterBadge = addBadge.ifBlank { "NOVO" },
                            title = addTitle.ifBlank { "Novo Capítulo" },
                            subtitle = addContent.take(80) + "...",
                            readTime = "5 min de leitura",
                            sections = listOf(
                                ReadingSection("", listOf(addContent.ifBlank { "Conteúdo personalizado do utilizador." }))
                            )
                        )
                        val mutable = chapters.toMutableList()
                        mutable.add(newChap)
                        chapters = mutable
                        showAddChapterDialog = false
                        selectedChapterIndex = mutable.lastIndex
                        onSaveEditedStructure(item.id, mutable)
                        feedbackMessage = "Novo capítulo adicionado e guardado."
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = KudulaElectricBlue)
                ) {
                    Text("Criar Capítulo")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddChapterDialog = false }) {
                    Text("Cancelar", color = TextSecondary)
                }
            }
        )
    }

    if (showDeleteConfirmDialog != null && item != null) {
        val deleteIdx = showDeleteConfirmDialog!!
        val targetChap = chapters.getOrNull(deleteIdx)
        if (targetChap != null) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirmDialog = null },
                containerColor = KudulaNavySurface,
                title = { Text("Remover da Estrutura", color = Color.White, fontWeight = FontWeight.Bold) },
                text = {
                    Text(
                        "Tens a certeza que desejas remover o cartão \"${targetChap.title}\" da estrutura de leitura?\n\n" +
                                "O ficheiro original e as tuas notas pessoais não serão apagados.",
                        color = Color.White,
                        fontSize = 13.sp
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val mutable = chapters.toMutableList()
                            if (deleteIdx in mutable.indices) {
                                mutable.removeAt(deleteIdx)
                                chapters = mutable
                                selectedChapterIndex = (deleteIdx - 1).coerceAtLeast(0)
                                onSaveEditedStructure(item.id, mutable)
                                feedbackMessage = "Cartão removido da estrutura."
                            }
                            showDeleteConfirmDialog = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                    ) {
                        Text("Remover Cartão")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirmDialog = null }) {
                        Text("Cancelar", color = TextSecondary)
                    }
                }
            )
        }
    }

    if (showRestoreAiConfirmDialog && item != null) {
        AlertDialog(
            onDismissRequest = { showRestoreAiConfirmDialog = false },
            containerColor = KudulaNavySurface,
            title = { Text("Restaurar Estrutura IA Original", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Desejas repor a estrutura do livro para a versão original detetada pela IA?\n\n" +
                            "Todas as edições manuais de títulos e ordens serão repostas. O ficheiro original e as tuas notas pessoais permanecem seguros.",
                    color = Color.White,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showRestoreAiConfirmDialog = false
                        val aiChapters = buildChaptersFromDocument(item.originalText.orEmpty(), analysis ?: item.summary)
                        chapters = aiChapters
                        selectedChapterIndex = 0
                        activeParagraphIndex = 0
                        onResetStructureToAi(item.id)
                        feedbackMessage = "Estrutura restaurada para a versão original da IA."
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = KudulaElectricBlue)
                ) {
                    Text("Restaurar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreAiConfirmDialog = false }) {
                    Text("Cancelar", color = TextSecondary)
                }
            }
        )
    }

    // Voice tuner
    if (showVoiceTunerSheet) {
        AlertDialog(
            onDismissRequest = { showVoiceTunerSheet = false },
            containerColor = KudulaNavySurface,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.RecordVoiceOver, contentDescription = null, tint = KudulaCyanGlow)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Configurações de voz",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Estilo de voz (preferência da Kudula)",
                        color = TextSecondary,
                        style = MaterialTheme.typography.labelSmall
                    )
                    listOf("Neural Socrática", "Narrativa Acadêmica", "Clara & Amigável").forEach { voiceType ->
                        val isSelected = voiceConfig.voiceType == voiceType
                        Surface(
                            onClick = { onVoiceConfigChange(voiceConfig.copy(voiceType = voiceType)) },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) KudulaElectricBlue else KudulaNavyContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = voiceType, color = Color.White, modifier = Modifier.padding(10.dp))
                        }
                    }

                    Text(
                        "Velocidade de Leitura: ${voiceConfig.speed}x",
                        color = TextSecondary,
                        style = MaterialTheme.typography.labelSmall
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf(0.75f, 1.0f, 1.25f, 1.5f, 2.0f).forEach { speed ->
                            val isSelected = voiceConfig.speed == speed
                            Surface(
                                onClick = { onVoiceConfigChange(voiceConfig.copy(speed = speed)) },
                                shape = RoundedCornerShape(6.dp),
                                color = if (isSelected) KudulaCyanGlow else KudulaNavyContainer,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                ) {
                                    Text(
                                        text = "${speed}x",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) KudulaNavyDark else Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showVoiceTunerSheet = false },
                    colors = ButtonDefaults.buttonColors(containerColor = KudulaElectricBlue)
                ) { Text("Aplicar") }
            }
        )
    }

    // Notes (persistência via callback)
    if (showNotesSheet && item != null) {
        AlertDialog(
            onDismissRequest = { showNotesSheet = false },
            containerColor = KudulaNavySurface,
            title = {
                Text("Anotações de leitura", color = Color.White, fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text(
                        "Regista pensamentos, dúvidas ou analogias pessoais:",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = userNotes,
                        onValueChange = { userNotes = it },
                        placeholder = { Text("Escreve aqui as tuas notas...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showNotesSheet = false

                        val note = userNotes.trim()
                        notesByChapterId[selectedChapter.id] = note
                        onSaveNote(item.id, selectedChapter.id, note)

                        feedbackMessage = if (note.isBlank()) {
                            "Nota vazia (nada para guardar)."
                        } else {
                            "Nota guardada."
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = KudulaElectricBlue)
                ) {
                    Text("Guardar nota")
                }
            }
        )
    }

    // Quiz
    if (showQuizDialog) {
        AlertDialog(
            onDismissRequest = { showQuizDialog = false },
            containerColor = KudulaNavySurface,
            title = {
                Text(
                    "Quiz de leitura (${selectedChapter.title})",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Qual é o foco da secção \"${selectedChapter.title}\"?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White
                    )
                    Text(
                        text = "Questionário socrático gerado para o conteúdo atual.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    listOf(
                        "Compreender e aplicar os conceitos apresentados na secção.",
                        "Ignorar as premissas e passar à frente.",
                        "Apenas memorizar sem raciocínio."
                    ).forEachIndexed { index, option ->
                        Surface(
                            onClick = {
                                quizAnswered = true
                                feedbackMessage = if (index == 0) {
                                    "Resposta correta! Excelente foco."
                                } else {
                                    "Revê o texto da secção e tenta novamente."
                                }
                                showQuizDialog = false
                            },
                            shape = RoundedCornerShape(10.dp),
                            color = KudulaNavyContainer,
                            border = BorderStroke(1.dp, BorderSubtle),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = option,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showQuizDialog = false }) {
                    Text("Fechar", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
private fun StickyBarActionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    tint: Color = TextSecondary,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val finalTint = if (enabled) tint else TextSecondary.copy(alpha = 0.35f)

    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        color = Color.Transparent,
        modifier = Modifier.clip(RoundedCornerShape(12.dp))
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(KudulaNavyContainer.copy(alpha = if (enabled) 1f else 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = finalTint,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = finalTint,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun KudulaProcessingIndicator(
    title: String,
    description: String
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = KudulaNavyContainer,
        border = BorderStroke(1.dp, BorderSubtle),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = KudulaCyanGlow
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = description,
                color = TextSecondary,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}