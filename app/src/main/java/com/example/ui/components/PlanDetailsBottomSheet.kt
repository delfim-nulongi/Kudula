package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
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
import com.example.model.KudulaPlan
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanDetailsBottomSheet(
    plan: KudulaPlan,
    isCurrentPlan: Boolean,
    onSelectPlan: (KudulaPlan) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = KudulaNavyDark,
        dragHandle = {
            BottomSheetDefaults.DragHandle(color = KudulaCyanGlow.copy(alpha = 0.5f))
        },
        modifier = modifier.testTag("plan_details_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = plan.displayName,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = KudulaElectricBlue.copy(alpha = 0.25f),
                            border = BorderStroke(1.dp, KudulaCyanGlow.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = plan.badge,
                                color = KudulaCyanGlow,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Text(
                        text = if (plan.isCustomQuote) "Sob consulta" else "${plan.priceMonthly}/mês (${plan.priceAnnual})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = KudulaGold
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_plan_details_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Fechar",
                        tint = Color.White.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = plan.description,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Quota Matrix / Overview Card
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = KudulaNavySurface,
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "CAPACIDADES E QUOTAS DE IA",
                        style = MaterialTheme.typography.labelSmall,
                        color = KudulaCyanGlow,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val quotas = when (plan) {
                        KudulaPlan.FREE -> listOf(
                            "IA Diária" to "20 interações socráticas",
                            "Uploads / Mês" to "3 PDFs ou vídeos",
                            "Voice Notes" to "Até 30s por gravação",
                            "Sincronização" to "Local (KLSE Offline)",
                            "Biblioteca" to "Armazenamento padrão"
                        )
                        KudulaPlan.STUDENT -> listOf(
                            "IA Diária" to "200 interações / sem fila",
                            "Uploads / Mês" to "30 PDFs e vídeos",
                            "Voice Notes" to "Até 60s com áudio KDAP",
                            "Sincronização" to "Nuvem + Multi-dispositivo",
                            "Biblioteca" to "Podcasts de Estudo IA"
                        )
                        KudulaPlan.PLUS -> listOf(
                            "IA Diária" to "500 interações / alta prioridade",
                            "Uploads / Mês" to "100 documentos e vídeos",
                            "Voice Notes" to "Ilimitadas com síntese",
                            "Sincronização" to "Nuvem Instantânea",
                            "Biblioteca" to "Career Hub & Future Self"
                        )
                        KudulaPlan.PRO -> listOf(
                            "IA Diária" to "Ilimitada / Ultra Rápida",
                            "Uploads / Mês" to "Ilimitados",
                            "Voice Notes" to "Career Twin treinado",
                            "Sincronização" to "Sincronização Criptográfica",
                            "Biblioteca" to "Proof of Learning & Skills DNA"
                        )
                        KudulaPlan.BUSINESS -> listOf(
                            "Colaboradores" to "Licenças corporativas",
                            "Dashboards" to "Gestão de upskilling",
                            "Segurança" to "Conformidade empresarial",
                            "Suporte" to "SLA 24/7 com onboarding",
                            "API" to "Acesso a endpoints NestJS"
                        )
                        KudulaPlan.INSTITUTION -> listOf(
                            "Campus" to "Estudantes e docentes",
                            "LMS" to "Integração Moodle / Canvas",
                            "Acreditação" to "Certificação oficial",
                            "Auditoria" to "Métricas ministeriais",
                            "Engenharia" to "Engenheiro dedicado"
                        )
                    }

                    quotas.forEach { (label, value) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                            Text(
                                text = value,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "TUDO O QUE ESTÁ INCLUÍDO",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 240.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(plan.fullDetails) { benefit ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = KudulaElectricBlue.copy(alpha = 0.2f),
                            modifier = Modifier.size(20.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = KudulaCyanGlow,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = benefit,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.9f),
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // CTA Button
            Button(
                onClick = {
                    onSelectPlan(plan)
                    onDismiss()
                },
                enabled = !isCurrentPlan,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isCurrentPlan) KudulaNavyContainer else KudulaElectricBlue,
                    disabledContainerColor = KudulaNavyContainer.copy(alpha = 0.6f)
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("modal_action_plan_button")
            ) {
                Text(
                    text = when {
                        isCurrentPlan -> "Plano Atual Ativo"
                        plan.isCustomQuote -> if (plan == KudulaPlan.BUSINESS) "Falar connosco" else "Contactar equipa"
                        else -> "Desbloquear ${plan.displayName}"
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = if (isCurrentPlan) Color.White.copy(alpha = 0.5f) else Color.White
                )
            }
        }
    }
}
