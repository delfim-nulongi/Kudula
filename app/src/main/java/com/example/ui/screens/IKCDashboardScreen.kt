package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.UserCognitiveProfile
import com.example.ui.components.IPAReadinessCard
import com.example.ui.components.RadialIKCChart
import com.example.ui.theme.*
import com.example.ui.util.LocalScreenWindowInfo

@Composable
fun IKCDashboardScreen(
    profile: UserCognitiveProfile,
    modifier: Modifier = Modifier
) {
    val windowInfo = LocalScreenWindowInfo.current
    val isNarrow = windowInfo.isNarrowCompact

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(KudulaNavyDark)
            .padding(horizontal = windowInfo.screenHorizontalPadding, vertical = 12.dp)
            .testTag("ikc_dashboard_screen"),
        verticalArrangement = Arrangement.spacedBy(if (isNarrow) 14.dp else 18.dp)
    ) {
        // Title
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(if (isNarrow) 34.dp else 38.dp)
                        .clip(CircleShape)
                        .background(KudulaCyanGlow.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Insights, contentDescription = null, tint = KudulaCyanGlow, modifier = Modifier.size(if (isNarrow) 18.dp else 22.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Índice Kudula de Compreensão",
                        style = if (isNarrow) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Text(
                        text = "IKC • Modelo Heptadimensional",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        fontSize = if (isNarrow) 11.sp else 12.sp
                    )
                }
            }
        }

        // Radar Visualizer Card
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = KudulaNavySurface),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.horizontalGradient(listOf(KudulaElectricBlue, KudulaViolet))
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "MAPA RADIAL DE 7 DIMENSÕES",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = KudulaCyanGlow,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    RadialIKCChart(ikc = profile.ikc)
                }
            }
        }

        // Telemetry Comparison Card (Chute vs Maestria Real)
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = KudulaNavyContainer),
                modifier = Modifier.fillMaxWidth().testTag("telemetry_metrics_card")
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Fingerprint, contentDescription = null, tint = KudulaVioletLight, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "MICROTELEMETRIA COMPORTAMENTAL",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = KudulaVioletLight,
                            letterSpacing = 0.5.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Diferenciação matemática entre chute superficial e domínio neural efetivo.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        TelemetryStatBox(
                            title = "Taxa de Domínio Real",
                            value = "${profile.authenticMasteryRate}%",
                            subtitle = "Fluidez e sem hesitação",
                            color = KudulaSuccess,
                            modifier = Modifier.weight(1f)
                        )
                        TelemetryStatBox(
                            title = "Bloqueios Evitados",
                            value = "${profile.panicFreezesAvoided}",
                            subtitle = "Pistas do KDAP em tempo hábil",
                            color = KudulaCyanGlow,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Learning Readiness Index (IPA)
        item {
            IPAReadinessCard(ipa = profile.ipa)
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun TelemetryStatBox(
    title: String,
    value: String,
    subtitle: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = KudulaNavySurface,
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = title, style = MaterialTheme.typography.labelSmall, color = TextTertiary, fontSize = 11.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = color)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = TextSecondary, fontSize = 10.sp)
        }
    }
}
