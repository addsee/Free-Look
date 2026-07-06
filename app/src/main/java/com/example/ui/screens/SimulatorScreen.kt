package com.example.ui.screens

import android.Manifest
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.MainViewModel
import com.example.data.SettingsEntity
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.delay
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow

@OptIn(ExperimentalPermissionsApi::class, ExperimentalLayoutApi::class)
@Composable
fun SimulatorScreen(viewModel: MainViewModel) {
    val isAr = viewModel.settingsState.collectAsState().value.language == "ar"
    val cameraPermissionState = rememberPermissionState(permission = Manifest.permission.CAMERA)
    
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Feature Header
        Text(
            text = viewModel.getString("floating_cursor_preview"),
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White
            ),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Start
        )

        // The Hands-Free Virtual Screen Gaze Sandbox
        InteractiveGazeSandbox(viewModel = viewModel)

        // Hardware CameraX Preview Section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (cameraPermissionState.status.isGranted) {
                    // Real working CameraX view
                    CameraPreviewView()
                    
                    // Superimposed Face Wireframe Mesh Overlay
                    FaceMeshOverlay()
                } else {
                    // Fallback visual illustration prompting permission
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.VideocamOff,
                            contentDescription = "Camera Disabled",
                            tint = Color.Red,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = viewModel.getString("camera_not_granted"),
                            color = Color.LightGray,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { cameraPermissionState.launchPermissionRequest() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8))
                        ) {
                            Text(if (isAr) "منح الإذن المباشر" else "Grant Permission", color = Color.Black)
                        }
                    }
                }
            }
        }

        // Virtual Headposture & Calibration Trackpad
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF111827))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = viewModel.getString("move_cursor_control"),
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 14.sp
                )
                
                Text(
                    text = if (isAr) {
                        "اسحب إصبعك على لوحة اللمس لمحاكاة حركة العين، أو انقر على أزرار التوجيه لمحاكاة إمالة الرأس (Pitch / Yaw)."
                    } else {
                        "Drag your finger on the trackpad to simulate eye gaze, or tap directional buttons to simulate precise head posture tilts."
                    },
                    color = Color.LightGray,
                    fontSize = 12.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Touch Trackpad
                    Box(
                        modifier = Modifier
                            .weight(1.2f)
                            .height(150.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF1E293B))
                            .border(1.dp, Color(0xFF00F0FF).copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .pointerInput(Unit) {
                                detectDragGestures(
                                    onDragStart = { viewModel.triggerDwellStart() },
                                    onDragEnd = { viewModel.triggerDwellCancel() },
                                    onDragCancel = { viewModel.triggerDwellCancel() },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        // Update simulated gaze positions
                                        viewModel.moveCursor(dragAmount.x / 10f, dragAmount.y / 10f)
                                    }
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.AdsClick,
                                contentDescription = "Trackpad",
                                tint = Color(0xFF00F0FF),
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isAr) "لوحة تتبع العين الافتراضية" else "Eye Gaze Touchpad",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isAr) "اسحب لتوجيه المؤشر" else "Drag to guide Gaze",
                                color = Color.Gray,
                                fontSize = 10.sp
                            )
                        }
                    }

                    // Directional posturing controller buttons
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        IconButton(
                            onClick = { viewModel.moveCursor(0f, -5f) },
                            modifier = Modifier.background(Color(0xFF1F2937), CircleShape)
                        ) {
                            Icon(Icons.Default.ArrowDropUp, "Up", tint = Color.White)
                        }
                        
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            IconButton(
                                onClick = { viewModel.moveCursor(-5f, 0f) },
                                modifier = Modifier.background(Color(0xFF1F2937), CircleShape)
                            ) {
                                Icon(Icons.Default.ArrowLeft, "Left", tint = Color.White)
                            }
                            IconButton(
                                onClick = { viewModel.moveCursor(5f, 0f) },
                                modifier = Modifier.background(Color(0xFF1F2937), CircleShape)
                            ) {
                                Icon(Icons.Default.ArrowRight, "Right", tint = Color.White)
                            }
                        }

                        IconButton(
                            onClick = { viewModel.moveCursor(0f, 5f) },
                            modifier = Modifier.background(Color(0xFF1F2937), CircleShape)
                        ) {
                            Icon(Icons.Default.ArrowDropDown, "Down", tint = Color.White)
                        }
                    }
                }
            }
        }

        // Test Facial Expression Triggers Panel
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF111827))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = viewModel.getString("test_gestures"),
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 14.sp
                )

                val gestures = listOf(
                    Triple("RAISE_EYEBROWS", if (isAr) "رفع الحاجبين ⬆" else "Raise Eyebrows ⬆", Color(0xFF10B981)),
                    Triple("FROWN_EYEBROWS", if (isAr) "تقطيب الحاجبين ⬇" else "Frown Eyebrows ⬇", Color(0xFFF59E0B)),
                    Triple("LEFT_BLINK", if (isAr) "غمزة العين اليسرى 😉" else "Left Blink 😉", Color(0xFF3B82F6)),
                    Triple("RIGHT_BLINK", if (isAr) "غمزة العين اليمنى 👁" else "Right Blink 👁", Color(0xFF8B5CF6)),
                    Triple("DOUBLE_BLINK", if (isAr) "غمزة مزدوجة 📱" else "Double Blink 📱", Color(0xFFEC4899)),
                    Triple("OPEN_MOUTH", if (isAr) "فتح الفم 👄" else "Open Mouth 👄", Color(0xFF14B8A6)),
                    Triple("PUFF_CHEEKS", if (isAr) "نفخ الخدين 📸" else "Puff Cheeks 📸", Color(0xFFEF4444)),
                    Triple("SMILE", if (isAr) "ابتسامة عريضة ☺" else "Big Smile ☺", Color(0xFFF43F5E))
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    gestures.forEach { (action, label, color) ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(color.copy(alpha = 0.15f))
                                .border(1.dp, color, RoundedCornerShape(20.dp))
                                .clickable {
                                    viewModel.performSimulatedGesture(action)
                                }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(text = label, color = color, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InteractiveGazeSandbox(viewModel: MainViewModel) {
    val isAr = viewModel.settingsState.collectAsState().value.language == "ar"
    val cursorPos by viewModel.cursorPos.collectAsState()
    val dwellProgress by viewModel.dwellProgress.collectAsState()
    val isLocking by viewModel.isLocking.collectAsState()
    val settings by viewModel.settingsState.collectAsState()
    
    val gameActive by viewModel.isGameActive.collectAsState()
    val gameTarget by viewModel.gameTarget.collectAsState()

    // Floating Sandbox Area
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .border(2.dp, Color(0xFF00F0FF).copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0D131F))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            
            // Decorative digital canvas elements
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(Color(0xFF1E293B).copy(alpha = 0.5f), Color.Transparent),
                            radius = 400f
                        )
                    )
            )

            // Target trigger box for Gaze-to-Speak Screen Reader
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(60.dp)
                    .align(Alignment.TopCenter)
                    .offset(y = 24.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1E293B))
                    .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isAr) "حدق هنا لقراءة النص بصوت عالٍ 🗣" else "Gaze here to read text aloud 🗣",
                    color = Color.LightGray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(8.dp)
                )
            }

            // Display Spoken Text if screen reader triggered
            val spokenText by viewModel.spokenText.collectAsState()
            if (spokenText.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .align(Alignment.BottomCenter)
                        .offset(y = (-16).dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF059669).copy(alpha = 0.9f))
                        .padding(8.dp)
                ) {
                    Text(
                        text = "🗣: $spokenText",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Active Game Target Dot (if training game is active)
            if (gameActive) {
                Box(
                    modifier = Modifier
                        .offset(x = (gameTarget.x * 2.8f).dp, y = (gameTarget.y * 1.5f).dp)
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFF3366).copy(alpha = 0.3f))
                        .border(2.dp, Color(0xFFFF3366), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFF3366))
                    )
                }
            }

            // The Floating Cursor itself, drawn dynamically based on settings
            FloatingGazeCursor(
                x = cursorPos.x,
                y = cursorPos.y,
                progress = dwellProgress,
                isLocking = isLocking,
                settings = settings
            )
        }
    }
}

@Composable
fun FloatingGazeCursor(
    x: Float,
    y: Float,
    progress: Float,
    isLocking: Boolean,
    settings: SettingsEntity
) {
    val cursorColor = Color(android.graphics.Color.parseColor(settings.cursorColorHex))
    val cursorSize = settings.cursorSize.dp
    val opacity = settings.cursorOpacity

    // Scale dynamically using state offsets
    Box(
        modifier = Modifier
            .offset(x = (x * 3.2f).dp, y = (y * 1.8f).dp)
            .size(cursorSize)
            .shadow(if (settings.glowEnabled) 12.dp else 0.dp, CircleShape, spotColor = cursorColor),
        contentAlignment = Alignment.Center
    ) {
        // Double cursor mode: Outer Dwell progress circle
        CircularProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxSize(),
            color = cursorColor,
            trackColor = cursorColor.copy(alpha = 0.2f),
            strokeWidth = 4.dp
        )

        // Cursor style definitions
        when (settings.cursorStyle) {
            "circle_pulse" -> {
                Box(
                    modifier = Modifier
                        .size(cursorSize * 0.4f)
                        .clip(CircleShape)
                        .background(cursorColor.copy(alpha = opacity))
                        .border(1.dp, Color.White, CircleShape)
                )
            }
            "hand" -> {
                Icon(
                    imageVector = Icons.Default.FrontHand,
                    contentDescription = "Hand Cursor",
                    tint = cursorColor,
                    modifier = Modifier.size(cursorSize * 0.6f)
                )
            }
            "target_ring" -> {
                Box(
                    modifier = Modifier
                        .size(cursorSize * 0.5f)
                        .border(2.dp, cursorColor, CircleShape)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .align(Alignment.Center)
                    )
                }
            }
            "butterfly" -> {
                Icon(
                    imageVector = Icons.Default.FlutterDash,
                    contentDescription = "Butterfly Gaze cursor",
                    tint = cursorColor,
                    modifier = Modifier.size(cursorSize * 0.65f)
                )
            }
            "futuristic_arrow" -> {
                Icon(
                    imageVector = Icons.Default.Navigation,
                    contentDescription = "Arrow Cursor",
                    tint = cursorColor,
                    modifier = Modifier
                        .size(cursorSize * 0.6f)
                        .rotate(45f)
                )
            }
            "water_drop" -> {
                Icon(
                    imageVector = Icons.Default.WaterDrop,
                    contentDescription = "Water drop cursor",
                    tint = cursorColor,
                    modifier = Modifier.size(cursorSize * 0.6f)
                )
            }
        }

        // Inner absolute precision target dot
        if (settings.doubleCursorEnabled) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(Color.White)
            )
        }
    }
}

@Composable
fun CameraPreviewView() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    AndroidView(
        factory = { ctx ->
            PreviewView(ctx).apply {
                scaleType = PreviewView.ScaleType.FILL_CENTER
            }
        },
        modifier = Modifier.fillMaxSize(),
        update = { previewView ->
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.surfaceProvider = previewView.surfaceProvider
                }
                
                val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview
                    )
                } catch (exc: Exception) {
                    // Fail gracefully
                }
            }, ContextCompat.getMainExecutor(context))
        }
    )
}

@Composable
fun FaceMeshOverlay() {
    var stepAngle by remember { mutableStateOf(0f) }
    
    LaunchedEffect(Unit) {
        while (true) {
            stepAngle += 10f
            if (stepAngle >= 360f) stepAngle = 0f
            delay(150)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .border(2.dp, Color(0xFF10B981).copy(alpha = 0.4f))
    ) {
        // Scanning Tech lines
        Box(
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .fillMaxHeight(0.7f)
                .align(Alignment.Center)
                .border(1.5.dp, Color(0xFF10B981), RoundedCornerShape(16.dp))
        ) {
            // Horizontal laser sweep
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .align(Alignment.Center)
                    .background(Color(0xFF10B981))
            )

            // Scanning Status blink text
            Text(
                text = "EYE SCAN [ONLINE]",
                color = Color(0xFF10B981),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 4.dp)
            )
        }

        // Mini status indicators
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomStart)
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("FPS: 60", color = Color(0xFF10B981), fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
            Text("LATENCY: 8ms", color = Color(0xFF10B981), fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}
