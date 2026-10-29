package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.viewmodel.VoiceNotesUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceNoteBottomSheet(
    uiState: VoiceNotesUiState,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    onCancelRecording: () -> Unit,
    onPlayAudioResponse: () -> Unit,
    onDeleteNote: (Long) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = KudulaNavyDark,
        dragHandle = {
            BottomSheetDefaults.DragHandle(color = KudulaCyanGlow.copy(alpha = 0.5f))
        },
        modifier = modifier.testTag("voice_note_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = KudulaElectricBlue.copy(alpha = 0.2f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, KudulaCyanGlow.copy(alpha = 0.4f)),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = null,
                                tint = KudulaCyanGlow,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Kudu Voice Notes",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Grava até 60s de perguntas orais",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.65f)
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_voice_notes_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Fechar",
                        tint = Color.White.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Active / Recorded Result Card if available
            AnimatedVisibility(
                visible = uiState.activeVoiceNote != null || uiState.isProcessing,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = KudulaNavySurface),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        Brush.horizontalGradient(listOf(KudulaElectricBlue, KudulaViolet))
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        if (uiState.isProcessing) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = KudulaCyanGlow,
                                    strokeWidth = 2.5.dp
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "KDAP a processar pergunta...",
                                    color = KudulaCyanGlow,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                )
                            }
                        } else if (uiState.activeVoiceNote != null) {
                            val note = uiState.activeVoiceNote
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = KudulaElectricBlue.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = "🎤 ${note.durationSeconds}s gravados",
                                        color = KudulaCyanGlow,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                                Text(
                                    text = "KDAP Tutor",
                                    color = KudulaViolet,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "\"${note.questionTranscript}\"",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.85f),
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            )

                            Spacer(modifier = Modifier.height(10.dp))
                            Divider(color = Color.White.copy(alpha = 0.1f))
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(verticalAlignment = Alignment.Top) {
                                Text(text = "💡", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = note.aiResponseText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Botão: Ouvir Resposta
                            Button(
                                onClick = onPlayAudioResponse,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (uiState.isPlayingAudio) KudulaViolet else KudulaElectricBlue
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("listen_voice_response_button")
                            ) {
                                Icon(
                                    imageVector = if (uiState.isPlayingAudio) Icons.Default.Pause else Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (uiState.isPlayingAudio) "A reproduzir resposta..." else "Ouvir resposta",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Animated Waves and Timer Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(KudulaNavySurface.copy(alpha = 0.7f))
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                if (uiState.isRecording) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Animated Waves
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.height(40.dp)
                        ) {
                            uiState.waveAmplitudes.forEach { amplitude ->
                                val height = (36.dp * amplitude).coerceIn(4.dp, 36.dp)
                                Box(
                                    modifier = Modifier
                                        .width(5.dp)
                                        .height(height)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(
                                            Brush.verticalGradient(
                                                listOf(KudulaCyanGlow, KudulaElectricBlue)
                                            )
                                        )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        val minutes = uiState.recordingDuration / 60
                        val seconds = uiState.recordingDuration % 60
                        Text(
                            text = String.format("%02d:%02d / 01:00", minutes, seconds),
                            color = KudulaCyanGlow,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else if (uiState.isPlayingAudio) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.height(40.dp)
                    ) {
                        uiState.waveAmplitudes.forEach { amplitude ->
                            val height = (32.dp * amplitude).coerceIn(4.dp, 32.dp)
                            Box(
                                modifier = Modifier
                                    .width(5.dp)
                                    .height(height)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(KudulaViolet)
                            )
                        }
                    }
                } else {
                    Text(
                        text = "Segura o botão para gravar ou clica para iniciar",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Big Circular Record Button with WhatsApp/ChatGPT voice inspired interaction
            val infiniteTransition = rememberInfiniteTransition(label = "pulse_mic")
            val pulseScale by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 1.15f,
                animationSpec = infiniteRepeatable(
                    animation = tween(800, easing = EaseInOutCubic),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "mic_pulse_anim"
            )

            var isDraggingToCancel by remember { mutableStateOf(false) }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(100.dp)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                onStartRecording()
                                tryAwaitRelease()
                                if (isDraggingToCancel) {
                                    onCancelRecording()
                                    isDraggingToCancel = false
                                } else {
                                    onStopRecording()
                                }
                            }
                        )
                    }
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDrag = { change, dragAmount ->
                                change.consume()
                                if (dragAmount.x < -30f) {
                                    isDraggingToCancel = true
                                }
                            },
                            onDragEnd = {
                                if (isDraggingToCancel) {
                                    onCancelRecording()
                                    isDraggingToCancel = false
                                }
                            }
                        )
                    }
                    .testTag("hold_to_record_voice_button")
            ) {
                // Outer Pulse Ring when recording
                if (uiState.isRecording) {
                    Box(
                        modifier = Modifier
                            .size(94.dp)
                            .scale(pulseScale)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(
                                        if (isDraggingToCancel) Color(0xFFEF4444).copy(alpha = 0.4f)
                                        else KudulaElectricBlue.copy(alpha = 0.4f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )
                }

                // Core Button
                Surface(
                    shape = CircleShape,
                    color = when {
                        isDraggingToCancel -> Color(0xFFEF4444)
                        uiState.isRecording -> KudulaElectricBlue
                        else -> KudulaNavySurface
                    },
                    border = androidx.compose.foundation.BorderStroke(
                        2.5.dp,
                        if (uiState.isRecording) KudulaCyanGlow else KudulaElectricBlue
                    ),
                    shadowElevation = 8.dp,
                    modifier = Modifier.size(76.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (uiState.isRecording) Icons.Default.Stop else Icons.Default.Mic,
                            contentDescription = "Gravar",
                            tint = Color.White,
                            modifier = Modifier.size(34.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action hints
            if (uiState.isRecording) {
                Text(
                    text = if (isDraggingToCancel) "Solta para cancelar" else "Desliza para a esquerda para cancelar ✕",
                    color = if (isDraggingToCancel) Color(0xFFEF4444) else Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            } else {
                Text(
                    text = "Segura para falar • Solta para enviar",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
