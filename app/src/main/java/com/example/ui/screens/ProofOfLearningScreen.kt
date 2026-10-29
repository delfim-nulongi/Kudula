package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ProofOfLearningBadge
import com.example.model.UserCognitiveProfile
import com.example.ui.components.KudulaEmptyStateCard
import com.example.ui.theme.*
import com.example.ui.util.LocalScreenWindowInfo

@Composable
fun ProofOfLearningScreen(
    profile: UserCognitiveProfile,
    badges: List<ProofOfLearningBadge>,
    modifier: Modifier = Modifier
) {
    val windowInfo = LocalScreenWindowInfo.current
    val isNarrow = windowInfo.isNarrowCompact

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(KudulaNavyDark)
            .padding(horizontal = windowInfo.screenHorizontalPadding, vertical = 12.dp)
            .testTag("proof_of_learning_screen"),
        verticalArrangement = Arrangement.spacedBy(if (isNarrow) 12.dp else 16.dp)
    ) {
        // Title
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(if (isNarrow) 34.dp else 38.dp)
                        .clip(CircleShape)
                        .background(KudulaGold.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = KudulaGold, modifier = Modifier.size(if (isNarrow) 18.dp else 22.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Proof of Learning (PoL)",
                        style = if (isNarrow) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Text(
                        text = "Credenciais Verificáveis • Sem Barreiras",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        fontSize = if (isNarrow) 11.sp else 12.sp
                    )
                }
            }
        }

        // Talent Marketplace Header Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = KudulaNavySurface),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.horizontalGradient(listOf(KudulaGold, KudulaCyanGlow))
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = KudulaGold.copy(alpha = 0.18f)
                        ) {
                            Text(
                                text = "TALENT MARKETPLACE READY",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = KudulaGold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        Text(
                            text = "${badges.size} Certificados Ativos",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextTertiary
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Identidade Digital de Competências",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = if (badges.isNotEmpty()) {
                            "O teu domínio é validado por telemetria motora e cognitiva contínua. Empresas internacionais filtram pelo teu grafo de raciocínio, sem barreiras de diplomas tradicionais."
                        } else {
                            "À medida que completas sessões de estudo, leituras e revisões ativas, o motor KCLS emite credenciais criptográficas verificáveis para o teu portfólio profissional internacional."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        }

        item {
            Text(
                text = "TIMELINE DE CERTIFICAÇÕES CRIPTOGRÁFICAS",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 0.8.sp
            )
        }

        if (badges.isEmpty()) {
            item {
                KudulaEmptyStateCard(
                    icon = Icons.Default.VerifiedUser,
                    badge = "Proof of Learning",
                    title = "Nenhuma credencial emitida ainda",
                    description = "As tuas credenciais criptográficas são emitidas automaticamente à medida que demonstras retenção duradoura no Grafo de Aprendizagem. Completa sessões de estudo para desbloquear os teus primeiros selos!",
                    testTag = "pol_empty_card"
                )
            }
        } else {
            items(badges) { badge ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("pol_card_${badge.id}"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = KudulaNavyContainer),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(BorderSubtle, BorderSubtle)))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = KudulaElectricBlue.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = badge.category.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = KudulaCyanGlow,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }

                        Text(text = badge.issueDate, style = MaterialTheme.typography.labelSmall, color = TextTertiary)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = badge.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "🎯 ${badge.verifiedMetric}",
                        style = MaterialTheme.typography.bodySmall,
                        color = KudulaSuccess,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = KudulaNavyDark,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = KudulaGold, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "HASH: ${badge.verificationHash}",
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                color = TextTertiary,
                                fontSize = 10.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
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
