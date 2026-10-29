package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.KudulaOfficialLogo
import com.example.ui.theme.*
import com.example.ui.util.LocalScreenWindowInfo
import com.example.viewmodel.AuthUiState
import com.example.viewmodel.AuthViewModel

enum class AuthMode {
    LOGIN,
    SIGNUP,
    RECOVER
}

/**
 * Tela Dedicada de Autenticação (F0.6 — Conforme imagem anexa 3)
 * Inclui:
 * 1. Login com Email + Senha
 * 2. Criar Conta
 * 3. Recuperar Credenciais
 * 4. Google Sign-In integrado
 * 5. Checkbox "Lembrar Senha"
 */
@Composable
fun AuthLoginScreen(
    authViewModel: AuthViewModel,
    authState: AuthUiState,
    onAuthSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val windowInfo = LocalScreenWindowInfo.current
    val isNarrow = windowInfo.isNarrowCompact
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    var mode by remember { mutableStateOf(AuthMode.LOGIN) }

    var nameInput by remember { mutableStateOf("") }
    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(authState.rememberCredentials) }

    val cardBackground = KudulaNavySurface
    val textPrimary = Color.White
    val textMuted = Color.White.copy(alpha = 0.75f)
    val inputContainer = Color(0xFF090E1A)
    val inputBorder = Color(0xFF2E3D56)
    val primaryAccent = KudulaElectricBlue

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(KudulaNavyDark, Color(0xFF131D38), KudulaNavyDark)
                )
            )
            .testTag("auth_login_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = windowInfo.screenHorizontalPadding, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // Kudula Branding Header
            KudulaOfficialLogo(
                size = if (isNarrow) 70.dp else 84.dp
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Dark Blue Card (harmonized with Explanatory Onboarding)
            Card(
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = cardBackground),
                border = BorderStroke(
                    1.5.dp,
                    Brush.verticalGradient(
                        listOf(
                            KudulaCyanGlow.copy(alpha = 0.60f),
                            KudulaElectricBlue.copy(alpha = 0.35f)
                        )
                    )
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
                modifier = Modifier
                    .fillMaxWidth(if (isNarrow) 0.96f else 0.88f)
                    .testTag("auth_white_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 28.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    // Segmented Tabs: Entrar / Criar Conta
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF090E1A))
                            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(12.dp))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Surface(
                            onClick = {
                                mode = AuthMode.LOGIN
                                authViewModel.clearMessages()
                            },
                            shape = RoundedCornerShape(10.dp),
                            color = if (mode == AuthMode.LOGIN) primaryAccent else Color.Transparent,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Entrar",
                                color = if (mode == AuthMode.LOGIN) Color.White else textMuted,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }

                        Surface(
                            onClick = {
                                mode = AuthMode.SIGNUP
                                authViewModel.clearMessages()
                            },
                            shape = RoundedCornerShape(10.dp),
                            color = if (mode == AuthMode.SIGNUP) primaryAccent else Color.Transparent,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Criar Conta",
                                color = if (mode == AuthMode.SIGNUP) Color.White else textMuted,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Title Header
                    Text(
                        text = when (mode) {
                            AuthMode.LOGIN -> "Entrar"
                            AuthMode.SIGNUP -> "Criar Conta"
                            AuthMode.RECOVER -> "Recuperar Credenciais"
                        },
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        ),
                        color = textPrimary
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Inputs according to mode
                    if (mode == AuthMode.SIGNUP) {
                        Text(
                            text = "Nome Completo",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium,
                            color = textMuted
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = nameInput,
                            onValueChange = { nameInput = it },
                            placeholder = { Text("Ex: Glória Dumba", color = Color.White.copy(alpha = 0.40f)) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = KudulaCyanGlow,
                                unfocusedBorderColor = inputBorder,
                                focusedTextColor = textPrimary,
                                unfocusedTextColor = textPrimary,
                                focusedContainerColor = inputContainer,
                                unfocusedContainerColor = inputContainer
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("auth_name_field")
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    // Email / Identificador Field
                    Text(
                        text = if (mode == AuthMode.LOGIN) "Nome ou Email" else "Email",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = textMuted
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = { emailInput = it },
                        placeholder = {
                            Text(
                                if (mode == AuthMode.LOGIN) "gloria@gmail.com ou Glória" else "teu.email@exemplo.com",
                                color = Color.White.copy(alpha = 0.40f)
                            )
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = KudulaCyanGlow,
                            unfocusedBorderColor = inputBorder,
                            focusedTextColor = textPrimary,
                            unfocusedTextColor = textPrimary,
                            focusedContainerColor = inputContainer,
                            unfocusedContainerColor = inputContainer
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("auth_email_field")
                    )

                    // Password Field (only for Login and Signup)
                    if (mode != AuthMode.RECOVER) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Password",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium,
                            color = textMuted
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = passwordInput,
                            onValueChange = { passwordInput = it },
                            placeholder = { Text("••••••••", color = Color.White.copy(alpha = 0.40f)) },
                            singleLine = true,
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = "Alternar visibilidade",
                                        tint = textMuted
                                    )
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = KudulaCyanGlow,
                                unfocusedBorderColor = inputBorder,
                                focusedTextColor = textPrimary,
                                unfocusedTextColor = textPrimary,
                                focusedContainerColor = inputContainer,
                                unfocusedContainerColor = inputContainer
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("auth_password_field")
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Row: Lembrar Senha & Recuperar Credencias (attachment 3 style)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable {
                                    rememberMe = !rememberMe
                                    authViewModel.setRememberCredentials(rememberMe)
                                }
                            ) {
                                Checkbox(
                                    checked = rememberMe,
                                    onCheckedChange = {
                                        rememberMe = it
                                        authViewModel.setRememberCredentials(it)
                                    },
                                    colors = CheckboxDefaults.colors(checkedColor = primaryAccent),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Lembrar Senha",
                                    fontSize = 12.sp,
                                    color = textMuted,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Text(
                                text = "Recuperar Credenciais",
                                fontSize = 12.sp,
                                color = KudulaCyanGlow,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.clickable {
                                    mode = AuthMode.RECOVER
                                    authViewModel.clearMessages()
                                }
                            )
                        }
                    }

                    // Status Messages (Error / Success)
                    authState.errorMessage?.let { error ->
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFFEE2E2),
                            border = BorderStroke(1.dp, Color(0xFFFCA5A5)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = error,
                                color = Color(0xFFDC2626),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }

                    authState.successMessage?.let { success ->
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFDCFCE7),
                            border = BorderStroke(1.dp, Color(0xFF86EFAC)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = success,
                                color = Color(0xFF16A34A),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Primary Action Button (Kudula Electric Blue)
                    val localContext = androidx.compose.ui.platform.LocalContext.current
                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            when (mode) {
                                AuthMode.LOGIN -> {
                                    authViewModel.continueWithEmail(emailInput.trim(), passwordInput, onSuccess = onAuthSuccess)
                                }
                                AuthMode.SIGNUP -> {
                                    authViewModel.signUpWithEmail(nameInput.trim(), emailInput.trim(), passwordInput, onSuccess = onAuthSuccess)
                                }
                                AuthMode.RECOVER -> {
                                    authViewModel.sendPasswordReset(emailInput.trim())
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = primaryAccent),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("auth_submit_button")
                    ) {
                        if (authState.isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Text(
                                text = when (mode) {
                                    AuthMode.LOGIN -> "Entrar"
                                    AuthMode.SIGNUP -> "Criar Conta & Continuar"
                                    AuthMode.RECOVER -> "Enviar Recuperação"
                                },
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 15.sp
                            )
                        }
                    }

                    // Google Button (Attachment 3: White button with Google G icon and "Google")
                    if (mode != AuthMode.RECOVER) {
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedButton(
                            onClick = {
                                focusManager.clearFocus()
                                authViewModel.continueWithGoogle(localContext, onSuccess = onAuthSuccess)
                            },
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, inputBorder),
                            colors = ButtonDefaults.outlinedButtonColors(containerColor = inputContainer),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("auth_google_button")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                // Google G Icon badge
                                Surface(
                                    shape = CircleShape,
                                    color = Color.Transparent,
                                    modifier = Modifier.size(20.dp)
                                ) {
                                    Text(text = "🌐", fontSize = 14.sp)
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Google",
                                    color = textPrimary,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.height(12.dp))
                        TextButton(
                            onClick = { mode = AuthMode.LOGIN },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = "Voltar ao Início de Sessão", color = primaryAccent, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Footer info
            Text(
                text = "Kudula KCLS • Sistema Operativo Cognitivo",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
