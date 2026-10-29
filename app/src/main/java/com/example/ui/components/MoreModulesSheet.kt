package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.viewmodel.KudulaScreen

data class ModuleItem(
    val screen: KudulaScreen,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val accent: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreModulesSheet(
    onSelectScreen: (KudulaScreen) -> Unit,
    onDismiss: () -> Unit
) {
    val modules = listOf(
        ModuleItem(KudulaScreen.HOME, "Início", "Visão geral", Icons.Default.Home, KudulaElectricBlue),
        ModuleItem(KudulaScreen.USER_PROFILE, "O Teu Perfil", "Conta Firebase", Icons.Default.AccountCircle, KudulaCyanGlow),
        ModuleItem(KudulaScreen.TIMELINE, "Timeline KLSE", "Histórico Cognitivo", Icons.Default.Timeline, KudulaCyanGlow),
        ModuleItem(KudulaScreen.REFLECTION_JOURNAL, "Diário Reflexivo", "Metacognição", Icons.Default.EditNote, KudulaVioletLight),
        ModuleItem(KudulaScreen.LIBRARY, "Biblioteca", "Knowledge Vault", Icons.Default.LocalLibrary, KudulaCyanGlow),
        ModuleItem(KudulaScreen.AI_TUTOR, "Tutor Kudu", "Diálogo Socrático", Icons.Default.AutoAwesome, KudulaVioletLight),
        ModuleItem(KudulaScreen.VOICE_NOTES, "Voice Notes", "Perguntas Orais (60s)", Icons.Default.Mic, KudulaCyanGlow),
        ModuleItem(KudulaScreen.YOUTUBE_INTELLIGENCE, "YouTube Intel", "Mapas & Resumos", Icons.Default.SmartDisplay, Color(0xFFEF4444)),
        ModuleItem(KudulaScreen.READING_WORKSPACE, "Leitura Ativa", "Destaques & IA", Icons.Default.MenuBook, KudulaTealLight),
        ModuleItem(KudulaScreen.SMART_REVISION, "Smart Revisão", "Repetição Espaçada", Icons.Default.Bolt, KudulaGold),
        ModuleItem(KudulaScreen.IKC_DASHBOARD, "Radar IKC", "7 Dimensões", Icons.Default.Analytics, KudulaCyanGlow),
        ModuleItem(KudulaScreen.CAREER_HUB, "MICK Carreiras", "Skills Gap", Icons.Default.Work, KudulaElectricBlue),
        ModuleItem(KudulaScreen.FUTURE_SELF, "Eu do Futuro", "Simulação Temporal", Icons.Default.AutoFixHigh, KudulaViolet),
        ModuleItem(KudulaScreen.PROOF_OF_LEARNING, "Proof of Learn", "SHA-256 Verificado", Icons.Default.VerifiedUser, KudulaGold),
        ModuleItem(KudulaScreen.COMMUNITY, "Comunidade", "Mentoria & Grupos", Icons.Default.Groups, KudulaSuccess),
        ModuleItem(KudulaScreen.ONBOARDING, "Onboarding", "Recalibrar Perfil", Icons.Default.Tune, KudulaElectricBlueLight),
        ModuleItem(KudulaScreen.PLANS, "Planos Kudula", "Assinatura & Upgrades", Icons.Default.WorkspacePremium, KudulaGold),
        ModuleItem(KudulaScreen.SETTINGS, "Definições", "Acessibilidade & IA", Icons.Default.Settings, TextSecondary)
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = KudulaNavySurface,
        scrimColor = Color.Black.copy(alpha = 0.65f),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp)
                .testTag("more_modules_sheet")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Arquitetura Kudula • KCLS",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Text(
                        text = "Navega diretamente pelos módulos do Sistema Operacional Cognitivo",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Fechar", tint = TextTertiary)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(modules) { module ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .clickable {
                                onSelectScreen(module.screen)
                                onDismiss()
                            }
                            .testTag("module_tile_${module.screen.route}"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = KudulaNavyContainer),
                        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(BorderSubtle, BorderSubtle)))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(module.accent.copy(alpha = 0.18f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = module.icon,
                                    contentDescription = module.title,
                                    tint = module.accent,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = module.title,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                textAlign = TextAlign.Center,
                                maxLines = 1
                            )
                            Text(
                                text = module.subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 9.sp,
                                color = TextTertiary,
                                textAlign = TextAlign.Center,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}
