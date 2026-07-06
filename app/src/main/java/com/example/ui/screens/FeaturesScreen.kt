package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import kotlinx.coroutines.launch
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.MainViewModel
import com.example.data.NoteEntity
import kotlinx.coroutines.delay

@Composable
fun FeaturesScreen(viewModel: MainViewModel) {
    val isAr = viewModel.settingsState.collectAsState().value.language == "ar"
    
    val sidebarVisible by viewModel.sidebarVisible.collectAsState()
    val keyboardVisible by viewModel.keyboardVisible.collectAsState()
    val magnifierVisible by viewModel.magnifierVisible.collectAsState()
    
    val sosActive by viewModel.sosActive.collectAsState()
    val fatigueAlertVisible by viewModel.fatigueAlertVisible.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Screen Title
            Text(
                text = viewModel.getString("superpowers"),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
            
            Text(
                text = viewModel.getString("superpowers_desc"),
                color = Color.LightGray,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )

            // Horizontal Tab view for subsections to make layout tidy
            var subTab by remember { mutableStateOf(0) }
            val subTabTitles = if (isAr) {
                listOf("الاتصالات والمنزل 🏠", "لوحة المفاتيح والعدسة ⌨", "الألعاب والتدريب 🎯")
            } else {
                listOf("Home & Contacts 🏠", "Keyboard & Zoom ⌨", "Games & Coach 🎯")
            }

            TabRow(
                selectedTabIndex = subTab,
                containerColor = Color(0xFF1E293B),
                contentColor = Color(0xFF00F0FF),
                modifier = Modifier.clip(RoundedCornerShape(8.dp))
            ) {
                subTabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = subTab == index,
                        onClick = { subTab = index },
                        text = { Text(title, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                    )
                }
            }

            // Sub Tab Switcher
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (subTab) {
                    0 -> HomeAndContactsTab(viewModel, isAr)
                    1 -> KeyboardAndMagnifierTab(viewModel, isAr)
                    2 -> GamesAndTrainingTab(viewModel, isAr)
                }
            }
        }

        // Overlay Panels
        // 1. Sliding Contact Sidebar (Double Blink activation)
        AnimatedVisibility(
            visible = sidebarVisible,
            enter = slideInHorizontally(initialOffsetX = { if (isAr) -it else it }),
            exit = slideOutHorizontally(targetOffsetX = { if (isAr) -it else it }),
            modifier = Modifier
                .fillMaxHeight()
                .width(180.dp)
                .background(Color(0xFF0F172A))
                .border(1.dp, Color(0xFF38BDF8).copy(alpha = 0.4f))
                .align(if (isAr) Alignment.CenterStart else Alignment.CenterEnd)
        ) {
            ContactSidebarContent(viewModel, isAr)
        }

        // 2. SOS Emergency Countdown Overlay
        if (sosActive) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Red.copy(alpha = 0.95f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "SOS",
                        tint = Color.White,
                        modifier = Modifier.size(100.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = if (isAr) "تنبيه طوارئ نشط! SOS" else "EMERGENCY ALERT TRIGGERED!",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (isAr) {
                            "جاري إرسال موقعك الجغرافي والاتصال بأرقام الطوارئ المفضلة تلقائياً..."
                        } else {
                            "Broadcasting coordinates to emergency services and dialing primary contact..."
                        },
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            }
        }

        // 3. Eye Fatigue Warning Dialog
        if (fatigueAlertVisible) {
            AlertDialog(
                onDismissRequest = { viewModel.dismissFatigueAlert() },
                containerColor = Color(0xFF1E293B),
                icon = { Icon(Icons.Default.BatteryAlert, contentDescription = "Eye Strain", tint = Color.Yellow) },
                title = {
                    Text(
                        if (isAr) "تنبيه إجهاد العين المفرط!" else "Critical Eye Fatigue Detected!",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            if (isAr) {
                                "لاحظت خوارزميات التتبع انخفاضاً حاداً في وتيرة رمش العين، وزيادة في دقة التحديق المستمر."
                            } else {
                                "gaze telemetry reports high tracking duration and low eye blink metrics."
                            },
                            color = Color.LightGray
                        )
                        Text(
                            if (isAr) {
                                "يرجى غلق العينين لمدة ١٠ ثوانٍ، أو القيام بتمارين العين المبينة أدناه:"
                            } else {
                                "Please take a short eye exercise to prevent strain:"
                            },
                            color = Color.LightGray,
                            fontWeight = FontWeight.Bold
                        )
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                        ) {
                            Text(
                                if (isAr) "👁 حركة دائرية للعين ٣ مرات باتجاه عقارب الساعة" else "👁 Rotate eyes clockwise 3 times",
                                color = Color(0xFF00F0FF),
                                modifier = Modifier.padding(12.dp),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.dismissFatigueAlert() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00F0FF))
                    ) {
                        Text(if (isAr) "تم الاستراحة 😌" else "Rested 😌", color = Color.Black)
                    }
                }
            )
        }
    }
}

@Composable
fun HomeAndContactsTab(viewModel: MainViewModel, isAr: Boolean) {
    val contacts by viewModel.contactsState.collectAsState()
    val smartLights by viewModel.smartHomeLights.collectAsState()
    val smartAC by viewModel.smartHomeAC.collectAsState()
    val smartDoorLocked by viewModel.smartHomeDoorLocked.collectAsState()
    val sidebarVisible by viewModel.sidebarVisible.collectAsState()

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        // Smart Home IoT Section (Feature 23)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = viewModel.getString("smart_home_title"),
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Icon(imageVector = Icons.Default.Home, contentDescription = "IoT", tint = Color(0xFF00F0FF))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Light Switcher
                        IoTCard(
                            title = if (isAr) "إنارة المعيشة" else "Living Room Light",
                            isActive = smartLights,
                            icon = Icons.Default.Lightbulb,
                            activeColor = Color.Yellow,
                            modifier = Modifier.weight(1f)
                        ) {
                            viewModel.toggleSmartHomeDevice("LIGHTS")
                        }

                        // AC Switcher
                        IoTCard(
                            title = if (isAr) "التكييف" else "AC Cooling",
                            isActive = smartAC,
                            icon = Icons.Default.AcUnit,
                            activeColor = Color(0xFF38BDF8),
                            modifier = Modifier.weight(1f)
                        ) {
                            viewModel.toggleSmartHomeDevice("AC")
                        }

                        // Door Switcher
                        IoTCard(
                            title = if (isAr) "قفل الباب" else "Door Lock",
                            isActive = !smartDoorLocked,
                            icon = if (smartDoorLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                            activeColor = Color.Red,
                            modifier = Modifier.weight(1f)
                        ) {
                            viewModel.toggleSmartHomeDevice("DOOR")
                        }
                    }
                }
            }
        }

        // Rapid Contacts Section (Feature 4)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = viewModel.getString("contacts_sidebar"),
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        IconButton(
                            onClick = { viewModel.performSimulatedGesture("DOUBLE_BLINK") },
                            colors = IconButtonDefaults.iconButtonColors(containerColor = Color(0xFF00F0FF).copy(alpha = 0.15f))
                        ) {
                            Icon(
                                imageVector = if (sidebarVisible) Icons.Default.Close else Icons.Default.Menu,
                                contentDescription = "Sidebar Toggle",
                                tint = Color(0xFF00F0FF)
                            )
                        }
                    }

                    Text(
                        text = if (isAr) {
                            "اضغط مرتين بالغمز السريع لتشغيل الشريط الجانبي الفوري، أو تفاعل مع جهات الاتصال أدناه:"
                        } else {
                            "Gaze and double-blink to trigger the overlay drawer. Explore default favorite contact shortcuts:"
                        },
                        color = Color.LightGray,
                        fontSize = 12.sp
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        contacts.forEach { contact ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF0F172A))
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val color = Color(android.graphics.Color.parseColor(contact.avatarColorHex))
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(color),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = contact.name.take(1),
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(text = contact.name, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                        Text(text = contact.phone, color = Color.Gray, fontSize = 12.sp)
                                    }
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    IconButton(
                                        onClick = { viewModel.performSimulatedGesture("LEFT_BLINK") },
                                        colors = IconButtonDefaults.iconButtonColors(containerColor = Color(0xFF10B981).copy(alpha = 0.2f))
                                    ) {
                                        Icon(Icons.Default.Phone, "Call", tint = Color(0xFF10B981))
                                    }
                                    IconButton(
                                        onClick = { viewModel.performSimulatedGesture("RIGHT_BLINK") },
                                        colors = IconButtonDefaults.iconButtonColors(containerColor = Color(0xFF25D366).copy(alpha = 0.2f))
                                    ) {
                                        Icon(Icons.Default.Chat, "WhatsApp", tint = Color(0xFF25D366))
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

@Composable
fun IoTCard(
    title: String,
    isActive: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    activeColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) activeColor.copy(alpha = 0.15f) else Color(0xFF0D131F)
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
            .border(
                1.dp,
                if (isActive) activeColor else Color.White.copy(alpha = 0.1f),
                RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isActive) activeColor else Color.Gray,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                color = if (isActive) Color.White else Color.Gray,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun KeyboardAndMagnifierTab(viewModel: MainViewModel, isAr: Boolean) {
    val coroutineScope = rememberCoroutineScope()
    val typedText by viewModel.typedText.collectAsState()
    val notes by viewModel.notesState.collectAsState()
    
    val words = if (isAr) {
        listOf("السلام", "عليكم", "نظرة", "حرة", "أنا", "بخير", "طائرة", "جوال")
    } else {
        listOf("Hello", "Welcome", "Nazra", "Horra", "I am", "Fine", "Smart", "Gaze")
    }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        // Gaze Direct Notepad & Voice dictation (Feature 1, 24)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = viewModel.getString("keyboard_title"),
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Icon(imageVector = Icons.Default.Keyboard, contentDescription = "Keyboard", tint = Color(0xFF00F0FF))
                    }

                    // Simulated Screen Type output
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF0F172A))
                            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (typedText.isEmpty()) {
                            Text(
                                if (isAr) "انظر للأحرف في الأسفل للكتابة..." else "Gaze at letters below to type...",
                                color = Color.Gray,
                                fontSize = 12.sp
                            )
                        } else {
                            Text(typedText, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Autocomplete Suggestions row
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        words.forEach { word ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0xFF10B981).copy(alpha = 0.1f))
                                    .border(1.dp, Color(0xFF10B981).copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                                    .clickable { viewModel.keyboardTypeCharacter("$word ") }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(word, color = Color(0xFF10B981), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }

                    // Simulated Keyboard Matrix Grid (Arabic & English adaptive)
                    val letters = if (isAr) {
                        listOf(
                            listOf("أ", "ب", "ت", "ث", "ج", "ح", "خ"),
                            listOf("د", "ذ", "ر", "ز", "س", "ش", "ص"),
                            listOf("ض", "ط", "ظ", "ع", "غ", "ف", "ق"),
                            listOf("ك", "ل", "م", "ن", "هـ", "و", "ي")
                        )
                    } else {
                        listOf(
                            listOf("A", "B", "C", "D", "E", "F", "G"),
                            listOf("H", "I", "J", "K", "L", "M", "N"),
                            listOf("O", "P", "Q", "R", "S", "T", "U"),
                            listOf("V", "W", "X", "Y", "Z", "SPC", "DEL")
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        letters.forEach { row ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                row.forEach { char ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(36.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFF0D131F))
                                            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                            .clickable {
                                                if (char == "SPC") viewModel.keyboardTypeCharacter(" ")
                                                else if (char == "DEL") viewModel.keyboardDelete()
                                                else viewModel.keyboardTypeCharacter(char)
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = char,
                                            color = if (char == "SPC" || char == "DEL") Color(0xFF00F0FF) else Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Control actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.saveTypedNote() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00F0FF)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Save, contentDescription = "Save")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (isAr) "حفظ الملاحظة" else "Save Note", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        Button(
                            onClick = { viewModel.keyboardClear() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.2f), contentColor = Color.Red),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(0.5f)
                        ) {
                            Text(if (isAr) "مسح" else "Clear", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Live Gaze Notes listing from Room
        if (notes.isNotEmpty()) {
            item {
                Text(if (isAr) "الملاحظات المحفوظة" else "Saved Gaze Notes", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
            items(notes) { note ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(note.content, color = Color.White, fontSize = 13.sp)
                        IconButton(onClick = {
                            coroutineScope.launch {
                                viewModel.repository.deleteNote(note)
                            }
                        }) {
                            Icon(Icons.Default.Delete, "Delete", tint = Color.Red.copy(alpha = 0.7f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GamesAndTrainingTab(viewModel: MainViewModel, isAr: Boolean) {
    val isGameActive by viewModel.isGameActive.collectAsState()
    val score by viewModel.gameScore.collectAsState()
    val timeLeft by viewModel.gameTimeLeft.collectAsState()
    val trainingHistory by viewModel.trainingState.collectAsState()
    val fatigueScore by viewModel.eyeFatigueScore.collectAsState()

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        // Daily Precision Training Games Dashboard (Feature 30)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = viewModel.getString("training_coach_title"),
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Icon(imageVector = Icons.Default.EmojiEvents, contentDescription = "Coach", tint = Color(0xFFFFD700))
                    }

                    Text(
                        text = if (isAr) {
                            "نمّ مهارات تتبع العين والسرعة لديك عبر تحديات تتبع الأهداف العشوائية لتعزيز التركيز البصري."
                        } else {
                            "Enhance your eye movement speed and zoom accuracy with interactive targets pursuit exercises."
                        },
                        color = Color.LightGray,
                        fontSize = 12.sp
                    )

                    // Active Game Controls
                    if (isGameActive) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF0F172A))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = if (isAr) "النقاط المجمعة: $score" else "Current Score: $score",
                                    color = Color(0xFF00F0FF),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = if (isAr) "الوقت المتبقي: $timeLeft ث" else "Time Left: $timeLeft s",
                                    color = Color.Yellow,
                                    fontSize = 12.sp
                                )
                            }
                            Button(
                                onClick = { viewModel.stopTrainingGame() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                            ) {
                                Text(if (isAr) "إنهاء التحدي" else "Stop", fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        Button(
                            onClick = { viewModel.startTrainingGame() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00F0FF)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = Color.Black)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (isAr) "ابدأ تحدي صيد الأهداف بصرياً 🎯" else "Start Gaze Pursuit Challenge 🎯", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Fatigue telemetry controller
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(if (isAr) "محاكي إجهاد وتعب العين (مؤقت الإرهاق)" else "Eye Fatigue Telemetry Engine", color = Color.White, fontWeight = FontWeight.Bold)
                    Text(
                        if (isAr) "يقوم التطبيق بتحليل معدل الرمش ومسار تتبع الحركة لاكتشاف الإرهاق تلقائياً." else "Telemetry analyzes micro blink count and continuous tracking path to compute strain metrics.",
                        color = Color.LightGray,
                        fontSize = 11.sp
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(if (isAr) "مؤشر التعب: $fatigueScore%" else "Fatigue Score: $fatigueScore%", color = if (fatigueScore > 75) Color.Red else Color.Green, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(if (isAr) "رمشات العين: ${viewModel.blinkCount.collectAsState().value}" else "Blink Count: ${viewModel.blinkCount.collectAsState().value}", color = Color.Gray, fontSize = 12.sp)
                        }
                        Button(
                            onClick = { viewModel.simulateEyeFatigueIncrease() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8).copy(alpha = 0.15f), contentColor = Color(0xFF38BDF8)),
                            border = BorderStroke(1.dp, Color(0xFF38BDF8))
                        ) {
                            Text(if (isAr) "محاكاة قراءة طويلة 👁" else "Simulate Reading strain 👁", fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // Dynamic badges earned logs
        if (trainingHistory.isNotEmpty()) {
            item {
                Text(if (isAr) "سجل أداء التدريب والجوائز" else "Precision Training Leaderboard", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
            items(trainingHistory) { log ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(log.challengeName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(if (isAr) "وسام: ${log.badgeEarned}" else "Badge: ${log.badgeEarned}", color = Color(0xFFFFD700), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Text("${log.score} pts", color = Color(0xFF00F0FF), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun ContactSidebarContent(viewModel: MainViewModel, isAr: Boolean) {
    val contacts by viewModel.contactsState.collectAsState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = if (isAr) "اتصال سريع 📞" else "Favorites 📞",
                color = Color(0xFF00F0FF),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier.padding(vertical = 12.dp)
            )

            contacts.forEach { contact ->
                val color = Color(android.graphics.Color.parseColor(contact.avatarColorHex))
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF1E293B))
                        .clickable { viewModel.performSimulatedGesture("LEFT_BLINK") }
                        .padding(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(color),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(contact.name.take(1), color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(contact.name, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                }
            }
        }

        // Close sidebar action trigger
        TextButton(
            onClick = { viewModel.performSimulatedGesture("DOUBLE_BLINK") }
        ) {
            Text(if (isAr) "إغلاق الشريط ❌" else "Dismiss ❌", color = Color.Red, fontSize = 11.sp)
        }
    }
}
