package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Patient
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddVisitLogScreen(
    viewModel: PatientViewModel,
    patientId: Int,
    modifier: Modifier = Modifier
) {
    val patient by viewModel.selectedPatient.collectAsState()

    // Form states
    var bpSystolicStr by remember { mutableStateOf("120") }
    var bpDiastolicStr by remember { mutableStateOf("80") }
    var weightStr by remember { mutableStateOf("") }
    var fhrStr by remember { mutableStateOf("140") }
    var sfhStr by remember { mutableStateOf("") }
    
    var urineProtein by remember { mutableStateOf("Negative") }
    var urineGlucose by remember { mutableStateOf("Negative") }
    var presentation by remember { mutableStateOf("Cephalic (Normal)") }
    var notes by remember { mutableStateOf("") }

    // Gestational age during this visit (calculated automatically from LMP, but modifiable)
    var gestWeeksStr by remember { mutableStateOf("") }
    var gestDaysStr by remember { mutableStateOf("") }

    // Date of visit inputs (default is today)
    val todayCal = Calendar.getInstance()
    var vDay by remember { mutableStateOf(todayCal.get(Calendar.DAY_OF_MONTH)) }
    var vMonth by remember { mutableStateOf(todayCal.get(Calendar.MONTH)) }
    var vYear by remember { mutableStateOf(todayCal.get(Calendar.YEAR)) }

    // Prefill Gestational Age when patient loads
    LaunchedEffect(patient) {
        patient?.let { p ->
            val (weeks, days) = PregnancyUtils.calculateGestationalAge(p.lmpDate)
            gestWeeksStr = weeks.toString()
            gestDaysStr = days.toString()
        }
    }

    // Diagnostics & warnings
    val systolicVal = bpSystolicStr.toIntOrNull() ?: 0
    val diastolicVal = bpDiastolicStr.toIntOrNull() ?: 0
    val fhrVal = fhrStr.toIntOrNull() ?: 0

    val hpAlert = systolicVal >= 140 || diastolicVal >= 90
    val fhrAlert = fhrVal != 0 && (fhrVal < 110 || fhrVal > 160)

    var showErrorMsg by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Log Clinical Visit",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateBack() },
                        modifier = Modifier.testTag("back_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Go Back",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            patient?.let { _ ->
                Text(
                    text = "VISIT CLINICAL PARAMETERS",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.2.sp
                )

                // Visit Date Selector Inputs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = vDay.toString(),
                        onValueChange = {
                            it.toIntOrNull()?.let { d -> if (d in 1..31) vDay = d }
                        },
                        label = { Text("Visit Day") },
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = (vMonth + 1).toString(),
                        onValueChange = {
                            it.toIntOrNull()?.let { m -> if (m in 1..12) vMonth = m - 1 }
                        },
                        label = { Text("Visit Month") },
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = vYear.toString(),
                        onValueChange = {
                            it.toIntOrNull()?.let { y -> if (y in 2020..2040) vYear = y }
                        },
                        label = { Text("Visit Year") },
                        modifier = Modifier.weight(1.5f)
                    )
                }

                Divider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.08f))

                // Calculated Gestational Age at visit
                Text(
                    text = "GESTATIONAL AGE AT VISIT",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = gestWeeksStr,
                        onValueChange = { gestWeeksStr = it },
                        label = { Text("Weeks") },
                        placeholder = { Text("32") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = gestDaysStr,
                        onValueChange = { gestDaysStr = it },
                        label = { Text("Days (0-6)") },
                        placeholder = { Text("3") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Divider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.08f))

                // Vitals Section (Maternal weight, blood pressure)
                Text(
                    text = "MATERNAL VITALS",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )

                // Weight and fundal height row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = weightStr,
                        onValueChange = { weightStr = it },
                        label = { Text("Weight (lbs)") },
                        placeholder = { Text("154.2") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = sfhStr,
                        onValueChange = { sfhStr = it },
                        label = { Text("Fundal Height. (cm)") },
                        placeholder = { Text("32") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // Blood Pressure row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = bpSystolicStr,
                        onValueChange = { bpSystolicStr = it },
                        label = { Text("BP Systolic (mmHg)") },
                        placeholder = { Text("120") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("systolic_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = bpDiastolicStr,
                        onValueChange = { bpDiastolicStr = it },
                        label = { Text("BP Diastolic (mmHg)") },
                        placeholder = { Text("80") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("diastolic_input"),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // Hypertension warning banner
                AnimatedVisibility(visible = hpAlert) {
                    WarningBanner(text = "⚠️ Elevated BP (≥140/90) matches Hypertensive Criteria. Screen for preeclampsia risk parameters (sudden vision blurring, intense headaches, severe swelling/edema).")
                }

                Divider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.08f))

                // Baby Section (FHR logs, presentation)
                Text(
                    text = "FETAL DIAGNOSTICS",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )

                OutlinedTextField(
                    value = fhrStr,
                    onValueChange = { fhrStr = it },
                    label = { Text("Fetal Heart Rate (bpm)") },
                    placeholder = { Text("140") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("fhr_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                AnimatedVisibility(visible = fhrAlert) {
                    WarningBanner(text = "⚠️ FHR outside normal limits (110 - 160 bpm). Inspect maternal positioning, fetal stimulation or monitor for fetal distress factors.")
                }

                // Fetal presentation horizontal list selector
                Text(
                    text = "Fetal Presentation",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                CustomHorizontalSelector(
                    options = listOf("Cephalic (Normal)", "Breech (Feet first)", "Transverse (Sideways)", "Posterior"),
                    selected = presentation,
                    onSelected = { presentation = it }
                )

                Divider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.08f))

                // Urinalysis section
                Text(
                    text = "MATERNAL URINALYSIS",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )

                Text(
                    text = "Urine Protein Track",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                CustomHorizontalSelector(
                    options = listOf("Negative", "Trace", "1+", "2+", "3+", "4+"),
                    selected = urineProtein,
                    onSelected = { urineProtein = it }
                )

                Text(
                    text = "Urine Glucose Track",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                CustomHorizontalSelector(
                    options = listOf("Negative", "Trace", "1+", "2+", "3+", "4+"),
                    selected = urineGlucose,
                    onSelected = { urineGlucose = it }
                )

                Divider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.08f))

                // Notes / Clinical Remarks
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Midwife Clinical Remarks & Management Plan...") },
                    minLines = 3,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("visit_notes_input")
                )

                // Error message banner
                AnimatedVisibility(visible = showErrorMsg != null) {
                    WarningBanner(text = showErrorMsg ?: "", isError = true)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Complete Visit log submission
                Button(
                    onClick = {
                        val weeks = gestWeeksStr.toIntOrNull()
                        val days = gestDaysStr.toIntOrNull()
                        val systolic = bpSystolicStr.toIntOrNull()
                        val diastolic = bpDiastolicStr.toIntOrNull()

                        if (weeks == null || days == null) {
                            showErrorMsg = "Please input valid gestational Weeks and Days!"
                            return@Button
                        }
                        if (systolic == null || diastolic == null) {
                            showErrorMsg = "Blood Pressure systolic & diastolic are required numbers!"
                            return@Button
                        }

                        // package visit date
                        val visitCal = Calendar.getInstance().apply {
                            set(Calendar.YEAR, vYear)
                            set(Calendar.MONTH, vMonth)
                            set(Calendar.DAY_OF_MONTH, vDay)
                        }

                        viewModel.saveVisitLog(
                            patientId = patientId,
                            visitDate = visitCal.timeInMillis,
                            gestationalWeeks = weeks,
                            gestationalDays = days,
                            systolicBp = systolic,
                            diastolicBp = diastolic,
                            weightLb = weightStr.toDoubleOrNull() ?: 0.0,
                            fetalHeartRate = fhrVal,
                            symphysisFundalHeight = sfhStr.toIntOrNull() ?: 0,
                            urineProtein = urineProtein,
                            urineGlucose = urineGlucose,
                            presentation = presentation,
                            notes = notes,
                            onSuccess = {
                                viewModel.navigateTo(Screen.PatientDetails(patientId))
                            }
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("save_visit_button"),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary
                    )
                ) {
                    Text("Secure Clinical Visit Log", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            } ?: run {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Loading patient profile details...", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
fun WarningBanner(
    text: String,
    isError: Boolean = false
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isError) MaterialTheme.colorScheme.errorContainer
                else MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f)
            )
            .padding(12.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = "Clinically Alert Warning",
            tint = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = if (isError) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onTertiaryContainer,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun CustomHorizontalSelector(
    options: List<String>,
    selected: String,
    onSelected: (String) -> Unit
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(options) { o ->
            val isSelected = selected == o
            ElevatedFilterChip(
                selected = isSelected,
                onClick = { onSelected(o) },
                label = { Text(o) },
                colors = FilterChipDefaults.elevatedFilterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    }
}
