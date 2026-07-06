package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.FeaturesScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SimulatorScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            viewModel = viewModel()
            val currentTab by viewModel.currentTab.collectAsState()
            val toastMessage by viewModel.toastMessage.collectAsState()
            val settings by viewModel.settingsState.collectAsState()
            val isAr = settings.language == "ar"

            MyApplicationTheme(darkTheme = true) {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        NavigationBar(
                            containerColor = Color(0xFF0F172A),
                            contentColor = Color(0xFF00F0FF),
                            modifier = Modifier
                                .windowInsetsPadding(WindowInsets.navigationBars)
                                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                        ) {
                            val tabs = listOf(
                                Triple(0, if (isAr) "التعليم" else "Onboarding", Icons.Default.School),
                                Triple(1, if (isAr) "المحاكي" else "Simulator", Icons.Default.CenterFocusWeak),
                                Triple(2, if (isAr) "٣٠ ميزة" else "Superpowers", Icons.Default.FlashOn),
                                Triple(3, if (isAr) "التخصيص" else "Customizer", Icons.Default.Tune)
                            )

                            tabs.forEach { (index, title, icon) ->
                                NavigationBarItem(
                                    selected = currentTab == index,
                                    onClick = { viewModel.selectTab(index) },
                                    icon = { Icon(icon, contentDescription = title) },
                                    label = { Text(title, fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = Color(0xFF0F172A),
                                        selectedTextColor = Color(0xFF00F0FF),
                                        indicatorColor = Color(0xFF00F0FF),
                                        unselectedIconColor = Color.Gray,
                                        unselectedTextColor = Color.Gray
                                    )
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .background(Color(0xFF0B0F19))
                    ) {
                        // Dynamic Tab crossfades
                        AnimatedContent(
                            targetState = currentTab,
                            transitionSpec = {
                                fadeIn() togetherWith fadeOut()
                            },
                            label = "tab_nav_animation"
                        ) { targetTab ->
                            when (targetTab) {
                                0 -> OnboardingScreen(viewModel = viewModel)
                                1 -> SimulatorScreen(viewModel = viewModel)
                                2 -> FeaturesScreen(viewModel = viewModel)
                                3 -> SettingsScreen(viewModel = viewModel)
                            }
                        }

                        // System-Wide Custom Gaze Toast Feedback HUD
                        AnimatedVisibility(
                            visible = toastMessage != null,
                            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(top = 16.dp)
                        ) {
                            toastMessage?.let { msg ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF00F0FF)),
                                    shape = RoundedCornerShape(24.dp),
                                    modifier = Modifier
                                        .padding(horizontal = 24.dp)
                                        .shadow(8.dp, RoundedCornerShape(24.dp)),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = "Status",
                                            tint = Color(0xFF0F172A),
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = msg,
                                            color = Color(0xFF0F172A),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            textAlign = TextAlign.Center
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
