package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.KudulaEvent
import com.example.data.local.entity.LearningSession
import com.example.ui.components.KudulaEmptyStateCard
import com.example.ui.components.MascotOwlAvatar
import com.example.ui.theme.*
import com.example.viewmodel.LearningSessionUiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed class TimelineItem(val timestamp: Long) {
    data class SessionItem(val session: LearningSession) : TimelineItem(session.timestamp)
    data class EventItem(val event: KudulaEvent) : TimelineItem(event.timestamp)
}

/**
 * Learning Session Timeline (KLSE)
 * Linha do tempo cognitiva vertical elegante com marcos de leitura, quizzes, metas e reflexões.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearningTimelineScreen(
    state: LearningSessionUiState,
    onSyncClick: () -> Unit,
    onNewReflectionClick: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf("Todos") }
    val filters = listOf("Todos", "Leitura", "Quizzes", "Metas", "Reflexões", "Kudu")

    // Merge sessions and events chronologically
    val allItems = remember(state.sessions, state.events, selectedFilter) {
        val sessionItems = state.sessions.map { TimelineItem.SessionItem(it) }
        val eventItems = state.events.map { TimelineItem.EventItem(it) }
        val merged = (sessionItems + eventItems).sortedByDescending { it.timestamp }

        when (selectedFilter) {
            "Leitura" -> merged.filter {
                when (it) {
                    is TimelineItem.SessionItem -> it.session.sourceType in listOf("PDF", "DOCX", "CHAPTER")
                    is TimelineItem.EventItem -> it.event.eventType in listOf(KudulaEvent.TYPE_PDF_OPENED, KudulaEvent.TYPE_CHAPTER_READ)
                }
            }
            "Quizzes" -> merged.filter {
                when (it) {
                    is TimelineItem.SessionItem -> it.session.sourceType == "QUIZ"
                    is TimelineItem.EventItem -> it.event.eventType == KudulaEvent.TYPE_QUIZ_COMPLETED
                }
            }
            "Metas" -> merged.filter {
                when (it) {
                    is TimelineItem.SessionItem -> false
                    is TimelineItem.EventItem -> it.event.eventType == KudulaEvent.TYPE_GOAL_COMPLETED
                }
            }
            "Reflexões" -> merged.filter {
                when (it) {
                    is TimelineItem.SessionItem -> false
                    is TimelineItem.EventItem -> it.event.eventType == KudulaEvent.TYPE_REFLECTION_CREATED
                }
            }
            "Kudu" -> merged.filter {
                when (it) {
                    is TimelineItem.SessionItem -> false
                    is TimelineItem.EventItem -> it.event.eventType == KudulaEvent.TYPE_KUDU_ENCOURAGED
                }
            }
            else -> merged
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Learning Timeline",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = KudulaCyanGlow.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "KLSE Offline",
                                    color = KudulaCyanGlow,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                        Text(
                            text = "Histórico cognitivo cronológico • Sincronização Local Room",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextTertiary,
                            fontSize = 10.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(
                        onClick = onSyncClick,
                        modifier = Modifier.testTag("timeline_sync_button")
                    ) {
                        if (state.isSyncing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = KudulaCyanGlow
                            )
                        } else {
                            Icon(Icons.Default.Sync, contentDescription = "Sincronizar", tint = KudulaCyanGlow)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = KudulaNavyDark)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNewReflectionClick,
                containerColor = KudulaElectricBlue,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.EditNote, contentDescription = null) },
                text = { Text("Nova Reflexão", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                modifier = Modifier.testTag("timeline_add_reflection_fab")
            )
        },
        containerColor = KudulaNavyDark,
        modifier = modifier.testTag("learning_timeline_screen")
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Metrics Card
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = KudulaNavySurface),
                    border = BorderStroke(1.dp, Brush.horizontalGradient(listOf(KudulaCyanGlow.copy(alpha = 0.5f), KudulaElectricBlue.copy(alpha = 0.3f)))),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Motor KLSE de Sessões Cognitivas",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "Armazenamento SQLite / Room sem dependência de nuvem",
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                            Surface(
                                shape = CircleShape,
                                color = if (state.sessions.any { !it.synced }) KudulaGold.copy(alpha = 0.2f) else KudulaSuccess.copy(alpha = 0.2f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(if (state.sessions.any { !it.synced }) KudulaGold else KudulaSuccess)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (state.sessions.any { !it.synced }) "Offline Pronto" else "Sincronizado",
                                        fontSize = 10.sp,
                                        color = if (state.sessions.any { !it.synced }) KudulaGold else KudulaSuccess,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Stats Grid
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StatPill(
                                title = "Tempo Foco",
                                value = "${state.totalMinutesStudied}m",
                                subtitle = "Acumulado",
                                accent = KudulaCyanGlow,
                                modifier = Modifier.weight(1f)
                            )
                            StatPill(
                                title = "Sessões",
                                value = "${state.sessions.size}",
                                subtitle = "Registadas",
                                accent = KudulaElectricBlue,
                                modifier = Modifier.weight(1f)
                            )
                            StatPill(
                                title = "Eventos",
                                value = "${state.events.size}",
                                subtitle = "Telemetria",
                                accent = KudulaVioletLight,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Last sync feedback
                        if (state.lastSyncMessage != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = state.lastSyncMessage,
                                color = KudulaCyanGlow,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Filter Chips Row
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(filters) { filter ->
                        FilterChip(
                            selected = selectedFilter == filter,
                            onClick = { selectedFilter = filter },
                            label = { Text(filter, fontSize = 11.sp, fontWeight = if (selectedFilter == filter) FontWeight.Bold else FontWeight.Normal) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = KudulaElectricBlue.copy(alpha = 0.35f),
                                selectedLabelColor = KudulaCyanGlow,
                                containerColor = KudulaNavyContainer,
                                labelColor = TextSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selectedFilter == filter,
                                borderColor = if (selectedFilter == filter) KudulaCyanGlow else BorderSubtle
                            )
                        )
                    }
                }
            }

            // Vertical Timeline Items
            if (allItems.isEmpty()) {
                item {
                    KudulaEmptyStateCard(
                        icon = Icons.Default.Timeline,
                        badge = "Motor KLSE",
                        title = "A tua linha do tempo cognitiva começa agora",
                        description = "Conforme realizas leituras, anotações de voz, revisões ou análises no YouTube, cada marco temporal será gravado localmente no teu Grafo de Aprendizagem.",
                        testTag = "timeline_empty_card"
                    )
                }
            } else {
                itemsIndexed(allItems) { index, item ->
                    val isLast = index == allItems.lastIndex
                    TimelineCardRow(
                        item = item,
                        isLast = isLast
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }
}

@Composable
private fun TimelineCardRow(
    item: TimelineItem,
    isLast: Boolean,
    modifier: Modifier = Modifier
) {
    val timeFormat = remember { SimpleDateFormat("HH:mm • dd MMM", Locale.getDefault()) }
    val formattedTime = remember(item.timestamp) { timeFormat.format(Date(item.timestamp)) }

    val icon: androidx.compose.ui.graphics.vector.ImageVector
    val iconColor: Color
    val cardTitle: String
    val cardSubtitle: String
    val badgeLabel: String
    val ikcInfo: String?

    when (item) {
        is TimelineItem.SessionItem -> {
            val s = item.session
            icon = when (s.sourceType) {
                "PDF", "DOCX", "CHAPTER" -> Icons.Default.MenuBook
                "QUIZ" -> Icons.Default.Bolt
                "YOUTUBE" -> Icons.Default.SmartDisplay
                "CAMERA" -> Icons.Default.CameraAlt
                else -> Icons.Default.Psychology
            }
            iconColor = KudulaCyanGlow
            cardTitle = s.concept
            cardSubtitle = "${s.studyMinutes} min de estudo focado • Fonte: ${s.sourceType}"
            badgeLabel = "Sessão KLSE"
            ikcInfo = "IKC: ${s.ikcPartial}% • IPA: ${s.ipaPartial}%"
        }
        is TimelineItem.EventItem -> {
            val e = item.event
            when (e.eventType) {
                KudulaEvent.TYPE_CHAPTER_READ, KudulaEvent.TYPE_PDF_OPENED -> {
                    icon = Icons.Default.MenuBook
                    iconColor = KudulaTealLight
                    cardTitle = "Leitura Cognitiva"
                    cardSubtitle = e.details
                    badgeLabel = "Leitura"
                    ikcInfo = "+2% Compreensão"
                }
                KudulaEvent.TYPE_QUIZ_COMPLETED -> {
                    icon = Icons.Default.CheckCircle
                    iconColor = KudulaGold
                    cardTitle = "Validação de Retenção"
                    cardSubtitle = e.details
                    badgeLabel = "Quiz"
                    ikcInfo = "IKC +3%"
                }
                KudulaEvent.TYPE_GOAL_COMPLETED -> {
                    icon = Icons.Default.EmojiEvents
                    iconColor = KudulaGold
                    cardTitle = "Meta Cognitiva Concluída"
                    cardSubtitle = e.details
                    badgeLabel = "Meta"
                    ikcInfo = "+50 XP • Streak ↑"
                }
                KudulaEvent.TYPE_REFLECTION_CREATED -> {
                    icon = Icons.Default.AutoAwesome
                    iconColor = KudulaVioletLight
                    cardTitle = "Diário Reflexivo Kudu"
                    cardSubtitle = e.details
                    badgeLabel = "Reflexão"
                    ikcInfo = "Metacognição"
                }
                KudulaEvent.TYPE_KUDU_ENCOURAGED -> {
                    icon = Icons.Default.ChatBubble
                    iconColor = KudulaElectricBlueLight
                    cardTitle = "Intervenção do Mentor Kudu"
                    cardSubtitle = e.details
                    badgeLabel = "Kudu"
                    ikcInfo = null
                }
                KudulaEvent.TYPE_YOUTUBE_PROCESSED -> {
                    icon = Icons.Default.SmartDisplay
                    iconColor = Color(0xFFEF4444)
                    cardTitle = "Processamento Multimodal"
                    cardSubtitle = e.details
                    badgeLabel = "YouTube"
                    ikcInfo = "Resumo + Mapa"
                }
                KudulaEvent.TYPE_CAMERA_CAPTURED -> {
                    icon = Icons.Default.CameraAlt
                    iconColor = KudulaCyanGlow
                    cardTitle = "Digitalização de Caderno"
                    cardSubtitle = e.details
                    badgeLabel = "Câmara KDAP"
                    ikcInfo = "Auto-EXIF"
                }
                else -> {
                    icon = Icons.Default.Notifications
                    iconColor = KudulaElectricBlue
                    cardTitle = e.eventType
                    cardSubtitle = e.details
                    badgeLabel = "Evento"
                    ikcInfo = null
                }
            }
        }
    }

    Row(modifier = modifier.fillMaxWidth()) {
        // Vertical Timeline Column (Node + Line)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(36.dp)
        ) {
            // Node circle
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.2f))
                    .border(BorderStroke(1.5.dp, iconColor), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(14.dp)
                )
            }

            // Connecting Vertical Line
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(72.dp)
                        .background(
                            Brush.verticalGradient(
                                listOf(iconColor.copy(alpha = 0.6f), KudulaElectricBlue.copy(alpha = 0.2f))
                            )
                        )
                )
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Card Content
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = KudulaNavyContainer),
            border = BorderStroke(1.dp, BorderSubtle),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = iconColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = badgeLabel,
                            color = iconColor,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Text(
                        text = formattedTime,
                        color = TextTertiary,
                        fontSize = 10.sp
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = cardTitle,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 13.sp
                )

                Text(
                    text = cardSubtitle,
                    color = TextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )

                if (ikcInfo != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = KudulaNavyDark.copy(alpha = 0.8f),
                        border = BorderStroke(1.dp, KudulaCyanGlow.copy(alpha = 0.25f))
                    ) {
                        Text(
                            text = ikcInfo,
                            color = KudulaCyanGlow,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatPill(
    title: String,
    value: String,
    subtitle: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = KudulaNavyContainer,
        border = BorderStroke(1.dp, accent.copy(alpha = 0.3f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, fontSize = 9.sp, color = TextTertiary)
            Text(value, fontSize = 15.sp, fontWeight = FontWeight.Black, color = accent)
            Text(subtitle, fontSize = 8.sp, color = TextSecondary)
        }
    }
}
