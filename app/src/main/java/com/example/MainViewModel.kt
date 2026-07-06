package com.example

import android.app.Application
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.example.data.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = Room.databaseBuilder(
        application,
        AppDatabase::class.java,
        "nazra_horra_db"
    ).build()

    val repository = AppRepository(db)

    // Current screen index: 0: Onboarding, 1: Control Simulator, 2: 30 Superpowers, 3: Customizer & Settings
    private val _currentTab = MutableStateFlow(0)
    val currentTab: StateFlow<Int> = _currentTab.asStateFlow()

    // Cursor position (normalized percentage of simulation canvas 0f..100f)
    private val _cursorPos = MutableStateFlow(Offset(50f, 40f))
    val cursorPos: StateFlow<Offset> = _cursorPos.asStateFlow()

    // Simulated Head pitch/yaw coordinates for fine control
    private val _headTilt = MutableStateFlow(Offset(0f, 0f))
    val headTilt: StateFlow<Offset> = _headTilt.asStateFlow()

    // Gaze/Dwell progress (0.0 to 1.0)
    private val _dwellProgress = MutableStateFlow(0f)
    val dwellProgress: StateFlow<Float> = _dwellProgress.asStateFlow()

    // Is eye-dwell locking / clicking in progress
    private val _isLocking = MutableStateFlow(false)
    val isLocking: StateFlow<Boolean> = _isLocking.asStateFlow()

    // Active cursor settings (cached from db)
    val settingsState: StateFlow<SettingsEntity> = repository.settingsFlow
        .map { it ?: SettingsEntity() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsEntity())

    // Active Keyboard State
    private val _keyboardVisible = MutableStateFlow(false)
    val keyboardVisible: StateFlow<Boolean> = _keyboardVisible.asStateFlow()

    private val _typedText = MutableStateFlow("")
    val typedText: StateFlow<String> = _typedText.asStateFlow()

    // Active Magnifier Screen State
    private val _magnifierVisible = MutableStateFlow(false)
    val magnifierVisible: StateFlow<Boolean> = _magnifierVisible.asStateFlow()

    // Alternate Screen Reader Speaked Text
    private val _spokenText = MutableStateFlow("")
    val spokenText: StateFlow<String> = _spokenText.asStateFlow()

    // Contacts
    val contactsState = repository.contactsFlow.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    // Notes
    val notesState = repository.notesFlow.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    // Macros
    val macrosState = repository.macrosFlow.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    // Training progression
    val trainingState = repository.trainingFlow.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    // Smart Home status simulation
    private val _smartHomeLights = MutableStateFlow(false)
    val smartHomeLights = _smartHomeLights.asStateFlow()

    private val _smartHomeAC = MutableStateFlow(false)
    val smartHomeAC = _smartHomeAC.asStateFlow()

    private val _smartHomeDoorLocked = MutableStateFlow(true)
    val smartHomeDoorLocked = _smartHomeDoorLocked.asStateFlow()

    // Sidebar panel visible
    private val _sidebarVisible = MutableStateFlow(false)
    val sidebarVisible = _sidebarVisible.asStateFlow()

    // Game variables
    private val _gameTarget = MutableStateFlow(Offset(30f, 30f))
    val gameTarget = _gameTarget.asStateFlow()

    private val _gameScore = MutableStateFlow(0)
    val gameScore = _gameScore.asStateFlow()

    private val _gameTimeLeft = MutableStateFlow(30)
    val gameTimeLeft = _gameTimeLeft.asStateFlow()

    private val _isGameActive = MutableStateFlow(false)
    val isGameActive = _isGameActive.asStateFlow()

    // Eye fatigue variables
    private val _eyeFatigueScore = MutableStateFlow(12) // out of 100
    val eyeFatigueScore = _eyeFatigueScore.asStateFlow()

    private val _blinkCount = MutableStateFlow(42)
    val blinkCount = _blinkCount.asStateFlow()

    private val _fatigueAlertVisible = MutableStateFlow(false)
    val fatigueAlertVisible = _fatigueAlertVisible.asStateFlow()

    // SOS Mode Triggered Overlay
    private val _sosActive = MutableStateFlow(false)
    val sosActive = _sosActive.asStateFlow()

    // Calibration Progress Step
    private val _calibrationStep = MutableStateFlow(0) // 0: not calibrating, 1-5 dots
    val calibrationStep = _calibrationStep.asStateFlow()

    // Toast/Status overlay text
    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage = _toastMessage.asStateFlow()

    init {
        viewModelScope.launch {
            repository.prepopulateIfEmpty()
            // Dwell clicking automated monitor loop
            monitorDwellClick()
        }
    }

    fun selectTab(index: Int) {
        _currentTab.value = index
    }

    // Move cursor manually or simulated head tilts
    fun moveCursor(dx: Float, dy: Float) {
        val current = _cursorPos.value
        val boundsMax = 100f
        val newX = (current.x + dx).coerceIn(5f, boundsMax - 5f)
        val newY = (current.y + dy).coerceIn(5f, boundsMax - 5f)
        _cursorPos.value = Offset(newX, newY)
        _headTilt.value = Offset(dx, dy)
    }

    // Simulated Eye / Gaze dwell logic
    fun triggerDwellStart() {
        if (_isLocking.value) return
        viewModelScope.launch {
            _isLocking.value = true
            val duration = settingsState.value.gazeDurationMs.toFloat()
            val steps = 20
            val delayStep = (duration / steps).toLong()
            for (i in 1..steps) {
                if (!_isLocking.value) break
                _dwellProgress.value = i.toFloat() / steps
                delay(delayStep)
            }
            if (_isLocking.value) {
                executeActionAtCursor()
                _dwellProgress.value = 0f
                _isLocking.value = false
            }
        }
    }

    fun triggerDwellCancel() {
        _isLocking.value = false
        _dwellProgress.value = 0f
    }

    private fun executeActionAtCursor() {
        // Trigger haptics and sounds if configured
        showToast(if (settingsState.value.language == "ar") "تم النقر بنجاح!" else "Clicked Successfully!")
        
        // Simulating smart target recognition at cursor location
        val cx = _cursorPos.value.x
        val cy = _cursorPos.value.y

        // Check if hitting the screen reader trigger target
        if (cx in 20f..80f && cy in 20f..45f) {
            val textToSpeak = if (settingsState.value.language == "ar") {
                "مرحباً بك في تطبيق نظرة حرة للمساعدة البصرية"
            } else {
                "Welcome to Nazra Horra App for Visual Assistance"
            }
            _spokenText.value = textToSpeak
        }

        // Check if hitting Game Target
        if (_isGameActive.value) {
            val tx = _gameTarget.value.x
            val ty = _gameTarget.value.y
            if ((cx - tx).absoluteValue < 12f && (cy - ty).absoluteValue < 12f) {
                _gameScore.value += 10
                _gameTarget.value = Offset(
                    (10..90).random().toFloat(),
                    (20..80).random().toFloat()
                )
                showToast(if (settingsState.value.language == "ar") "إصابة ممتازة! +١٠ نقاط" else "Perfect Hit! +10 Points")
            }
        }
    }

    // Trigger gestures directly from simulator control panel
    fun performSimulatedGesture(gesture: String) {
        val isAr = settingsState.value.language == "ar"
        when (gesture) {
            "RAISE_EYEBROWS" -> {
                showToast(if (isAr) "تم التمرير لأعلى (سكرول ⬆)" else "Scrolled Up ⬆")
            }
            "FROWN_EYEBROWS" -> {
                showToast(if (isAr) "تم التمرير لأسفل (سكرول ⬇)" else "Scrolled Down ⬇")
            }
            "LEFT_BLINK" -> {
                showToast(if (isAr) "إجراء: رجوع ⬅" else "Action: Back ⬅")
            }
            "RIGHT_BLINK" -> {
                showToast(if (isAr) "إجراء: الشاشة الرئيسية 🏠" else "Action: Home 🏠")
            }
            "DOUBLE_BLINK" -> {
                _sidebarVisible.value = !_sidebarVisible.value
                showToast(if (isAr) "عرض الاختصارات الجانبية 📱" else "Toggled Shortcuts Sidebar 📱")
            }
            "OPEN_MOUTH" -> {
                _keyboardVisible.value = !_keyboardVisible.value
                showToast(if (isAr) "تغيير حالة لوحة المفاتيح الطائرة ⌨" else "Toggled Floating Keyboard ⌨")
            }
            "PUFF_CHEEKS" -> {
                showToast(if (isAr) "تم التقاط لقطة شاشة 📸" else "Captured Screenshot 📸")
            }
            "SMILE" -> {
                showToast(if (isAr) "التقاط صورة سيلفي باسمة ☺" else "Captured Smiling Selfie ☺")
            }
            "TRIPLE_BLINK_STRONG" -> {
                triggerSOS()
            }
        }
    }

    private fun triggerSOS() {
        viewModelScope.launch {
            _sosActive.value = true
            showToast(if (settingsState.value.language == "ar") "تم تفعيل وضع الطوارئ SOS!" else "Emergency SOS Mode Activated!")
            delay(5000)
            _sosActive.value = false
        }
    }

    fun startCalibration() {
        viewModelScope.launch {
            _calibrationStep.value = 1
            showToast(if (settingsState.value.language == "ar") "انظر للنقطة الوامضة في الأعلى" else "Look at the blinking point at the top")
            delay(2000)
            _calibrationStep.value = 2
            showToast(if (settingsState.value.language == "ar") "انظر للنقطة الوامضة في اليمين" else "Look at the blinking point on the right")
            delay(2000)
            _calibrationStep.value = 3
            showToast(if (settingsState.value.language == "ar") "انظر للنقطة في الأسفل" else "Look at the blinking point at the bottom")
            delay(2000)
            _calibrationStep.value = 4
            showToast(if (settingsState.value.language == "ar") "انظر للنقطة في اليسار" else "Look at the blinking point on the left")
            delay(2000)
            _calibrationStep.value = 5
            showToast(if (settingsState.value.language == "ar") "انظر لمنتصف الشاشة تماماً" else "Look exactly at the screen center")
            delay(2000)
            _calibrationStep.value = 0
            showToast(if (settingsState.value.language == "ar") "تمت المعايرة بنجاح تام!" else "Calibration completed successfully!")
        }
    }

    fun startTrainingGame() {
        if (_isGameActive.value) return
        _isGameActive.value = true
        _gameScore.value = 0
        _gameTimeLeft.value = 30
        _gameTarget.value = Offset(30f, 40f)
        viewModelScope.launch {
            while (_gameTimeLeft.value > 0 && _isGameActive.value) {
                delay(1000)
                _gameTimeLeft.value -= 1
            }
            if (_isGameActive.value) {
                _isGameActive.value = false
                val score = _gameScore.value
                val isAr = settingsState.value.language == "ar"
                val badge = if (score >= 60) "قناص بصري ذهبي (Golden Eye)" else "مُتدرب نشط (Active Eye)"
                repository.addTrainingProgress(
                    TrainingProgressEntity(
                        challengeName = if (isAr) "تحدي تتبع الأهداف" else "Target Pursuit",
                        score = score,
                        badgeEarned = badge
                    )
                )
                showToast(if (isAr) "انتهى الوقت! مجموع نقاطك: $score وحصلت على وسام: $badge" else "Time's up! Total score: $score, earned badge: $badge")
            }
        }
    }

    fun stopTrainingGame() {
        _isGameActive.value = false
    }

    fun toggleLanguage() {
        viewModelScope.launch {
            val current = settingsState.value
            val nextLang = if (current.language == "ar") "en" else "ar"
            repository.updateSettings(current.copy(language = nextLang))
            showToast(if (nextLang == "ar") "تم تغيير اللغة للعربية" else "Language switched to English")
        }
    }

    fun updateControlMode(mode: Int) {
        viewModelScope.launch {
            val current = settingsState.value
            repository.updateSettings(current.copy(controlMode = mode))
        }
    }

    fun updateCursorStyle(style: String) {
        viewModelScope.launch {
            val current = settingsState.value
            repository.updateSettings(current.copy(cursorStyle = style))
        }
    }

    fun updateCursorColor(hex: String) {
        viewModelScope.launch {
            val current = settingsState.value
            repository.updateSettings(current.copy(cursorColorHex = hex))
        }
    }

    fun updateCursorSize(size: Float) {
        viewModelScope.launch {
            val current = settingsState.value
            repository.updateSettings(current.copy(cursorSize = size))
        }
    }

    fun toggleDoubleCursor(enabled: Boolean) {
        viewModelScope.launch {
            val current = settingsState.value
            repository.updateSettings(current.copy(doubleCursorEnabled = enabled))
        }
    }

    fun toggleBatterySaver(enabled: Boolean) {
        viewModelScope.launch {
            val current = settingsState.value
            repository.updateSettings(current.copy(batterySaverEnabled = enabled))
            showToast(
                if (enabled) {
                    if (current.language == "ar") "وضع توفير البطارية مفعّل (تقليل سرعة التتبع)" else "Battery Saver Active (Optimized frame rates)"
                } else {
                    if (current.language == "ar") "وضع الأداء العالي مفعّل" else "High Performance mode Active"
                }
            )
        }
    }

    fun keyboardTypeCharacter(char: String) {
        _typedText.value += char
    }

    fun keyboardDelete() {
        val current = _typedText.value
        if (current.isNotEmpty()) {
            _typedText.value = current.substring(0, current.length - 1)
        }
    }

    fun keyboardClear() {
        _typedText.value = ""
    }

    fun saveTypedNote() {
        val text = _typedText.value
        if (text.isBlank()) return
        viewModelScope.launch {
            repository.addNote(NoteEntity(content = text))
            _typedText.value = ""
            showToast(if (settingsState.value.language == "ar") "تم حفظ الملاحظة الإملائية!" else "Note saved successfully!")
        }
    }

    fun toggleSmartHomeDevice(device: String) {
        when (device) {
            "LIGHTS" -> _smartHomeLights.value = !_smartHomeLights.value
            "AC" -> _smartHomeAC.value = !_smartHomeAC.value
            "DOOR" -> _smartHomeDoorLocked.value = !_smartHomeDoorLocked.value
        }
        val isAr = settingsState.value.language == "ar"
        showToast(if (isAr) "تم إرسال الأمر للجهاز الذكي" else "Command dispatched to smart device")
    }

    fun toggleMagnifier(enabled: Boolean) {
        _magnifierVisible.value = enabled
    }

    fun simulateEyeFatigueIncrease() {
        var current = _eyeFatigueScore.value + 15
        if (current > 100) current = 100
        _eyeFatigueScore.value = current
        _blinkCount.value += (1..3).random()
        if (current > 75) {
            _fatigueAlertVisible.value = true
        }
    }

    fun dismissFatigueAlert() {
        _fatigueAlertVisible.value = false
        _eyeFatigueScore.value = 20 // Reset fatigue
    }

    private fun showToast(msg: String) {
        _toastMessage.value = msg
        viewModelScope.launch {
            delay(2000)
            if (_toastMessage.value == msg) {
                _toastMessage.value = null
            }
        }
    }

    private suspend fun monitorDwellClick() {
        // Simple automatic feedback logic could go here
    }

    // Dynamic Localization Dictionary
    fun getString(key: String): String {
        val ar = settingsState.value.language == "ar"
        return when (key) {
            "app_name" -> if (ar) "نظرة حرة" else "Nazra Horra"
            "onboarding" -> if (ar) "التعليم والتدريب" else "Onboarding"
            "simulator" -> if (ar) "محاكي التحكم" else "Control Simulator"
            "superpowers" -> if (ar) "٣٠ ميزة خارقة" else "30 Superpowers"
            "settings" -> if (ar) "تخصيص وإعدادات" else "Customize & Setup"
            
            // Onboarding Strings
            "welcome_title" -> if (ar) "مرحباً بك في نظرة حرة" else "Welcome to Nazra Horra"
            "welcome_desc" -> if (ar) "بوابتك المبتكرة للتحكم الكامل بالهاتف الذكي باستخدام حركة العين، الرأس، وتعابير الوجه دون لمس الإطلاق." else "Your innovative gateway to control your smartphone entirely using eyes, head tilt, and micro facial expressions without any physical touch."
            "next" -> if (ar) "التالي ⬅" else "Next ➡"
            "skip" -> if (ar) "تخطي" else "Skip"
            "start_calibration" -> if (ar) "ابدأ المعايرة البصرية 🎯" else "Start Gaze Calibration 🎯"
            "calibration_intro" -> if (ar) "معايرة دقة تتبع العين" else "Eye Tracking Precision Calibration"
            "calibration_desc" -> if (ar) "يرجى تركيز نظرك على النقاط المضيئة لتجربة تحكم متناهية الدقة خالية من الأخطاء." else "Please focus your gaze on the illuminated dots to calibrate a pixel-perfect, error-free hands-free experience."

            // Simulator screen
            "camera_feed" -> if (ar) "كاميرا التتبع المباشرة" else "Live Tracking Camera"
            "camera_not_granted" -> if (ar) "الرجاء منح إذن الكاميرا للبدء" else "Please grant camera permission to start"
            "floating_cursor_preview" -> if (ar) "معاينة المؤشر العائم" else "Floating Cursor Preview"
            "test_gestures" -> if (ar) "اختبار إيماءات الوجه (لوحة تحكم المحاكي)" else "Simulate Facial Gestures"
            "move_cursor_control" -> if (ar) "لوحة تحكم بحركة الرأس الافتراضية" else "Simulated Head-Tracking movement"
            "dwelled_count" -> if (ar) "مستوى تقدم التحديق: " else "Dwell Progress: "
            "tilt_up" -> if (ar) "رأس لأعلى ⬆" else "Tilt Up ⬆"
            "tilt_down" -> if (ar) "رأس لأسفل ⬇" else "Tilt Down ⬇"
            "tilt_left" -> if (ar) "رأس لليسار ⬅" else "Tilt Left ⬅"
            "tilt_right" -> if (ar) "رأس لليمين ➡" else "Tilt Right ➡"

            // Customize Screen
            "cursor_customize_title" -> if (ar) "مكتبة تخصيص المؤشرات الذكية" else "Smart Cursor Customizer Library"
            "cursor_style" -> if (ar) "طراز المؤشر البصري" else "Gaze Cursor Visual Style"
            "cursor_size" -> if (ar) "حجم المؤشر" else "Cursor Scale Size"
            "cursor_color" -> if (ar) "لون المؤشر المضيء" else "Glow Color Scheme"
            "double_cursor" -> if (ar) "نمط المؤشر المزدوج (نقطة مركزية دقيقة)" else "Double Cursor Mode (Precision inner dot)"
            "click_blocker" -> if (ar) "مانع النقرات العشوائية الذكي" else "Smart Accidental-Tap Blocker"
            "battery_saver" -> if (ar) "وضع الحفاظ على الطاقة (تتبع مستشعر)" else "Battery-Saver (Sensors-only Mode)"

            // Superpowers Screen
            "superpowers_desc" -> if (ar) "استكشف ٣٠ أداة ذكية صممت خصيصاً لتمنحك استقلالية تامة في استخدام هاتفك." else "Explore 30 purpose-built intelligent tools that grant you absolute autonomy on your smartphone."
            "screen_reader_title" -> if (ar) "قارئ النص بالتحديق المباشر" else "Gaze Direct Screen Reader"
            "keyboard_title" -> if (ar) "لوحة المفاتيح الطائرة بالتحديق" else "Gaze-Dwell Floating Keyboard"
            "magnifier_title" -> if (ar) "مكبر الشاشة التلقائي الدائري" else "Circular Screen Magnifier"
            "contacts_sidebar" -> if (ar) "شريط الاتصال السريع (غمزة مزدوجة)" else "Speed dial Sidebar (Double-Blink)"
            "smart_home_title" -> if (ar) "التحكم بالمنزل الذكي بالنظر" else "Optical IoT Smart Home Hub"
            "training_coach_title" -> if (ar) "مساعد التدريب اليومي والجوائز" else "Daily Precision Training & Badges"
            
            else -> key
        }
    }
}
