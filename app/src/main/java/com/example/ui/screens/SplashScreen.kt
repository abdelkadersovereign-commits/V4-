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
        "SYSTEM_READY // ASYRIA V4 STABLE"
    )

    // Glowing animation for the verse
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

    LaunchedEffect(Unit) {
        alpha.animateTo(1f, animationSpec = tween(1200))
        for (check in systemChecks) {
            logs.add("> $check")
            listState.animateScrollToItem(logs.size - 1)
            delay(400)
        }
        delay(600)
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
        // Center Verse
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "وَيَنْصُرَكَ اللَّهُ نَصْرًا عَزِيزًا",
                color = AmberZen,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .alpha(alpha.value * glowAlpha)
                    .padding(horizontal = 16.dp),
                style = androidx.compose.ui.text.TextStyle(
                    shadow = androidx.compose.ui.graphics.Shadow(
                        color = AmberZen.copy(alpha = 0.8f),
                        blurRadius = 25f
                    )
                )
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "A.SYRIA SOVEREIGN OS V4",
                color = CyberCyan.copy(alpha = 0.6f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 4.sp,
                fontFamily = FontFamily.Monospace
            )
        }
        
        // Scrolling Diagnostics at the Bottom Left
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth(0.6f)
                .height(100.dp)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Bottom
            ) {
                items(logs) { log ->
                    Text(
                        text = log,
                        color = (if (log.contains("READY") || log.contains("STABLE")) CyberCyan else Color.White).copy(alpha = 0.5f),
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(vertical = 1.dp)
                    )
                }
            }
        }
    }
}
