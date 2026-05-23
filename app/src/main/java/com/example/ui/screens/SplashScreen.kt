package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.foundation.Canvas
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmberZen
import com.example.ui.theme.VoidBlack
import com.example.ui.theme.CyberCyan
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onNavigateToDashboard: () -> Unit) {
    val alpha = remember { Animatable(0f) }
    val shieldScale = remember { Animatable(0.4f) }
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

    val infiniteTransition = rememberInfiniteTransition(label = "glow")

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    val shieldPulse by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shieldPulse"
    )

    val scanLine by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scanLine"
    )

    val neonPulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "neonPulse"
    )

    val ringRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ringRotation"
    )

    LaunchedEffect(Unit) {
        shieldScale.animateTo(1f, animationSpec = spring(dampingRatio = 0.6f, stiffness = 200f))
        alpha.animateTo(1f, animationSpec = tween(800))
        for (check in systemChecks) {
            logs.add("> $check")
            if (logs.size > 0) listState.animateScrollToItem(logs.size - 1)
            delay(300)
        }
        delay(800)
        onNavigateToDashboard()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0xFF020E1A), Color(0xFF000509)),
                    radius = 1800f
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Background grid
        Canvas(modifier = Modifier.fillMaxSize().alpha(0.08f)) {
            val gridSpacing = 30.dp.toPx()
            var x = 0f
            while (x <= size.width) {
                drawLine(Color.Cyan, Offset(x, 0f), Offset(x, size.height), strokeWidth = 0.5f)
                x += gridSpacing
            }
            var y = 0f
            while (y <= size.height) {
                drawLine(Color.Cyan, Offset(0f, y), Offset(size.width, y), strokeWidth = 0.5f)
                y += gridSpacing
            }
        }

        // Corner frame brackets
        Canvas(modifier = Modifier.fillMaxSize().alpha(alpha.value * 0.6f)) {
            val strokeWidth = 1.5.dp.toPx()
            val cornerSize = 50.dp.toPx()
            val pad = 16.dp.toPx()

            val framePath = Path().apply {
                moveTo(pad + cornerSize, pad); lineTo(pad, pad); lineTo(pad, pad + cornerSize)
                moveTo(pad, size.height - pad - cornerSize); lineTo(pad, size.height - pad); lineTo(pad + cornerSize, size.height - pad)
                moveTo(size.width - pad - cornerSize, size.height - pad); lineTo(size.width - pad, size.height - pad); lineTo(size.width - pad, size.height - pad - cornerSize)
                moveTo(size.width - pad, pad + cornerSize); lineTo(size.width - pad, pad); lineTo(size.width - pad - cornerSize, pad)
            }
            drawPath(framePath, CyberCyan, style = Stroke(strokeWidth, cap = StrokeCap.Round))
        }

        // Main content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App name badge
            Box(
                modifier = Modifier
                    .alpha(alpha.value)
                    .border(0.5.dp, CyberCyan.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                    .background(CyberCyan.copy(alpha = 0.05f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 14.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "A.SYRIA SOVEREIGN OS v4.0.0",
                    color = CyberCyan.copy(alpha = 0.7f),
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Shield logo with rotating ring
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .scale(shieldScale.value * shieldPulse),
                contentAlignment = Alignment.Center
            ) {
                // Outer rotating dashed ring
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2, size.height / 2)
                    val outerRadius = size.minDimension / 2f - 4.dp.toPx()
                    drawCircle(
                        color = CyberCyan.copy(alpha = 0.2f * glowAlpha),
                        radius = outerRadius,
                        style = Stroke(1.5.dp.toPx())
                    )
                    drawCircle(
                        color = CyberCyan.copy(alpha = 0.06f),
                        radius = outerRadius - 8.dp.toPx(),
                        style = Stroke(0.8.dp.toPx())
                    )

                    // Rotating arc indicators
                    val sweepAngle = 60f
                    val gaps = listOf(0f, 120f, 240f)
                    gaps.forEach { startAngle ->
                        drawArc(
                            color = CyberCyan.copy(alpha = 0.7f * glowAlpha),
                            startAngle = startAngle + ringRotation,
                            sweepAngle = sweepAngle,
                            useCenter = false,
                            style = Stroke(2.dp.toPx(), cap = StrokeCap.Round),
                            size = androidx.compose.ui.geometry.Size(outerRadius * 2, outerRadius * 2),
                            topLeft = Offset(center.x - outerRadius, center.y - outerRadius)
                        )
                    }
                }

                // Shield shape
                Canvas(modifier = Modifier.size(100.dp)) {
                    val shieldPath = Path().apply {
                        val w = size.width
                        val h = size.height
                        moveTo(w / 2f, 0f)
                        lineTo(w * 0.9f, h * 0.2f)
                        lineTo(w * 0.9f, h * 0.55f)
                        cubicTo(w * 0.9f, h * 0.8f, w / 2f, h, w / 2f, h)
                        cubicTo(w / 2f, h, w * 0.1f, h * 0.8f, w * 0.1f, h * 0.55f)
                        lineTo(w * 0.1f, h * 0.2f)
                        close()
                    }
                    drawPath(shieldPath, CyberCyan.copy(alpha = 0.12f))
                    drawPath(shieldPath, CyberCyan.copy(alpha = 0.8f * glowAlpha), style = Stroke(2.dp.toPx()))

                    // Inner crosshair
                    val cx = size.width / 2
                    val cy = size.height / 2
                    drawLine(CyberCyan.copy(alpha = 0.6f), Offset(cx, cy - 22f), Offset(cx, cy + 22f), strokeWidth = 1.5f)
                    drawLine(CyberCyan.copy(alpha = 0.6f), Offset(cx - 22f, cy), Offset(cx + 22f, cy), strokeWidth = 1.5f)
                    drawCircle(CyberCyan.copy(alpha = 0.5f * glowAlpha), radius = 8f, center = Offset(cx, cy), style = Stroke(1.5f))
                    drawCircle(CyberCyan.copy(alpha = 0.25f), radius = 16f, center = Offset(cx, cy), style = Stroke(0.8f))
                }

                // Scan line animation
                Canvas(modifier = Modifier.size(100.dp)) {
                    val scanY = size.height * scanLine
                    drawLine(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color.Transparent, CyberCyan.copy(alpha = 0.5f), Color.Transparent)
                        ),
                        start = Offset(0f, scanY),
                        end = Offset(size.width, scanY),
                        strokeWidth = 1.5f
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Quranic verse
            Box(
                modifier = Modifier
                    .alpha(alpha.value)
                    .fillMaxWidth()
                    .border(
                        width = 0.5.dp,
                        color = AmberZen.copy(alpha = 0.35f * glowAlpha),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .background(AmberZen.copy(alpha = 0.04f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 24.dp, vertical = 18.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "وَيَنْصُرَكَ اللَّهُ نَصْرًا عَزِيزًا",
                        color = AmberZen.copy(alpha = glowAlpha),
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Light,
                        textAlign = TextAlign.Center,
                        style = androidx.compose.ui.text.TextStyle(
                            shadow = androidx.compose.ui.graphics.Shadow(
                                color = AmberZen.copy(alpha = 0.5f),
                                blurRadius = 20f
                            )
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "الفتح [ 48:3 ]",
                        color = AmberZen.copy(alpha = 0.5f),
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Diagnostics log
            Box(
                modifier = Modifier
                    .alpha(alpha.value)
                    .fillMaxWidth()
                    .height(72.dp)
                    .border(0.5.dp, CyberCyan.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                    .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Bottom
                ) {
                    items(logs) { log ->
                        val isSuccess = log.contains("READY") || log.contains("STABLE") || log.contains("OPTIMIZED")
                        Text(
                            text = log,
                            color = (if (isSuccess) CyberCyan else Color.White).copy(alpha = if (isSuccess) 0.7f else 0.35f),
                            fontSize = 8.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(vertical = 0.5.dp)
                        )
                    }
                }
            }
        }

        // Designer signature
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
                .alpha(alpha.value),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "DESIGNED BY",
                color = Color.White.copy(alpha = 0.25f * neonPulseAlpha),
                fontSize = 7.sp,
                letterSpacing = 2.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = "ABOUDA.AL.SHEKH.YOSSEF",
                color = CyberCyan.copy(alpha = 0.5f * neonPulseAlpha),
                fontSize = 10.sp,
                fontWeight = FontWeight.Thin,
                letterSpacing = 4.sp,
                fontFamily = FontFamily.Monospace,
                style = androidx.compose.ui.text.TextStyle(
                    shadow = androidx.compose.ui.graphics.Shadow(
                        color = CyberCyan.copy(alpha = 0.4f),
                        blurRadius = 10f
                    )
                )
            )
        }
    }
}
