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
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import com.example.data.SovereignDataStore
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.net.Inet4Address
import java.net.NetworkInterface
import java.util.Calendar
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.random.Random

class DashboardViewModel(application: Application) : AndroidViewModel(application), SensorEventListener {

    private val sensorManager = application.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val magnetometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    // Accelerometer / Parallax State
    private val _roll = MutableStateFlow(0f)
    val roll: StateFlow<Float> = _roll.asStateFlow()

    private val _pitch = MutableStateFlow(0f)
    val pitch: StateFlow<Float> = _pitch.asStateFlow()

    private val _azimuth = MutableStateFlow(0f)
    val azimuth: StateFlow<Float> = _azimuth.asStateFlow()

    private val alpha = 0.12f
    private var currentRoll = 0f
    private var currentPitch = 0f
    
    private var gravity: FloatArray? = null
    private var geomagnetic: FloatArray? = null

    private val _isAcademyGenerating = MutableStateFlow(false)
    val isAcademyGenerating: StateFlow<Boolean> = _isAcademyGenerating.asStateFlow()

    fun startSensors() {
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
        magnetometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun stopSensors() {
        sensorManager.unregisterListener(this)
    }

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

    private val _isNeuralLinkOffline = MutableStateFlow(false)
    val isNeuralLinkOffline: StateFlow<Boolean> = _isNeuralLinkOffline.asStateFlow()

    private val _isTestingKey = MutableStateFlow(false)
    val isTestingKey: StateFlow<Boolean> = _isTestingKey.asStateFlow()

    private val _linkAnalysisResult = MutableStateFlow<String?>(null)
    val linkAnalysisResult: StateFlow<String?> = _linkAnalysisResult.asStateFlow()

    private val _isAnalyzingLink = MutableStateFlow(false)
    val isAnalyzingLink: StateFlow<Boolean> = _isAnalyzingLink.asStateFlow()

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

    private val _isVaultAuthenticated = MutableStateFlow(false)
    val isVaultAuthenticated: StateFlow<Boolean> = _isVaultAuthenticated.asStateFlow()

    private val _isSettingsAuthenticated = MutableStateFlow(false)
    val isSettingsAuthenticated: StateFlow<Boolean> = _isSettingsAuthenticated.asStateFlow()

    fun setVaultAuthenticated(auth: Boolean) {
        _isVaultAuthenticated.value = auth
    }

    fun setSettingsAuthenticated(auth: Boolean) {
        _isSettingsAuthenticated.value = auth
    }

    // Phase 7 Settings states and overlays
    private val dataStore = SovereignDataStore(application)

    // Phase 11: Cyber-Rank System & Academy Visibility states
    private val _isArabic = MutableStateFlow(true)
    val isArabic: StateFlow<Boolean> = _isArabic.asStateFlow()

    fun setArabic(enabled: Boolean) {
        viewModelScope.launch {
            dataStore.saveIsArabic(enabled)
            fetchStrategicIntelligence()
        }
    }

    private val _cyberScore = MutableStateFlow(0)
    val cyberScore: StateFlow<Int> = _cyberScore.asStateFlow()

    private val _operatorName = MutableStateFlow("Sovereign_Operator")
    val operatorName: StateFlow<String> = _operatorName.asStateFlow()

    private val _neuralRole = MutableStateFlow("Sovereign Node v4")
    val neuralRole: StateFlow<String> = _neuralRole.asStateFlow()

    val cyberRank: StateFlow<String> = combine(_cyberScore, _isArabic) { score, isAr ->
        if (isAr) {
            when {
                score <= 50 -> "شيفرة مبتدئة"
                score <= 150 -> "حارس السينتينل"
                else -> "شبح سيادي"
            }
        } else {
            when {
                score <= 50 -> "Novice Cipher"
                score <= 150 -> "Sentinel Guard"
                else -> "Sovereign Ghost"
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Novice Cipher")

    val cyberProgress: StateFlow<Float> = combine(_cyberScore, MutableStateFlow(Unit)) { score, _ ->
        when {
            score <= 50 -> (score.toFloat() / 50f).coerceIn(0f, 1f)
            score <= 150 -> ((score - 50).toFloat() / 100f).coerceIn(0f, 1f)
            else -> 1.0f
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0f)

    private val _isAcademyOpen = MutableStateFlow(false)
    val isAcademyOpen: StateFlow<Boolean> = _isAcademyOpen.asStateFlow()

    private val _isResourcesOpen = MutableStateFlow(false)
    val isResourcesOpen: StateFlow<Boolean> = _isResourcesOpen.asStateFlow()

    fun addCyberScore(points: Int) {
        viewModelScope.launch {
            dataStore.saveCyberScore(_cyberScore.value + points)
        }
    }

    fun setAcademyOpen(open: Boolean) {
        _isAcademyOpen.value = open
    }

    fun setResourcesOpen(open: Boolean) {
        _isResourcesOpen.value = open
    }

    private val _isStealthMode = MutableStateFlow(false)
    val isStealthMode: StateFlow<Boolean> = _isStealthMode.asStateFlow()

    private val _isNeuralProxy = MutableStateFlow(false)
    val isNeuralProxy: StateFlow<Boolean> = _isNeuralProxy.asStateFlow()

    private val _customApiKey = MutableStateFlow("")
    val customApiKey: StateFlow<String> = _customApiKey.asStateFlow()

    private val _projectName = MutableStateFlow("")
    val projectName: StateFlow<String> = _projectName.asStateFlow()

    private val _projectId = MutableStateFlow("")
    val projectId: StateFlow<String> = _projectId.asStateFlow()

    private val _projectNumber = MutableStateFlow("")
    val projectNumber: StateFlow<String> = _projectNumber.asStateFlow()

    private val _isSettingsOpen = MutableStateFlow(false)
    val isSettingsOpen: StateFlow<Boolean> = _isSettingsOpen.asStateFlow()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private suspend fun generateContentSafely(prompt: String): String {
        return withContext(Dispatchers.IO) {
            val apiKey = if (_customApiKey.value.isNotBlank()) _customApiKey.value else com.asyria.v4.BuildConfig.GEMINI_API_KEY
            if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") throw Exception("Error 401: Invalid Key")

            try {
                executeNeuralProxyRequest(prompt, apiKey)
            } catch (e: Exception) {
                throw e
            }
        }
    }

    private fun executeNeuralProxyRequest(prompt: String, apiKey: String): String {
        // Phase 25 Structure: Standard Gemini 3.5 Flash Request with URL Context Tool
        val json = JSONObject().apply {
            put("contents", org.json.JSONArray().put(
                JSONObject().apply {
                    put("parts", org.json.JSONArray().put(
                        JSONObject().put("text", prompt)
                    ))
                }
            ))
            // Mandatory tool for automated context analysis
            put("tools", org.json.JSONArray().put(
                JSONObject().put("url_context", JSONObject())
            ))
        }
        
        val body = json.toString().toRequestBody("application/json".toMediaType())
        // Hardened Endpoint
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent"
        
        val requestBuilder = Request.Builder()
            .url(url)
            .post(body)
            .header("Content-Type", "application/json")
            .header("x-goog-api-key", apiKey)
        
        // Phase 23: Sovereignty Identity Headers
        if (_projectId.value.isNotBlank()) {
            requestBuilder.header("x-goog-project-id", _projectId.value)
        }
        if (_projectNumber.value.isNotBlank()) {
            requestBuilder.header("x-goog-project-number", _projectNumber.value)
        }
        
        val request = requestBuilder.build()
        val response = okHttpClient.newCall(request).execute()
        
        if (!response.isSuccessful) {
            val code = response.code
            val errBody = response.body?.string() ?: ""
            throw Exception(if (code == 403) "Regional Block - Enable Neural Proxy" else if (code == 401) "Invalid API Key or Project ID" else "Error $code: $errBody")
        }
        
        val resBody = response.body?.string() ?: ""
        val jsonRes = JSONObject(resBody)
        
        // Extract Usage Metadata for Terminal Display
        val usage = jsonRes.optJSONObject("usageMetadata")
        if (usage != null) {
            val promptTokens = usage.optInt("promptTokenCount")
            val candidateTokens = usage.optInt("candidatesTokenCount")
            val totalTokens = usage.optInt("totalTokenCount")
            _terminalResponse.value += "\n\n[ NEURAL LOAD: $totalTokens Tokens (In: $promptTokens, Out: $candidateTokens) ]"
        }

        return jsonRes.getJSONArray("candidates")
            .getJSONObject(0)
            .getJSONObject("content")
            .getJSONArray("parts")
            .getJSONObject(0)
            .getString("text")
    }

    fun setStealthMode(enabled: Boolean) {
        viewModelScope.launch {
            dataStore.saveStealthMode(enabled)
            _isStealthMode.value = enabled
        }
    }

    fun setNeuralProxy(enabled: Boolean) {
        viewModelScope.launch {
            dataStore.saveNeuralProxy(enabled)
            _isNeuralProxy.value = enabled
        }
    }

    fun updateCustomApiKey(key: String) {
        viewModelScope.launch {
            dataStore.saveGeminiApiKey(key)
        }
    }

    fun updateProjectName(name: String) {
        viewModelScope.launch {
            dataStore.saveProjectName(name)
        }
    }

    fun updateProjectId(id: String) {
        viewModelScope.launch {
            dataStore.saveProjectId(id)
        }
    }

    fun updateProjectNumber(number: String) {
        viewModelScope.launch {
            dataStore.saveProjectNumber(number)
        }
    }

    fun updateOperatorName(name: String) {
        viewModelScope.launch {
            dataStore.saveOperatorName(name)
        }
    }

    fun updateNeuralRole(role: String) {
        viewModelScope.launch {
            dataStore.saveNeuralRole(role)
        }
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

    fun testNeuralLink() {
        viewModelScope.launch {
            _isTestingKey.value = true
            val apiKey = if (_customApiKey.value.isNotBlank()) _customApiKey.value else com.asyria.v4.BuildConfig.GEMINI_API_KEY
            if (apiKey.isEmpty() || apiKey.contains("MY_GEMINI_API_KEY") || _projectName.value.isBlank() || _projectId.value.isBlank() || _projectNumber.value.isBlank()) {
                _isNeuralLinkOffline.value = true
                _terminalResponse.value = "[ TERMINAL ALERT ] All Project Identity Fields (API Key, Project ID, Project Number) are required."
                _isTestingKey.value = false
                return@launch
            }

            try {
                kotlinx.coroutines.withTimeout(15000) {
                    generateContentSafely("ping")
                }
                _isNeuralLinkOffline.value = false
                _terminalResponse.value = "FULL NEURAL LINK ESTABLISHED ✅"
            } catch (e: Exception) {
                _isNeuralLinkOffline.value = true
                _terminalResponse.value = "[ TERMINAL ALERT ] ${e.message}"
            } finally {
                _isTestingKey.value = false
            }
        }
    }

    init {
        // Register Sensors
        startSensors()

        // Register Battery Level
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        application.registerReceiver(batteryReceiver, filter)

        // Spawn central ticker loop for active counters and state poller
        viewModelScope.launch {
            dataStore.isArabic.collect { _isArabic.value = it }
        }
        viewModelScope.launch {
            dataStore.stealthMode.collect { _isStealthMode.value = it }
        }
        viewModelScope.launch {
            dataStore.cyberScore.collect { _cyberScore.value = it }
        }
        viewModelScope.launch {
            dataStore.geminiApiKey.collect { 
                _customApiKey.value = it 
            }
        }
        viewModelScope.launch {
            dataStore.projectName.collect { _projectName.value = it }
        }
        viewModelScope.launch {
            dataStore.projectId.collect { _projectId.value = it }
        }
        viewModelScope.launch {
            dataStore.projectNumber.collect { _projectNumber.value = it }
        }
        viewModelScope.launch {
            dataStore.operatorName.collect { _operatorName.value = it }
        }
        viewModelScope.launch {
            dataStore.neuralRole.collect { _neuralRole.value = it }
        }
        viewModelScope.launch {
            dataStore.neuralProxy.collect { _isNeuralProxy.value = it }
        }
        
        startNeuralServices()
    }

    private fun startNeuralServices() {
        monitoringJob = viewModelScope.launch {
            var loopCount = 0
            while (true) {
                pollNetworkIntel()
                
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

            var generatedBlueprint = ""
            val apiKey = if (_customApiKey.value.isNotBlank()) _customApiKey.value else com.asyria.v4.BuildConfig.GEMINI_API_KEY
            if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
                try {
                    val prompt = """
                        Analyze this innovation idea:
                        Title: $title
                        Category: $category
                        Idea Details: $ideaRaw
                        
                        List required technical components, and potential security risks. Format it as a professional engineering blueprint. Limit your response strictly to 180 words.
                    """.trimIndent()

                    val res = generateContentSafely(prompt)
                    if (!res.isNullOrEmpty()) {
                        generatedBlueprint = res.trim()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                generatedBlueprint = "[ TERMINAL ALERT ] Neural Link is offline. Please configure a valid GEMINI API KEY in the System Settings to restore active intelligence."
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

    // Task 1: Fetch short strategic intelligence brief from Gemini (or crisp offline source)
    fun fetchStrategicIntelligence() {
        viewModelScope.launch {
            val isAr = _isArabic.value
            val apiKey = if (_customApiKey.value.isNotBlank()) _customApiKey.value else com.asyria.v4.BuildConfig.GEMINI_API_KEY
            if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
                try {
                    kotlinx.coroutines.withTimeout(15000) {
                        val promptLocale = if (isAr) "Arabic (العربية)" else "English"
                        val prompt = "Provide a single, very short daily security alert or threat insight about a modern device hazard (such as WhatsApp hijacking, public charging port dangers / juice jacking, fake Wi-Fi, malicious QR codes) formatted in $promptLocale. Limit output to exactly 12 words or less. Do not use quotes, markdown or introductory text."
                        val res = generateContentSafely(prompt)
                        val cleanRes = res.trim().replace("\n", " ")
                        if (cleanRes.isNotEmpty()) {
                            _intelligenceBrief.value = "[!] ALERT: $cleanRes"
                            _isNeuralLinkOffline.value = false
                        }
                    }
                    return@launch
                } catch (e: Exception) {
                    e.printStackTrace()
                    _isNeuralLinkOffline.value = true
                }
            } else {
                _isNeuralLinkOffline.value = true
            }
            // Fallback to offline decrypt brief in correct language if model generates empty or throws Exception
            val fallbackInsight = if (isAr) {
                val list = listOf(
                    "تجنب منافذ الشحن العامة بالمطارات لحماية جهازك الرقمي من الاختراقات السلكية الحيوية.",
                    "الهندسة الاجتماعية المتقدمة تستهدف مستخدمي كود واتساب للتلفيق واستلام حساباتهم.",
                    "تجنب الاتصال بشبكات الواي فاي العامة دون تفعيل نفق اتصال مشفر مسبقاً.",
                    "امسح فقط الأكواد التفاعلية QR المضمونة لتفادي برمجيات الفيشينغ الخبيثة التلقائية."
                )
                list[Random.nextInt(list.size)]
            } else {
                val list = listOf(
                    "Avoid public charging ports (Juice Jacking) to secure local device data hardware.",
                    "Advanced social engineering vectors target active WhatsApp authentication pins.",
                    "Do not connect to secondary public Wi-Fi zones without VPN tunnels verified.",
                    "QR code phishing (Quishing) executes automatic remote malicious software payloads."
                )
                list[Random.nextInt(list.size)]
            }
            _intelligenceBrief.value = "[!] ALERT: $fallbackInsight"
        }
    }

    // Task 3: Interactive query execution on Gemini. Uses real simulated satellite streaming typewriter visual
    fun sendTerminalQuery(query: String) {
        if (query.isBlank()) return
        viewModelScope.launch {
            _isThinking.value = true
            _terminalResponse.value = ""
            
            var rawResponse = ""
            val apiKey = if (_customApiKey.value.isNotBlank()) _customApiKey.value else com.asyria.v4.BuildConfig.GEMINI_API_KEY
            
            if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
                try {
                    kotlinx.coroutines.withTimeout(15000) {
                        val res = generateContentSafely(
                            "You are the high-end A.SYRIA V4 brain. Analyze the following project idea or question comprehensively and give a tactical security security brief style response. Under 50 words: $query"
                        )
                        if (!res.isNullOrEmpty()) {
                            rawResponse = res.trim()
                        }
                    }
                    _isNeuralLinkOffline.value = false
                } catch (e: Exception) {
                    e.printStackTrace()
                    _isNeuralLinkOffline.value = true
                }
            } else {
                rawResponse = "[ TERMINAL ALERT ] Neural Link is offline. Please configure a valid GEMINI API KEY in the System Settings to restore active intelligence."
            }

            if (rawResponse.isEmpty() || rawResponse == offlineTerminalResponses[0]) {
                _isNeuralLinkOffline.value = true
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

    // Phase 12: Dynamic Syllabus Module Scenario Generation using Gemini API
    fun generateAcademyScenarios(
        moduleNameEn: String,
        moduleNameAr: String,
        useArabic: Boolean,
        onSuccess: (String) -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        viewModelScope.launch {
            _isAcademyGenerating.value = true
            val apiKey = if (_customApiKey.value.isNotBlank()) _customApiKey.value else com.asyria.v4.BuildConfig.GEMINI_API_KEY
            if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
                try {
                    val promptLocale = if (useArabic) "Arabic (العربية)" else "English"
                    val prompt = """
                        Act as the A.SYRIA Professional Cyber-Security Professor.
                        Generate 3 unique, random, and highly challenging cybersecurity scenarios from a database of real-world attack patterns (OWASP Top 10, social engineering, zero-day exploits, nation-state actor vectors, IoT vulnerabilities, and blockchain security).
                        Never repeat the same scenario twice in a session.
                        Focus on the specific syllabus module: '$moduleNameEn' / '$moduleNameAr'.
                        Display everything strictly in $promptLocale.
                        Each scenario should include: A professional description of the threat scenario, 4 multiple-choice options with technical depth, and the correct answer index (0-3).
                        Format the response strictly as a JSON object inside a 'scenarios' key. Do not output anything other than standard JSON. No markdown backticks.
                        JSON Schema:
                        {
                          "scenarios": [
                            {
                              "description": "Realistic scenario description...",
                              "options": ["Option 1", "Option 2", "Option 3", "Option 4"],
                              "correct_index": 0
                            }
                          ]
                        }
                    """.trimIndent()
                    val res = generateContentSafely(prompt)
                    // Remove markdown wrapper if any
                    val cleanRes = res.replace("```json", "").replace("```", "").trim()
                    if (cleanRes.isNotBlank()) {
                        onSuccess(cleanRes)
                        _isAcademyGenerating.value = false
                        return@launch
                    }
                } catch (e: Exception) {
                    _isAcademyGenerating.value = false
                    onFailure(e)
                    return@launch
                }
            }
            _isAcademyGenerating.value = false
            onFailure(Exception("Neural Link offline or unavailable"))
        }
    }

    // Phase 12: Adaptive Learning Feedback via Strategic Debrief with bilingual support
    fun generateStrategicDebrief(scenario: String, choiceText: String, useArabic: Boolean, onResponse: (String) -> Unit) {
        viewModelScope.launch {
            val apiKey = if (_customApiKey.value.isNotBlank()) _customApiKey.value else com.asyria.v4.BuildConfig.GEMINI_API_KEY
            if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
                try {
                    val promptLocale = if (useArabic) "Arabic (العربية)" else "English"
                    val prompt = """
                        The user chose '$choiceText' for the cybersecurity scenario: '$scenario'. 
                        Explain why this choice is Secure or Insecure, and give a one-sentence tip to prevent this in real life.
                        Output the response strictly in $promptLocale. Limit your explanation to 60 words. No introduction.
                    """.trimIndent()
                    val res = generateContentSafely(prompt)
                    if (res.isNotBlank()) {
                        onResponse(res.trim())
                        return@launch
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            onResponse("") // Trigger fallback
        }
    }

    fun analyzeResourceLink(url: String, resourceTitle: String = "Unknown Reference") {
        viewModelScope.launch {
            _isAnalyzingLink.value = true
            _linkAnalysisResult.value = null
            
            val isAr = _isArabic.value

            // Background Scanning Optimization: perform a HEAD request
            var linkStatus = "Unknown"
            try {
                withContext(Dispatchers.IO) {
                    val headRequest = Request.Builder().url(url).head().build()
                    val response = okHttpClient.newCall(headRequest).execute()
                    linkStatus = if (response.isSuccessful) "Active/Reachable (HTTP ${response.code})" else "Unreachable/Blocked (HTTP ${response.code})"
                }
            } catch (e: Exception) {
                linkStatus = "Failed to reach (${e.message})"
            }
            
            val apiKey = if (_customApiKey.value.isNotBlank()) _customApiKey.value else com.asyria.v4.BuildConfig.GEMINI_API_KEY
            if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
                try {
                    kotlinx.coroutines.withTimeout(20000) {
                        val promptLocale = if (isAr) "Arabic (العربية)" else "English"
                        val prompt = """
                            Act as the A.SYRIA Link Scanner engine.
                            Analyze the security of this resource link:
                            Title: $resourceTitle
                            URL: $url
                            HTTP Status Check: $linkStatus
                            
                            Provide a Neural Risk Analysis including: 
                            1. Safety Score (0-100)
                            2. Potential Threats (if any)
                            3. Tactical Recommendation
                            
                            Output format strictly in $promptLocale. Limit to 80 words. Focus on technical accuracy and sovereign protection.
                        """.trimIndent()
                        
                        val res = generateContentSafely(prompt)
                        _linkAnalysisResult.value = res.trim()
                    }
                    _isNeuralLinkOffline.value = false
                } catch (e: Exception) {
                    _isNeuralLinkOffline.value = true
                    _linkAnalysisResult.value = if (isAr) "فشل الاتصال العصبي أثناء فحص الرابط: ${e.message}" else "Neural uplink failed during link analysis: ${e.message}"
                }
            } else {
                _isNeuralLinkOffline.value = true
                _linkAnalysisResult.value = if (isAr) "[ تنبيه نهائي ] العقدة العصبية غير متصلة. يرجى إضافة مفتاح Gemini API صالح في إعدادات النظام." else "[ TERMINAL ALERT ] Neural Link is offline. Please configure a valid GEMINI API KEY in the System Settings to restore active intelligence."
            }
            _isAnalyzingLink.value = false
        }
    }

    fun clearLinkAnalysis() {
        _linkAnalysisResult.value = null
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return
        
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                gravity = event.values
                val ax = event.values[0]
                val ay = event.values[1]

                val targetRoll = -ax * 4f
                val targetPitch = ay * 4f

                currentRoll = currentRoll + alpha * (targetRoll - currentRoll)
                currentPitch = currentPitch + alpha * (targetPitch - currentPitch)

                _roll.value = currentRoll
                _pitch.value = currentPitch
            }
            Sensor.TYPE_MAGNETIC_FIELD -> {
                geomagnetic = event.values
            }
        }

        if (gravity != null && geomagnetic != null) {
            val rMatrix = FloatArray(9)
            val iMatrix = FloatArray(9)
            if (SensorManager.getRotationMatrix(rMatrix, iMatrix, gravity, geomagnetic)) {
                val orientation = FloatArray(3)
                SensorManager.getOrientation(rMatrix, orientation)
                var az = Math.toDegrees(orientation[0].toDouble()).toFloat()
                if (az < 0) az += 360f
                _azimuth.value = az
            }
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
