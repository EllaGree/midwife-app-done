package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
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
fun AddEditPatientScreen(
    viewModel: PatientViewModel,
    patientId: Int? = null,
    modifier: Modifier = Modifier
) {
    val selectedPatient by viewModel.selectedPatient.collectAsState()

    // Form states
    var name by remember { mutableStateOf("") }
    var ageStr by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var bloodType by remember { mutableStateOf("O+") }
    var gravida by remember { mutableStateOf(1) }
    var para by remember { mutableStateOf(0) }
    var emergencyName by remember { mutableStateOf("") }
    var emergencyPhone by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("Active") }

    // LMP Date inputs
    val todayCal = Calendar.getInstance()
    var lmpDay by remember { mutableStateOf(todayCal.get(Calendar.DAY_OF_MONTH)) }
    var lmpMonth by remember { mutableStateOf(todayCal.get(Calendar.MONTH)) } // 0-indexed
    var lmpYear by remember { mutableStateOf(todayCal.get(Calendar.YEAR)) }

    // Load existing patient details if editing
    LaunchedEffect(selectedPatient) {
        if (patientId != null && selectedPatient != null) {
            val p = selectedPatient!!
            name = p.name
            ageStr = p.age.toString()
            phone = p.phone
            email = p.email
            bloodType = p.bloodType
            gravida = p.gravida
            para = p.para
            emergencyName = p.emergencyContactName
            emergencyPhone = p.emergencyContactPhone
            notes = p.notes
            status = p.status

            val lmpCal = Calendar.getInstance().apply { timeInMillis = p.lmpDate }
            lmpDay = lmpCal.get(Calendar.DAY_OF_MONTH)
            lmpMonth = lmpCal.get(Calendar.MONTH)
            lmpYear = lmpCal.get(Calendar.YEAR)
        }
    }

    var showErrorMsg by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (patientId == null) "New Patient Intake" else "Edit Patient Profile",
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
            // General Info Card
            SectionLabel(text = "Maternal Profile")

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Maternal Full Name") },
                placeholder = { Text("Jane Doe") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_name")
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = ageStr,
                    onValueChange = { ageStr = it },
                    label = { Text("Maternal Age (years)") },
                    placeholder = { Text("28") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("input_age")
                )

                // Blood Type selector (Simple chips)
                Column(
                    modifier = Modifier.weight(1.2f)
                ) {
                    Text(
                        text = "Blood Group Type",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    var showBloodMenu by remember { mutableStateOf(false) }
                    Box {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .clickable { showBloodMenu = true }
                                .padding(horizontal = 16.dp, vertical = 14.dp)
                        ) {
                            Text(
                                text = bloodType,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 16.sp
                            )
                        }

                        DropdownMenu(
                            expanded = showBloodMenu,
                            onDismissRequest = { showBloodMenu = false }
                        ) {
                            val types = listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")
                            types.forEach { type ->
                                DropdownMenuItem(
                                    text = { Text(type, fontWeight = FontWeight.Bold) },
                                    onClick = {
                                        bloodType = type
                                        showBloodMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            SectionLabel(text = "Primary Contact Details")

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Phone Number") },
                placeholder = { Text("+1 (123) 456-7890") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_phone")
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email Address") },
                placeholder = { Text("jane.doe@example.com") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_email")
            )

            // Obstetric History (G/P calculation helpers)
            SectionLabel(text = "Obstetric History (G / P)")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Gravida G (number of times pregnant)
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Gravida (G)",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        IconButton(
                            onClick = { if (gravida > 1) gravida-- },
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Text("-", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        }
                        Text(
                            text = gravida.toString(),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.ExtraBold
                        )
                        IconButton(
                            onClick = { gravida++ },
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Text("+", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        }
                    }
                }

                // Para P (number of births)
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Para (P)",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        IconButton(
                            onClick = { if (para > 0) para-- },
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Text("-", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        }
                        Text(
                            text = para.toString(),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.ExtraBold
                        )
                        IconButton(
                            onClick = { para++ },
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Text("+", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        }
                    }
                }
            }

            // LMP Date Input
            SectionLabel(text = "LMP (Last Menstrual Period)")
            Text(
                text = "Used for auto-calculating Gestational Age and Estimated Due Date (EDD).",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = lmpDay.toString(),
                    onValueChange = {
                        it.toIntOrNull()?.let { d -> if (d in 1..31) lmpDay = d }
                    },
                    label = { Text("Day") },
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = (lmpMonth + 1).toString(),
                    onValueChange = {
                        it.toIntOrNull()?.let { m -> if (m in 1..12) lmpMonth = m - 1 }
                    },
                    label = { Text("Month") },
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = lmpYear.toString(),
                    onValueChange = {
                        it.toIntOrNull()?.let { y -> if (y in 2020..2040) lmpYear = y }
                    },
                    label = { Text("Year") },
                    modifier = Modifier.weight(1.5f)
                )
            }

            // Emergency Contacts
            SectionLabel(text = "Emergency Clinical contact")

            OutlinedTextField(
                value = emergencyName,
                onValueChange = { emergencyName = it },
                label = { Text("Emergency Contact Name") },
                placeholder = { Text("John Doe (Partner)") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_emergency_name")
            )

            OutlinedTextField(
                value = emergencyPhone,
                onValueChange = { emergencyPhone = it },
                label = { Text("Emergency Phone Number") },
                placeholder = { Text("+1 (123) 456-9999") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_emergency_phone")
            )

            // Medical History & Care Notes
            SectionLabel(text = "Clinical Intake remarks")

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Allergies, high-risk flags & medical remarks...") },
                minLines = 3,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_notes")
            )

            // Care Stage Selection (Active / Postpartum / Delivered)
            SectionLabel(text = "Active care status")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val statuses = listOf("Active", "Postpartum", "Delivered")
                statuses.forEach { s ->
                    val isSelected = status == s
                    ElevatedAssistChip(
                        onClick = { status = s },
                        label = { Text(s) },
                        leadingIcon = {
                            if (isSelected) {
                                Icon(Icons.Default.Check, contentDescription = "Active selected")
                            }
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chip_status_$s")
                    )
                }
            }

            // Error Message displaying
            AnimatedVisibility(visible = showErrorMsg != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.errorContainer)
                        .padding(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Error notification",
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = showErrorMsg ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Master Save button
            Button(
                onClick = {
                    if (name.isBlank()) {
                        showErrorMsg = "Patient name is required!"
                        return@Button
                    }
                    val finalAge = ageStr.toIntOrNull()
                    if (finalAge == null || finalAge <= 0) {
                        showErrorMsg = "Please enter a valid age!"
                        return@Button
                    }

                    // package lmpDate
                    val lmpCal = Calendar.getInstance().apply {
                        set(Calendar.YEAR, lmpYear)
                        set(Calendar.MONTH, lmpMonth)
                        set(Calendar.DAY_OF_MONTH, lmpDay)
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                    }

                    viewModel.savePatient(
                        id = patientId ?: 0,
                        name = name,
                        age = finalAge,
                        phone = phone,
                        email = email,
                        bloodType = bloodType,
                        gravida = gravida,
                        para = para,
                        lmpDate = lmpCal.timeInMillis,
                        emergencyContactName = emergencyName,
                        emergencyContactPhone = emergencyPhone,
                        notes = notes,
                        status = status,
                        onSuccess = { generatedId ->
                            // Navigate to details if successfully generated, or back to details if edited
                            viewModel.navigateTo(Screen.PatientDetails(generatedId))
                        }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("save_patient_button"),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    text = if (patientId == null) "Complete Intake Record" else "Save Intake Changes",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
fun SectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        letterSpacing = 1.2.sp,
        modifier = Modifier.padding(top = 8.dp)
    )
}
