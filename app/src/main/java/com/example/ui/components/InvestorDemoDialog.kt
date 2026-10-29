package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.window.Dialog
import com.example.model.AiLoadingState
import com.example.model.MascotMood
import com.example.ui.theme.*

@Composable
fun InvestorDemoDialog(
    currentStep: Int,
    loadingState: AiLoadingState,
    onStepClick: (Int) -> Unit,
    onRunStep: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("investor_demo_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = KudulaNavyDark),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.linearGradient(listOf(KudulaCyanGlow, KudulaElectricBlue, KudulaViolet))
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = KudulaElectricBlue.copy(alpha = 0.3f),
                            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(KudulaCyanGlow, KudulaElectricBlue)))
                        ) {
                            Box(modifier = Modifier.size(36.dp), contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = KudulaCyanGlow,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "INVESTOR DEMO MODE",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = KudulaGold.copy(alpha = 0.25f)
                                ) {
                                    Text(
                                        text = "MVP SIMULATOR",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = KudulaGold,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Demonstração executiva do loop cognitivo completo",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Fechar", tint = TextTertiary)
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                if (loadingState.isProcessing) {
                    IntelligentAiLoadingCard(
                        phaseLabel = loadingState.phase.label,
                        phaseDetail = loadingState.contextualMessage.ifBlank { loadingState.phase.detail },
                        progress = loadingState.progress
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // 4-Step Interactive Pipeline
                val steps = listOf(
                    Triple(
                        "1. Ingestão KCLS Instantânea",
                        "Extrai conceitos de 42 páginas em grafo semântico & analogias urbanas",
                        Icons.Default.DocumentScanner
                    ),
                    Triple(
                        "2. Salto Heptadimensional IKC",
                        "Simula microtelemetria motora: +4% Compreensão, +150 XP sem hesitação",
                        Icons.Default.Analytics
                    ),
                    Triple(
                        "3. Salto Temporal: Eu do Futuro",
                        "Projeção algorítmica a 6 meses: desbloqueio de Arquiteto de IA",
                        Icons.Default.Visibility
                    ),
                    Triple(
                        "4. Cunhagem de Proof of Learning",
                        "Emite credencial verificável SHA-256 pronta para exportar",
                        Icons.Default.Verified
                    )
                )

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    steps.forEachIndexed { idx, (title, desc, icon) ->
                        val isCurrent = idx == currentStep
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onStepClick(idx)
                                    onRunStep(idx)
                                },
                            shape = RoundedCornerShape(14.dp),
                            color = if (isCurrent) KudulaElectricBlue.copy(alpha = 0.25f) else KudulaNavyContainer,
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = if (isCurrent) {
                                    Brush.horizontalGradient(listOf(KudulaCyanGlow, KudulaElectricBlue))
                                } else {
                                    Brush.linearGradient(listOf(BorderSubtle, BorderSubtle))
                                }
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(if (isCurrent) KudulaElectricBlue else KudulaNavySurface),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = if (isCurrent) Color.White else KudulaCyanGlow,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = title,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isCurrent) KudulaCyanGlow else TextPrimary
                                    )
                                    Text(
                                        text = desc,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary,
                                        fontSize = 11.sp
                                    )
                                }

                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Executar",
                                    tint = if (isCurrent) KudulaGold else TextTertiary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Run Full Demo Button
                Button(
                    onClick = { onRunStep(currentStep) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("run_demo_step_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = KudulaElectricBlue),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.FlashOn, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Executar Etapa ${currentStep + 1} de 4",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
