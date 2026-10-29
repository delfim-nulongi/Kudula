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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
import com.example.data.security.PlanGuard
import com.example.data.security.PlanSwitchVerification
import com.example.model.KudulaPlan
import com.example.ui.components.PlanDetailsBottomSheet
import com.example.ui.theme.*
import com.example.ui.util.LocalScreenWindowInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlansScreen(
    currentPlan: KudulaPlan,
    onSelectPlan: (KudulaPlan) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val windowInfo = LocalScreenWindowInfo.current
    val isDebugMode = remember { PlanGuard.isManualSwitchAllowed() }
    var selectedPlanForDetails by remember { mutableStateOf<KudulaPlan?>(null) }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }
    var showCheckoutDialogForPlan by remember { mutableStateOf<KudulaPlan?>(null) }

    fun handlePlanSelection(plan: KudulaPlan) {
        val verification = PlanGuard.verifyPlanTransition(plan, currentPlan)
        when (verification) {
            is PlanSwitchVerification.AllowedInDebug -> {
                onSelectPlan(plan)
                snackbarMessage = "Plano ${plan.displayName} ativado com sucesso! [Debug Mode]"
            }
            is PlanSwitchVerification.RequiresCheckout -> {
                showCheckoutDialogForPlan = plan
            }
            is PlanSwitchVerification.CorporateContactRequired -> {
                snackbarMessage = "Pedido de contacto corporativo para ${plan.displayName} registado!"
            }
            is PlanSwitchVerification.AlreadyActive -> {
                snackbarMessage = "O plano ${plan.displayName} já é o teu plano ativo."
            }
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(KudulaNavyDark)
            .testTag("plans_screen"),
        containerColor = KudulaNavyDark,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Planos Kudula",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Text(
                            text = "Acelera o teu potencial cognitivo",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("plans_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = KudulaNavyDark)
            )
        },
        snackbarHost = {
            snackbarMessage?.let { msg ->
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    containerColor = KudulaNavySurface,
                    contentColor = KudulaCyanGlow,
                    action = {
                        TextButton(onClick = { snackbarMessage = null }) {
                            Text("OK", color = Color.White)
                        }
                    }
                ) {
                    Text(text = msg, fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = windowInfo.screenHorizontalPadding, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Explanation Banner
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = KudulaNavySurface),
                    border = BorderStroke(
                        1.dp,
                        Brush.horizontalGradient(listOf(KudulaElectricBlue, KudulaViolet))
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = KudulaElectricBlue.copy(alpha = 0.25f),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.WorkspacePremium,
                                    contentDescription = null,
                                    tint = KudulaCyanGlow,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Inteligência sem Bloqueios",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Escolhe o plano que melhor se alinha com os teus objetivos académicos e de carreira.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            // Cards for each plan
            items(KudulaPlan.entries) { plan ->
                val isCurrent = plan == currentPlan
                PlanCard(
                    plan = plan,
                    isCurrentPlan = isCurrent,
                    onSelect = { handlePlanSelection(plan) },
                    onViewDetails = { selectedPlanForDetails = plan }
                )
            }

            // Footer note
            item {
                Text(
                    text = "A Kudula opera com total transparência. Todos os dados permanecem confidenciais e criptografados localmente com o KLSE.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center,
                    fontSize = 11.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                )
            }
        }
    }

    // Bottom Sheet for deep details & feature matrix
    selectedPlanForDetails?.let { plan ->
        PlanDetailsBottomSheet(
            plan = plan,
            isCurrentPlan = plan == currentPlan,
            onSelectPlan = {
                onSelectPlan(it)
                snackbarMessage = if (it.isCustomQuote) {
                    "Pedido de contacto para o plano ${it.displayName} registado!"
                } else {
                    "Plano ${it.displayName} ativado com sucesso!"
                }
            },
            onDismiss = { selectedPlanForDetails = null }
        )
    }
}

@Composable
private fun PlanCard(
    plan: KudulaPlan,
    isCurrentPlan: Boolean,
    onSelect: () -> Unit,
    onViewDetails: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = when {
        isCurrentPlan -> KudulaCyanGlow
        plan == KudulaPlan.STUDENT -> KudulaElectricBlue
        plan == KudulaPlan.PLUS -> KudulaVioletLight
        plan == KudulaPlan.PRO -> KudulaGold
        else -> Color.White.copy(alpha = 0.15f)
    }

    val glowBrush = when (plan) {
        KudulaPlan.STUDENT -> Brush.horizontalGradient(listOf(KudulaElectricBlue, KudulaCyanGlow))
        KudulaPlan.PLUS -> Brush.horizontalGradient(listOf(KudulaViolet, KudulaElectricBlue))
        KudulaPlan.PRO -> Brush.horizontalGradient(listOf(KudulaGold, Color(0xFFF59E0B)))
        else -> Brush.horizontalGradient(listOf(Color(0xFF2E3D56), Color(0xFF1E293B)))
    }

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = KudulaNavySurface),
        border = BorderStroke(if (isCurrentPlan) 2.dp else 1.2.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isCurrentPlan) 8.dp else 3.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("plan_card_${plan.id}")
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Top row: Name, Badge and Current indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = plan.displayName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isCurrentPlan) KudulaSuccess.copy(alpha = 0.2f) else KudulaElectricBlue.copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, if (isCurrentPlan) KudulaSuccess.copy(alpha = 0.5f) else KudulaCyanGlow.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = if (isCurrentPlan) "🟢 O teu plano atual" else plan.badge,
                            color = if (isCurrentPlan) KudulaSuccess else KudulaCyanGlow,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                // Price display
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = plan.priceMonthly,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (plan == KudulaPlan.PRO) KudulaGold else Color.White
                    )
                    if (!plan.isCustomQuote && plan != KudulaPlan.FREE) {
                        Text(
                            text = "/mês",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = plan.description,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(14.dp))
            Divider(color = Color.White.copy(alpha = 0.08f))
            Spacer(modifier = Modifier.height(14.dp))

            // 3 Key Benefits with checkmarks
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                plan.threeKeyBenefits.forEach { benefit ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = if (isCurrentPlan) KudulaSuccess else KudulaCyanGlow,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = benefit,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action row: Primary Action button and "Ver tudo" detail trigger
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(
                    onClick = onViewDetails,
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.testTag("plan_view_details_${plan.id}")
                ) {
                    Text(
                        text = "Ver tudo",
                        color = KudulaCyanGlow,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = KudulaCyanGlow,
                        modifier = Modifier.size(14.dp)
                    )
                }

                Button(
                    onClick = onSelect,
                    enabled = !isCurrentPlan,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = when {
                            plan == KudulaPlan.PRO -> KudulaGold
                            plan.isCustomQuote -> KudulaViolet
                            else -> KudulaElectricBlue
                        },
                        disabledContainerColor = KudulaNavyContainer.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
                    modifier = Modifier.testTag("plan_action_${plan.id}")
                ) {
                    Text(
                        text = when {
                            isCurrentPlan -> "Ativo"
                            plan.isCustomQuote -> if (plan == KudulaPlan.BUSINESS) "Falar connosco" else "Contactar equipa"
                            else -> "Desbloquear"
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = when {
                            isCurrentPlan -> Color.White.copy(alpha = 0.5f)
                            plan == KudulaPlan.PRO -> KudulaNavyDark
                            else -> Color.White
                        }
                    )
                }
            }
        }
    }
}
