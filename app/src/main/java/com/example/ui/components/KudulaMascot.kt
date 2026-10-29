package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.MascotMood
import com.example.ui.theme.*

@Composable
fun KudulaMascotCompanion(
    mood: MascotMood,
    message: String,
    onDismiss: () -> Unit,
    onTap: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition(label = "mascot")
    val eyeGlow by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    val bounceOffset by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounce"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("kudula_mascot_card")
            .clickable { onTap() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = KudulaNavyContainer.copy(alpha = 0.95f)
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(
                listOf(KudulaElectricBlue.copy(alpha = 0.6f), KudulaViolet.copy(alpha = 0.6f))
            )
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Mascot Animated Avatar
            Box(
                modifier = Modifier
                    .size(if (compact) 46.dp else 56.dp)
                    .offset(y = bounceOffset.dp),
                contentAlignment = Alignment.Center
            ) {
                MascotOwlAvatar(mood = mood, eyeGlow = eyeGlow)
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "KUDU",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = KudulaCyanGlow
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = KudulaViolet.copy(alpha = 0.25f)
                    ) {
                        Text(
                            text = when (mood) {
                                MascotMood.HAPPY -> "Ativo"
                                MascotMood.THINKING -> "A analisar..."
                                MascotMood.ENCOURAGING -> "Apoio KDAP"
                                MascotMood.SLEEPY -> "Sugerindo Pausa"
                                MascotMood.CELEBRATING -> "Celebração!"
                                MascotMood.CURIOUS -> "Curioso"
                                MascotMood.PROUD -> "Orgulhoso de Ti"
                                MascotMood.CALM -> "Foco Profundo"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = KudulaVioletLight,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextPrimary,
                    maxLines = if (compact) 2 else 3,
                    overflow = TextOverflow.Ellipsis
                )
            }

            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .size(32.dp)
                    .testTag("dismiss_mascot_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Ocultar companheiro",
                    tint = TextTertiary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun MascotOwlAvatar(
    mood: MascotMood,
    eyeGlow: Float,
    modifier: Modifier = Modifier
) {
    val auraColor = when (mood) {
        MascotMood.HAPPY -> KudulaCyanGlow
        MascotMood.CELEBRATING, MascotMood.PROUD -> KudulaGold
        MascotMood.ENCOURAGING -> KudulaSuccess
        MascotMood.THINKING -> KudulaVioletLight
        MascotMood.SLEEPY -> Color(0xFF94A3B8)
        MascotMood.CURIOUS -> KudulaElectricBlueLight
        MascotMood.CALM -> KudulaTealLight
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(KudulaNavySurface, KudulaNavyDark)
                )
            )
            .border(2.dp, auraColor.copy(alpha = 0.8f * eyeGlow), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        // High-quality friendly 3D Pixar-style Kudu owl mascot
        Image(
            painter = painterResource(R.drawable.kudu_friendly_mascot),
            contentDescription = "Mascote Kudu Amigável",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Mood micro-badge with cheerful emoji
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(16.dp)
                .clip(CircleShape)
                .background(auraColor)
                .border(1.dp, Color.White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = when (mood) {
                    MascotMood.HAPPY -> "✨"
                    MascotMood.CELEBRATING -> "🎉"
                    MascotMood.PROUD -> "👑"
                    MascotMood.ENCOURAGING -> "💪"
                    MascotMood.THINKING -> "💭"
                    MascotMood.SLEEPY -> "💤"
                    MascotMood.CURIOUS -> "🔍"
                    MascotMood.CALM -> "🌿"
                },
                fontSize = 8.sp
            )
        }
    }
}

/**
 * Priority 9 — Premium Microinteractions: AI Transformation Phases
 */
enum class AiTransformationPhase(
    val title: String,
    val detail: String,
    val iconEmoji: String,
    val mood: MascotMood
) {
    THINKING(
        "Estruturação de Raciocínio",
        "Kudu está a decompor o desafio em blocos cognitivos...",
        "🧠",
        MascotMood.THINKING
    ),
    READING(
        "Leitura Ativa & Síntese Semântica",
        "A extrair conceitos-chave e densidade terminológica...",
        "📖",
        MascotMood.CURIOUS
    ),
    SIMPLIFYING(
        "Simplificação & Analogias Vivas",
        "A traduzir complexidade abstrata em imagens intuitivas...",
        "💡",
        MascotMood.HAPPY
    ),
    CONNECTING(
        "Conexão ao Grafo de Conhecimento",
        "A interligar novos nós neurais ao teu mapa prévio...",
        "⚡",
        MascotMood.CELEBRATING
    )
}

/**
 * Priority 2 & 9 — Intelligent Loading Component with Transformation Microinteractions.
 * Replaces generic spinners with Kudu avatar states, dynamic phases and contextual animations.
 */
@Composable
fun IntelligentAiLoadingCard(
    phaseLabel: String = "Estruturação Neural",
    phaseDetail: String = "Kudu está a conectar grafos de conhecimento e analogias...",
    progress: Float = 0.65f,
    currentPhase: AiTransformationPhase? = null,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ai_load")
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    // Dynamic phase cycle if not explicitly pinned
    var autoPhaseIndex by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        val phases = AiTransformationPhase.entries
        while (true) {
            kotlinx.coroutines.delay(2200)
            autoPhaseIndex = (autoPhaseIndex + 1) % phases.size
        }
    }

    val activePhase = currentPhase ?: AiTransformationPhase.entries[autoPhaseIndex]
    val displayTitle = currentPhase?.title ?: activePhase.title
    val displayDetail = currentPhase?.detail ?: activePhase.detail

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("intelligent_ai_loading_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = KudulaNavyContainer),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(
                listOf(
                    KudulaElectricBlue.copy(alpha = pulseGlow),
                    KudulaViolet.copy(alpha = pulseGlow)
                )
            )
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(68.dp),
                contentAlignment = Alignment.Center
            ) {
                MascotOwlAvatar(mood = activePhase.mood, eyeGlow = pulseGlow)
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(text = activePhase.iconEmoji, fontSize = 16.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = displayTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = KudulaCyanGlow
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = displayDetail,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                modifier = Modifier.padding(horizontal = 8.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Microinteraction Phase Indicators (Thinking -> Reading -> Simplifying -> Connecting)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AiTransformationPhase.entries.forEachIndexed { idx, phase ->
                    val isCurrent = phase == activePhase
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isCurrent) KudulaElectricBlue else KudulaNavySurface,
                        border = if (isCurrent) androidx.compose.foundation.BorderStroke(1.dp, KudulaCyanGlow) else null
                    ) {
                        Text(
                            text = phase.name.take(4),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isCurrent) Color.White else TextTertiary,
                            fontSize = 9.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Liquid Morph Progress Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF1E293B))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress.coerceIn(0.15f, 1f))
                        .fillMaxHeight()
                        .background(
                            Brush.horizontalGradient(
                                listOf(KudulaElectricBlue, KudulaViolet, KudulaCyanGlow)
                            )
                        )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "«A inteligência artificial de Kudula não resume apenas. Revela o que importa.»",
                style = MaterialTheme.typography.labelSmall,
                color = TextTertiary,
                fontSize = 11.sp
            )
        }
    }
}

/**
 * Priority 5 — Zero Raw Empty States.
 * Contextual card with illustration, Kudu mascot, encouraging message and CTA.
 */
@Composable
fun SmartEmptyStateCard(
    title: String,
    message: String,
    ctaText: String,
    onCtaClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("smart_empty_state_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = KudulaNavyContainer.copy(alpha = 0.8f)),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(listOf(BorderSubtle, KudulaElectricBlue.copy(alpha = 0.3f)))
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(modifier = Modifier.size(60.dp)) {
                MascotOwlAvatar(mood = MascotMood.CURIOUS, eyeGlow = 0.8f)
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(18.dp))

            Surface(
                onClick = onCtaClick,
                shape = RoundedCornerShape(12.dp),
                color = KudulaElectricBlue,
                shadowElevation = 4.dp
            ) {
                Text(
                    text = ctaText,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp)
                )
            }
        }
    }
}
