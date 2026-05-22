package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmberGold
import com.example.ui.theme.VoidBlack
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onNavigateToDashboard: () -> Unit) {
    val alpha = remember { Animatable(0f) }
    var bootLogText by remember { mutableStateOf("INITIATING SOVEREIGN UPLINK...") }
    val logs = listOf(
        "INITIATING EXOSPHERE UPLINK...",
        "DECRYPTING SACRED CONTEXT CORES...",
        "STABILIZING CORE NEURAL AXELS...",
        "POLISHING SILENT PARALLAX ENGINES...",
        "A-SYRIA ENCRYPTED COGNITIVE BUFFER READY."
    )

    LaunchedEffect(key1 = true) {
        alpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 900)
        )
        for (i in logs.indices) {
            bootLogText = logs[i]
            delay(500)
        }
        delay(200)
        onNavigateToDashboard()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(VoidBlack)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "وَيَنْصُرَكَ اللَّهُ نَصْرًا عَزِيزًا",
                color = AmberGold,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .alpha(alpha.value)
                    .padding(horizontal = 16.dp)
            )
            
            Spacer(modifier = Modifier.height(45.dp))
            
            Text(
                text = "UPLINK_LOG: > $bootLogText",
                color = AmberGold.copy(alpha = 0.5f),
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(alpha.value)
            )
        }
    }
}
