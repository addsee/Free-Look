package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DoubleArrow
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.MainActivity
import com.example.MainViewModel
import com.example.R
import kotlinx.coroutines.delay

@Composable
fun OnboardingScreen(viewModel: MainViewModel) {
    var step by remember { mutableStateOf(0) }
    val isAr = viewModel.settingsState.collectAsState().value.language == "ar"
    
    val calibrationStep by viewModel.calibrationStep.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF0F172A), Color(0xFF1E293B))
                )
            )
    ) {
        if (calibrationStep > 0) {
            // Interactive Calibration Matrix Overlay
            CalibrationOverlay(viewModel = viewModel, currentStep = calibrationStep)
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Header Brand Name
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = viewModel.getString("app_name"),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF00F0FF)
                        )
                    )
                    
                    TextButton(
                        onClick = { viewModel.toggleLanguage() },
                        colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF38BDF8))
                    ) {
                        Text(if (isAr) "English" else "العربية", fontWeight = FontWeight.SemiBold)
                    }
                }

                // Main Slider Area
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    AnimatedContent(
                        targetState = step,
                        transitionSpec = {
                            fadeIn() togetherWith fadeOut()
                        },
                        label = "onboarding_step_anim"
                    ) { currentStep ->
                        when (currentStep) {
                            0 -> WelcomeStep(isAr)
                            1 -> CalibrationIntroStep(isAr)
                            2 -> DwellTrainingStep(isAr, viewModel)
                            3 -> ExpressionsStep(isAr)
                        }
                    }
                }

                // Navigation Controls Bottom
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (step > 0) {
                        TextButton(
                            onClick = { step-- },
                            colors = ButtonDefaults.textButtonColors(contentColor = Color.LightGray)
                        ) {
                            Text(if (isAr) "السابق" else "Back")
                        }
                    } else {
                        Spacer(modifier = Modifier.width(48.dp))
                    }

                    // Page Indicator dots
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        for (i in 0..3) {
                            Box(
                                modifier = Modifier
                                    .size(if (step == i) 12.dp else 8.dp)
                                    .clip(CircleShape)
                                    .background(if (step == i) Color(0xFF00F0FF) else Color.DarkGray)
                            )
                        }
                    }

                    Button(
                        onClick = {
                            if (step < 3) {
                                step++
                            } else {
                                viewModel.selectTab(1) // Advance to main Control Simulator tab
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF00F0FF),
                            contentColor = Color(0xFF0F172A)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(48.dp)
                    ) {
                        Text(
                            text = if (step == 3) (if (isAr) "ابدأ الآن 🚀" else "Start Now 🚀") else (if (isAr) "التالي" else "Next"),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WelcomeStep(isAr: Boolean) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Hero Banner generated via generate_image
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .shadow(16.dp, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_onboarding_hero_1783315810770),
                contentDescription = "Nazra Horra Core Feature illustration",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = if (isAr) "تطبيق نظرة حرة للمساعدة البصرية" else "Nazra Horra - Hands-Free Control",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (isAr) {
                "أول مشغل هاتف متكامل مخصص لذوي الاحتياجات الخاصة ممن لا يملكون يدين. تحكم بهاتفك بالكامل وبدقة متناهية عبر تتبع العين وتعابير الوجه."
            } else {
                "The first complete eyes-only operating launcher for physical impairments. Navigate your device with custom precision cursor utilizing eye tracking and head postures."
            },
            style = MaterialTheme.typography.bodyMedium.copy(
                color = Color(0xFF94A3B8),
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            ),
            modifier = Modifier.padding(horizontal = 8.dp)
        )
    }
}

@Composable
fun CalibrationIntroStep(isAr: Boolean) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = Icons.Default.Visibility,
            contentDescription = "Calibration",
            tint = Color(0xFF38BDF8),
            modifier = Modifier
                .size(100.dp)
                .padding(8.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = if (isAr) "معايرة دقة تتبع العين" else "Eye Tracking Precision Calibration",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (isAr) {
                "يرجى تركيز نظرك على النقاط المضيئة الوامضة التي تظهر على حواف الشاشة لتوليد نموذج تحكم خالي من الأخطاء والإنحرافات البصرية."
            } else {
                "Follow and gaze steadily at the glowing calibration dots across the screen boundaries. This registers optimal gaze pitch angles personalized for your face profile."
            },
            style = MaterialTheme.typography.bodyMedium.copy(
                color = Color(0xFF94A3B8),
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            ),
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Demo interactive calibration starter trigger button
        var isCalibratingLocal by remember { mutableStateOf(false) }
        val context = LocalContext.current
        val model = (context as MainActivity).viewModel

        Button(
            onClick = {
                model.startCalibration()
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF38BDF8),
                contentColor = Color.Black
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(56.dp)
        ) {
            Text(if (isAr) "ابدأ المعايرة البصرية المباشرة 🎯" else "Begin Calibration Overlay 🎯", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

@Composable
fun DwellTrainingStep(isAr: Boolean, viewModel: MainViewModel) {
    val progress by viewModel.dwellProgress.collectAsState()
    val isLocking by viewModel.isLocking.collectAsState()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = if (isAr) "التدرب على التحديق (قفل التحديد)" else "Dwell Gaze Clicks Training",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (isAr) {
                "انقر طويلاً (تحديق مستمر بالعين) على الزر الدائري لتجربة حلقة النقر المؤقتة حتى تمتلئ تماماً للتأكيد."
            } else {
                "Practice dwelling (staring at a button) to execute click actions without touching. Stare at the central circle and observe the fill circle animate."
            },
            style = MaterialTheme.typography.bodyMedium.copy(
                color = Color(0xFF94A3B8),
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            ),
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Large Interactive Dwell Button Simulator
        Box(
            modifier = Modifier
                .size(140.dp)
                .clip(CircleShape)
                .background(if (isLocking) Color(0xFF0F172A) else Color(0xFF1E293B))
                .border(3.dp, Color(0xFF00F0FF).copy(alpha = 0.3f), CircleShape)
                .clickable {
                    viewModel.triggerDwellStart()
                },
            contentAlignment = Alignment.Center
        ) {
            // Glow progression
            CircularProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxSize().scale(0.9f),
                color = Color(0xFF00F0FF),
                strokeWidth = 8.dp
            )

            if (progress >= 1f) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Success",
                    tint = Color(0xFF00FF88),
                    modifier = Modifier.size(48.dp)
                )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (isAr) "حدق هنا" else "Gaze Here",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        color = Color(0xFF00F0FF),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    )
                }
            }
        }
        
        if (progress > 0f) {
            TextButton(
                onClick = { viewModel.triggerDwellCancel() },
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text(if (isAr) "إلغاء التحديد" else "Cancel Dwell", color = Color.Red)
            }
        }
    }
}

@Composable
fun ExpressionsStep(isAr: Boolean) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = if (isAr) "إيماءات الوجه البديلة للمس" else "Facial Gestures Superpowers",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (isAr) {
                "التطبيق يتعرف تلقائياً على تعابير وجهك للتحكم بالتمرير، السحب، والرجوع للخلف:"
            } else {
                "The tracker continuously maps real-time facial micro-expressions to vital navigation macros:"
            },
            style = MaterialTheme.typography.bodyMedium.copy(
                color = Color(0xFF94A3B8),
                textAlign = TextAlign.Center
            ),
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Visual gesture dictionary list
        val gestureList = if (isAr) {
            listOf(
                "رفع الحواجب ⬆" to "التمرير لأعلى الصفحة (Scroll Up)",
                "تقطيب الحواجب ⬇" to "التمرير لأسفل الصفحة (Scroll Down)",
                "غمزة العين اليسرى 😉" to "الرجوع خطوة للخلف (Back Button)",
                "غمزة العين اليمنى 👁" to "الشاشة الرئيسية للجوال (Home Screen)",
                "فتح الفم 👄" to "سحب وإفلات العناصر أو فتح الكيبورد"
            )
        } else {
            listOf(
                "Raise Eyebrows ⬆" to "Auto-scroll page upwards",
                "Frown Eyebrows ⬇" to "Auto-scroll page downwards",
                "Left Eye Blink 😉" to "Navigate backward (Back Action)",
                "Right Eye Blink 👁" to "Instantly return home (Home Action)",
                "Open Mouth 👄" to "Drag and Drop overlay objects or toggle keyboard"
            )
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            gestureList.forEach { (title, desc) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF1E293B))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF00F0FF))
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(text = title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                        Text(text = desc, color = Color(0xFF94A3B8), fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun CalibrationOverlay(viewModel: MainViewModel, currentStep: Int) {
    Box(modifier = Modifier.fillMaxSize()) {
        Text(
            text = viewModel.getString("calibration_intro"),
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(bottom = 60.dp)
        )
        
        Text(
            text = viewModel.getString("calibration_desc"),
            style = MaterialTheme.typography.bodySmall,
            color = Color.LightGray,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(top = 40.dp)
                .padding(horizontal = 40.dp)
        )

        // Blinking Dot according to active step
        val dotAlignment = when (currentStep) {
            1 -> Alignment.TopCenter
            2 -> Alignment.CenterEnd
            3 -> Alignment.BottomCenter
            4 -> Alignment.CenterStart
            else -> Alignment.Center
        }

        var isBlinking by remember { mutableStateOf(true) }
        LaunchedEffect(currentStep) {
            while (true) {
                isBlinking = !isBlinking
                delay(300)
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (isBlinking) Color(0xFF00F0FF) else Color(0xFF0055AA)
                    )
                    .border(2.dp, Color.White, CircleShape)
                    .shadow(16.dp, CircleShape)
                    .align(dotAlignment),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                )
            }
        }
    }
}
