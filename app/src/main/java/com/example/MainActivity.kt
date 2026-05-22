package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.work.*
import com.example.worker.NotificationWorker
import java.util.concurrent.TimeUnit
import com.example.ui.screens.SettingsScreen
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.AcademyScreen
import com.example.ui.screens.ResourcesScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.AmberZen
import com.example.ui.theme.VoidBlack
import com.example.ui.viewmodel.DashboardViewModel

import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    
    // Initialize Sovereign Pulse Protocol (WorkManager)
    val workRequest = PeriodicWorkRequestBuilder<NotificationWorker>(4, TimeUnit.HOURS)
      .setInitialDelay(15, TimeUnit.MINUTES)
      .build()
    WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
      "sovereign_pulse",
      ExistingPeriodicWorkPolicy.KEEP,
      workRequest
    )

    setContent {
      MyApplicationTheme(darkTheme = true, dynamicColor = false) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
          val navController = rememberNavController()
          val vm: DashboardViewModel = viewModel()
          
          DisposableEffect(Unit) {
            val observer = LifecycleEventObserver { _, event ->
              when (event) {
                Lifecycle.Event.ON_RESUME -> vm.startSensors()
                Lifecycle.Event.ON_PAUSE -> vm.stopSensors()
                else -> {}
              }
            }
            lifecycle.addObserver(observer)
            onDispose {
              lifecycle.removeObserver(observer)
            }
          }

          LaunchedEffect(Unit) {
            Toast.makeText(applicationContext, "Neural Link Established", Toast.LENGTH_SHORT).show()
          }

          NavHost(
            navController = navController, 
            startDestination = "splash",
            enterTransition = { fadeIn(animationSpec = tween(700)) },
            exitTransition = { fadeOut(animationSpec = tween(700)) }
          ) {
            composable("splash") {
              SplashScreen(onNavigateToDashboard = {
                navController.navigate("dashboard") {
                  popUpTo("splash") { inclusive = true }
                }
              })
            }
            composable("dashboard") {
              val isAr by vm.isArabic.collectAsState()
              var activeTab by remember { mutableStateOf("home") }
              val haptic = LocalHapticFeedback.current

              // Synchronize state flows from viewmodel triggers to bottom navigation clicks
              val isAcademyOpen by vm.isAcademyOpen.collectAsState()
              val isResourcesOpen by vm.isResourcesOpen.collectAsState()
              val isSettingsOpen by vm.isSettingsOpen.collectAsState()

              LaunchedEffect(isAcademyOpen) {
                if (isAcademyOpen) {
                  activeTab = "academy"
                  vm.setAcademyOpen(false) 
                }
              }

              LaunchedEffect(isResourcesOpen) {
                if (isResourcesOpen) {
                  activeTab = "resources"
                  vm.setResourcesOpen(false) 
                }
              }

              val currentLayoutDirection = if (isAr) LayoutDirection.Rtl else LayoutDirection.Ltr
              CompositionLocalProvider(LocalLayoutDirection provides currentLayoutDirection) {
                Scaffold(
                  modifier = Modifier.fillMaxSize(),
                  containerColor = VoidBlack,
                  bottomBar = {
                    NavigationBar(
                      containerColor = Color(0xFF04070D),
                      tonalElevation = 8.dp,
                      modifier = Modifier
                        .border(
                          width = 0.5.dp,
                          color = CyberCyan.copy(alpha = 0.15f),
                          shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                        )
                    ) {
                      // Tab 1: Home
                      NavigationBarItem(
                        selected = activeTab == "home",
                        onClick = {
                          haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                          activeTab = "home"
                          vm.setSettingsOpen(false)
                        },
                        icon = { 
                          Box(contentAlignment = Alignment.Center) {
                            if (activeTab == "home") {
                              Box(
                                modifier = Modifier
                                  .size(40.dp)
                                  .background(CyberCyan.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                                  .border(1.dp, CyberCyan.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                              )
                            }
                            Icon(Icons.Default.Home, contentDescription = "Home", tint = if (activeTab == "home") CyberCyan else Color.White.copy(alpha = 0.4f)) 
                          }
                        },
                        label = {
                          Text(
                            text = if (isAr) "الرئيسية" else "Home",
                            color = if (activeTab == "home") CyberCyan else Color.White.copy(alpha = 0.4f),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                          )
                        },
                        colors = NavigationBarItemDefaults.colors(
                          indicatorColor = CyberCyan.copy(alpha = 0.12f)
                        )
                      )

                      // Tab 2: Academy
                      NavigationBarItem(
                        selected = activeTab == "academy",
                        onClick = {
                          haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                          activeTab = "academy"
                          vm.setSettingsOpen(false)
                        },
                        icon = { 
                          Box(contentAlignment = Alignment.Center) {
                            if (activeTab == "academy") {
                              Box(
                                modifier = Modifier
                                  .size(40.dp)
                                  .background(CyberCyan.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                                  .border(1.dp, CyberCyan.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                              )
                            }
                            Icon(Icons.Default.Check, contentDescription = "Academy", tint = if (activeTab == "academy") CyberCyan else Color.White.copy(alpha = 0.4f)) 
                          }
                        },
                        label = {
                          Text(
                            text = if (isAr) "الأكاديمية" else "Academy",
                            color = if (activeTab == "academy") CyberCyan else Color.White.copy(alpha = 0.4f),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                          )
                        },
                        colors = NavigationBarItemDefaults.colors(
                          indicatorColor = CyberCyan.copy(alpha = 0.12f)
                        )
                      )

                      // Tab 3: Resources
                      NavigationBarItem(
                        selected = activeTab == "resources",
                        onClick = {
                          haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                          activeTab = "resources"
                          vm.setSettingsOpen(false)
                        },
                        icon = { 
                          Box(contentAlignment = Alignment.Center) {
                            if (activeTab == "resources") {
                              Box(
                                modifier = Modifier
                                  .size(40.dp)
                                  .background(CyberCyan.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                                  .border(1.dp, CyberCyan.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                              )
                            }
                            Icon(Icons.Default.Search, contentDescription = "Resources", tint = if (activeTab == "resources") CyberCyan else Color.White.copy(alpha = 0.4f)) 
                          }
                        },
                        label = {
                          Text(
                            text = if (isAr) "المصادر" else "Resources",
                            color = if (activeTab == "resources") CyberCyan else Color.White.copy(alpha = 0.4f),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                          )
                        },
                        colors = NavigationBarItemDefaults.colors(
                          indicatorColor = CyberCyan.copy(alpha = 0.12f)
                        )
                      )

                      // Tab 4: Settings
                      NavigationBarItem(
                        selected = isSettingsOpen,
                        onClick = {
                          haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                          vm.setSettingsOpen(true)
                        },
                        icon = { Icon(Icons.Default.Settings, contentDescription = "Settings", tint = if (isSettingsOpen) CyberCyan else Color.White.copy(alpha = 0.4f)) },
                        label = {
                          Text(
                            text = if (isAr) "الإعدادات" else "Settings",
                            color = if (isSettingsOpen) CyberCyan else Color.White.copy(alpha = 0.4f),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                          )
                        },
                        colors = NavigationBarItemDefaults.colors(
                          indicatorColor = CyberCyan.copy(alpha = 0.12f)
                        )
                      )
                    }
                  }
                ) { innerPadding ->
                  Box(
                    modifier = Modifier
                      .fillMaxSize()
                      .padding(innerPadding)
                  ) {
                    AnimatedContent(
                      targetState = activeTab,
                      transitionSpec = {
                        (slideInHorizontally(animationSpec = tween(400)) { it } + fadeIn(tween(400)))
                          .togetherWith(slideOutHorizontally(animationSpec = tween(400)) { -it } + fadeOut(tween(400)))
                      },
                      label = "tabAnimation"
                    ) { tab ->
                      when (tab) {
                        "home" -> {
                          DashboardScreen(viewModel = vm)
                        }
                        "academy" -> {
                          AcademyScreen(viewModel = vm)
                        }
                        "resources" -> {
                          ResourcesScreen(viewModel = vm, onClose = { activeTab = "home" })
                        }
                      }
                    }

                    // Settings Overlay with slide from right motion
                    AnimatedVisibility(
                      visible = isSettingsOpen,
                      enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(),
                      exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut()
                    ) {
                      SettingsScreen(
                        viewModel = vm,
                        onClose = { vm.setSettingsOpen(false) }
                      )
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}
