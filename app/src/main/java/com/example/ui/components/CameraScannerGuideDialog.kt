package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.*

/**
 * Moldura visual de enquadramento com animação de captura e feedback KDAP.
 */
@Composable
fun CameraScannerGuideDialog(
    onDismiss: () -> Unit,
    onCaptureClick: () -> Unit,
    kdapMessage: String? = null,
    isCapturing: Boolean = false,
    modifier: Modifier = Modifier
) {
    // Scanning laser bar animation
    val infiniteTransition = rememberInfiniteTransition(label = "scanner_laser")
    val laserOffsetY by infiniteTransition.animateFloat(
        initialValue = 0.05f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser_y"
    )

    // Shutter flash effect
    var showFlash by remember { mutableStateOf(false) }

    LaunchedEffect(isCapturing) {
        if (isCapturing) {
            showFlash = true
            kotlinx.coroutines.delay(200)
            showFlash = false
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.92f))
                .testTag("camera_scanner_dialog")
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp, start = 20.dp, end = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = null,
                        tint = KudulaCyanGlow,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Digitalização Cognitiva KDAP",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Fechar",
                        tint = Color.White
                    )
                }
            }

            // Framing Viewfinder Overlay
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 28.dp, vertical = 90.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Enquadra a página ou caderno na moldura",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))

                // The Document Frame with Corner Reticles and Scanning Laser
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(KudulaNavySurface.copy(alpha = 0.5f))
                        .border(
                            BorderStroke(1.5.dp, KudulaCyanGlow.copy(alpha = 0.6f)),
                            RoundedCornerShape(16.dp)
                        )
                        .drawBehind {
                            // Draw corner reticles
                            val strokeW = 4.dp.toPx()
                            val cornerLen = 28.dp.toPx()

                            // Top Left
                            drawLine(KudulaElectricBlue, Offset(0f, 0f), Offset(cornerLen, 0f), strokeW)
                            drawLine(KudulaElectricBlue, Offset(0f, 0f), Offset(0f, cornerLen), strokeW)

                            // Top Right
                            drawLine(KudulaElectricBlue, Offset(size.width, 0f), Offset(size.width - cornerLen, 0f), strokeW)
                            drawLine(KudulaElectricBlue, Offset(size.width, 0f), Offset(size.width, cornerLen), strokeW)

                            // Bottom Left
                            drawLine(KudulaElectricBlue, Offset(0f, size.height), Offset(cornerLen, size.height), strokeW)
                            drawLine(KudulaElectricBlue, Offset(0f, size.height), Offset(0f, size.height - cornerLen), strokeW)

                            // Bottom Right
                            drawLine(KudulaElectricBlue, Offset(size.width, size.height), Offset(size.width - cornerLen, size.height), strokeW)
                            drawLine(KudulaElectricBlue, Offset(size.width, size.height), Offset(size.width, size.height - cornerLen), strokeW)

                            // Scanning Laser Bar
                            val laserY = size.height * laserOffsetY
                            drawLine(
                                brush = Brush.horizontalGradient(
                                    listOf(Color.Transparent, KudulaCyanGlow, Color.White, KudulaCyanGlow, Color.Transparent)
                                ),
                                start = Offset(0f, laserY),
                                end = Offset(size.width, laserY),
                                strokeWidth = 3.dp.toPx()
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    // Document alignment indicator
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = KudulaNavyDark.copy(alpha = 0.75f),
                            border = BorderStroke(1.dp, KudulaCyanGlow.copy(alpha = 0.4f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.FlashOn, contentDescription = null, tint = KudulaGold, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Auto-correção EXIF & Compressão Ativa",
                                    fontSize = 11.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    // Shutter Flash Animation
                    if (showFlash) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.White.copy(alpha = 0.7f))
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // KDAP Message Banner (if provided)
                if (kdapMessage != null) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = KudulaElectricBlue.copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, KudulaCyanGlow.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "🎯", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = kdapMessage,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Shutter Button with pulsing aura
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(KudulaCyanGlow.copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center
                ) {
                    FilledIconButton(
                        onClick = {
                            showFlash = true
                            onCaptureClick()
                        },
                        modifier = Modifier.size(64.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = KudulaElectricBlue)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Tirar Fotografia",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        }
    }
}
