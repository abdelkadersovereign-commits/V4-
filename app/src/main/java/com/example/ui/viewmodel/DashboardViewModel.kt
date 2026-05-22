package com.example.ui.viewmodel

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.database.InventorIdea
import com.example.data.database.InventorIdeaRepository
import com.example.data.database.SovereigntyCipher
import com.example.data.ContextualVerseEngine
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.net.Inet4Address
import java.net.NetworkInterface
import java.util.Calendar
import kotlin.random.Random

class DashboardViewModel(application: Application) : AndroidViewModel(application), SensorEventListener {

    private val sensorManager = application.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    // Accelerometer / Parallax State
    private val _roll = MutableStateFlow(0f)
    val roll: StateFlow<Float> = _roll.asStateFlow()

    private val _pitch = MutableStateFlow(0f)
    val pitch: StateFlow<Float> = _pitch.asStateFlow()

    private val alpha = 0.12f
    private var currentRoll = 0f
    private var currentPitch = 0f

    // Sovereign Context State: Network Interface
    private val _connectionType = MutableStateFlow("Determining...")
    val connectionType: StateFlow<String> = _connectionType.asStateFlow()

    private val _ipAddress = MutableStateFlow("127.0.0.1")
    val ipAddress: StateFlow<String> = _ipAddress.asStateFlow()

    // Sovereign Context State: Power Info
    private val _batteryPercentage = MutableStateFlow(100)
    val batteryPercentage: StateFlow<Int> = _batteryPercentage.asStateFlow()

    private val _chargingStatus = MutableStateFlow("UNKNOWN")
    val chargingStatus: StateFlow<String> = _chargingStatus.asStateFlow()

    // Spiritual Prayer Countdown Tracker
    private val _nextPrayerName = MutableStateFlow("Fajr")
    val nextPrayerName: StateFlow<String> = _nextPrayerName.asStateFlow()

    private val _nextPrayerCountdown = MutableStateFlow("00:00:00")
    val nextPrayerCountdown: StateFlow<String> = _nextPrayerCountdown.asStateFlow()

    private val _nextPrayerProgress = MutableStateFlow(0f)
    val nextPrayerProgress: StateFlow<Float> = _nextPrayerProgress.asStateFlow()

    // Gemini AI and Strategic Core State
    private val _isThinking = MutableStateFlow(false)
    val isThinking: StateFlow<Boolean> = _isThinking.asStateFlow()

    private val _intelligenceBrief = MutableStateFlow("[INTEL_UPLINK] > Initializing satellite intelligence stream...")
    val intelligenceBrief: StateFlow<String> = _intelligenceBrief.asStateFlow()

    private val _terminalInput = MutableStateFlow("")
    val terminalInput: StateFlow<String> = _terminalInput.asStateFlow()

    private val _terminalResponse = MutableStateFlow("")
    val terminalResponse: StateFlow<String> = _terminalResponse.asStateFlow()

    private val _isTerminalExpanded = MutableStateFlow(false)
    val isTerminalExpanded: StateFlow<Boolean> = _isTerminalExpanded.asStateFlow()

    // Local Persistence Infrastructure: Room Setup
    private val database = AppDatabase.getDatabase(application)
    private val repository = InventorIdeaRepository(database.inventorIdeaDao())

    // UI state flows for Room list view
    val savedIdeas: StateFlow<List<InventorIdea>> = repository.allIdeas
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Ambient Contextual Wisdom flow combining system telemetry
    val ambientInsight: StateFlow<ContextualVerseEngine.SpiritualInsight> = combine(
        _batteryPercentage,
        _chargingStatus,
        _connectionType
    ) { battery, charging, connection ->
        val isCharging = charging == "CHARGING"
        ContextualVerseEngine.getInsight(battery, isCharging, connection)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ContextualVerseEngine.getInsight(100, false, "Determining...")
    )

    // Layout panels overlays state
    private val _isForgePanelOpen = MutableStateFlow(false)
    val isForgePanelOpen: StateFlow<Boolean> = _isForgePanelOpen.asStateFlow()

    private val _isVaultViewOpen = MutableStateFlow(false)
    val isVaultViewOpen: StateFlow<Boolean> = _isVaultViewOpen.asStateFlow()

    // Phase 7 Settings states and overlays
    private val prefs = application.getSharedPreferences("asyria_settings", Context.MODE_PRIVATE)

    private val _isStealthMode = MutableStateFlow(prefs.getBoolean("stealth_mode", false))
    val isStealthMode: StateFlow<Boolean> = _isStealthMode.asStateFlow()

    private val _customApiKey = MutableStateFlow(prefs.getString("custom_api_key", "") ?: "")
    val customApiKey: StateFlow<String> = _customApiKey.asStateFlow()

    private val _isSettingsOpen = MutableStateFlow(false)
    val isSettingsOpen: StateFlow<Boolean> = _isSettingsOpen.asStateFlow()

    fun setStealthMode(enabled: Boolean) {
        _isStealthMode.value = enabled
        prefs.edit().putBoolean("stealth_mode", enabled).apply()
    }

    fun updateCustomApiKey(key: String) {
        _customApiKey.value = key
        prefs.edit().putString("custom_api_key", key).apply()
        initializeGenerativeModel()
    }

    fun setSettingsOpen(open: Boolean) {
        _isSettingsOpen.value = open
    }

    // Forge interface inputs state
    private val _forgeTitle = MutableStateFlow("")
    val forgeTitle: StateFlow<String> = _forgeTitle.asStateFlow()

    private val _forgeCategory = MutableStateFlow("SecOps")
    val forgeCategory: StateFlow<String> = _forgeCategory.asStateFlow()

    private val _forgeIdea = MutableStateFlow("")
    val forgeIdea: StateFlow<String> = _forgeIdea.asStateFlow()

    private val _forgeBlueprint = MutableStateFlow("")
    val forgeBlueprint: StateFlow<String> = _forgeBlueprint.asStateFlow()

    private var monitoringJob: Job? = null
    private var generativeModel: GenerativeModel? = null

    // Fallback security and news items if offline/no key
    private val offlineBriefs = listOf(
        "OVERWATCH PROTOCOL ACTIVE: Enforce zero-trust architecture across local visual segments.",
        "CYBER SHIELD UPDATE: Core firewall active. Security levels in southern sectors stabilized.",
        "COGNITIVE INTEL: Encrypt terminal tunnels using local neural asymmetric signatures.",
        "SATELLITE WARNING: Quantum vector injection attempts detected in perimeter. No breach.",
        "SECURE TELEMETRY: Decentralize visual frame buffers to resist spatial memory leakage.",
        "COGNITIVE INNOVATION: Isolated sandbox environments deployed for tactical analytical runs."
    )

    // Fallback query responses for typewriter strategic terminal
    private val offlineTerminalResponses = listOf(
        "COGNITIVE ANALYSIS: Target structure verified with optimal efficiency. Standard microservices alignment meets expectations. Recommendation: Deploy redundant localized storage clusters to satisfy regional failover requirements.",
        "STRATEGIC BRIEF: Unified sovereign mainframe online. System dependencies compiled successfully. High threat prevention active. Direct actions suggest reinforcing container isolation and limiting remote API handshakes.",
        "TACTICAL SYSTEM: Core network interfaces are nominal. Rebalancing resources to visual core processors. Recommended path: Proceed with Phase 5 synchronization while keeping high-density firewalls online."
    )

    private val fallbackBlueprints = listOf(
        "SOVEREIGN ENGINEERING BLUEPRINT v1.0\n\nCATEGORY: %CATEGORY%\nPROJECT TITLE: %TITLE%\n\n[1] MISSION CORE LOGIC\nDecentralized sandbox nodes with local encrypted persistence keys. Real-time data validation via SHA-256 verification rings.\n\n[2] SYSTEM INVENTORY\n- Asymmetric key store\n- Local Room encrypted SQLite memory buffers\n- Gemini edge analytical co-processors\n\n[3] RISK ASSESSMENT\n- Remote vector interception: Mitigation via localized network offline locks.\n- Physical side-channel leaks: Mitigation via automatic memory purge routines.",
        "COGNITIVE DEPLOYMENT DOCUMENTATION\n\nSPEC: %TITLE% (%CATEGORY%)\n\n[1] ARCHITECTURAL SPECIFICATION\nHigh efficiency parallel analytical loops running inside clean-room containers. Strict state matching ensures atomic operations.\n\n[2] IMPLEMENTATION PIPELINE\n- Core MVVM State Engine\n- Parallax rendering thread\n- Local hardware security modules\n\n[3] PERIMETER ANALYSIS\n- Dependency collision: Low risk; isolated workspace checks active.\n- Local storage leakage: Secured via Sovereignty Symmetric AES ciphers."
    )

    // Register battery state broadcast receiver safely
    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.let {
                val level = it.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale = it.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                if (level != -1 && scale != -1) {
                    _batteryPercentage.value = ((level.toFloat() / scale.toFloat()) * 100).toInt()
                }

                val status = it.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                        status == BatteryManager.BATTERY_STATUS_FULL
                _chargingStatus.value = if (isCharging) "CHARGING" else "DISCHARGING"
            }
        }
    }

    private fun initializeGenerativeModel() {
        val apiKey = if (_customApiKey.value.isNotBlank()) _customApiKey.value else com.example.BuildConfig.GEMINI_API_KEY
        val isValidKey = apiKey.isNotEmpty() && apiKey != "MY_GEMINI_API_KEY" && !apiKey.contains("PLACEHOLDER")
        if (isValidKey) {
            generativeModel = GenerativeModel(
                modelName = "gemini-3.5-flash",
                apiKey = apiKey
            )
        } else {
            generativeModel = null
        }
    }

    init {
        // Initialize Gemini model based on config or custom overrides
        initializeGenerativeModel()

        // Register Accelerometer
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }

        // Register Battery Level
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        application.registerReceiver(batteryReceiver, filter)

        // Spawn central ticker loop for active counters and state poller
        startNeuralServices()
    }

    private fun startNeuralServices() {
        monitoringJob = viewModelScope.launch {
            var loopCount = 0
            while (true) {
                pollNetworkIntel()
                calculateSovereignPrayerEngine()
                
                // Fetch dynamic security intelligence brief every 300 seconds (5 minutes) or on startup
                if (loopCount % 300 == 0) {
                    fetchStrategicIntelligence()
                }
                
                loopCount++
                delay(1000)
            }
        }
    }

    // Navigation and Layout Actions
    fun setTerminalExpanded(expanded: Boolean) {
        _isTerminalExpanded.value = expanded
        if (!expanded) {
            _terminalInput.value = ""
            _terminalResponse.value = ""
        }
    }

    fun setForgePanelOpen(open: Boolean) {
        _isForgePanelOpen.value = open
        if (!open) {
            clearForgeFields()
        }
    }

    fun setVaultViewOpen(open: Boolean) {
        _isVaultViewOpen.value = open
    }

    // Input state mutations
    fun updateTerminalInput(input: String) {
        _terminalInput.value = input
    }

    fun updateForgeTitle(title: String) {
        _forgeTitle.value = title
    }

    fun updateForgeCategory(category: String) {
        _forgeCategory.value = category
    }

    fun updateForgeIdea(idea: String) {
        _forgeIdea.value = idea
    }

    private fun clearForgeFields() {
        _forgeTitle.value = ""
        _forgeCategory.value = "SecOps"
        _forgeIdea.value = ""
        _forgeBlueprint.value = ""
    }

    // Local Database Actions
    fun deleteIdea(ideaId: Int) {
        viewModelScope.launch {
            repository.deleteIdeaById(ideaId)
        }
    }

    fun wipeVault() {
        viewModelScope.launch {
            repository.clearAll()
        }
    }

    // Task 2: AI Forging System - formats design blueprints and stores in secure Room database
    fun forgeAndSaveIdea() {
        val title = _forgeTitle.value.trim()
        val category = _forgeCategory.value.trim()
        val ideaRaw = _forgeIdea.value.trim()

        if (title.isEmpty() || ideaRaw.isEmpty()) return

        viewModelScope.launch {
            _isThinking.value = true
            _forgeBlueprint.value = ""

            val model = generativeModel
            var generatedBlueprint = ""

            if (model != null) {
                try {
                    val prompt = """
                        Analyze this innovation idea:
                        Title: $title
                        Category: $category
                        Idea Details: $ideaRaw
                        
                        List required technical components, and potential security risks. Format it as a professional engineering blueprint. Limit your response strictly to 180 words.
                    """.trimIndent()

                    val res = model.generateContent(prompt).text
                    if (!res.isNullOrEmpty()) {
                        generatedBlueprint = res.trim()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            // High aesthetic fallback system
            if (generatedBlueprint.isEmpty()) {
                val selectTemplate = fallbackBlueprints[Random.nextInt(fallbackBlueprints.size)]
                generatedBlueprint = selectTemplate
                    .replace("%TITLE%", title)
                    .replace("%CATEGORY%", category)
            }

            // Typewriter stream effect simulation for extreme terminal feels
            _isThinking.value = false
            
            // Build and persist actual encrypted Room database entity
            val encryptedEntity = InventorIdea.createEncrypted(
                title = title,
                category = category,
                originalIdea = ideaRaw,
                geminiBlueprint = generatedBlueprint
            )
            repository.insertIdea(encryptedEntity)

            // Let character stream trigger live on screen UI updates
            for (i in 1..generatedBlueprint.length step 3) {
                val endIdx = (i + 2).coerceAtMost(generatedBlueprint.length)
                _forgeBlueprint.value = generatedBlueprint.substring(0, endIdx)
                delay(12)
            }
            _forgeBlueprint.value = generatedBlueprint
        }
    }

    private fun pollNetworkIntel() {
        val connectivityManager = getApplication<Application>().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)

        if (capabilities != null) {
            when {
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                    _connectionType.value = "WIFI (SECURE)"
                }
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                    _connectionType.value = "CELLULAR (LTE/5G)"
                }
                else -> {
                    _connectionType.value = "CONNECTED"
                }
            }
        } else {
            _connectionType.value = "DISCONNECTED"
        }

        _ipAddress.value = extractLocalIp()
    }

    private fun extractLocalIp(): String {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val intf = interfaces.nextElement()
                val addrs = intf.inetAddresses
                while (addrs.hasMoreElements()) {
                    val addr = addrs.nextElement()
                    if (!addr.isLoopbackAddress && addr is Inet4Address) {
                        return addr.hostAddress ?: "127.0.0.1"
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return "127.0.0.1"
    }

    private fun calculateSovereignPrayerEngine() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val second = calendar.get(Calendar.SECOND)
        val currentSeconds = hour * 3600 + minute * 60 + second

        val fajrSeconds = 4 * 3600 + 30 * 60
        val dhuhrSeconds = 12 * 3600 + 45 * 60
        val asrSeconds = 16 * 3600 + 15 * 60
        val maghribSeconds = 19 * 3600 + 30 * 60
        val ishaSeconds = 21 * 3600 + 0 * 60

        val nextName: String
        val remaining: Int
        val maxCycle: Int

        when {
            currentSeconds <= fajrSeconds -> {
                nextName = "FAJR"
                remaining = fajrSeconds - currentSeconds
                maxCycle = 27000
            }
            currentSeconds <= dhuhrSeconds -> {
                nextName = "DHUHR"
                remaining = dhuhrSeconds - currentSeconds
                maxCycle = 29700
            }
            currentSeconds <= asrSeconds -> {
                nextName = "ASR"
                remaining = asrSeconds - currentSeconds
                maxCycle = 12600
            }
            currentSeconds <= maghribSeconds -> {
                nextName = "MAGHRIB"
                remaining = maghribSeconds - currentSeconds
                maxCycle = 11700
            }
            currentSeconds <= ishaSeconds -> {
                nextName = "ISHA"
                remaining = ishaSeconds - currentSeconds
                maxCycle = 5400
            }
            else -> {
                nextName = "FAJR"
                remaining = (86400 - currentSeconds) + fajrSeconds
                maxCycle = 27000
            }
        }

        _nextPrayerName.value = nextName

        val h = remaining / 3600
        val m = (remaining % 3600) / 60
        val s = remaining % 60
        _nextPrayerCountdown.value = String.format("%02d:%02d:%02d", h, m, s)

        val elapsed = maxCycle - remaining
        _nextPrayerProgress.value = (elapsed.toFloat() / maxCycle.toFloat()).coerceIn(0f, 1f)
    }

    // Task 1: Fetch short strategic intelligence brief from Gemini (or crisp offline source)
    fun fetchStrategicIntelligence() {
        viewModelScope.launch {
            val model = generativeModel
            if (model != null) {
                try {
                    val prompt = "Provide a single, short Android security warning or technology brief. Limit answer strictly to 15 words or less, direct tone, no fluff."
                    val res = model.generateContent(prompt).text ?: ""
                    val cleanRes = res.trim().replace("\n", " ")
                    if (cleanRes.isNotEmpty()) {
                        _intelligenceBrief.value = "[INTEL_UPLINK] > $cleanRes"
                        return@launch
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            // Fallback to offline decrypt brief if model generates empty or throws Exception
            val randomBrief = offlineBriefs[Random.nextInt(offlineBriefs.size)]
            _intelligenceBrief.value = "[INTEL_UPLINK] > $randomBrief"
        }
    }

    // Task 3: Interactive query execution on Gemini. Uses real simulated satellite streaming typewriter visual
    fun sendTerminalQuery(query: String) {
        if (query.isBlank()) return
        viewModelScope.launch {
            _isThinking.value = true
            _terminalResponse.value = ""
            
            val model = generativeModel
            var rawResponse = ""

            if (model != null) {
                try {
                    val res = model.generateContent(
                        "You are the high-end A.SYRIA V4 brain. Analyze the following project idea or question comprehensively and give a tactical security security brief style response. Under 50 words: $query"
                    ).text
                    if (!res.isNullOrEmpty()) {
                        rawResponse = res.trim()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            if (rawResponse.isEmpty()) {
                // Return high fidelity contextual feedback offline
                rawResponse = offlineTerminalResponses[Random.nextInt(offlineTerminalResponses.size)]
            }

            // Stream response using character level Typewriter Animation in coroutine
            _isThinking.value = false
            for (i in 1..rawResponse.length) {
                _terminalResponse.value = rawResponse.substring(0, i)
                delay(30) // Elegant tech delay representing live uplink stream
            }
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            val ax = event.values[0]
            val ay = event.values[1]

            val targetRoll = -ax * 4f
            val targetPitch = ay * 4f

            currentRoll = currentRoll + alpha * (targetRoll - currentRoll)
            currentPitch = currentPitch + alpha * (targetPitch - currentPitch)

            _roll.value = currentRoll
            _pitch.value = currentPitch
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onCleared() {
        super.onCleared()
        sensorManager.unregisterListener(this)
        try {
            getApplication<Application>().unregisterReceiver(batteryReceiver)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        monitoringJob?.cancel()
    }
}
