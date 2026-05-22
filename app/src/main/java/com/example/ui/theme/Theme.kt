package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme =
  darkColorScheme(
    primary = CyberCyan,
    secondary = AmberZen,
    tertiary = AmberZen,
    background = VoidBlack,
    surface = VoidBlack,
    onPrimary = VoidBlack,
    onSecondary = VoidBlack,
    onTertiary = VoidBlack,
    onBackground = CyberCyan,
    onSurface = CyberCyan
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true,
  // Dynamic color is available on Android 12+
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme = DarkColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
