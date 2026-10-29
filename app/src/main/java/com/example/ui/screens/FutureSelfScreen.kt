package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.FutureSelfMilestone
import com.example.model.UserCognitiveProfile
import com.example.ui.components.KudulaEmptyStateCard
import com.example.ui.theme.*
import com.example.ui.util.LocalScreenWindowInfo

@Composable
fun FutureSelfScreen(
    profile: UserCognitiveProfile,
    milestones: List<FutureSelfMilestone>,
    modifier: Modifier = Modifier
) {
    val windowInfo = LocalScreenWindowInfo.current
    val isNarrow = windowInfo.isNarrowCompact
    var selectedIndex by remember { mutableIntStateOf(1) } // default: 6 months
    val selectedMilestone = milestones.getOrNull(selectedIndex) ?: milestones.firstOrNull()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(KudulaNavyDark)
            .padding(horizontal = windowInfo.screenHorizontalPadding, vertical = 12.dp)
            .testTag("future_self_screen"),
        verticalArrangement = Arrangement.spacedBy(if (isNarrow) 14.dp else 18.dp)
    ) {
        // Title
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(if (isNarrow) 34.dp else 38.dp)
                        .clip(CircleShape)
                        .background(KudulaVioletLight.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.AutoFixHigh, contentDescription = null, tint = KudulaVioletLight, modifier = Modifier.size(if (isNarrow) 18.dp else 22.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Simulador do Teu Eu Futuro",
                        style = if (isNarrow) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Text(
                        text = "Projeção Neurocognitiva de Ritmo",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        fontSize = if (isNarrow) 11.sp else 12.sp
                    )
                }
            }
        }

        if (milestones.isEmpty() || selectedMilestone == null) {
            item {
                KudulaEmptyStateCard(
                    icon = Icons.Default.AutoFixHigh,
                    badge = "Simulador Neurocognitivo",
                    title = "Calibra o teu perfil para simular o Eu Futuro",
                    description = "O Simulador gera projeções preditivas baseadas no teu perfil vocacional e ritmo de estudo. Conclui o Onboarding Cognitivo para desbloquear a tua linha temporal.",
                    testTag = "future_self_empty_card"
                )
            }
        } else {
            // Timeline Selector Tabs
            item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                milestones.forEachIndexed { index, ms ->
                    val isSelected = selectedIndex == index
                    val displayLabel = when {
                        ms.timeframe.contains("Hoje", ignoreCase = true) -> "Hoje"
                        ms.timeframe.contains("6 Meses", ignoreCase = true) -> "6 Meses"
                        ms.timeframe.contains("1 Ano", ignoreCase = true) -> "1 Ano"
                        ms.timeframe.contains("2 Anos", ignoreCase = true) -> "2 Anos"
                        else -> ms.timeframe
                    }
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) KudulaViolet else KudulaNavyContainer,
                        border = BorderStroke(1.dp, if (isSelected) KudulaVioletLight else BorderSubtle),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedIndex = index }
                            .testTag("future_timeframe_${index}")
                    ) {
                        Text(
                            text = displayLabel,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else TextSecondary,
                            fontSize = if (isNarrow) 10.sp else 11.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .padding(vertical = 10.dp, horizontal = 4.dp)
                                .fillMaxWidth()
                        )
                    }
                }
            }
        }

        // Current You vs Future You Comparison Card
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = KudulaNavySurface),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.horizontalGradient(listOf(KudulaCyanGlow, KudulaVioletLight))
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(if (isNarrow) 14.dp else 20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "VOCÊ HOJE",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextTertiary,
                                fontSize = 10.sp
                            )
                            Text(
                                text = "IKC: ${profile.ikc.overallAverage}",
                                style = if (isNarrow) MaterialTheme.typography.titleSmall else MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "${profile.xp} XP",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.TrendingFlat,
                            contentDescription = null,
                            tint = KudulaCyanGlow,
                            modifier = Modifier.size(if (isNarrow) 26.dp else 36.dp)
                        )

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = selectedMilestone.timeframe.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = KudulaCyanGlow,
                                fontSize = 10.sp
                            )
                            Text(
                                text = if (isNarrow) "IKC: ${selectedMilestone.projectedIKC}" else "IKC PROJETADO: ${selectedMilestone.projectedIKC}",
                                style = if (isNarrow) MaterialTheme.typography.titleSmall else MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = KudulaSuccess
                            )
                            Text(
                                text = "${selectedMilestone.projectedXP} XP",
                                style = MaterialTheme.typography.bodySmall,
                                color = KudulaGold,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = BorderSubtle)
                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "PAPEL CONQUISTADO:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextTertiary
                    )
                    Text(
                        text = selectedMilestone.targetRole,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "NOVAS HABILIDADES DESBLOQUEADAS:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextTertiary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    selectedMilestone.unlockedAbilities.forEach { ability ->
                        Row(
                            modifier = Modifier.padding(vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = KudulaSuccess, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = ability, style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                        }
                    }
                }
            }
        }

        // Mentor Guidance Quote from the Future
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = KudulaNavyContainer),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(KudulaViolet.copy(alpha = 0.4f), BorderSubtle)))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🔮 ", fontSize = 18.sp)
                        Text(
                            text = "CONSELHO DO TEU EU DO FUTURO",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = KudulaVioletLight
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "«${selectedMilestone.guidanceQuote}»",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        lineHeight = 22.sp,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }
            }
        }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
