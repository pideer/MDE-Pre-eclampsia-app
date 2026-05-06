package com.example.pre_eclampsiascreener.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pre_eclampsiascreener.ble.managers.CalibrateManager
import com.example.pre_eclampsiascreener.ble.repo.CalibrateRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

// ---------------------------------------------------------------------------
// Data model
// ---------------------------------------------------------------------------

/** A single timed PI sample used for the waveform display */
data class PiSample(val value: Float, val timestampMs: Long)

/** A calibration point: one averaged PI paired with measured BP */
data class CalibrationPoint(
    val pi: Float,
    val systolic: Int,
    val diastolic: Int,
)

/** Linear regression result: BP = slope * PI + intercept */
data class LinearModel(
    val slopeSystolic: Float,
    val interceptSystolic: Float,
    val slopeDiastolic: Float,
    val interceptDiastolic: Float,
)

enum class CalibrationStep {
    IDLE,
    REST_RECORD,
    REST_INPUT,
    ACTIVE_INSTRUCT,
    ACTIVE_RECORD,
    ACTIVE_INPUT,
    FINAL_REST_RECORD,
    FINAL_REST_INPUT,
    RESULTS,
}

// ---------------------------------------------------------------------------
// ViewModel
// ---------------------------------------------------------------------------

class CalibrationViewModel : ViewModel() {

    // -- Streaming window: last 30 s of samples (approx) -------------------
    private val WINDOW_MS = 30_000L

    private val _waveform = MutableStateFlow<List<PiSample>>(emptyList())
    val waveform: StateFlow<List<PiSample>> = _waveform.asStateFlow()

    val currentPi: StateFlow<Float?> = _waveform
        .map { it.lastOrNull()?.value }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    // -- Step machine -------------------------------------------------------
    private val _step = MutableStateFlow(CalibrationStep.IDLE)
    val step: StateFlow<CalibrationStep> = _step.asStateFlow()

    // -- Calibration points collected --------------------------------------
    private val _points = MutableStateFlow<List<CalibrationPoint>>(emptyList())
    val points: StateFlow<List<CalibrationPoint>> = _points.asStateFlow()

    // -- Regression model --------------------------------------------------
    private val _model = MutableStateFlow<LinearModel?>(null)
    val model: StateFlow<LinearModel?> = _model.asStateFlow()

    // -- Per-step collected window avg -------------------------------------
    private var windowAvgPi: Float? = null

    // -- Recording countdown (seconds) ------------------------------------
    private val RECORD_DURATION_S = 30
    private val _countdown = MutableStateFlow(RECORD_DURATION_S)
    val countdown: StateFlow<Int> = _countdown.asStateFlow()

    private var recordingJob: Job? = null
    private var collectJob: Job? = null

    // -- Streaming ---------------------------------------------------------

    fun startStream() {
        collectJob?.cancel()
        collectJob = viewModelScope.launch {
            // Start the BLE subscription — suspend, so launch it on its own
            launch { CalibrateManager.startStream() }

            // Collect the PI samples that addSample() pushes into sensor
            CalibrateRepository.sensor.collect { allSamples ->
                val now = System.currentTimeMillis()
                val incoming = allSamples.lastOrNull() ?: return@collect
                val sample = PiSample(incoming, now)
                val updated = (_waveform.value + sample)
                    .filter { now - it.timestampMs <= WINDOW_MS }
                _waveform.value = updated
            }
        }
    }

    fun stopStream() {
        collectJob?.cancel()
        collectJob = null
        viewModelScope.launch {
            CalibrateManager.stopStream()
        }
    }

    // -- Step transitions --------------------------------------------------

    fun beginRestRecord() {
        _step.value = CalibrationStep.REST_RECORD
        startRecording()
    }

    fun beginActiveInstruct() {
        _step.value = CalibrationStep.ACTIVE_INSTRUCT
    }

    fun beginActiveRecord() {
        _step.value = CalibrationStep.ACTIVE_RECORD
        startRecording()
    }

    fun beginFinalRestRecord() {
        _step.value = CalibrationStep.FINAL_REST_RECORD
        startRecording()
    }

    private fun startRecording() {
        _countdown.value = RECORD_DURATION_S
        recordingJob?.cancel()
        recordingJob = viewModelScope.launch {
            repeat(RECORD_DURATION_S) {
                delay(1_000)
                _countdown.value -= 1
            }
            // Auto-advance after countdown
            windowAvgPi = _waveform.value
                .filter { System.currentTimeMillis() - it.timestampMs <= WINDOW_MS }
                .map { it.value }
                .takeIf { it.isNotEmpty() }
                ?.average()
                ?.toFloat()
            when (_step.value) {
                CalibrationStep.REST_RECORD   -> _step.value = CalibrationStep.REST_INPUT
                CalibrationStep.ACTIVE_RECORD -> _step.value = CalibrationStep.ACTIVE_INPUT
                CalibrationStep.FINAL_REST_RECORD -> _step.value = CalibrationStep.FINAL_REST_INPUT
                else -> {}
            }
        }
    }

    fun submitBp(systolic: Int, diastolic: Int) {
        val pi = windowAvgPi ?: return
        _points.value = _points.value + CalibrationPoint(pi, systolic, diastolic)
        when (_step.value) {
            CalibrationStep.REST_INPUT        -> _step.value = CalibrationStep.ACTIVE_INSTRUCT
            CalibrationStep.ACTIVE_INPUT      -> _step.value = CalibrationStep.FINAL_REST_RECORD
                .also { beginFinalRestRecord() }
            CalibrationStep.FINAL_REST_INPUT  -> {
                computeRegression()
                _step.value = CalibrationStep.RESULTS
            }
            else -> {}
        }
    }

    fun reset() {
        recordingJob?.cancel()
        _step.value = CalibrationStep.IDLE
        _points.value = emptyList()
        _model.value = null
        _waveform.value = emptyList()
        _countdown.value = RECORD_DURATION_S
        windowAvgPi = null
    }

    private fun computeRegression() {
        val pts = _points.value
        if (pts.size < 2) return
        val n = pts.size.toFloat()
        val sumX  = pts.sumOf { it.pi.toDouble() }.toFloat()
        val sumXX = pts.sumOf { (it.pi * it.pi).toDouble() }.toFloat()
        val denom = n * sumXX - sumX * sumX

        val sumYsys  = pts.sumOf { it.systolic.toDouble() }.toFloat()
        val sumXYsys = pts.sumOf { (it.pi * it.systolic).toDouble() }.toFloat()
        val slopeSys = (n * sumXYsys - sumX * sumYsys) / denom
        val intSys   = (sumYsys - slopeSys * sumX) / n

        val sumYdia  = pts.sumOf { it.diastolic.toDouble() }.toFloat()
        val sumXYdia = pts.sumOf { (it.pi * it.diastolic).toDouble() }.toFloat()
        val slopeDia = (n * sumXYdia - sumX * sumYdia) / denom
        val intDia   = (sumYdia - slopeDia * sumX) / n

        _model.value = LinearModel(slopeSys, intSys, slopeDia, intDia)
    }

    override fun onCleared() {
        super.onCleared()
        stopStream()
    }
}

// ---------------------------------------------------------------------------
// Top-level screen
// ---------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalibrateScreen(
    onNavigateBack: () -> Unit,
    viewModel: CalibrationViewModel = viewModel(),
) {
    val step      by viewModel.step.collectAsState()
    val waveform  by viewModel.waveform.collectAsState()
    val countdown by viewModel.countdown.collectAsState()
    val points    by viewModel.points.collectAsState()
    val model     by viewModel.model.collectAsState()
    val currentPi by viewModel.currentPi.collectAsState()

    // Start streaming as soon as screen is visible
    LaunchedEffect(Unit) { viewModel.startStream() }
    DisposableEffect(Unit) { onDispose { viewModel.stopStream() } }

    val colorScheme = MaterialTheme.colorScheme

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "BP Calibration",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorScheme.surface,
                ),
            )
        },
        containerColor = colorScheme.background,
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            // ── Step indicator ──────────────────────────────────────────
            StepIndicator(step)

            // ── Bottom card — slides between Idle / Recording / Input ───
            AnimatedContent(
                targetState = step,
                transitionSpec = {
                    (slideInVertically { it } + fadeIn()) togetherWith
                            (slideOutVertically { -it } + fadeOut())
                },
                label = "step_content",
            ) { currentStep ->
                when (currentStep) {
                    CalibrationStep.IDLE -> IdleCard(
                        onStart = { viewModel.beginRestRecord() }
                    )
                    CalibrationStep.REST_RECORD -> RecordingCard(
                        label = "Resting — keep still",
                        countdown = countdown,
                    )
                    CalibrationStep.REST_INPUT -> BpInputCard(
                        label = "Enter resting blood pressure",
                        onSubmit = { sys, dia -> viewModel.submitBp(sys, dia) },
                    )
                    CalibrationStep.ACTIVE_INSTRUCT -> ActiveInstructCard(
                        onReady = { viewModel.beginActiveRecord() }
                    )
                    CalibrationStep.ACTIVE_RECORD -> RecordingCard(
                        label = "Elevated activity — keep moving",
                        countdown = countdown,
                    )
                    CalibrationStep.ACTIVE_INPUT -> BpInputCard(
                        label = "Enter elevated blood pressure",
                        onSubmit = { sys, dia -> viewModel.submitBp(sys, dia) },
                    )
                    CalibrationStep.FINAL_REST_RECORD -> RecordingCard(
                        label = "Return to rest — relax",
                        countdown = countdown,
                    )
                    CalibrationStep.FINAL_REST_INPUT -> BpInputCard(
                        label = "Enter final resting blood pressure",
                        onSubmit = { sys, dia -> viewModel.submitBp(sys, dia) },
                    )
                    CalibrationStep.RESULTS -> ResultsCard(
                        points = points,
                        model  = model,
                        onReset = { viewModel.reset() },
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Step indicator
// ---------------------------------------------------------------------------

private val STEPS_META = listOf(
    CalibrationStep.REST_RECORD to "Rest",
    CalibrationStep.ACTIVE_RECORD to "Active",
    CalibrationStep.FINAL_REST_RECORD to "Re-rest",
    CalibrationStep.RESULTS to "Results",
)

@Composable
private fun StepIndicator(current: CalibrationStep) {
    val activeIndex = when (current) {
        CalibrationStep.IDLE                             -> -1
        CalibrationStep.REST_RECORD, CalibrationStep.REST_INPUT -> 0
        CalibrationStep.ACTIVE_INSTRUCT, CalibrationStep.ACTIVE_RECORD, CalibrationStep.ACTIVE_INPUT -> 1
        CalibrationStep.FINAL_REST_RECORD, CalibrationStep.FINAL_REST_INPUT -> 2
        CalibrationStep.RESULTS                          -> 3
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        STEPS_META.forEachIndexed { index, (_, label) ->
            val done   = index < activeIndex
            val active = index == activeIndex
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                done   -> MaterialTheme.colorScheme.primary
                                active -> MaterialTheme.colorScheme.primaryContainer
                                else   -> MaterialTheme.colorScheme.surfaceVariant
                            }
                        ),
                ) {
                    if (done) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(16.dp),
                        )
                    } else {
                        Text(
                            "${index + 1}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (active)
                                MaterialTheme.colorScheme.onPrimaryContainer
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    label,
                    fontSize = 11.sp,
                    color = if (active || done)
                        MaterialTheme.colorScheme.onSurface
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
            if (index < STEPS_META.lastIndex) {
                Divider(
                    modifier = Modifier
                        .weight(1f)
                        .padding(bottom = 16.dp),
                    color = if (done) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.outlineVariant,
                    thickness = 1.5.dp,
                )
            }
        }
    }
}
// ---------------------------------------------------------------------------
// Idle card
// ---------------------------------------------------------------------------

@Composable
private fun IdleCard(onStart: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                Icons.Default.Favorite,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp),
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "Blood Pressure Calibration",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "This process will collect three recordings — resting, elevated, and resting again — " +
                        "to build a personal PI-to-BP model for this patient.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = onStart,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Start Calibration", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Recording card
// ---------------------------------------------------------------------------

@Composable
private fun RecordingCard(label: String, countdown: Int) {
    val progress = countdown / 30f
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(2.dp),
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(20.dp))
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.size(96.dp),
                    strokeWidth = 6.dp,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
                Text(
                    "$countdown",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                "Recording will complete automatically",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

// ---------------------------------------------------------------------------
// BP input card
// ---------------------------------------------------------------------------

@Composable
private fun BpInputCard(label: String, onSubmit: (Int, Int) -> Unit) {
    var sys by remember { mutableStateOf("") }
    var dia by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(2.dp),
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = sys,
                    onValueChange = { sys = it.filter { c -> c.isDigit() }.take(3) },
                    label = { Text("Systolic") },
                    suffix = { Text("mmHg") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                )
                OutlinedTextField(
                    value = dia,
                    onValueChange = { dia = it.filter { c -> c.isDigit() }.take(3) },
                    label = { Text("Diastolic") },
                    suffix = { Text("mmHg") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                )
            }
            error?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = {
                    val s = sys.toIntOrNull()
                    val d = dia.toIntOrNull()
                    when {
                        s == null || d == null -> error = "Please enter both values"
                        s !in 60..250          -> error = "Systolic must be 60–250 mmHg"
                        d !in 40..150          -> error = "Diastolic must be 40–150 mmHg"
                        d >= s                 -> error = "Diastolic must be less than systolic"
                        else                   -> { error = null; onSubmit(s, d) }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text("Confirm & Continue", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Active instruct card
// ---------------------------------------------------------------------------

@Composable
private fun ActiveInstructCard(onReady: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
        ),
    ) {
        Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.DirectionsRun,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(40.dp),
            )
            Spacer(Modifier.height(12.dp))
            Text(
                "Elevate Blood Pressure",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Ask the patient to perform light physical activity (e.g. stepping in place, " +
                        "walking briskly) for 2–3 minutes to elevate their blood pressure, then tap Ready.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = onReady,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                ),
            ) {
                Text("Patient is Active — Start Recording", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Results card: scatter + regression plot + model readout
// ---------------------------------------------------------------------------

@Composable
private fun ResultsCard(
    points: List<CalibrationPoint>,
    model: LinearModel?,
    onReset: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Scatter + regression chart
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(2.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    "PI vs Blood Pressure",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Scatter points + linear regression",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(16.dp))
                RegressionChart(
                    points = points,
                    model = model,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp),
                )
                Spacer(Modifier.height(8.dp))
                // Legend
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    LegendDot(MaterialTheme.colorScheme.primary, "Systolic")
                    LegendDot(MaterialTheme.colorScheme.tertiary, "Diastolic")
                }
            }
        }

        // Model readout
        model?.let { m ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(2.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                ),
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        "Calibration Model",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.height(12.dp))
                    ModelRow(
                        label = "Systolic",
                        color = MaterialTheme.colorScheme.primary,
                        slope = m.slopeSystolic,
                        intercept = m.interceptSystolic,
                    )
                    Spacer(Modifier.height(8.dp))
                    ModelRow(
                        label = "Diastolic",
                        color = MaterialTheme.colorScheme.tertiary,
                        slope = m.slopeDiastolic,
                        intercept = m.interceptDiastolic,
                    )
                    Spacer(Modifier.height(16.dp))
                    // Calibration data table
                    Text(
                        "Calibration Points",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(Modifier.height(8.dp))
                    points.forEachIndexed { i, pt ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                "Point ${i + 1}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                            Text(
                                "PI ${"%.3f".format(pt.pi)}",
                                style = MaterialTheme.typography.bodySmall,
                            )
                            Text(
                                "${pt.systolic}/${pt.diastolic} mmHg",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                        if (i < points.lastIndex) Divider(modifier = Modifier.padding(vertical = 4.dp))
                    }
                }
            }
        }

        OutlinedButton(
            onClick = onReset,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Recalibrate")
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color),
        )
        Spacer(Modifier.width(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun ModelRow(label: String, color: Color, slope: Float, intercept: Float) {
    val sign = if (intercept >= 0f) "+" else "−"
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, fontWeight = FontWeight.SemiBold, color = color)
        Text(
            "BP = ${"%.2f".format(slope)} × PI $sign ${"%.1f".format(abs(intercept))} mmHg",
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

// ---------------------------------------------------------------------------
// Regression chart (Canvas)
// ---------------------------------------------------------------------------

@Composable
private fun RegressionChart(
    points: List<CalibrationPoint>,
    model: LinearModel?,
    modifier: Modifier = Modifier,
) {
    val primary  = MaterialTheme.colorScheme.primary
    val tertiary = MaterialTheme.colorScheme.tertiary
    val outline  = MaterialTheme.colorScheme.outlineVariant
    val onSurface = MaterialTheme.colorScheme.onSurface

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .drawWithCache {
                onDrawBehind {
                    if (points.isEmpty()) return@onDrawBehind

                    val padL = 48.dp.toPx()
                    val padR = 16.dp.toPx()
                    val padT = 16.dp.toPx()
                    val padB = 32.dp.toPx()
                    val chartW = size.width - padL - padR
                    val chartH = size.height - padT - padB

                    // Axis ranges
                    val piMin   = (points.minOf { it.pi } * 0.8f).coerceAtMost(points.minOf { it.pi } - 0.05f)
                    val piMax   = (points.maxOf { it.pi } * 1.2f).coerceAtLeast(points.maxOf { it.pi } + 0.05f)
                    val bpMin   = (points.minOf { it.diastolic } - 10f).coerceAtLeast(40f)
                    val bpMax   = (points.maxOf { it.systolic } + 10f).coerceAtMost(250f)

                    fun piToX(pi: Float)  = padL + (pi - piMin)  / (piMax - piMin)  * chartW
                    fun bpToY(bp: Float)  = padT + chartH - (bp - bpMin) / (bpMax - bpMin) * chartH

                    // Grid lines (4)
                    val gridStroke = Stroke(width = 0.8.dp.toPx())
                    repeat(5) { i ->
                        val y = padT + i * chartH / 4
                        drawLine(outline, Offset(padL, y), Offset(padL + chartW, y), strokeWidth = 0.8.dp.toPx())
                        val label = "%.0f".format(bpMax - i * (bpMax - bpMin) / 4)
                        drawContext.canvas.nativeCanvas.drawText(
                            label,
                            padL - 6.dp.toPx(),
                            y + 4.dp.toPx(),
                            android.graphics.Paint().apply {
                                textSize = 9.dp.toPx()
                                textAlign = android.graphics.Paint.Align.RIGHT
                                color = onSurface.copy(alpha = 0.6f).toArgb()
                            },
                        )
                    }

                    // Regression lines
                    model?.let { m ->
                        val sysPts = listOf(
                            Offset(piToX(piMin), bpToY(m.slopeSystolic * piMin + m.interceptSystolic)),
                            Offset(piToX(piMax), bpToY(m.slopeSystolic * piMax + m.interceptSystolic)),
                        )
                        val diaPts = listOf(
                            Offset(piToX(piMin), bpToY(m.slopeDiastolic * piMin + m.interceptDiastolic)),
                            Offset(piToX(piMax), bpToY(m.slopeDiastolic * piMax + m.interceptDiastolic)),
                        )
                        drawLine(primary,  sysPts[0], sysPts[1], strokeWidth = 2.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 6f)))
                        drawLine(tertiary, diaPts[0], diaPts[1], strokeWidth = 2.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 6f)))
                    }

                    // Scatter points
                    points.forEach { pt ->
                        // Systolic dot
                        drawCircle(
                            color = primary,
                            radius = 6.dp.toPx(),
                            center = Offset(piToX(pt.pi), bpToY(pt.systolic.toFloat())),
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 3.dp.toPx(),
                            center = Offset(piToX(pt.pi), bpToY(pt.systolic.toFloat())),
                        )
                        // Diastolic dot
                        drawCircle(
                            color = tertiary,
                            radius = 6.dp.toPx(),
                            center = Offset(piToX(pt.pi), bpToY(pt.diastolic.toFloat())),
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 3.dp.toPx(),
                            center = Offset(piToX(pt.pi), bpToY(pt.diastolic.toFloat())),
                        )
                        // Vertical connector
                        drawLine(
                            color = outline,
                            start = Offset(piToX(pt.pi), bpToY(pt.systolic.toFloat())),
                            end   = Offset(piToX(pt.pi), bpToY(pt.diastolic.toFloat())),
                            strokeWidth = 1.dp.toPx(),
                        )
                    }

                    // X axis labels
                    points.forEach { pt ->
                        drawContext.canvas.nativeCanvas.drawText(
                            "%.3f".format(pt.pi),
                            piToX(pt.pi),
                            padT + chartH + 20.dp.toPx(),
                            android.graphics.Paint().apply {
                                textSize = 9.dp.toPx()
                                textAlign = android.graphics.Paint.Align.CENTER
                                color = onSurface.copy(alpha = 0.6f).toArgb()
                            },
                        )
                    }

                    // Axes
                    drawLine(outline, Offset(padL, padT), Offset(padL, padT + chartH), strokeWidth = 1.5.dp.toPx())
                    drawLine(outline, Offset(padL, padT + chartH), Offset(padL + chartW, padT + chartH), strokeWidth = 1.5.dp.toPx())
                }
            },
    )
}

// ---------------------------------------------------------------------------
// Preview helpers
// ---------------------------------------------------------------------------

/** Generates a fake sine-ish waveform for previews */
private fun fakeSamples(count: Int = 120, basePi: Float = 0.45f): List<PiSample> {
    val now = System.currentTimeMillis()
    val windowMs = 30_000L
    return List(count) { i ->
        val t = i.toFloat() / count
        val noise = (Math.random() * 0.04 - 0.02).toFloat()
        val value = basePi + 0.06f * kotlin.math.sin(t * 12 * Math.PI.toFloat()) + noise
        PiSample(value, now - windowMs + (t * windowMs).toLong())
    }
}

private val fakePoints = listOf(
    CalibrationPoint(pi = 0.38f, systolic = 112, diastolic = 72),
    CalibrationPoint(pi = 0.52f, systolic = 134, diastolic = 86),
    CalibrationPoint(pi = 0.41f, systolic = 118, diastolic = 76),
)

private val fakeModel = LinearModel(
    slopeSystolic    = 168f,
    interceptSystolic = 48f,
    slopeDiastolic   = 104f,
    interceptDiastolic = 33f,
)

@Preview(name = "1 – Idle / Start", showBackground = true, widthDp = 390, heightDp = 780)
@Composable
private fun PreviewIdle() {
    MaterialTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                StepIndicator(CalibrationStep.IDLE)
                IdleCard(onStart = {})
            }
        }
    }
}

@Preview(name = "2 – Rest Recording", showBackground = true, widthDp = 390, heightDp = 780)
@Composable
private fun PreviewRestRecord() {
    MaterialTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                StepIndicator(CalibrationStep.REST_RECORD)
                RecordingCard(label = "Resting — keep still", countdown = 22)
            }
        }
    }
}

@Preview(name = "3 – Rest BP Input", showBackground = true, widthDp = 390, heightDp = 780)
@Composable
private fun PreviewRestInput() {
    MaterialTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                StepIndicator(CalibrationStep.REST_INPUT)
                BpInputCard(label = "Enter resting blood pressure", onSubmit = { _, _ -> })
            }
        }
    }
}

@Preview(name = "4 – Active Instructions", showBackground = true, widthDp = 390, heightDp = 780)
@Composable
private fun PreviewActiveInstruct() {
    MaterialTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                StepIndicator(CalibrationStep.ACTIVE_INSTRUCT)
                ActiveInstructCard(onReady = {})
            }
        }
    }
}

@Preview(name = "5 – Active Recording", showBackground = true, widthDp = 390, heightDp = 780)
@Composable
private fun PreviewActiveRecord() {
    MaterialTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                StepIndicator(CalibrationStep.ACTIVE_RECORD)
                RecordingCard(label = "Elevated activity — keep moving", countdown = 14)
            }
        }
    }
}

@Preview(name = "6 – Active BP Input", showBackground = true, widthDp = 390, heightDp = 780)
@Composable
private fun PreviewActiveInput() {
    MaterialTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                StepIndicator(CalibrationStep.ACTIVE_INPUT)
                BpInputCard(label = "Enter elevated blood pressure", onSubmit = { _, _ -> })
            }
        }
    }
}

@Preview(name = "7 – Final Rest Recording", showBackground = true, widthDp = 390, heightDp = 780)
@Composable
private fun PreviewFinalRestRecord() {
    MaterialTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                StepIndicator(CalibrationStep.FINAL_REST_RECORD)
                RecordingCard(label = "Return to rest — relax", countdown = 8)
            }
        }
    }
}

@Preview(name = "8 – Final Rest BP Input", showBackground = true, widthDp = 390, heightDp = 780)
@Composable
private fun PreviewFinalRestInput() {
    MaterialTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                StepIndicator(CalibrationStep.FINAL_REST_INPUT)
                BpInputCard(label = "Enter final resting blood pressure", onSubmit = { _, _ -> })
            }
        }
    }
}

@Preview(name = "9 – Results", showBackground = true, widthDp = 390, heightDp = 900)
@Composable
private fun PreviewResults() {
    MaterialTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                StepIndicator(CalibrationStep.RESULTS)
                ResultsCard(
                    points  = fakePoints,
                    model   = fakeModel,
                    onReset = {},
                )
            }
        }
    }
}

@Preview(name = "Dark – Active Recording", showBackground = true, widthDp = 390, heightDp = 780,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PreviewDarkActiveRecord() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                StepIndicator(CalibrationStep.ACTIVE_RECORD)
                RecordingCard(label = "Elevated activity — keep moving", countdown = 14)
            }
        }
    }
}

@Preview(name = "Dark – Results", showBackground = true, widthDp = 390, heightDp = 900,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PreviewDarkResults() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                StepIndicator(CalibrationStep.RESULTS)
                ResultsCard(
                    points  = fakePoints,
                    model   = fakeModel,
                    onReset = {},
                )
            }
        }
    }
}