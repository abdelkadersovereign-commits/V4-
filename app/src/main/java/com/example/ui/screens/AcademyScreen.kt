package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.ui.geometry.Offset
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmberZen
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.GlassWhite
import com.example.ui.theme.VoidBlack
import com.example.ui.viewmodel.DashboardViewModel
import org.json.JSONObject
import org.json.JSONArray

data class AcademyScenario(
    val description: String,
    val options: List<String>,
    val correctIndex: Int
)

@Immutable
data class AcademySyllabusModule(
    val id: String,
    val titleAr: String,
    val titleEn: String,
    val descAr: String,
    val descEn: String,
    val icon: String
)

@Composable
fun ShimmerEffect(
    modifier: Modifier = Modifier,
    width: Float = 0.5f
) {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val xShimmer by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "xShimmer"
    )

    val brush = Brush.linearGradient(
        colors = listOf(
            CyberCyan.copy(alpha = 0.05f),
            CyberCyan.copy(alpha = 0.2f),
            CyberCyan.copy(alpha = 0.05f),
        ),
        start = Offset(xShimmer - 300f, xShimmer - 300f),
        end = Offset(xShimmer, xShimmer)
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(20.dp)
            .background(brush, RoundedCornerShape(4.dp))
    )
}

@Composable
fun AcademyScreen(
    viewModel: DashboardViewModel,
    modifier: Modifier = Modifier
) {
    val isAr by viewModel.isArabic.collectAsState()
    val layoutDirection = if (isAr) LayoutDirection.Rtl else LayoutDirection.Ltr
    val haptic = LocalHapticFeedback.current

    var selectedModule by remember { mutableStateOf<AcademySyllabusModule?>(null) }

    val syllabusModules = remember {
        listOf(
            AcademySyllabusModule(
                id = "social_engineering",
                titleAr = "الهندسة الاجتماعية",
                titleEn = "Social Engineering",
                descAr = "طرق التلاعب بالبشر للحصول على معلومات بأساليب احتيال متقدمة.",
                descEn = "Manipulating humans into revealing confidential credentials with advanced psychological vectors.",
                icon = "👥"
            ),
            AcademySyllabusModule(
                id = "mobile_security",
                titleAr = "أمن الهواتف",
                titleEn = "Mobile Security",
                descAr = "حماية أنظمة iOS و Android من تطبيقات التجسس العميقة.",
                descEn = "Securing iOS and Android hosts against zero-click kernel spyware.",
                icon = "📱"
            ),
            AcademySyllabusModule(
                id = "network_safety",
                titleAr = "سلامة الشبكات",
                titleEn = "Network Safety",
                descAr = "إحباط هجمات اعتراض البيانات وهجمات الرجل في المنتصف الذكية.",
                descEn = "Detecting and disabling Wi-Fi interception vectors & Man-in-the-Middle payloads.",
                icon = "🌐"
            ),
            AcademySyllabusModule(
                id = "privacy_rights",
                titleAr = "حقوق الخصوصية",
                titleEn = "Privacy Rights",
                descAr = "الاستراتيجيات الرقمية والتشريعية لمقاومة التتبع ومسح البيانات.",
                descEn = "Tactics and legal models to prevent metadata aggregation & non-consensual scraping.",
                icon = "⚖"
            )
        )
    }

    BackHandler(enabled = selectedModule != null) {
        selectedModule = null
    }

    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(VoidBlack, Color(0xFF04070D))
                    )
                )
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isAr) "الأكاديمية السيادية" else "SOVEREIGN ACADEMY",
                        color = CyberCyan,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 2.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = if (isAr) "منهج الدفاع المتقدم والاستخبارات المهنية" else "ADVANCED DEFENSIVE & INTEL CURRICULUM",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Quick Language Toggle inside screen
                Box(
                    modifier = Modifier
                        .border(1.dp, CyberCyan.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                        .clickable {
                            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                            viewModel.setArabic(!isAr)
                        }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (isAr) "EN" else "العربية",
                        color = CyberCyan,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Module view switching
            AnimatedContent(
                targetState = selectedModule,
                transitionSpec = {
                    slideInHorizontally(initialOffsetX = { if (targetState != null) it else -it }) + fadeIn() togetherWith
                    slideOutHorizontally(targetOffsetX = { if (targetState != null) -it else it }) + fadeOut()
                },
                label = "academyTransition"
            ) { module ->
                if (module == null) {
                    // Showcase syllabus modules
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = if (isAr) "اختر ملف المنهج للبدء بالاختبار العصبي:" else "SELECT A SYLLABUS DIRECTORY FOR NEURAL TESTING:",
                            color = AmberZen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        syllabusModules.forEach { item ->
                            val title = if (isAr) item.titleAr else item.titleEn
                            val desc = if (isAr) item.descAr else item.descEn

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                                    .border(0.5.dp, CyberCyan.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                    .background(Color.White.copy(alpha = 0.02f), RoundedCornerShape(12.dp))
                                    .clickable {
                                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                                        selectedModule = item
                                    }
                                    .padding(16.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    Text(
                                        text = item.icon,
                                        fontSize = 24.sp,
                                        modifier = Modifier
                                            .background(CyberCyan.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                            .padding(8.dp)
                                    )

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = title.uppercase(),
                                            color = Color.White,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = desc,
                                            color = Color.White.copy(alpha = 0.6f),
                                            fontSize = 11.sp,
                                            lineHeight = 16.sp
                                        )
                                    }

                                    Text(
                                        text = "🡠",
                                        color = CyberCyan,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // Active neural scenario test for selected syllabus module
                    NeuralModuleTestView(
                        module = module,
                        viewModel = viewModel,
                        isAr = isAr,
                        onBack = { selectedModule = null }
                    )
                }
            }
        }
    }
}

@Composable
fun NeuralModuleTestView(
    module: AcademySyllabusModule,
    viewModel: DashboardViewModel,
    isAr: Boolean,
    onBack: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var isGeneratingScenarios by remember { mutableStateOf(false) }
    var scenariosList by remember { mutableStateOf<List<AcademyScenario>>(emptyList()) }
    var currentScenarioIndex by remember { mutableStateOf(0) }
    var selectedOptionIndex by remember { mutableStateOf<Int?>(null) }
    var debriefText by remember { mutableStateOf("") }
    var isGeneratingDebrief by remember { mutableStateOf(false) }
    var hasAnsweredCurrent by remember { mutableStateOf(false) }

    // Multi-Language high fidelity offline scenarios per module
    val offlineScenarios = remember(module.id) {
        when (module.id) {
            "social_engineering" -> listOf(
                AcademyScenario(
                    description = if (isAr) {
                        "تلقيت استدعاءً طارئاً يزعم أنه من فريق الدعم الفني لشركة Uber منتحلاً هوية مهندس مهاد فني، ويطلب الحصول على رمز التحقق للمصادقة الثنائية (MFA Fatigue) بزعم تجنب توقف حسابك."
                    } else {
                        "You receive successive, urgent push alerts from Uber support, followed by an SMS claiming a support specialist needs your Multi-Factor Authentication (MFA) validation code immediately to resolve a pending terminal error. What protocol should you execute?"
                    },
                    options = if (isAr) {
                        listOf(
                            "أ) مشاركة الكود المستلم لتسهيل عملية الدعم الفني فورا.",
                            "ب) تجاهل الكود والانتظار حتى يتوقف التنبيه التلقائي.",
                            "ج) رفض إرسال الكود بالمطلق، وتأكيد المحاولة مع مركز الدعم الرسمي بشكل منعزل للتحقق من المخترق.",
                            "د) تقديم رمز مرور عشوائي لتشويش محاولات الطرف الطالب."
                        )
                    } else {
                        listOf(
                            "A) Grant the code to expedite validation and restore workflow.",
                            "B) Standby and hope the automated alerts expire without further vector damage.",
                            "C) Strictly reject giving any code, flag the session as unauthorized, and contact support through authenticated systems.",
                            "D) Provide a pseudo-random OTP sequence to confuse the tracking entity."
                        )
                    },
                    correctIndex = 2
                )
            )
            "mobile_security" -> listOf(
                AcademyScenario(
                    description = if (isAr) {
                        "تلقيت تنبيهاً أمنياً غامضاً يحثك على تثبيت ملف تعريفي خارجي (Configuration Profile) على هاتفك مستغلاً هندسة أمنية لتثبيت برمجية تجسس نووية خبيثة تشبه Pegasus."
                    } else {
                        "An online landing page alerts you that your smartphone has critical system leaks and prompts you to download and install an enterprise 'Configuration Profile'. This closely replicates advanced zero-click Pegasus spyware vector deployments. What is your action?"
                    },
                    options = if (isAr) {
                        listOf(
                            "أ) تثبيت الملف لضمان استقرار هاتفك الأمني فورا وصيانة المشكلة.",
                            "ب) رفض تثبيت أي ملفات مواصفات تعريف غير موثقة، وفحص الشهادات النشطة حالياً عبر المسار الأمني.",
                            "ج) إعادة تشغيل الهاتف وتثبيت الملف في الوضع الآمن للتأكد من سلامته.",
                            "د) التقاط لقطة شاشة للاستفسار في المجموعات الأمنية قبل البدء."
                        )
                    } else {
                        listOf(
                            "A) Proceed with the installation to maintain system upgrades.",
                            "B) Reject the configuration install prompt, verify active MDM certificates globally, and enable strict Lockdown/Isolated sandbox protocols.",
                            "C) Restart the kernel and install in safe mode to parse telemetry safely.",
                            "D) Take multiple snapshots to analyze later on a public sandbox forum."
                        )
                    },
                    correctIndex = 1
                )
            )
            "network_safety" -> listOf(
                AcademyScenario(
                    description = if (isAr) {
                        "أثناء اتصالك بشبكة Wi-Fi غير آمنة في مطار، تدرك وجود هجمات اعتراض نشطة (MITM) تحاول إرسال تحديثات شهادات Root خبيثة للاستماع لكامل اتصالاتك الحيوية."
                    } else {
                        "While connected to an open airport lounge Wi-Fi network, your terminal issues SSL verification warnings and attempts to inject a hostile root CA certificate, mimicking a tactical Man-in-the-Middle (MITM) attack. How do you respond?"
                    },
                    options = if (isAr) {
                        listOf(
                            "أ) تفعيل شهادة الجذر لتخطي قيود الشبكة والاتصال السريع.",
                            "ب) إزالة الاتصال بالشبكة كلياً، والانتقال إلى ناقل البيانات الخليوي واستعمال نفق VPN عسكري تكتيكي متماسك.",
                            "ج) الاستمرار بالتصفح مع تجاهل تحذيرات شهادات الأمان (HTTPS Warnings).",
                            "د) مسح ذاكرة التخزين المؤقت للمتصفح ومحاولة الضغط المستمر."
                        )
                    } else {
                        listOf(
                            "A) Accept the updated root CA to bypass the airport captive portal limits.",
                            "B) Sever the Wi-Fi connection immediately, fallback to secure cellular LTE data, and establish a military-grade private VPN tunnel.",
                            "C) Continue browsing using alternative browsers while suppressing security warnings.",
                            "D) Clear regional cookies and flush DNS tables continuously."
                        )
                    },
                    correctIndex = 1
                )
            )
            else -> listOf(
                AcademyScenario(
                    description = if (isAr) {
                        "أكتشفت قيام أداة ألعاب بمسح بياناتك التعريفية وسجل المواقع والصفحات السابقة عبر تقنيات سحب البيانات التعويضية وملفات التعريف بدون موافقتك الصريحة."
                    } else {
                        "A modern web tool is discovered to be programmatically scraping your historical metadata, search trends, and localized search buffers using cross-site third-party tracker APIs without consent. How do you handle this?"
                    },
                    options = if (isAr) {
                        listOf(
                            "أ) حذف الملف المؤقت للمتصفح دون تعديل أذونات الخصوصية العامة.",
                            "ب) حظر ملفات تتبع الطرف الثالث عبر المتصفح (3rd-Party Cookies)، وتفعيل طلبات عدم تتبع البيانات، واستخدام محرك بحث يحمي الخصوصية.",
                            "ج) شكوى الخدمة الموفرة لرفع دعوى جماعية كخطوة وحيدة.",
                            "د) كتم الإعلانات الفردية يدوياً للتخلص من الملاحقات."
                        )
                    } else {
                        listOf(
                            "A) Delete the temporary browser folder and clear local system directories manually.",
                            "B) Restrict all third-party cookies, enforce strict anti-fingerprint blocks in browser profiles, and shift searches to zero-logging networks.",
                            "C) File a privacy appeal under global frameworks as your primary reactive response.",
                            "D) Suppress individual personalized ad tracking units manually each session."
                        )
                    },
                    correctIndex = 1
                )
            )
        }
    }

    // Trigger dynamic scenarios loading
    LaunchedEffect(module.id) {
        isGeneratingScenarios = true
        scenariosList = emptyList()
        currentScenarioIndex = 0
        selectedOptionIndex = null
        debriefText = ""
        hasAnsweredCurrent = false

        viewModel.generateAcademyScenarios(
            moduleNameEn = module.titleEn,
            moduleNameAr = module.titleAr,
            useArabic = isAr,
            onSuccess = { jsonString ->
                try {
                    val root = JSONObject(jsonString)
                    val array = root.getJSONArray("scenarios")
                    val parsed = mutableListOf<AcademyScenario>()
                    for (i in 0 until array.length()) {
                        val itemObj = array.getJSONObject(i)
                        val desc = itemObj.getString("description")
                        val optsArr = itemObj.getJSONArray("options")
                        val options = mutableListOf<String>()
                        for (j in 0 until optsArr.length()) {
                            options.add(optsArr.getString(j))
                        }
                        val correct = itemObj.getInt("correct_index")
                        parsed.add(AcademyScenario(desc, options, correct))
                    }
                    if (parsed.isNotEmpty()) {
                        scenariosList = parsed
                    } else {
                        scenariosList = offlineScenarios
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    scenariosList = offlineScenarios
                } finally {
                    isGeneratingScenarios = false
                }
            },
            onFailure = {
                scenariosList = offlineScenarios
                isGeneratingScenarios = false
            }
        )
    }

    val isAcademyGenerating by viewModel.isAcademyGenerating.collectAsState()

    if (isAcademyGenerating) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = if (isAr) "استدعاء السيناريوهات السيادية للجلسة..." else "INVOKING SOVEREIGN SESSION SCENARIOS...",
                color = CyberCyan,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            Column(
                modifier = Modifier.fillMaxWidth(0.9f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ShimmerEffect(modifier = Modifier.height(60.dp))
                ShimmerEffect(modifier = Modifier.height(100.dp))
                ShimmerEffect(modifier = Modifier.height(40.dp))
                ShimmerEffect(modifier = Modifier.height(40.dp))
                ShimmerEffect(modifier = Modifier.height(40.dp))
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = if (isAr) "تحسين نماذج الهجوم والدفاع العصبية" else "OPTIMIZING NEURAL OFFENSE/DEFENSE MODELS",
                color = Color.White.copy(alpha = 0.4f),
                fontSize = 8.sp,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
        // Back Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isAr) "🡠 عودة للمناهج" else "🡠 BACK TO SYLLABUS",
                color = AmberZen,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier
                    .clickable { 
                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                        onBack() 
                    }
                    .padding(vertical = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (isGeneratingScenarios) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 80.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = if (isAr) "جاري استدعاء العقل الاصطناعي..." else "DYNAMIC COMPILING VIA GEMINI COGNITION...",
                    color = CyberCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = if (isAr) "تحليل حوادث الاختراقات الواقعية وبناء السيناريو الفعلي..." else "Synthesizing educational simulations targeting historical breaches...",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )
            }
        } else if (scenariosList.isNotEmpty()) {
            val currentScenario = scenariosList[currentScenarioIndex]

            // Question Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isAr) {
                        "السؤال ${currentScenarioIndex + 1} من ${scenariosList.size}"
                    } else {
                        "QUESTION ${currentScenarioIndex + 1} OF ${scenariosList.size}"
                    },
                    color = CyberCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = if (isAr) "مستوى النقاط: +25 نقطة" else "REWARD: +25 PTS",
                    color = AmberZen,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Scenario Board
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CyberCyan.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                    .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Text(
                    text = currentScenario.description,
                    color = Color.White,
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (isAr) "اختر الحل التكتيكي الأكثر أماناً وحماية:" else "CHOOSE THE SECURE STRATEGIC TACTIC:",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Render options beautifully
            currentScenario.options.forEachIndexed { idx, option ->
                val isSelected = selectedOptionIndex == idx
                val isCorrect = idx == currentScenario.correctIndex
                
                val optionBorderColor = when {
                    isSelected -> if (hasAnsweredCurrent) {
                        if (isCorrect) Color.Green else Color.Red
                    } else CyberCyan
                    hasAnsweredCurrent && isCorrect -> Color.Green.copy(alpha = 0.6f)
                    else -> Color.White.copy(alpha = 0.15f)
                }
                
                val optionBgColor = when {
                    isSelected -> if (hasAnsweredCurrent) {
                        if (isCorrect) Color.Green.copy(alpha = 0.1f) else Color.Red.copy(alpha = 0.1f)
                    } else CyberCyan.copy(alpha = 0.08f)
                    hasAnsweredCurrent && isCorrect -> Color.Green.copy(alpha = 0.05f)
                    else -> Color.White.copy(alpha = 0.02f)
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .border(1.dp, optionBorderColor, RoundedCornerShape(10.dp))
                        .background(optionBgColor, RoundedCornerShape(10.dp))
                        .clickable(enabled = !hasAnsweredCurrent) {
                            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                            selectedOptionIndex = idx
                        }
                        .padding(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Surface(
                            modifier = Modifier.size(10.dp),
                            shape = RoundedCornerShape(50),
                            color = if (isSelected) CyberCyan else Color.Transparent,
                            border = BorderStroke(1.1.dp, if (isSelected) CyberCyan else Color.White.copy(alpha = 0.4f))
                        ) {}
                        Text(
                            text = option,
                            color = if (isSelected) Color.White else Color.White.copy(alpha = 0.8f),
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Submit Button
            if (!hasAnsweredCurrent) {
                Button(
                    onClick = {
                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                        val finalChoice = selectedOptionIndex ?: return@Button
                        hasAnsweredCurrent = true
                        
                        // Score Addition
                        if (finalChoice == currentScenario.correctIndex) {
                            viewModel.addCyberScore(10)
                        }

                        // Generate Adaptive Feedback via Gemini
                        isGeneratingDebrief = true
                        debriefText = ""
                        viewModel.generateStrategicDebrief(
                            scenario = currentScenario.description,
                            choiceText = currentScenario.options[finalChoice],
                            useArabic = isAr,
                            onResponse = { responseDebrief ->
                                if (responseDebrief.isNotBlank()) {
                                    debriefText = responseDebrief
                                } else {
                                    // Local specific offline feedback
                                    debriefText = if (isAr) {
                                        if (finalChoice == currentScenario.correctIndex) "قرار ممتاز ومطابق للأمن السيبراني العالي! الوعي الصارم والتحقق يفشلان عمليات الاختراق. تم منح +25 نقطة." else "تصرف عالي الخطورة. تذكر دائماً عزل قنوات الاتصال والامتناع عن منح رموز التحقق أو الملفات المشبوهة."
                                    } else {
                                        if (finalChoice == currentScenario.correctIndex) "Action secure. Zero-Trust validation stops real-world malicious breach vectors perfectly! +25 PTS awarded." else "Critical operational vulnerability identified. Remember to always sever unverified configurations or channels immediately."
                                    }
                                }
                                isGeneratingDebrief = false
                            }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyberCyan.copy(alpha = 0.1f),
                        disabledContainerColor = Color.White.copy(alpha = 0.02f)
                    ),
                    border = BorderStroke(1.dp, if (selectedOptionIndex != null) CyberCyan else Color.White.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(10.dp),
                    enabled = selectedOptionIndex != null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Text(
                        text = if (isAr) "إرسال الجواب للتحقق" else "SUBMIT DEPLOYED ANSWER",
                        color = if (selectedOptionIndex != null) CyberCyan else Color.White.copy(alpha = 0.3f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Adaptive Feedback Debrief
            AnimatedVisibility(
                visible = hasAnsweredCurrent && (isGeneratingDebrief || debriefText.isNotEmpty()),
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Spacer(modifier = Modifier.height(14.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, AmberZen.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                        .background(GlassWhite, RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(AmberZen, RoundedCornerShape(50))
                        )
                        Text(
                            text = if (isAr) "التقرير التكتيكي الاستراتيجي DEBRIEF:" else "STRATEGIC OPERATIONS DEBRIEF:",
                            color = AmberZen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    if (isGeneratingDebrief) {
                        Text(
                            text = if (isAr) "جاري استدعاء المحللين التكتيكيين..." else "Connecting to high-order threat matrices for decision analytics...",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    } else {
                        Text(
                            text = debriefText,
                            color = Color.White,
                            fontSize = 13.sp,
                            lineHeight = 20.sp,
                            textAlign = TextAlign.Start,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Next Question or Exit
            if (hasAnsweredCurrent && !isGeneratingDebrief) {
                Spacer(modifier = Modifier.height(18.dp))
                Button(
                    onClick = {
                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                        if (currentScenarioIndex + 1 < scenariosList.size) {
                            currentScenarioIndex++
                            selectedOptionIndex = null
                            debriefText = ""
                            hasAnsweredCurrent = false
                        } else {
                            onBack()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(
                        text = if (isAr) {
                            if (currentScenarioIndex + 1 < scenariosList.size) "السيناريو التالي 🡠" else "عودة للمناهج"
                        } else {
                            if (currentScenarioIndex + 1 < scenariosList.size) "NEXT SCENARIO 🡠" else "COMPLETE MODULE"
                        },
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}
}
