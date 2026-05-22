package com.example.ui.screens

import androidx.compose.animation.*
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
import kotlin.random.Random

// Structured Scenarios Entity for UI representation
data class AcademyScenario(
    val description: String,
    val options: List<String>,
    val correctIndex: Int
)

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun CyberAcademyScreen(
    viewModel: DashboardViewModel,
    onClose: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var isGeneratingScenarios by remember { mutableStateOf(false) }
    var scenariosList by remember { mutableStateOf<List<AcademyScenario>>(emptyList()) }
    var currentScenarioIndex by remember { mutableStateOf(0) }
    var selectedOptionIndex by remember { mutableStateOf<Int?>(null) }
    var debriefText by remember { mutableStateOf("") }
    var isGeneratingDebrief by remember { mutableStateOf(false) }
    var hasAnsweredCurrent by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    // Hardcoded Fallback scenarios matching Phase 11 instruction in Arabic with high fidelity
    val offlineScenarios = remember {
        listOf(
            AcademyScenario(
                description = "تلقيت مكالمة هاتفية من شخص يدعي أنه مديرك المالي في الشركة، ويطلب منك تحويل مبلغ طارئ بشكل عاجل مستخدماً نبرة صوته المألوفة تماماً وضغطاً نفسياً شديداً. ما هو الإجراء الأكثر أماناً؟",
                options = listOf(
                    "أ) تحويل المبلغ فوراً لتجنب أي مشاكل وظيفية.",
                    "ب) إبلاغ زملائك بالخطوة للتحقق من هويتهم وصحتهم.",
                    "ج) إنهاء المكالمة والاتصال بمديرك عبر قناة اتصال رسمية وموثوقة مسبقاً للتحقق من الطلب.",
                    "د) طلب المستندات الموقعة عبر البريد الإلكتروني العادي وإجراء التحويل."
                ),
                correctIndex = 2
            ),
            AcademyScenario(
                description = "عند مسح رمز استجابة سريعة (QR Code) على طاولة مقهى عام لدفع الفاتورة، تم تحويلك إلى صفحة تطلب إدخال تفاصيل بطاقتك الائتمانية دون أي تأكيد أو توقيع رقمي معتمد. ماذا تفعل؟",
                options = listOf(
                    "أ) إدخال البيانات المطلوبة لاستكمال عملية الدفع بسرعة.",
                    "ب) تجاهل الصفحة، وإبلاغ إدارة المقهى فوراً بوجود احتمالية ملصق رمز QR احتيالي، والدفع نقداً.",
                    "ج) التقاط صورة للملصق ومشاركتها في وسائل التواصل الاجتماعي للتحذير.",
                    "د) إعادة محاولة مسح الرمز عدة مرات للتأكد من الرابط."
                ),
                correctIndex = 1
            ),
            AcademyScenario(
                description = "تلقيت رسالة من صديق مقرب على منصة تواصل اجتماعي يطلب منك مشاركة رمز التحقق (OTP) الذي وصل إلى هاتفك الآن لمساعدته في استعادة حسابه المغلق. ما هو رد الفعل السليم؟",
                options = listOf(
                    "أ) إرسال الرمز فوراً لمساعدة صديقك المحتاج.",
                    "ب) تجاهل الرسالة تماماً وحظر صديقك مدى الحياة.",
                    "ج) عدم إرسال الرمز بتاتاً، والاتصال بصديقك هاتفياً عبر مكالمة صوتية للتأكد من اختراق حسابه.",
                    "د) إرسال رمز خاطئ لاختبار ما إذا كان صديقك حقيقياً."
                ),
                correctIndex = 2
            )
        )
    }

    // Load scenarios on screen trigger
    LaunchedEffect(Unit) {
        isGeneratingScenarios = true
        errorMessage = ""
        viewModel.generateAcademyScenarios(
            onSuccess = { responseJson ->
                try {
                    val cleaned = cleanJson(responseJson)
                    val json = JSONObject(cleaned)
                    val array = json.getJSONArray("scenarios")
                    val parsed = mutableListOf<AcademyScenario>()
                    for (i in 0 until array.length()) {
                        val obj = array.getJSONObject(i)
                        val desc = obj.getString("description")
                        val optsArr = obj.getJSONArray("options")
                        val options = mutableListOf<String>()
                        for (j in 0 until optsArr.length()) {
                            options.add(optsArr.getString(j))
                        }
                        val correct = obj.getInt("correct_index")
                        parsed.add(AcademyScenario(desc, options, correct))
                    }
                    if (parsed.isNotEmpty()) {
                        scenariosList = parsed
                    } else {
                        scenariosList = offlineScenarios
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    // Fallback to high fidelity offline list
                    scenariosList = offlineScenarios
                } finally {
                    isGeneratingScenarios = false
                }
            },
            onFailure = {
                // Network or Key missing failure - graceful fallback
                scenariosList = offlineScenarios
                isGeneratingScenarios = false
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.94f))
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Screen Header in Cyber-noir Display style
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "NEURAL CYBER ACADEMY",
                        color = CyberCyan,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 3.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "DYNAMIC ZERO-TRUST TRAINING HUB",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Box(
                    modifier = Modifier
                        .border(1.dp, CyberCyan.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                        .clickable {
                            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                            onClose()
                        }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "X",
                        color = CyberCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (isGeneratingScenarios) {
                // Interactive loading states
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 100.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "QUANTUM GENERATOR ACTIVE...",
                        color = AmberZen,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Synthesizing new cybersecurity attack models from Gemini cognitive cloud...",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            } else if (scenariosList.isNotEmpty()) {
                val currentScenario = scenariosList[currentScenarioIndex]

                // Full Robust Arabic RTL Support block
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.Start
                    ) {
                        // Progress / index indicator
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "السؤال ${currentScenarioIndex + 1} من ${scenariosList.size}",
                                color = CyberCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "درجة الأكاديمية: +25 نقطة للإجابة الصحيحة",
                                color = Color.White.copy(alpha = 0.4f),
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        // Threat scenario description card
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, CyberCyan.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                .background(GlassWhite, RoundedCornerShape(12.dp))
                                .padding(16.dp)
                        ) {
                            Text(
                                text = currentScenario.description,
                                color = Color.White,
                                fontSize = 14.sp,
                                lineHeight = 22.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Render options one by one with glowing responsive indicators
                        Text(
                            text = "اختر الإجراء المناسب والأكثر أماناً:",
                            color = CyberCyan.copy(alpha = 0.7f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

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
                                    .padding(vertical = 6.dp)
                                    .border(0.75.dp, optionBorderColor, RoundedCornerShape(10.dp))
                                    .background(optionBgColor, RoundedCornerShape(10.dp))
                                    .clickable(enabled = !hasAnsweredCurrent) {
                                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                                        selectedOptionIndex = idx
                                    }
                                    .padding(14.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .border(
                                                1.dp,
                                                if (isSelected) CyberCyan else Color.White.copy(alpha = 0.4f),
                                                RoundedCornerShape(50)
                                            )
                                            .background(
                                                if (isSelected) CyberCyan else Color.Transparent,
                                                RoundedCornerShape(50)
                                            )
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = option,
                                        color = if (isSelected) Color.White else Color.White.copy(alpha = 0.85f),
                                        fontSize = 13.sp,
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Verify button / action controller
                        if (!hasAnsweredCurrent && selectedOptionIndex != null) {
                            Button(
                                onClick = {
                                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                    val finalChoice = selectedOptionIndex!!
                                    hasAnsweredCurrent = true
                                    
                                    // Gamification score addition
                                    if (finalChoice == currentScenario.correctIndex) {
                                        viewModel.addCyberScore(25)
                                    }

                                    // Dynamic strategic debrief generator from Gemini
                                    isGeneratingDebrief = true
                                    debriefText = ""
                                    viewModel.generateStrategicDebrief(
                                        scenario = currentScenario.description,
                                        choiceText = currentScenario.options[finalChoice],
                                        onResponse = { responseDebrief ->
                                            if (responseDebrief.isNotBlank()) {
                                                debriefText = responseDebrief
                                            } else {
                                                // Robust fallback strategic tips
                                                debriefText = getLocalDebriefFallback(currentScenarioIndex, finalChoice)
                                            }
                                            isGeneratingDebrief = false
                                        }
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CyberCyan.copy(alpha = 0.2f)),
                                border = BorderStroke(1.dp, CyberCyan),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth().height(48.dp)
                            ) {
                                Text(
                                    text = "إرسال الجواب لمراجعة الخبير",
                                    color = CyberCyan,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Adaptive Learning Feedback: AmberZen glass card
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
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(AmberZen, RoundedCornerShape(50))
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "التقرير التكتيكي الاستراتيجي DEBRIEF:",
                                        color = AmberZen,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                if (isGeneratingDebrief) {
                                    Text(
                                        text = "جاري الاتصال بالعقل الاستراتيجي لتحليل قرارك الرقمي...",
                                        color = Color.White.copy(alpha = 0.5f),
                                        fontSize = 11.sp
                                    )
                                } else {
                                    Text(
                                        text = debriefText,
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        lineHeight = 20.sp
                                    )
                                }
                            }
                        }

                        // Progress to next scenario controller
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
                                        // Completed all Scenarios
                                        onClose()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth().height(48.dp)
                            ) {
                                Text(
                                    text = if (currentScenarioIndex + 1 < scenariosList.size) "السيناريو التالي 🡠" else "إنهاء التدريب التفاعلي",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            } else {
                // Connection or structural parsing failed state
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 100.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "NEURAL SYNC RE-ROUTING...",
                        color = Color.Red,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Establishing secure connection matrix.",
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

// Clean JSON payload wrapping cleanly
fun cleanJson(raw: String): String {
    val trimmed = raw.trim()
    if (trimmed.startsWith("```json")) {
        return trimmed.removePrefix("```json").removeSuffix("```").trim()
    } else if (trimmed.startsWith("```")) {
        return trimmed.removePrefix("```").removeSuffix("```").trim()
    }
    return trimmed
}

// Full offline backup and secure de-briefings
fun getLocalDebriefFallback(scenarioIdx: Int, chosenIdx: Int): String {
    return when (scenarioIdx) {
        0 -> {
            if (chosenIdx == 2) {
                "قرار ممتاز وأمن بالكامل! التأكد المستمر من هويات الأشخاص عبر قنوات مسبقة ومستقلة يفشل الذكاء الاصطناعي على الانتحال. نصيحة: اعتمد دائماً قنوات اتصال مشفرة ومؤكدة لمنع التلاعب الجنائي."
            } else {
                "تصرف عالي الخطورة! انتحال الصوت بصنع الذكاء الاصطناعي دقيق للغاية ومضلل، والموافقة المباشرة قد تسبب كوارث للشركة. نصيحة: لا توافق مطلقاً على تحويل أموال بمكالمة هاتفية دون التأكيد المرئي الفعلي."
            }
        }
        1 -> {
            if (chosenIdx == 1) {
                "رائع! رمز الاستجابة السريعة (QR) معرض دائمًا للملصقات الاحتيالية الخبيثة (Quishing). التجاهل وإبلاغ المقهى يحد من الضرر. نصيحة: تأكد دائماً من سلامة الموقع الفعلي للملصق ولا تشارك بطاقتك بمواقع مجهولة."
            } else {
                "انتبه للغاية! قد تنقلك رموز الـ QR لصفحات خداعية بالكامل تسرق بيانات بطاقتك الائتمانية. نصيحة: تجنب كتابة أرقام بطاقات بمواقع دفع غير معروفة ومسحوبة من ملصقات بالأماكن العامة."
            }
        }
        2 -> {
            if (chosenIdx == 2) {
                "رائع! كود التحقق الثنائي (OTP) سري بالمطلق وشخصي ولا يعطى لأصدقاء، لأن جهاز صديقك مخترق بنسبة مؤكدة. نصيحة: تذكر دائماً ألا تعطي الرموز السرية لأي شخص كأولى بديهيات الحماية."
            } else {
                "خطأ في منتهى الخطورة الرقمية! إعطاء الـ OTP يسلم حسابك ومحادثات ومفاتيحك للمهاجم مباشرة. نصيحة: لا تشارك الرموز الرقمية الآمنة مع أي اتصال افتراضي تحت أي عذر."
            }
        }
        else -> "الرجاء ممارسة أساليب الأمان الرقمي الصفرية في تحركاتك التقنية اليومية لمنع استغلال الأنظمة الحيوية."
    }
}
