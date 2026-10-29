package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.auth.AuthState
import com.example.data.plan.PlanManager
import com.example.model.KudulaPlan
import com.example.model.UserCognitiveProfile
import com.example.ui.components.AnimatedNeuralLiquidBackground
import com.example.ui.components.KudulaOfficialLogo
import com.example.ui.theme.*
import com.example.ui.util.LocalScreenWindowInfo
import com.example.viewmodel.AuthUiState
import com.example.viewmodel.AuthViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * User Profile Screen (F0.6)
 * Integrada com o Firebase Authentication para gestão completa da conta:
 * - Informações de conta (Nome, Email, UID, Provedor de Login)
 * - Edição de nome de apresentação
 * - Redefinição de senha
 * - Vinculação de conta Google
 * - Resumo do perfil cognitivo (IKC, Área, MBTI, Kudu)
 * - Terminar sessão (Sign Out)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    profile: UserCognitiveProfile,
    authState: AuthUiState,
    authViewModel: AuthViewModel,
    onUpdateName: (String) -> Unit,
    onContinueToOnboarding: () -> Unit = {},
    onRecalibrateProfile: () -> Unit,
    onNavigateBack: () -> Unit,
    onSignOutSuccess: () -> Unit,
    onNavigateToPlans: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val windowInfo = LocalScreenWindowInfo.current
    val isNarrow = windowInfo.isNarrowCompact

    val planManager = remember { PlanManager.getInstance() }
    val currentPlan by planManager.currentPlan.collectAsStateWithLifecycle()

    var showEditNameDialog by remember { mutableStateOf(false) }
    var editNameInput by remember { mutableStateOf(profile.name) }

    var showSignOutConfirmDialog by remember { mutableStateOf(false) }
    var showResetPasswordDialog by remember { mutableStateOf(false) }

    // User profile photo picker support
    var selectedPhotoUri by remember { mutableStateOf<Uri?>(null) }
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedPhotoUri = uri
        }
    }

    val currentAuth = authState.authState
    val displayName = if (currentAuth is AuthState.Authenticated) {
        currentAuth.displayName ?: profile.name
    } else profile.name

    val userEmail = if (currentAuth is AuthState.Authenticated) {
        currentAuth.email ?: "${profile.name.lowercase().replace(" ", ".")}@kudula.ai"
    } else "Sessão Local (Não Vinculada)"

    val authProvider = if (currentAuth is AuthState.Authenticated) {
        if (currentAuth.email?.endsWith("@gmail.com") == true) "Google Account" else "Email & Palavra-passe"
    } else "Sessão Anónima Offline"

    val userUid = if (currentAuth is AuthState.Authenticated) currentAuth.uid else "kudula-local-device"

    BackHandler {
        onNavigateBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Perfil do Utilizador",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
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
        containerColor = KudulaNavyDark,
        modifier = modifier.testTag("user_profile_screen")
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = windowInfo.screenHorizontalPadding, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header: Avatar & Main Identity Card with animated neural network and liquid glass background
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF09101C)),
                    border = BorderStroke(
                        1.5.dp,
                        Brush.linearGradient(
                            listOf(
                                KudulaCyanGlow.copy(alpha = 0.70f),
                                Color(0xFF10B981).copy(alpha = 0.60f),
                                KudulaElectricBlue.copy(alpha = 0.65f)
                            )
                        )
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .testTag("profile_identity_card")
                ) {
                    AnimatedNeuralLiquidBackground(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                        // Avatar Badge with Photo Support
                        Box(contentAlignment = Alignment.BottomEnd) {
                            Box(
                                modifier = Modifier
                                    .size(92.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.radialGradient(listOf(KudulaElectricBlue, KudulaNavyContainer))
                                    )
                                    .border(2.5.dp, KudulaCyanGlow, CircleShape)
                                    .clickable {
                                        photoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    }
                                    .testTag("profile_avatar_tap_area"),
                                contentAlignment = Alignment.Center
                            ) {
                                if (selectedPhotoUri != null) {
                                    AsyncImage(
                                        model = selectedPhotoUri,
                                        contentDescription = "Foto de Perfil",
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Text(
                                        text = displayName.take(2).uppercase(),
                                        fontWeight = FontWeight.Black,
                                        fontSize = 30.sp,
                                        color = Color.White
                                    )
                                }
                            }

                            // Camera / Add photo badge action button
                            Surface(
                                shape = CircleShape,
                                color = KudulaCyanGlow,
                                border = BorderStroke(2.dp, Color(0xFF1E232B)),
                                modifier = Modifier
                                    .size(32.dp)
                                    .clickable {
                                        photoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    }
                                    .testTag("add_profile_photo_button")
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.AddAPhoto,
                                        contentDescription = "Adicionar Foto de Perfil",
                                        tint = KudulaNavyDark,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Text affordance to easily add/change photo
                        TextButton(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(26.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = null,
                                tint = KudulaCyanGlow,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (selectedPhotoUri != null) "Alterar foto" else "Adicionar foto",
                                color = KudulaCyanGlow,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Display name with edit action
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = displayName,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            IconButton(
                                onClick = {
                                    editNameInput = displayName
                                    showEditNameDialog = true
                                },
                                modifier = Modifier.size(26.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Editar Nome",
                                    tint = Color.White.copy(alpha = 0.7f),
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }

                        Text(
                            text = userEmail,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Provider Badge & Verification status
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = KudulaElectricBlue.copy(alpha = 0.25f),
                                border = BorderStroke(1.dp, KudulaCyanGlow.copy(alpha = 0.4f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = "🛡️", fontSize = 11.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = authProvider,
                                        color = KudulaCyanGlow,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = KudulaSuccess.copy(alpha = 0.20f),
                                border = BorderStroke(1.dp, KudulaSuccess.copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = "✓", color = KudulaSuccess, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Verificado",
                                        color = KudulaSuccess,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Entrada 1 — Perfil: Cartão do Plano Atual
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = KudulaNavyContainer.copy(alpha = 0.9f),
                            border = BorderStroke(1.2.dp, KudulaCyanGlow.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNavigateToPlans() }
                                .testTag("profile_plan_card")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "Plano Atual",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextSecondary,
                                        fontSize = 11.sp
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = "🟢", fontSize = 11.sp)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = currentPlan.displayName,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Gerir Plano",
                                        color = KudulaCyanGlow,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = null,
                                        tint = KudulaCyanGlow,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

            // Fluxo de Onboarding Cognitivo (Exibido apenas quando pendente)
            if (!profile.onboardingCompleted) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = KudulaElectricBlue.copy(alpha = 0.15f)),
                        border = BorderStroke(1.dp, KudulaCyanGlow.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth().testTag("onboarding_mandatory_flow_banner")
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "🧠", fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Perfil Cognitivo Pendente",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = KudulaCyanGlow,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Ainda não configuraste o teu perfil cognitivo. Configura agora para calibrar o Kudu.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = onContinueToOnboarding,
                        colors = ButtonDefaults.buttonColors(containerColor = KudulaElectricBlue),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("continue_to_onboarding_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Configurar Perfil Cognitivo",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = if (isNarrow) 14.sp else 15.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Configurar",
                                tint = Color.White
                            )
                        }
                    }
                }
            }

            // Section 1: Gestão da Tua Conta
            item {
                Text(
                    text = "Gestão da Tua Conta",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = KudulaNavySurface),
                    border = BorderStroke(1.dp, BorderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        // Action 1: Alterar Nome
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    editNameInput = displayName
                                    showEditNameDialog = true
                                }
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Badge, contentDescription = null, tint = KudulaCyanGlow, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("Nome de Apresentação", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                    Text(displayName, color = TextSecondary, fontSize = 12.sp)
                                }
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextTertiary)
                        }

                        HorizontalDivider(color = BorderSubtle.copy(alpha = 0.4f))

                        // Action 2: Redefinir Senha
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { showResetPasswordDialog = true }
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LockReset, contentDescription = null, tint = KudulaGold, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("Palavra-passe / Redefinição", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                    Text("Enviar email de redefinição de senha", color = TextSecondary, fontSize = 12.sp)
                                }
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextTertiary)
                        }

                        HorizontalDivider(color = BorderSubtle.copy(alpha = 0.4f))

                        // Action 3: Sincronização e UID
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CloudSync, contentDescription = null, tint = KudulaElectricBlue, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("Identificador Único (UID)", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                    Text(userUid.take(16) + "...", color = TextTertiary, fontSize = 11.sp)
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = KudulaCyanGlow.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "KLSE Ativo",
                                    color = KudulaCyanGlow,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Section 2: Perfil Cognitivo Kudula
            item {
                Text(
                    text = "Resumo do Perfil Cognitivo",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = KudulaNavySurface),
                    border = BorderStroke(1.dp, BorderSubtle),
                    modifier = Modifier.fillMaxWidth().testTag("cognitive_profile_summary_card")
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Estado:", color = TextSecondary, fontSize = 13.sp)
                            Text(
                                text = if (profile.onboardingCompleted) "Configurado" else "Ainda não configurado",
                                color = if (profile.onboardingCompleted) KudulaSuccess else KudulaGold,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Área Académica:", color = TextSecondary, fontSize = 13.sp)
                            Text(profile.academicArea.ifBlank { "—" }, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Área Profissional:", color = TextSecondary, fontSize = 13.sp)
                            Text(profile.professionalArea.ifBlank { "—" }, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Objetivo Principal:", color = TextSecondary, fontSize = 13.sp)
                            Text(profile.primaryGoal.ifBlank { "—" }, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("MBTI:", color = TextSecondary, fontSize = 13.sp)
                            Text(profile.mbti?.ifBlank { "—" } ?: "—", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("RIASEC:", color = TextSecondary, fontSize = 13.sp)
                            Text(profile.riasec?.ifBlank { "—" } ?: "—", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Inteligências:", color = TextSecondary, fontSize = 13.sp)
                            Text(
                                text = if (profile.multipleIntelligences.isEmpty()) "Ainda não avaliadas" else profile.multipleIntelligences.joinToString(", "),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("IKC Global:", color = TextSecondary, fontSize = 13.sp)
                            Text(
                                text = if (profile.ikc.hasData) "${profile.ikc.overallAverage} / 100" else "— (Sem dados de atividade)",
                                color = if (profile.ikc.hasData) KudulaSuccess else TextTertiary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("IPA Global:", color = TextSecondary, fontSize = 13.sp)
                            Text(
                                text = if (profile.ipa.hasData) "${profile.ipa.score} / 100" else "— (Sem dados de atividade)",
                                color = if (profile.ipa.hasData) KudulaCyanGlow else TextTertiary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("XP de Atividade:", color = TextSecondary, fontSize = 13.sp)
                            Text("${profile.xp} XP", color = KudulaGold, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Sequência (Streak):", color = TextSecondary, fontSize = 13.sp)
                            Text("${profile.streakDays} dias", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Button(
                            onClick = onRecalibrateProfile,
                            colors = ButtonDefaults.buttonColors(containerColor = KudulaElectricBlue.copy(alpha = 0.3f)),
                            border = BorderStroke(1.dp, KudulaCyanGlow.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("recalibrate_profile_button")
                        ) {
                            Icon(Icons.Default.Tune, contentDescription = null, tint = KudulaCyanGlow, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (profile.onboardingCompleted) "Atualizar Perfil Cognitivo" else "Configurar Perfil Cognitivo",
                                color = KudulaCyanGlow,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Section 3: Sessão & Segurança
            item {
                Text(
                    text = "Sessão & Segurança",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = KudulaNavySurface),
                    border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Podes terminar a sessão a qualquer momento. Os teus dados de estudo offline permanecem salvos em Room.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )

                        Button(
                            onClick = { showSignOutConfirmDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444).copy(alpha = 0.2f)),
                            border = BorderStroke(1.dp, Color(0xFFEF4444)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("profile_sign_out_button")
                        ) {
                            Icon(Icons.Default.Logout, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Terminar Sessão", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            item {
                OutlinedButton(
                    onClick = onNavigateBack,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    border = BorderStroke(1.dp, KudulaCyanGlow.copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("profile_return_home_button")
                ) {
                    Icon(Icons.Default.Home, contentDescription = null, tint = KudulaCyanGlow, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Voltar para o Menu Principal (Home)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Dialog: Editar Nome
    if (showEditNameDialog) {
        AlertDialog(
            onDismissRequest = { showEditNameDialog = false },
            containerColor = KudulaNavySurface,
            title = { Text("Editar Nome", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Atualiza o nome exibido na tua conta Kudula:", color = TextSecondary, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = editNameInput,
                        onValueChange = { editNameInput = it },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = KudulaCyanGlow,
                            unfocusedBorderColor = BorderSubtle,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val trimmed = editNameInput.trim()
                        if (trimmed.isNotBlank()) {
                            authViewModel.updateDisplayName(trimmed)
                            onUpdateName(trimmed)
                        }
                        showEditNameDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = KudulaElectricBlue)
                ) {
                    Text("Guardar", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditNameDialog = false }) {
                    Text("Cancelar", color = TextSecondary)
                }
            }
        )
    }

    // Dialog: Confirmar Redefinição de Palavra-passe
    if (showResetPasswordDialog) {
        AlertDialog(
            onDismissRequest = { showResetPasswordDialog = false },
            containerColor = KudulaNavySurface,
            title = { Text("Redefinir Palavra-passe", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = "Desejas enviar um link de recuperação para o email $userEmail?",
                    color = TextSecondary,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        authViewModel.sendPasswordReset(userEmail)
                        showResetPasswordDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = KudulaGold)
                ) {
                    Text("Enviar Link", color = KudulaNavyDark, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetPasswordDialog = false }) {
                    Text("Cancelar", color = TextSecondary)
                }
            }
        )
    }

    // Dialog: Confirmar Terminar Sessão
    if (showSignOutConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutConfirmDialog = false },
            containerColor = KudulaNavySurface,
            title = { Text("Terminar Sessão?", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = "Tens a certeza de que queres sair? O teu progresso fica seguro no dispositivo e na nuvem.",
                    color = TextSecondary,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSignOutConfirmDialog = false
                        authViewModel.signOut()
                        onSignOutSuccess()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Sim, Sair", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutConfirmDialog = false }) {
                    Text("Cancelar", color = TextSecondary)
                }
            }
        )
    }
}
