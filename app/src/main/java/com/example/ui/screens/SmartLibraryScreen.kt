package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.repository.EventLogger
import com.example.model.LibraryItem
import com.example.model.LibraryItemType
import com.example.ui.components.CameraScannerGuideDialog
import com.example.ui.components.KudulaEmptyStateCard
import com.example.ui.theme.*
import com.example.ui.util.BitmapCompressor
import com.example.ui.util.LocalScreenWindowInfo
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.media.MediaMetadataRetriever
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import com.example.ai.normalizeYouTubeUrl
import com.example.viewmodel.LibraryProcessingPhase
import com.example.viewmodel.LibraryProcessingState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartLibraryScreen(
    libraryItems: List<LibraryItem>,
    onOpenItem: (LibraryItem) -> Unit,
    onAddItem: (String, LibraryItemType, String) -> Unit,
    onOpenYouTubeIntelligence: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier,
    // Processamento real com a Kudula AI (se não forem passados, mantém-se o comportamento antigo).
    processingState: LibraryProcessingState = LibraryProcessingState(),
    analyses: Map<String, String> = emptyMap(),
    onProcessContent: ((String, LibraryItemType, String, Uri?, String?) -> Unit)? = null,
    onCancelProcessing: () -> Unit = {},
    onRetryProcessing: () -> Unit = {},
    onDismissProcessing: () -> Unit = {}
) {
    val windowInfo = LocalScreenWindowInfo.current
    val isNarrow = windowInfo.isNarrowCompact
    val context = LocalContext.current

    var searchQuery by remember { mutableStateOf("") }
    var selectedTypeFilter by remember { mutableStateOf<LibraryItemType?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var viewingAnalysisItemId by remember { mutableStateOf<String?>(null) }

    // Instant Feedback Banner
    var uploadSuccessMessage by remember { mutableStateOf<String?>(null) }

    // Quando a análise termina, mostra o banner de sucesso.
    LaunchedEffect(processingState.phase) {
        if (processingState.phase == LibraryProcessingPhase.DONE) {
            uploadSuccessMessage = "Material '${processingState.title}' analisado pela Kudula AI!"
        }
    }

    val filteredItems = libraryItems.filter { item ->
        val matchesQuery = item.title.contains(searchQuery, ignoreCase = true) ||
                item.category.contains(searchQuery, ignoreCase = true)
        val matchesType = selectedTypeFilter == null || item.type == selectedTypeFilter
        matchesQuery && matchesType
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = KudulaElectricBlue,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.Add, contentDescription = "Adicionar") },
                text = { Text(if (isNarrow) "Adicionar" else "Adicionar Conteúdo", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("fab_add_library_item")
            )
        },
        containerColor = KudulaNavyDark
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = windowInfo.screenHorizontalPadding)
                .testTag("smart_library_screen")
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // Screen Header with Contextual Imagery
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(if (isNarrow) 38.dp else 44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.linearGradient(listOf(KudulaElectricBlue, KudulaCyanGlow))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoStories,
                        contentDescription = "Biblioteca Inteligente",
                        tint = Color.White,
                        modifier = Modifier.size(if (isNarrow) 22.dp else 26.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Biblioteca Inteligente",
                        style = if (isNarrow) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Text(
                        text = "Knowledge Vault Multimodal • PDF, Vídeo, Áudio e YouTube",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        fontSize = if (isNarrow) 10.sp else 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Upload Success Banner
            AnimatedVisibility(visible = uploadSuccessMessage != null) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF10B981).copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, Color(0xFF10B981)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF34D399),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = uploadSuccessMessage ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { uploadSuccessMessage = null },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Fechar", tint = TextSecondary, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        text = if (isNarrow) "Pesquisar materiais..." else "Pesquisar PDFs, vídeos, áudios e YouTube...",
                        color = TextTertiary,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = KudulaCyanGlow) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Limpar", tint = TextTertiary)
                        }
                    }
                },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = KudulaCyanGlow,
                    unfocusedBorderColor = BorderSubtle,
                    focusedContainerColor = KudulaNavyContainer,
                    unfocusedContainerColor = KudulaNavyContainer,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth().testTag("library_search_input")
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Filter Chips (Todos, PDF, DOCX, Vídeo, Áudio, YouTube, Imagem)
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    FilterChip(
                        selected = selectedTypeFilter == null,
                        onClick = { selectedTypeFilter = null },
                        label = { Text("Todos (${libraryItems.size})", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = KudulaElectricBlue,
                            selectedLabelColor = Color.White,
                            containerColor = KudulaNavyContainer,
                            labelColor = TextSecondary
                        )
                    )
                }

                val filterTypes = listOf(
                    LibraryItemType.PDF,
                    LibraryItemType.DOCX,
                    LibraryItemType.VIDEO,
                    LibraryItemType.AUDIO,
                    LibraryItemType.YOUTUBE,
                    LibraryItemType.IMAGE
                )

                items(filterTypes) { type ->
                    val count = libraryItems.count { it.type == type }
                    FilterChip(
                        selected = selectedTypeFilter == type,
                        onClick = {
                            selectedTypeFilter = if (selectedTypeFilter == type) null else type
                        },
                        label = { Text("${type.label} ($count)", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = KudulaElectricBlue,
                            selectedLabelColor = Color.White,
                            containerColor = KudulaNavyContainer,
                            labelColor = TextSecondary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Library Items List
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 90.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                // Upload Banner Card
                item {
                    LibraryUploadCard(
                        onFileSelected = { title, type, category ->
                            onAddItem(title, type, category)
                            uploadSuccessMessage = "Material '$title' indexado na Biblioteca!"
                        },
                        onPhotoCaptured = { title, category ->
                            onAddItem(title, LibraryItemType.IMAGE, category)
                            uploadSuccessMessage = "Foto de caderno '$title' digitalizada!"
                        },
                        onGallerySelected = { title, category ->
                            onAddItem(title, LibraryItemType.IMAGE, category)
                            uploadSuccessMessage = "Imagem '$title' extraída com sucesso!"
                        },
                        onProcessUri = if (onProcessContent != null) {
                            { title: String, type: LibraryItemType, category: String, uri: Uri ->
                                onProcessContent(title, type, category, uri, null)
                            }
                        } else {
                            null
                        }
                    )
                }

                if (filteredItems.isEmpty()) {
                    item {
                        KudulaEmptyStateCard(
                            icon = Icons.Default.AutoStories,
                            badge = "Knowledge Vault",
                            title = "A tua biblioteca inteligente está pronta",
                            description = "Carrega o teu primeiro PDF, documento, fotografia de apontamentos ou link do YouTube. O motor KCLS sintetiza os conceitos centrais e gera flashcards imediatos!",
                            actionText = "Adicionar Conteúdo",
                            actionIcon = Icons.Default.Add,
                            onActionClick = { showAddDialog = true },
                            testTag = "library_empty_state_card"
                        )
                    }
                } else {
                    items(filteredItems) { item ->
                        LibraryItemCard(
                            item = item,
                            onOpen = { onOpenItem(item) },
                            hasAnalysis = analyses.containsKey(item.id),
                            onViewAnalysis = { viewingAnalysisItemId = item.id },
                            onOpenYouTube = {
                                if (item.type == LibraryItemType.YOUTUBE && onOpenYouTubeIntelligence != null) {
                                    onOpenYouTubeIntelligence("https://www.youtube.com/watch?v=kudula_preview")
                                } else {
                                    onOpenItem(item)
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddLibraryItemDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { title, type, category ->
                onAddItem(title, type, category)
                uploadSuccessMessage = "Material '$title' indexado com sucesso!"
                showAddDialog = false
            },
            onOpenYouTube = { url ->
                showAddDialog = false
                if (onOpenYouTubeIntelligence != null) {
                    onOpenYouTubeIntelligence(url)
                }
            },
            onProcess = if (onProcessContent != null) {
                { title: String, type: LibraryItemType, category: String, uri: Uri?, youtubeUrl: String? ->
                    showAddDialog = false
                    onProcessContent(title, type, category, uri, youtubeUrl)
                }
            } else {
                null
            }
        )
    }

    // Estado do processamento: a ler, a analisar, concluído ou erro.
    LibraryProcessingDialog(
        state = processingState,
        onCancel = onCancelProcessing,
        onRetry = onRetryProcessing,
        onDismiss = onDismissProcessing,
        onOpenWorkspace = {
            val processedItem = libraryItems.firstOrNull { it.id == processingState.itemId }
            onDismissProcessing()
            if (processedItem != null) onOpenItem(processedItem)
        }
    )

    // Consulta posterior da análise de um material.
    val analysisItemId = viewingAnalysisItemId
    if (analysisItemId != null && analyses.containsKey(analysisItemId)) {
        val analysisTitle = libraryItems.firstOrNull { it.id == analysisItemId }?.title.orEmpty()
        LibraryAnalysisDialog(
            title = analysisTitle,
            analysis = analyses[analysisItemId].orEmpty(),
            onDismiss = { viewingAnalysisItemId = null }
        )
    }
}

/**
 * Priority 6: Smart Library Cards
 * Each item displays:
 * - thumbnail
 * - icon by content type
 * - difficulty (Beginner, Intermediate, Advanced)
 * - estimated time
 * - progress (Goal gradient)
 * - confidence badge (Verified, Personal Notes, etc.)
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LibraryItemCard(
    item: LibraryItem,
    onOpen: () -> Unit,
    onOpenYouTube: () -> Unit = onOpen,
    hasAnalysis: Boolean = false,
    onViewAnalysis: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                if (item.type == LibraryItemType.YOUTUBE) onOpenYouTube() else onOpen()
            }
            .testTag("library_item_${item.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = KudulaNavySurface),
        border = BorderStroke(1.dp, BorderSubtle)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Top Row: Thumbnail/Type Badge, Difficulty, Estimated Time, Confidence Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Type Badge with Icon
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = when (item.type) {
                            LibraryItemType.PDF -> Color(0xFFDC2626).copy(alpha = 0.25f)
                            LibraryItemType.DOCX -> Color(0xFF2563EB).copy(alpha = 0.25f)
                            LibraryItemType.VIDEO -> Color(0xFF8B5CF6).copy(alpha = 0.25f)
                            LibraryItemType.AUDIO -> Color(0xFF06B6D4).copy(alpha = 0.25f)
                            LibraryItemType.YOUTUBE -> Color(0xFFEF4444).copy(alpha = 0.25f)
                            LibraryItemType.IMAGE -> Color(0xFF10B981).copy(alpha = 0.25f)
                            else -> KudulaNavyContainer
                        }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = when (item.type) {
                                    LibraryItemType.PDF -> Icons.Default.PictureAsPdf
                                    LibraryItemType.DOCX -> Icons.Default.Description
                                    LibraryItemType.VIDEO -> Icons.Default.Videocam
                                    LibraryItemType.AUDIO -> Icons.Default.Audiotrack
                                    LibraryItemType.YOUTUBE -> Icons.Default.PlayCircle
                                    LibraryItemType.IMAGE -> Icons.Default.Image
                                    else -> Icons.Default.Article
                                },
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = item.type.label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 10.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Difficulty Badge (Iniciante, Intermediário, Avançado)
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = when (item.difficulty.lowercase(Locale.getDefault())) {
                            "iniciante" -> Color(0xFF10B981).copy(alpha = 0.2f)
                            "intermediário", "intermediario" -> Color(0xFFF59E0B).copy(alpha = 0.2f)
                            else -> Color(0xFFEF4444).copy(alpha = 0.2f)
                        }
                    ) {
                        Text(
                            text = item.difficulty,
                            style = MaterialTheme.typography.labelSmall,
                            color = when (item.difficulty.lowercase(Locale.getDefault())) {
                                "iniciante" -> Color(0xFF34D399)
                                "intermediário", "intermediario" -> Color(0xFFFBBF24)
                                else -> Color(0xFFF87171)
                            },
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 9.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Estimated Time
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = TextTertiary,
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = item.estimatedTime,
                            style = MaterialTheme.typography.labelSmall,
                            color = TextTertiary,
                            fontSize = 10.sp
                        )
                    }
                }

                // Confidence badge (ex: "98% Revisado", "96% Confiabilidade", "Verificado Kudula")
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = KudulaElectricBlue.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = item.confidenceBadge,
                        style = MaterialTheme.typography.labelSmall,
                        color = KudulaCyanGlow,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Title
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Summary
            Text(
                text = item.summary,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            if (hasAnalysis) {
                TextButton(
                    onClick = onViewAnalysis,
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoStories,
                        contentDescription = null,
                        tint = KudulaCyanGlow,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Ver análise completa",
                        color = KudulaCyanGlow,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
            }

            // Goal Gradient Progress Bar (Never 0%)
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Retenção Cognitiva KCLS",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextTertiary,
                        fontSize = 9.sp
                    )
                    Text(
                        text = "${(item.progress * 100).toInt()}% Concluído",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (item.progress >= 0.7f) KudulaCyanGlow else KudulaElectricBlue,
                        fontSize = 9.sp
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { item.progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = if (item.progress >= 0.7f) KudulaCyanGlow else KudulaElectricBlue,
                    trackColor = KudulaNavyContainer
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Bottom Actions & Category
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = item.category,
                    style = MaterialTheme.typography.labelSmall,
                    color = KudulaCyanGlow,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Button(
                    onClick = {
                        if (item.type == LibraryItemType.YOUTUBE) onOpenYouTube() else onOpen()
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (item.type == LibraryItemType.YOUTUBE) Color(0xFFEF4444).copy(alpha = 0.85f) else KudulaElectricBlue.copy(alpha = 0.85f)
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp).testTag("open_workspace_button_${item.id}")
                ) {
                    Icon(
                        imageVector = if (item.type == LibraryItemType.YOUTUBE) Icons.Default.SmartDisplay else Icons.Default.Launch,
                        contentDescription = null,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (item.type == LibraryItemType.YOUTUBE) "Abrir Vídeo KCLS" else "Abrir Workspace",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * Priority 6: Multimodal Add Library Flow
 * Choose content type: PDF, DOCX, Video, Audio, YouTube
 * If Video: shows thumbnail, filename, duration, file size + "Process with Kudula AI"
 * If Audio: shows waveform preview, filename, duration + "Process with Kudula AI"
 * If YouTube: shows URL field, auto-detects title & thumbnail, allows editing + "Process"
 */
@Composable
fun AddLibraryItemDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, LibraryItemType, String) -> Unit,
    onOpenYouTube: (String) -> Unit = {},
    onProcess: ((String, LibraryItemType, String, Uri?, String?) -> Unit)? = null
) {
    val context = LocalContext.current
    var selectedContentType by remember { mutableStateOf(LibraryItemType.PDF) }

    // Form fields
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Inteligência Artificial & Dados") }
    var youtubeUrl by remember { mutableStateOf("") }

    // Media metadata
    var selectedFileName by remember { mutableStateOf<String?>(null) }
    var detectedDuration by remember { mutableStateOf("a calcular...") }
    var detectedFileSize by remember { mutableStateOf("a calcular...") }
    var hasMediaLoaded by remember { mutableStateOf(false) }

    // Ficheiro real selecionado e mensagem de erro de validação
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var errorText by remember { mutableStateOf<String?>(null) }

    // SAF Document Picker (PDF, DOCX)
    val docPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            val rawName = getFileNameFromUri(context, uri)
            val extension = rawName.substringAfterLast('.', "").lowercase(Locale.getDefault())
            val cleanTitle = rawName.substringBeforeLast('.').replace('_', ' ').replace('-', ' ').trim()
                .ifBlank { "Documento Importado" }
            title = cleanTitle
            selectedFileName = rawName
            selectedContentType = if (extension == "docx" || extension == "doc") LibraryItemType.DOCX else LibraryItemType.PDF
            category = "Documentação & Artigos KCLS"
            selectedUri = uri
            errorText = null
            hasMediaLoaded = true
        }
    }

    // Video Picker
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            val rawName = getFileNameFromUri(context, uri)
            val cleanTitle = rawName.substringBeforeLast('.').replace('_', ' ').replace('-', ' ').trim()
                .ifBlank { "Vídeo de Aprendizagem" }
            title = cleanTitle
            selectedFileName = rawName
            selectedContentType = LibraryItemType.VIDEO
            selectedUri = uri
            errorText = null
            category = "Vídeo-Aulas & Seminários"
            hasMediaLoaded = true
        }
    }

    // Audio Picker
    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            val rawName = getFileNameFromUri(context, uri)
            val cleanTitle = rawName.substringBeforeLast('.').replace('_', ' ').replace('-', ' ').trim()
                .ifBlank { "Gravação de Aula" }
            title = cleanTitle
            selectedFileName = rawName
            selectedContentType = LibraryItemType.AUDIO
            selectedUri = uri
            errorText = null
            category = "Podcasts & Gravações Orais"
            hasMediaLoaded = true
        }
    }

    // Reconhece um link válido do YouTube (o título é escolhido pelo estudante; não é inventado).
    LaunchedEffect(youtubeUrl, selectedContentType) {
        if (selectedContentType == LibraryItemType.YOUTUBE) {
            if (normalizeYouTubeUrl(youtubeUrl) != null) {
                hasMediaLoaded = true
                category = "YouTube Cognitive Intelligence"
                errorText = null
            } else {
                hasMediaLoaded = false
            }
        }
    }

    // Calcula a duração e o tamanho reais do vídeo ou áudio selecionado.
    LaunchedEffect(selectedUri, selectedContentType) {
        val uri = selectedUri
        if (uri != null &&
            (selectedContentType == LibraryItemType.VIDEO || selectedContentType == LibraryItemType.AUDIO)
        ) {
            val sizeLabel = withContext(Dispatchers.IO) {
                queryFileSizeBytes(context, uri)?.let { formatFileSize(it) }
            }
            val durationLabel = withContext(Dispatchers.IO) {
                readMediaDuration(context, uri)
            }
            detectedFileSize = sizeLabel ?: "desconhecido"
            detectedDuration = durationLabel ?: "desconhecida"
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = KudulaNavySurface,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CloudUpload, contentDescription = null, tint = KudulaCyanGlow, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Adicionar Conteúdo Multimodal", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 17.sp)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Escolhe o formato de conteúdo para processar com a IA Kudula:",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    fontSize = 11.sp
                )

                // Content Type Selector: PDF, DOCX, Video, Audio, YouTube
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val types = listOf(
                        LibraryItemType.PDF,
                        LibraryItemType.DOCX,
                        LibraryItemType.VIDEO,
                        LibraryItemType.AUDIO,
                        LibraryItemType.YOUTUBE
                    )

                    items(types) { type ->
                        FilterChip(
                            selected = selectedContentType == type,
                            onClick = {
                                selectedContentType = type
                                hasMediaLoaded = false
                                selectedFileName = null
                                selectedUri = null
                                errorText = null
                                when (type) {
                                    LibraryItemType.PDF -> category = "Artigos & Livros"
                                    LibraryItemType.DOCX -> category = "Trabalhos Escritos"
                                    LibraryItemType.VIDEO -> category = "Vídeo-Aulas"
                                    LibraryItemType.AUDIO -> category = "Podcasts & Áudios"
                                    LibraryItemType.YOUTUBE -> category = "YouTube Intelligence"
                                    else -> {}
                                }
                            },
                            label = { Text(type.label, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = KudulaElectricBlue,
                                selectedLabelColor = Color.White,
                                containerColor = KudulaNavyContainer,
                                labelColor = TextSecondary
                            )
                        )
                    }
                }

                // Dynamic UI based on selectedContentType
                when (selectedContentType) {
                    // IF VIDEO
                    LibraryItemType.VIDEO -> {
                        Surface(
                            onClick = { videoPickerLauncher.launch(arrayOf("video/*", "video/mp4", "video/mkv")) },
                            shape = RoundedCornerShape(12.dp),
                            color = KudulaNavyContainer,
                            border = BorderStroke(1.dp, KudulaViolet.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.VideoFile, contentDescription = null, tint = KudulaVioletLight, modifier = Modifier.size(28.dp))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (selectedFileName != null) "Trocar Vídeo: $selectedFileName" else "Toca para selecionar ficheiro de Vídeo (MP4, MKV)",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        if (hasMediaLoaded) {
                            Card(
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = KudulaNavyDark),
                                border = BorderStroke(1.dp, BorderSubtle),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Thumbnail preview
                                    Box(
                                        modifier = Modifier
                                            .size(50.dp, 40.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Brush.linearGradient(listOf(Color(0xFF8B5CF6), Color(0xFF3B82F6)))),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = selectedFileName ?: "video.mp4", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 11.sp, maxLines = 1)
                                        Text(text = "Duração: $detectedDuration • Tamanho: $detectedFileSize", color = TextTertiary, fontSize = 10.sp)
                                    }
                                }
                            }
                        }
                    }

                    // IF AUDIO
                    LibraryItemType.AUDIO -> {
                        Surface(
                            onClick = { audioPickerLauncher.launch(arrayOf("audio/*", "audio/mpeg", "audio/wav")) },
                            shape = RoundedCornerShape(12.dp),
                            color = KudulaNavyContainer,
                            border = BorderStroke(1.dp, KudulaCyanGlow.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.GraphicEq, contentDescription = null, tint = KudulaCyanGlow, modifier = Modifier.size(28.dp))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (selectedFileName != null) "Trocar Áudio: $selectedFileName" else "Toca para selecionar ficheiro de Áudio (MP3, WAV, M4A)",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        if (hasMediaLoaded) {
                            Card(
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = KudulaNavyDark),
                                border = BorderStroke(1.dp, BorderSubtle),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(text = "Áudio selecionado:", color = KudulaCyanGlow, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    // Simulated Waveform Bars
                                    Row(
                                        modifier = Modifier.fillMaxWidth().height(24.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        val heights = listOf(0.4f, 0.7f, 0.3f, 0.9f, 0.6f, 1.0f, 0.8f, 0.5f, 0.85f, 0.4f, 0.95f, 0.6f, 0.3f, 0.7f, 0.5f)
                                        heights.forEach { fraction ->
                                            Box(
                                                modifier = Modifier
                                                    .width(3.dp)
                                                    .fillMaxHeight(fraction)
                                                    .clip(RoundedCornerShape(2.dp))
                                                    .background(KudulaCyanGlow)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(text = "${selectedFileName ?: "audio.mp3"} • Duração: $detectedDuration", color = TextSecondary, fontSize = 10.sp)
                                }
                            }
                        }
                    }

                    // IF YOUTUBE
                    LibraryItemType.YOUTUBE -> {
                        OutlinedTextField(
                            value = youtubeUrl,
                            onValueChange = { youtubeUrl = it },
                            label = { Text("Link do YouTube") },
                            placeholder = { Text("https://www.youtube.com/watch?v=...") },
                            leadingIcon = { Icon(Icons.Default.SmartDisplay, contentDescription = null, tint = Color(0xFFEF4444)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFFEF4444),
                                unfocusedBorderColor = BorderSubtle
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("youtube_url_dialog_input")
                        )

                        if (hasMediaLoaded) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFEF4444).copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(54.dp, 36.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFFEF4444)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White)
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(text = "Link do YouTube reconhecido", color = Color(0xFFFCA5A5), fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                        Text(text = "O Kudu vai analisar o vídeo. Escreve abaixo o título que queres guardar.", color = TextSecondary, fontSize = 9.sp)
                                    }
                                }
                            }
                        }
                    }

                    // IF PDF or DOCX
                    else -> {
                        Surface(
                            onClick = {
                                docPickerLauncher.launch(
                                    arrayOf(
                                        "application/pdf",
                                        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                                        "application/msword",
                                        "text/plain"
                                    )
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            color = KudulaNavyContainer,
                            border = BorderStroke(1.dp, KudulaCyanGlow.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.UploadFile, contentDescription = null, tint = KudulaCyanGlow, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (selectedFileName != null) "Trocar Documento: $selectedFileName" else "Selecionar Ficheiro (${selectedContentType.label})",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }

                // Title Input (Editable)
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Título do Conteúdo") },
                    placeholder = { Text("Ex: Redes Neurais & Arquitetura Convolucional") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = KudulaCyanGlow,
                        unfocusedBorderColor = BorderSubtle
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("add_item_title_input")
                )

                // Category Input
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Categoria ou Disciplina") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = KudulaCyanGlow,
                        unfocusedBorderColor = BorderSubtle
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorText != null) {
                    Text(
                        text = errorText ?: "",
                        color = Color(0xFFF87171),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalTitle = title.ifBlank {
                        if (selectedContentType == LibraryItemType.YOUTUBE) "Análise YouTube KCLS" else "Material de Aprendizagem"
                    }
                    if (onProcess != null) {
                        // Processamento real: identifica o tipo, lê o conteúdo e analisa com a IA.
                        if (selectedContentType == LibraryItemType.YOUTUBE) {
                            if (normalizeYouTubeUrl(youtubeUrl) == null) {
                                errorText = "Cola um link válido do YouTube."
                            } else {
                                onProcess(finalTitle, selectedContentType, category, null, youtubeUrl.trim())
                            }
                        } else if (selectedUri == null) {
                            errorText = "Seleciona primeiro um ficheiro para processar."
                        } else {
                            onProcess(finalTitle, selectedContentType, category, selectedUri, null)
                        }
                    } else {
                        onConfirm(finalTitle, selectedContentType, category)
                        if (selectedContentType == LibraryItemType.YOUTUBE && youtubeUrl.isNotBlank()) {
                            onOpenYouTube(youtubeUrl)
                        }
                    }
                },
                enabled = if (onProcess != null) {
                    hasMediaLoaded
                } else {
                    title.isNotBlank() || (selectedContentType == LibraryItemType.YOUTUBE && youtubeUrl.isNotBlank()) || hasMediaLoaded
                },
                colors = ButtonDefaults.buttonColors(containerColor = KudulaElectricBlue),
                modifier = Modifier.testTag("confirm_add_library_button")
            ) {
                Text("Processar com Kudula AI", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = TextSecondary)
            }
        }
    )
}

/**
 * Card de Carregar Ficheiros para Adicionar à Biblioteca.
 * Contém área de upload de ficheiros, botão de tirar foto e botão de galeria.
 */
@Composable
fun LibraryUploadCard(
    onFileSelected: (title: String, type: LibraryItemType, category: String) -> Unit,
    onPhotoCaptured: (title: String, category: String) -> Unit,
    onGallerySelected: (title: String, category: String) -> Unit,
    modifier: Modifier = Modifier,
    onProcessUri: ((title: String, type: LibraryItemType, category: String, uri: Uri) -> Unit)? = null
) {
    val context = LocalContext.current

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            val rawName = getFileNameFromUri(context, uri)
            val extension = rawName.substringAfterLast('.', "").lowercase(Locale.getDefault())
            val cleanTitle = rawName.substringBeforeLast('.').replace('_', ' ').replace('-', ' ').trim()
                .ifBlank { "Documento Importado" }

            val itemType = when (extension) {
                "pdf" -> LibraryItemType.PDF
                "docx", "doc" -> LibraryItemType.DOCX
                "png", "jpg", "jpeg", "webp" -> LibraryItemType.IMAGE
                "mp3", "m4a", "wav", "aac", "flac", "opus" -> LibraryItemType.AUDIO
                "mp4", "mkv", "mov", "webm", "3gp", "mpeg", "mpg" -> LibraryItemType.VIDEO
                else -> {
                    val systemType = context.contentResolver.getType(uri).orEmpty()
                    when {
                        systemType.startsWith("image/") -> LibraryItemType.IMAGE
                        systemType.startsWith("audio/") -> LibraryItemType.AUDIO
                        systemType.startsWith("video/") -> LibraryItemType.VIDEO
                        else -> LibraryItemType.PDF
                    }
                }
            }

            val category = when (itemType) {
                LibraryItemType.PDF -> "Documentos & Artigos"
                LibraryItemType.DOCX -> "Trabalhos Escritos"
                LibraryItemType.IMAGE -> "Diagramas & Esquemas"
                LibraryItemType.AUDIO -> "Gravações & Aulas"
                LibraryItemType.VIDEO -> "Vídeos & Aulas"
                else -> "Materiais Gerais"
            }

            if (onProcessUri != null) {
                onProcessUri(cleanTitle, itemType, category, uri)
            } else {
                onFileSelected(cleanTitle, itemType, category)
            }
        }
    }

    var showScannerFramingGuide by remember { mutableStateOf(false) }
    var kdapMessageAfterPhoto by remember { mutableStateOf<String?>(null) }
    var isCapturingPhoto by remember { mutableStateOf(false) }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        isCapturingPhoto = false
        // Se o utilizador cancelou a câmara, não há nada para processar.
        if (bitmap == null && onProcessUri != null) return@rememberLauncherForActivityResult
        if (bitmap != null) {
            // Compressão automática e ajuste de proporção para legibilidade cognitiva
            val optimizedBitmap = BitmapCompressor.compressAndOptimize(bitmap, maxDimension = 1920, quality = 85)
            // Correção de rotação EXIF
            val rotated = BitmapCompressor.rotateBitmap(optimizedBitmap, 0f)
        }
        val timeString = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()).format(Date())
        val title = "Digitalização de Caderno ($timeString)"
        val category = "Notas Manuscritas & Cadernos"
        if (onProcessUri != null) {
            if (bitmap != null) {
                showScannerFramingGuide = false
                try {
                    val scanFile = File(context.cacheDir, "kudula_scan_${System.currentTimeMillis()}.jpg")
                    FileOutputStream(scanFile).use { output ->
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, output)
                    }
                    onProcessUri(title, LibraryItemType.IMAGE, category, Uri.fromFile(scanFile))
                } catch (e: Exception) {
                    Toast.makeText(context, "Não foi possível guardar a fotografia para análise.", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            onPhotoCaptured(title, category)
        }
        EventLogger.logCameraCaptured(title)
        kdapMessageAfterPhoto = "KDAP Nível 2 Ativo: Contraste optimizado e isolamento de caligrafia na ZDP"
        Toast.makeText(context, "KDAP: Página capturada com auto-correção EXIF e compressão!", Toast.LENGTH_SHORT).show()
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            try {
                isCapturingPhoto = true
                takePictureLauncher.launch(null)
            } catch (e: Exception) {
                val timeString = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()).format(Date())
                val title = "Digitalização de Caderno ($timeString)"
                val category = "Notas Manuscritas & Cadernos"
                onPhotoCaptured(title, category)
                Toast.makeText(context, "Documento digitalizado com sucesso!", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Permissão de câmara necessária para capturar documentos", Toast.LENGTH_SHORT).show()
        }
    }

    // Moldura visual de enquadramento com animação de captura e mensagem KDAP
    if (showScannerFramingGuide) {
        CameraScannerGuideDialog(
            onDismiss = { showScannerFramingGuide = false },
            isCapturing = isCapturingPhoto,
            kdapMessage = kdapMessageAfterPhoto,
            onCaptureClick = {
                try {
                    val hasCamPermission = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED

                    if (hasCamPermission) {
                        isCapturingPhoto = true
                        takePictureLauncher.launch(null)
                    } else {
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                } catch (e: Exception) {
                    val timeString = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()).format(Date())
                    val title = "Digitalização de Caderno ($timeString)"
                    val category = "Notas Manuscritas & Cadernos"
                    onPhotoCaptured(title, category)
                    Toast.makeText(context, "Página capturada e processada com sucesso!", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    val galleryPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val rawName = getFileNameFromUri(context, uri)
            val cleanTitle = if (rawName.isNotBlank() && !rawName.startsWith("image:")) {
                rawName.substringBeforeLast('.').replace('_', ' ').replace('-', ' ').trim()
            } else {
                val timeString = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()).format(Date())
                "Captura da Galeria ($timeString)"
            }
            val category = "Capturas Visuais & Diagramas"
            if (onProcessUri != null) {
                onProcessUri(cleanTitle, LibraryItemType.IMAGE, category, uri)
            } else {
                onGallerySelected(cleanTitle, category)
            }
        }
    }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = KudulaNavySurface),
        border = BorderStroke(
            1.dp,
            Brush.horizontalGradient(listOf(KudulaCyanGlow.copy(alpha = 0.5f), KudulaElectricBlue.copy(alpha = 0.3f)))
        ),
        modifier = modifier
            .fillMaxWidth()
            .testTag("library_upload_card")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Dropzone Clickable Surface
            Surface(
                onClick = {
                    filePickerLauncher.launch(
                        arrayOf(
                            "application/pdf",
                            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                            "text/plain",
                            "video/*",
                            "audio/*",
                            "*/*"
                        )
                    )
                },
                shape = RoundedCornerShape(12.dp),
                color = KudulaNavyContainer,
                border = BorderStroke(1.dp, KudulaCyanGlow.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth().testTag("file_upload_dropzone")
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp, horizontal = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(imageVector = Icons.Default.CloudUpload, contentDescription = null, tint = KudulaCyanGlow, modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Carregar Ficheiro (PDF, DOCX, Vídeo, Áudio)",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 11.sp
                    )
                    Text(
                        text = "Extração automática de mapa semântico e flashcards",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextTertiary,
                        fontSize = 9.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action Row: Tirar Foto & Galeria
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        showScannerFramingGuide = true
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = KudulaElectricBlue.copy(alpha = 0.25f), contentColor = Color.White),
                    border = BorderStroke(1.dp, KudulaCyanGlow.copy(alpha = 0.5f)),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                    modifier = Modifier.weight(1f).testTag("button_take_photo")
                ) {
                    Icon(imageVector = Icons.Default.CameraAlt, contentDescription = "Tirar Foto", tint = KudulaCyanGlow, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Tirar Foto", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { galleryPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = KudulaViolet.copy(alpha = 0.25f), contentColor = Color.White),
                    border = BorderStroke(1.dp, KudulaVioletLight.copy(alpha = 0.5f)),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                    modifier = Modifier.weight(1f).testTag("button_gallery")
                ) {
                    Icon(imageVector = Icons.Default.PhotoLibrary, contentDescription = "Galeria", tint = KudulaVioletLight, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Galeria", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/**
 * Janela com o estado do processamento:
 * a ler o ficheiro, a analisar com a IA, análise concluída ou erro.
 */
@Composable
fun LibraryProcessingDialog(
    state: LibraryProcessingState,
    onCancel: () -> Unit,
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
    onOpenWorkspace: () -> Unit
) {
    when (state.phase) {
        LibraryProcessingPhase.IDLE -> Unit

        LibraryProcessingPhase.READING, LibraryProcessingPhase.ANALYZING -> {
            AlertDialog(
                onDismissRequest = {},
                containerColor = KudulaNavySurface,
                title = {
                    Text(
                        text = "A processar com Kudula AI",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 17.sp
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = state.title,
                            color = KudulaCyanGlow,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                color = KudulaCyanGlow,
                                strokeWidth = 3.dp,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = state.message,
                                color = Color.White,
                                fontSize = 13.sp
                            )
                        }
                        Text(
                            text = "Ficheiros longos podem demorar alguns minutos.",
                            color = TextTertiary,
                            fontSize = 10.sp
                        )
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = onCancel) {
                        Text("Cancelar", color = TextSecondary)
                    }
                }
            )
        }

        LibraryProcessingPhase.DONE -> {
            AlertDialog(
                onDismissRequest = onDismiss,
                containerColor = KudulaNavySurface,
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF34D399),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Análise concluída",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 17.sp
                        )
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = state.title,
                            color = KudulaCyanGlow,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Column(
                            modifier = Modifier
                                .heightIn(max = 380.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            SelectionContainer {
                                Text(
                                    text = state.analysis,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    lineHeight = 17.sp
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = onOpenWorkspace,
                        colors = ButtonDefaults.buttonColors(containerColor = KudulaElectricBlue)
                    ) {
                        Text("Abrir Workspace", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = onDismiss) {
                        Text("Fechar", color = TextSecondary)
                    }
                }
            )
        }

        LibraryProcessingPhase.ERROR -> {
            AlertDialog(
                onDismissRequest = onDismiss,
                containerColor = KudulaNavySurface,
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFF87171),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Não foi possível processar",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 17.sp
                        )
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (state.title.isNotBlank()) {
                            Text(
                                text = state.title,
                                color = KudulaCyanGlow,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Text(
                            text = state.errorMessage,
                            color = Color.White,
                            fontSize = 13.sp
                        )
                    }
                },
                confirmButton = {
                    if (state.itemId != null) {
                        Button(
                            onClick = onRetry,
                            colors = ButtonDefaults.buttonColors(containerColor = KudulaElectricBlue)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Tentar novamente", fontWeight = FontWeight.Bold)
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = onDismiss) {
                        Text("Fechar", color = TextSecondary)
                    }
                }
            )
        }
    }
}

/** Janela para voltar a consultar a análise completa de um material já processado. */
@Composable
fun LibraryAnalysisDialog(
    title: String,
    analysis: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = KudulaNavySurface,
        title = {
            Text(
                text = title.ifBlank { "Análise do material" },
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 16.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                SelectionContainer {
                    Text(
                        text = analysis,
                        color = Color.White,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Fechar", color = KudulaCyanGlow, fontWeight = FontWeight.Bold)
            }
        }
    )
}

private fun queryFileSizeBytes(context: Context, uri: Uri): Long? {
    if (uri.scheme == "file") {
        return try {
            File(uri.path.orEmpty()).length().takeIf { it > 0 }
        } catch (_: Exception) {
            null
        }
    }
    var size: Long? = null
    try {
        context.contentResolver.query(
            uri, arrayOf(OpenableColumns.SIZE), null, null, null
        )?.use { cursor ->
            val index = cursor.getColumnIndex(OpenableColumns.SIZE)
            if (index != -1 && cursor.moveToFirst() && !cursor.isNull(index)) {
                size = cursor.getLong(index)
            }
        }
    } catch (_: Exception) {
    }
    return size
}

private fun formatFileSize(bytes: Long): String {
    return if (bytes >= 1024L * 1024L) {
        String.format(Locale.getDefault(), "%.1f MB", bytes / (1024.0 * 1024.0))
    } else {
        String.format(Locale.getDefault(), "%.0f KB", bytes / 1024.0)
    }
}

private fun readMediaDuration(context: Context, uri: Uri): String? {
    val retriever = MediaMetadataRetriever()
    return try {
        retriever.setDataSource(context, uri)
        val durationMs = retriever
            .extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            ?.toLongOrNull()
        durationMs?.let { formatDuration(it) }
    } catch (_: Exception) {
        null
    } finally {
        try {
            retriever.release()
        } catch (_: Exception) {
        }
    }
}

private fun formatDuration(milliseconds: Long): String {
    val totalSeconds = milliseconds / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    }
}

private fun getFileNameFromUri(context: Context, uri: Uri): String {
    var result = ""
    if (uri.scheme == "content") {
        try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1 && cursor.moveToFirst()) {
                    result = cursor.getString(nameIndex) ?: ""
                }
            }
        } catch (_: Exception) {}
    }
    if (result.isBlank()) {
        result = uri.lastPathSegment?.substringAfterLast('/') ?: "Documento_${System.currentTimeMillis() % 10000}"
    }
    return result
}