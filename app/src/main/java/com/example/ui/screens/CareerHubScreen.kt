package com.example.ui.screens

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CareerRole
import com.example.model.UserCognitiveProfile
import com.example.ui.components.KudulaEmptyStateCard
import com.example.ui.theme.*
import com.example.ui.util.LocalScreenWindowInfo

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CareerHubScreen(
    profile: UserCognitiveProfile,
    roles: List<CareerRole>,
    onExploreRole: (CareerRole) -> Unit,
    modifier: Modifier = Modifier
) {
    val windowInfo = LocalScreenWindowInfo.current
    val isNarrow = windowInfo.isNarrowCompact
    var selectedRole by remember { mutableStateOf(roles.firstOrNull()) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(KudulaNavyDark)
            .padding(horizontal = windowInfo.screenHorizontalPadding, vertical = 12.dp)
            .testTag("career_hub_screen"),
        verticalArrangement = Arrangement.spacedBy(if (isNarrow) 14.dp else 18.dp)
    ) {
        // Header
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(if (isNarrow) 34.dp else 38.dp)
                        .clip(CircleShape)
                        .background(KudulaElectricBlue.copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Work, contentDescription = null, tint = KudulaElectricBlueLight, modifier = Modifier.size(if (isNarrow) 18.dp else 22.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Hub de Carreiras • MICK",
                        style = if (isNarrow) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Text(
                        text = "Motor de Inteligência de Carreira (KCIE)",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        fontSize = if (isNarrow) 11.sp else 12.sp
                    )
                }
            }
        }

        // Cognitive Alignment Card (MBTI + RIASEC + Intelligences)
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = KudulaNavySurface),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.horizontalGradient(listOf(KudulaViolet, KudulaElectricBlue))
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(if (isNarrow) 14.dp else 18.dp)) {
                    Text(
                        text = "PERFIL PSICOMÉTRICO & VOCACIONAL",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = KudulaCyanGlow,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Column {
                            Text("MBTI Cognitivo", style = MaterialTheme.typography.labelSmall, color = TextTertiary, fontSize = 10.sp)
                            Text(profile.mbti ?: "—", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Column {
                            Text("Código RIASEC", style = MaterialTheme.typography.labelSmall, color = TextTertiary, fontSize = 10.sp)
                            Text(profile.riasec ?: "—", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = KudulaGold)
                        }
                        Column {
                            Text("Aptidão Dominante", style = MaterialTheme.typography.labelSmall, color = TextTertiary, fontSize = 10.sp)
                            Text(profile.multipleIntelligences.firstOrNull() ?: "—", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = KudulaSuccess)
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "CARREIRAS ALINHADAS AO TEU PERFIL",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 0.8.sp
            )
        }

        if (roles.isEmpty()) {
            item {
                KudulaEmptyStateCard(
                    icon = Icons.Default.WorkOutline,
                    badge = "Motor KCIE",
                    title = "Nenhuma trilha de carreira mapeada ainda",
                    description = "O Hub de Carreiras mapeia as tuas aptidões à medida que concluis o Onboarding Cognitivo ou realizas sessões de estudo. Começa a tua jornada para desbloquear recomendações de mercado!",
                    testTag = "career_hub_empty_card"
                )
            }
        } else {
            items(roles) { role ->
            val isSelected = selectedRole?.id == role.id
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { selectedRole = role }
                    .testTag("career_role_card_${role.id}"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) KudulaNavySurface else KudulaNavyContainer
                ),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = if (isSelected) Brush.horizontalGradient(listOf(KudulaCyanGlow, KudulaElectricBlue)) else Brush.linearGradient(listOf(BorderSubtle, BorderSubtle))
                )
            ) {
                Column(modifier = Modifier.padding(if (isNarrow) 14.dp else 18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = role.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = KudulaElectricBlue.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "${role.matchScore}% Match",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = KudulaCyanGlow,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontSize = if (isNarrow) 11.sp else 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = role.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "💰 ${role.salaryGlobal}",
                            style = MaterialTheme.typography.labelSmall,
                            color = KudulaSuccess,
                            fontWeight = FontWeight.Bold,
                            fontSize = if (isNarrow) 10.sp else 11.sp
                        )
                        Text(
                            text = "📈 ${role.marketTrend}",
                            style = MaterialTheme.typography.labelSmall,
                            color = KudulaGold,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = if (isNarrow) 10.sp else 11.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Skills Gap Analysis
                    Text(
                        text = "ANÁLISE DE LACUNA DE COMPETÊNCIAS (SKILLS GAP):",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextTertiary,
                        fontSize = 10.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        role.topSkillsNeeded.take(2).forEach { skill ->
                            Surface(shape = RoundedCornerShape(6.dp), color = KudulaNavyDark) {
                                Text(
                                    text = "✅ $skill",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = KudulaSuccess,
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                        }
                        role.missingSkills.take(1).forEach { skill ->
                            Surface(shape = RoundedCornerShape(6.dp), color = KudulaNavyDark) {
                                Text(
                                    text = "⚡ Em falta: $skill",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = KudulaWarning,
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
