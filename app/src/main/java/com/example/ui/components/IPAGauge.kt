package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.IPAScore
import com.example.ui.theme.*

@Composable
fun IPAReadinessCard(
    ipa: IPAScore,
    modifier: Modifier = Modifier
) {
    val barColor = when {
        ipa.score >= 80 -> KudulaSuccess
        ipa.score >= 60 -> KudulaCyanGlow
        ipa.score >= 40 -> KudulaWarning
        else -> KudulaError
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("ipa_readiness_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = KudulaNavySurface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(listOf(barColor.copy(alpha = 0.5f), BorderSubtle))
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(barColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = "Prontidão",
                            tint = barColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "ÍNDICE DE PRONTIDÃO (IPA)",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = if (ipa.hasData) "${ipa.score} / 100 • Prontidão Alta" else "— / 100 • A calibrar",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = barColor.copy(alpha = 0.18f)
                ) {
                    Text(
                        text = if (ipa.hasData) "${ipa.score}%" else "—",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (ipa.hasData) barColor else TextTertiary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(KudulaNavyContainer)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction = if (ipa.hasData) (ipa.score / 100f).coerceIn(0f, 1f) else 0f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(KudulaElectricBlue, barColor)
                            )
                        )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MetricPill(label = "Carga Cognitiva", value = if (ipa.hasData && ipa.cognitiveLoad.isNotBlank()) ipa.cognitiveLoad else "—", color = KudulaSuccess)
                MetricPill(label = "Fadiga Estimada", value = if (ipa.hasData && ipa.fatigueLevel.isNotBlank()) ipa.fatigueLevel else "—", color = KudulaCyanGlow)
                MetricPill(label = "Atenção Neural", value = if (ipa.hasData) "Foco Pleno" else "A iniciar", color = KudulaElectricBlueLight)
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (ipa.hasData && ipa.recommendations.isNotBlank()) "💡 ${ipa.recommendations}" else "💡 Completa a primeira sessão de estudo para o Kudu calibrar o teu IPA.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun MetricPill(label: String, value: String, color: Color) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextTertiary,
            fontSize = 10.sp
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = color,
            fontSize = 12.sp
        )
    }
}
