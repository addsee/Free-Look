package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.MainViewModel
import com.example.data.SettingsEntity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(viewModel: MainViewModel) {
    val isAr = viewModel.settingsState.collectAsState().value.language == "ar"
    val settings by viewModel.settingsState.collectAsState()
    val scrollState = rememberScrollState()
    
    val scope = rememberCoroutineScope()

    // Accessibility Service Simulator State
    var isServiceActiveSimulated by remember { mutableStateOf(false) }
    var serviceActivationDialogVisible by remember { mutableStateOf(false) }
    var isBackingUp by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Screen Title
        Text(
            text = viewModel.getString("settings"),
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White
            ),
            modifier = Modifier.fillMaxWidth()
        )

        // 1. Accessibility Service Activation Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            border = BorderStroke(1.dp, if (isServiceActiveSimulated) Color(0xFF10B981) else Color(0xFFEF4444).copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isAr) "خدمة إمكانية الوصول بالعين خلف الكواليس" else "Eyes Overlay Accessibility Service",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 14.sp
                    )
                    
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isServiceActiveSimulated) Color(0xFF10B981).copy(alpha = 0.15f)
                                else Color(0xFFEF4444).copy(alpha = 0.15f)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (isServiceActiveSimulated) (if (isAr) "نشط ومفعّل" else "Active") else (if (isAr) "غير مفعّل" else "Inactive"),
                            color = if (isServiceActiveSimulated) Color(0xFF10B981) else Color(0xFFEF4444),
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }
                }

                Text(
                    text = if (isAr) {
                        "تتيح هذه الخدمة لمؤشر تتبع العين بالطفو التام والتحكم بجميع التطبيقات الخارجية المثبتة على جوالك دون قيود."
                    } else {
                        "This background accessibility handler renders the system-wide overlay cursor and intercepts navigation on all outside apps."
                    },
                    color = Color.LightGray,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )

                Button(
                    onClick = {
                        if (!isServiceActiveSimulated) {
                            serviceActivationDialogVisible = true
                        } else {
                            isServiceActiveSimulated = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isServiceActiveSimulated) Color.Gray else Color(0xFF38BDF8),
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = if (isServiceActiveSimulated) Icons.Default.Cancel else Icons.Default.Power,
                        contentDescription = "Power"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isServiceActiveSimulated) {
                            if (isAr) "إيقاف الخدمة" else "Deactivate Background Service"
                        } else {
                            if (isAr) "تفعيل الخدمة الآن 🔓" else "Activate Accessibility Service 🔓"
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }

        // 2. Interactive Cursor Design Customizer (Feature 11)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF111827))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = viewModel.getString("cursor_customize_title"),
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                // Cursor style library list selector
                Text(text = viewModel.getString("cursor_style"), color = Color.Gray, fontSize = 12.sp)
                
                val styles = listOf(
                    "circle_pulse" to (if (isAr) "دائرة نابضة" else "Pulsing Circle"),
                    "hand" to (if (isAr) "يد دقيقة" else "Precision Hand"),
                    "target_ring" to (if (isAr) "حلقة هدف" else "Target Gaze Ring"),
                    "butterfly" to (if (isAr) "فراشة بصري" else "Sight Butterfly"),
                    "futuristic_arrow" to (if (isAr) "سهم مستقبلي" else "Cyber Arrow"),
                    "water_drop" to (if (isAr) "قطرة ندى" else "Dew Droplet")
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    styles.forEach { (styleId, styleName) ->
                        val isSelected = settings.cursorStyle == styleId
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) Color(0xFF00F0FF).copy(alpha = 0.2f) else Color(0xFF1E293B))
                                .border(
                                    1.dp,
                                    if (isSelected) Color(0xFF00F0FF) else Color.White.copy(alpha = 0.1f),
                                    RoundedCornerShape(20.dp)
                                )
                                .clickable { viewModel.updateCursorStyle(styleId) }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = styleName,
                                color = if (isSelected) Color(0xFF00F0FF) else Color.LightGray,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                // Size Slider
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = viewModel.getString("cursor_size"), color = Color.Gray, fontSize = 12.sp)
                        Text(text = "${settings.cursorSize.toInt()} dp", color = Color(0xFF00F0FF), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Slider(
                        value = settings.cursorSize,
                        onValueChange = { viewModel.updateCursorSize(it) },
                        valueRange = 24f..96f,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFF00F0FF),
                            activeTrackColor = Color(0xFF00F0FF)
                        )
                    )
                }

                // Glow Color Palettes
                Text(text = viewModel.getString("cursor_color"), color = Color.Gray, fontSize = 12.sp)
                val colorPalettes = listOf(
                    "#00F0FF" to Color(0xFF00F0FF), // Cyan
                    "#39FF14" to Color(0xFF39FF14), // Neon Green
                    "#BD00FF" to Color(0xFFBD00FF), // Electric Purple
                    "#FF3333" to Color(0xFFFF3333), // Solar Red
                    "#FFD700" to Color(0xFFFFD700), // Gold Yellow
                    "#FFFFFF" to Color.White
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    colorPalettes.forEach { (hex, color) ->
                        val isSelected = settings.cursorColorHex.equals(hex, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    3.dp,
                                    if (isSelected) Color.White else Color.Transparent,
                                    CircleShape
                                )
                                .clickable { viewModel.updateCursorColor(hex) }
                        )
                    }
                }

                // Cursor switches
                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                // Precision Gaze Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = viewModel.getString("double_cursor"),
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = if (isAr) "تراكب حلقة المؤقت الدائرية مع نقطة فريزر متناهية الصغر" else "Renders inner focusing point overlay",
                            color = Color.Gray,
                            fontSize = 11.sp
                        )
                    }
                    Switch(
                        checked = settings.doubleCursorEnabled,
                        onCheckedChange = { viewModel.toggleDoubleCursor(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF00F0FF))
                    )
                }
            }
        }

        // 3. System Utility Toggles (Feature 10, 12, 11)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF111827))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                
                // language switcher row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isAr) "تغيير لغة التطبيق بالكامل" else "Toggle App Language",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = if (isAr) "التبديل الفوري بين العربية والإنجليزية بالكامل" else "Switch instantly between Arabic and English",
                            color = Color.Gray,
                            fontSize = 11.sp
                        )
                    }
                    Button(
                        onClick = { viewModel.toggleLanguage() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                        border = BorderStroke(1.dp, Color(0xFF00F0FF))
                    ) {
                        Text(if (isAr) "EN" else "العربية", color = Color(0xFF00F0FF), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                // Click blocker toggle (Feature 10)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = viewModel.getString("click_blocker"),
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = if (isAr) "التمييز التلقائي بين القراءة وتأكيد النقر لتفادي الأخطاء" else "Intelligent dwell click anti-accidental filter",
                            color = Color.Gray,
                            fontSize = 11.sp
                        )
                    }
                    var clickBlockerLocal by remember { mutableStateOf(true) }
                    Switch(
                        checked = clickBlockerLocal,
                        onCheckedChange = { clickBlockerLocal = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF00F0FF))
                    )
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                // Battery Saver toggle (Feature 12)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = viewModel.getString("battery_saver"),
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = if (isAr) "التحكم باستخدام مستشعرات الحركة (الرأس) فقط لتوفير الطاقة" else "Use head accelerometer sensors only on low charge",
                            color = Color.Gray,
                            fontSize = 11.sp
                        )
                    }
                    Switch(
                        checked = settings.batterySaverEnabled,
                        onCheckedChange = { viewModel.toggleBatterySaver(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF00F0FF))
                    )
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                // Cloud Backup button (Feature 11)
                Button(
                    onClick = {
                        scope.launch {
                            isBackingUp = true
                            delay(2000)
                            isBackingUp = false
                            viewModel.performSimulatedGesture("DOUBLE_BLINK") // Show sidebar to confirm active status
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isBackingUp) {
                        CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(20.dp))
                    } else {
                        Icon(Icons.Default.CloudUpload, contentDescription = "Backup", tint = Color.Black)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isAr) "النسخ الاحتياطي السحابي الفوري" else "Sync Cloud Gaze Profile",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }

    // Service activation Dialog Simulator
    if (serviceActivationDialogVisible) {
        AlertDialog(
            onDismissRequest = { serviceActivationDialogVisible = false },
            containerColor = Color(0xFF1E293B),
            icon = { Icon(Icons.Default.Accessibility, contentDescription = "Accessibility", tint = Color(0xFF38BDF8)) },
            title = {
                Text(
                    text = if (isAr) "إعدادات إمكانية الوصول في الهاتف" else "Android Accessibility Service Setup",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = if (isAr) {
                            "سيقوم النظام الآن بتوجيهك إلى إعدادات الهاتف العامة. يرجى اتباع الخطوات:"
                        } else {
                            "System will now request to activate overlay accessibility privileges. Please follow:"
                        },
                        color = Color.LightGray,
                        fontSize = 13.sp
                    )
                    
                    val steps = if (isAr) {
                        listOf(
                            "١. انزل إلى خيار 'التطبيقات المثبتة' (Downloaded Apps)",
                            "٢. اضغط على خيار 'نظرة حرة' (Nazra Horra)",
                            "٣. قم بتمويل المفتاح إلى 'استخدام الخدمة' (Use Nazra Horra)"
                        )
                    } else {
                        listOf(
                            "1. Scroll down to 'Downloaded Apps / Services'",
                            "2. Click on 'Nazra Horra'",
                            "3. Toggle switch to 'On / Use Service'"
                        )
                    }

                    steps.forEach { step ->
                        Text(step, color = Color(0xFF00F0FF), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        isServiceActiveSimulated = true
                        serviceActivationDialogVisible = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00F0FF))
                ) {
                    Text(if (isAr) "محاكاة التفعيل الفوري" else "Simulate Activation", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { serviceActivationDialogVisible = false }) {
                    Text(if (isAr) "إلغاء" else "Cancel", color = Color.LightGray)
                }
            }
        )
    }
}
