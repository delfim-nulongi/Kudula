package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LockReset
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
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.*

/**
 * Login Progressivo Invisível (F0.6)
 * Convite elegante disparado apenas quando existe valor real para o utilizador guardar.
 */
@Composable
fun ProgressiveLoginDialog(
    reason: String?,
    onContinueWithGoogle: () -> Unit,
    onContinueWithEmail: (email: String, pass: String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showEmailForm by remember { mutableStateOf(false) }
    var emailInput by remember { mutableStateOf("") }
    var passInput by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = KudulaNavySurface,
            border = BorderStroke(
                1.5.dp,
                Brush.horizontalGradient(
                    listOf(KudulaCyanGlow.copy(alpha = 0.6f), KudulaElectricBlue, KudulaVioletLight.copy(alpha = 0.5f))
                )
            ),
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("progressive_login_dialog")
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Dismiss
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    reason?.let {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = KudulaElectricBlue.copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, KudulaCyanGlow.copy(alpha = 0.3f))
                        ) {
                            Text(
                                text = "✨ $it",
                                color = KudulaCyanGlow,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    } ?: Spacer(modifier = Modifier.width(1.dp))

                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Fechar", tint = TextTertiary)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Brand Logo Badge
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(KudulaNavyContainer),
                    contentAlignment = Alignment.Center
                ) {
                    KudulaFaviconLogo(size = 48.dp, showAuraGlow = false)
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Official Message
                Text(
                    text = "O teu progresso já começou.",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    fontSize = 19.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Guarda tudo o que conquistaste e continua exatamente de onde paraste.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                if (!showEmailForm) {
                    // Google Button
                    Button(
                        onClick = onContinueWithGoogle,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("login_button_google"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "G", fontWeight = FontWeight.Black, fontSize = 18.sp, color = Color(0xFF4285F4))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Continuar com Google",
                                color = Color(0xFF1E293B),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Email Button
                    OutlinedButton(
                        onClick = { showEmailForm = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("login_button_email"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border = BorderStroke(1.dp, KudulaCyanGlow.copy(alpha = 0.5f))
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Email, contentDescription = null, tint = KudulaCyanGlow, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Continuar com Email",
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                        }
                    }
                } else {
                    // Email Input Form
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = emailInput,
                            onValueChange = { emailInput = it },
                            placeholder = { Text("O teu email", fontSize = 12.sp, color = TextTertiary) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("login_input_email"),
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = passInput,
                            onValueChange = { passInput = it },
                            placeholder = { Text("Palavra-passe", fontSize = 12.sp, color = TextTertiary) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("login_input_pass"),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Button(
                            onClick = {
                                if (emailInput.isNotBlank()) {
                                    onContinueWithEmail(emailInput, passInput)
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(46.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = KudulaElectricBlue)
                        ) {
                            Text("Guardar e Sincronizar", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Dismiss Button ("Agora não")
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("login_button_not_now")
                ) {
                    Text(
                        text = "Agora não",
                        color = TextTertiary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
