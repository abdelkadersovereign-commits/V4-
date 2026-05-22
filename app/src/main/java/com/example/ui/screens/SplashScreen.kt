package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmberZen
import com.example.ui.theme.VoidBlack
import com.example.ui.theme.CyberCyan
import kotlinx.coroutines.delay

import androidx.compose.foundation.border
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.shape.RoundedCornerShape

@Composable
fun SplashScreen(onNavigateToDashboard: () -> Unit) {
    val alpha = remember { Animatable(0f) }
    val logs = remember { mutableStateListOf<String>() }
    val listState = rememberLazyListState()
    
    val systemChecks = listOf(
        "CORE_INIT // SOVEREIGN ENGINE START",
        "HW_ENCRYPT // BIOMETRIC SHIELD SYNCING",
        "SAT_GRID // PRAYER TIMES CALIBRATION",
        "VAULT_BOOT // DECRYPTING DESIGN BLUEPRINTS",
        "NEURAL_HUD // CALIBRATING SENSORS",
        "CYBER_SYNC // ESTABLISHING AI UPLINK",
        "SECURITY_CHECK // OPTIMIZING FIREWALLS",
        "SYSTEM_READY // ASYRIA V4 STABLE",
        "SYSTEM_OPTIMIZED // ALL CORES LOCKED"
    )

    // Glowing animation for the verse
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    // Neon Pulse for Designer Name
    val neonPulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "neonPulse"
    )

    LaunchedEffect(Unit) {
        alpha.animateTo(1f, animationSpec = tween(1500))
        for (check in systemChecks) {
            logs.add("> $check")
            listState.animateScrollToItem(logs.size - 1)
            delay(350)
        }
        delay(1000)
        onNavigateToDashboard()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(VoidBlack)
            .padding(24.dp)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Sovereign Frame (Rect with corner cut-outs)
        Canvas(modifier = Modifier.fillMaxSize().alpha(alpha.value * 0.3f)) {
            val strokeWidth = 1.dp.toPx()
            val cornerSize = 40.dp.toPx()
            val drawPadding = 10.dp.toPx()
            val canvasWidth = size.width
            val canvasHeight = size.height
            
            val framePath = Path().apply {
                // Top-Left
                moveTo(drawPadding + cornerSize, drawPadding)
                lineTo(drawPadding, drawPadding)
                lineTo(drawPadding, drawPadding + cornerSize)
                
                // Bottom-Left
                moveTo(drawPadding, canvasHeight - drawPadding - cornerSize)
                lineTo(drawPadding, canvasHeight - drawPadding)
                lineTo(drawPadding + cornerSize, canvasHeight - drawPadding)
                
                // Bottom-Right
                moveTo(canvasWidth - drawPadding - cornerSize, canvasHeight - drawPadding)
                lineTo(canvasWidth - drawPadding, canvasHeight - drawPadding)
                lineTo(canvasWidth - drawPadding, canvasHeight - drawPadding - cornerSize)
                
                // Top-Right
                moveTo(canvasWidth - drawPadding, drawPadding + cornerSize)
                lineTo(canvasWidth - drawPadding, drawPadding)
                lineTo(canvasWidth - drawPadding - cornerSize, drawPadding)
            }
            
            drawPath(
                path = framePath,
                color = CyberCyan,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        // Center Verse with Frame
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .padding(20.dp)
                    .border(
                        width = 0.5.dp,
                        color = AmberZen.copy(alpha = 0.2f * glowAlpha),
                        shape = RoundedCornerShape(2.dp)
                    )
                    .padding(horizontal = 30.dp, vertical = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "وَيَنْصُرَكَ اللَّهُ نَصْرًا عَزِيزًا",
                    color = AmberZen,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Light,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.alpha(alpha.value * glowAlpha),
                    style = androidx.compose.ui.text.TextStyle(
                        shadow = androidx.compose.ui.graphics.Shadow(
                            color = AmberZen.copy(alpha = 0.6f),
                            blurRadius = 30f
                        )
                    )
                )
            }
        }
        
        // Designer Signature at Bottom Center
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "DESIGNED BY",
                color = Color.White.copy(alpha = 0.4f * neonPulseAlpha),
                fontSize = 8.sp,
                letterSpacing = 2.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "ABOUDA.AL.SHEKH.YOSSEF",
                color = CyberCyan.copy(alpha = neonPulseAlpha),
                fontSize = 12.sp,
                fontWeight = FontWeight.Thin,
                letterSpacing = 6.sp,
                fontFamily = FontFamily.Monospace,
                style = androidx.compose.ui.text.TextStyle(
                    shadow = androidx.compose.ui.graphics.Shadow(
                        color = CyberCyan.copy(alpha = 0.5f),
                        blurRadius = 15f
                    )
                )
            )
        }

        // Scrolling Diagnostics at the Bottom Left
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(bottom = 20.dp)
                .fillMaxWidth(0.6f)
                .height(60.dp)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Bottom
            ) {
                items(logs) { log ->
                    Text(
                        text = log,
                        color = (if (log.contains("READY") || log.contains("STABLE")) CyberCyan else Color.White).copy(alpha = 0.4f),
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(vertical = 0.5.dp)
                    )
                }
            }
        }
    }
}
