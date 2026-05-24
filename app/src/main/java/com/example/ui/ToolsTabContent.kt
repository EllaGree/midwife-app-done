package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ClinicalNote
import com.example.data.Medication
import com.example.data.MidwifeTask
import com.example.data.Patient
import java.util.Calendar

// Categories for clinical notes
val clinicalNoteCategories = listOf("Routine", "BP", "Glucose", "Scan", "Labour", "Concern", "Referral")

@Composable
fun getCategoryColor(category: String): Color {
    return when (category.uppercase()) {
        "BP" -> Color(0xFF673AB7) // Purple
        "GLUCOSE" -> Color(0xFF03A9F4) // Light Blue
        "SCAN" -> Color(0xFFE040FB) // Magenta
        "ROUTINE" -> Color(0xFF4CAF50) // Green
        "LABOUR" -> Color(0xFFFF5722) // Orange
        "CONCERN" -> Color(0xFFFFC107) // Amber/Yellow
        "REFERRAL" -> Color(0xFFE91E63) // Deep Pink
        else -> MaterialTheme.colorScheme.secondary
    }
}

@Composable
fun getCategoryLabelColor(category: String): Color {
    return when (category.uppercase()) {
        "CONCERN" -> Color.Black
        else -> Color.White
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolsTabSection(
    viewModel: PatientViewModel,
    selectedSubTab: Int,
    onSubTabChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val patients by viewModel.patients.collectAsState()
    val notes by viewModel.clinicalNotes.collectAsState()
    val tasks by viewModel.midwifeTasks.collectAsState()
    val medications by viewModel.medications.collectAsState()

    val tabTitles = listOf("📋 Notes", "✅ Tasks", "💊 Meds", "📊 Stats")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Sub-Tab Row
        PrimaryTabRow(
            selectedTabIndex = selectedSubTab,
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
            contentColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.fillMaxWidth()
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedSubTab == index,
                    onClick = { onSubTabChange(index) },
                    modifier = Modifier.testTag("tools_subtag_$index")
                ) {
                    Text(
                        text = title,
                        modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (selectedSubTab == index) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            when (selectedSubTab) {
                0 -> ClinicalNotesSubScreen(
                    patients = patients,
                    notes = notes,
                    onAddNote = { pid, cat, txt -> viewModel.insertClinicalNote(pid, cat, txt) },
                    onDeleteNote = { viewModel.deleteClinicalNote(it) }
                )
                1 -> TaskListSubScreen(
                    tasks = tasks,
                    onAddTask = { desc, pri, due -> viewModel.insertTask(desc, pri, due) },
                    onToggleTask = { viewModel.toggleTaskCompletion(it) },
                    onDeleteTask = { viewModel.deleteTask(it) }
                )
                2 -> MedicationsSubScreen(
                    patients = patients,
                    medications = medications,
                    onAddMed = { pid, name, dose, date, notes -> viewModel.insertMedication(pid, name, dose, date, notes) },
                    onDeleteMed = { viewModel.deleteMedication(it) }
                )
                3 -> StatsSubScreen(
                    patients = patients,
                    notes = notes,
                    medications = medications,
                    tasks = tasks
                )
            }
        }
    }
}

// ==========================================
// CLINICAL NOTES MINI-SCREEN
// ==========================================
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ClinicalNotesSubScreen(
    patients: List<Patient>,
    notes: List<ClinicalNote>,
    onAddNote: (Int, String, String) -> Unit,
    onDeleteNote: (ClinicalNote) -> Unit
) {
    var expandedDropdown by remember { mutableStateOf(false) }
    var selectedPatient by remember { mutableStateOf<Patient?>(null) }
    var selectedCategory by remember { mutableStateOf("Routine") }
    var noteText by remember { mutableStateOf("") }
    var showAddForm by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Patient Clinical Notes",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            IconButton(
                onClick = { showAddForm = !showAddForm },
                modifier = Modifier.testTag("notes_toggle_form_btn")
            ) {
                Icon(
                    imageVector = if (showAddForm) Icons.Default.Close else Icons.Default.AddCircle,
                    contentDescription = "Toggle Add Note Form",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        AnimatedVisibility(visible = showAddForm) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Log Clinical Observation",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Select Patient Dropdown simulator
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedCard(
                            onClick = { expandedDropdown = !expandedDropdown },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("note_select_patient_card")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = selectedPatient?.name ?: "Select Patient *",
                                    color = if (selectedPatient != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown")
                            }
                        }

                        DropdownMenu(
                            expanded = expandedDropdown,
                            onDismissRequest = { expandedDropdown = false },
                            modifier = Modifier.fillMaxWidth(0.9f)
                        ) {
                            if (patients.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("No patients registered yet") },
                                    onClick = { expandedDropdown = false }
                                )
                            } else {
                                patients.forEach { patient ->
                                    DropdownMenuItem(
                                        text = { Text(patient.name) },
                                        onClick = {
                                            selectedPatient = patient
                                            expandedDropdown = false
                                        },
                                        modifier = Modifier.testTag("dropdown_item_${patient.id}")
                                    )
                                }
                            }
                        }
                    }

                    // Category Chooser FlowRow
                    Text(text = "Category:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        clinicalNoteCategories.forEach { category ->
                            val isSelected = selectedCategory == category
                            val color = getCategoryColor(category)
                            InputChip(
                                selected = isSelected,
                                onClick = { selectedCategory = category },
                                label = { Text(category) },
                                colors = InputChipDefaults.inputChipColors(
                                    selectedContainerColor = color,
                                    selectedLabelColor = getCategoryLabelColor(category)
                                ),
                                modifier = Modifier.testTag("filter_note_chip_$category")
                            )
                        }
                    }

                    // Observation Details
                    OutlinedTextField(
                        value = noteText,
                        onValueChange = { noteText = it },
                        label = { Text("Clinical Note / Observation Text *") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(84.dp)
                            .testTag("note_text_input"),
                        maxLines = 3,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        )
                    )

                    Button(
                        onClick = {
                            val patient = selectedPatient
                            if (patient != null && noteText.isNotBlank()) {
                                onAddNote(patient.id, selectedCategory, noteText.trim())
                                noteText = ""
                                selectedPatient = null
                                showAddForm = false
                            }
                        },
                        enabled = selectedPatient != null && noteText.isNotBlank(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("save_note_btn")
                    ) {
                        Text("Add Observation Note")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Recents Timeline
        if (notes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Empty Notes",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(60.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "No clinical notes logged yet",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "Click the '+' pill to log the first clinician notes.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(notes, key = { it.id }) { note ->
                    val matchingPatient = patients.find { it.id == note.patientId }
                    val categoryColor = getCategoryColor(note.category)
                    val categoryLabelColor = getCategoryLabelColor(note.category)

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = matchingPatient?.name ?: "Unknown Patient (Removed)",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = PregnancyUtils.formatDate(note.date),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(categoryColor)
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = note.category,
                                            color = categoryLabelColor,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(6.dp))

                                    IconButton(
                                        onClick = { onDeleteNote(note) },
                                        modifier = Modifier
                                            .size(28.dp)
                                            .testTag("delete_note_${note.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete Note",
                                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = note.noteText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// MIDWIFE TASKS TO-DO SCREEN
// ==========================================
@Composable
fun TaskListSubScreen(
    tasks: List<MidwifeTask>,
    onAddTask: (String, String, Long) -> Unit,
    onToggleTask: (MidwifeTask) -> Unit,
    onDeleteTask: (MidwifeTask) -> Unit
) {
    var taskText by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("Medium") }
    var year by remember { mutableStateOf(Calendar.getInstance().get(Calendar.YEAR)) }
    var month by remember { mutableStateOf(Calendar.getInstance().get(Calendar.MONTH)) }
    var day by remember { mutableStateOf(Calendar.getInstance().get(Calendar.DAY_OF_MONTH)) }
    var showAddForm by remember { mutableStateOf(false) }

    val pendingTasks = tasks.filter { !it.isCompleted }
    val completedTasks = tasks.filter { it.isCompleted }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Midwife Private Tasks",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            IconButton(
                onClick = { showAddForm = !showAddForm },
                modifier = Modifier.testTag("tasks_toggle_form_btn")
            ) {
                Icon(
                    imageVector = if (showAddForm) Icons.Default.Close else Icons.Default.AddCircle,
                    contentDescription = "Toggle Add Task Form",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        AnimatedVisibility(visible = showAddForm) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Add To-Do Item",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    OutlinedTextField(
                        value = taskText,
                        onValueChange = { taskText = it },
                        label = { Text("Task description *") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("task_description_input"),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        )
                    )

                    // Priority Selector
                    Text("Priority:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Low", "Medium", "High").forEach { p ->
                            val isSelected = priority == p
                            val col = when (p) {
                                "High" -> Color(0xFFD32F2F)
                                "Medium" -> Color(0xFFFFA000)
                                else -> Color(0xFF388E3C)
                            }
                            OutlinedButton(
                                onClick = { priority = p },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("priority_chip_$p"),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (isSelected) col else Color.Transparent,
                                    contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            ) {
                                Text(p, fontSize = 12.sp)
                            }
                        }
                    }

                    // Due Date Inputs
                    Text("Due Date (D/M/Y):", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = day.toString(),
                            onValueChange = { it.toIntOrNull()?.let { d -> if (d in 1..31) day = d } },
                            label = { Text("Day") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = (month + 1).toString(),
                            onValueChange = { it.toIntOrNull()?.let { m -> if (m in 1..12) month = m - 1 } },
                            label = { Text("Month") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = year.toString(),
                            onValueChange = { it.toIntOrNull()?.let { y -> if (y in 2020..2040) year = y } },
                            label = { Text("Year") },
                            modifier = Modifier.weight(1.5f)
                        )
                    }

                    Button(
                        onClick = {
                            if (taskText.isNotBlank()) {
                                val cal = Calendar.getInstance().apply {
                                    set(Calendar.YEAR, year)
                                    set(Calendar.MONTH, month)
                                    set(Calendar.DAY_OF_MONTH, day)
                                    set(Calendar.HOUR_OF_DAY, 23)
                                    set(Calendar.MINUTE, 59)
                                }
                                onAddTask(taskText.trim(), priority, cal.timeInMillis)
                                taskText = ""
                                priority = "Medium"
                                showAddForm = false
                            }
                        },
                        enabled = taskText.isNotBlank(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("save_task_btn")
                    ) {
                        Text("Add Task")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (pendingTasks.isNotEmpty()) {
                item {
                    Text(
                        text = "Pending Tasks (${pendingTasks.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                items(pendingTasks, key = { it.id }) { task ->
                    TaskRowItem(
                        task = task,
                        onToggle = { onToggleTask(task) },
                        onDelete = { onDeleteTask(task) }
                    )
                }
            }

            if (completedTasks.isNotEmpty()) {
                item {
                    Text(
                        text = "Completed Tasks (${completedTasks.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                items(completedTasks, key = { it.id }) { task ->
                    TaskRowItem(
                        task = task,
                        onToggle = { onToggleTask(task) },
                        onDelete = { onDeleteTask(task) }
                    )
                }
            }

            if (tasks.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "No tasks",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier.size(60.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "No to-do tasks logged yet",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "Tasks help you keep track of midwife duties privately.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TaskRowItem(
    task: MidwifeTask,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    val priColor = when (task.priority) {
        "High" -> Color(0xFFD32F2F)
        "Medium" -> Color(0xFFFFA000)
        else -> Color(0xFF388E3C)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onToggle,
                modifier = Modifier.testTag("task_toggle_${task.id}")
            ) {
                Icon(
                    imageVector = if (task.isCompleted) Icons.Default.CheckCircle else Icons.Default.FavoriteBorder,
                    contentDescription = "Toggle Complete",
                    tint = if (task.isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.description,
                    fontWeight = FontWeight.SemiBold,
                    color = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyLarge,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(priColor.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${task.priority} Priority",
                            color = priColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }

                    Text(
                        text = "Due: ${PregnancyUtils.formatDate(task.dueDate)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        fontSize = 11.sp
                    )
                }
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.testTag("task_delete_${task.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Task",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                )
            }
        }
    }
}

// ==========================================
// MEDICATIONS TRACKING SCREEN
// ==========================================
@Composable
fun MedicationsSubScreen(
    patients: List<Patient>,
    medications: List<Medication>,
    onAddMed: (Int, String, String, Long, String) -> Unit,
    onDeleteMed: (Medication) -> Unit
) {
    var expandedDropdown by remember { mutableStateOf(false) }
    var selectedPatient by remember { mutableStateOf<Patient?>(null) }
    var medName by remember { mutableStateOf("") }
    var dosage by remember { mutableStateOf("") }
    var year by remember { mutableStateOf(Calendar.getInstance().get(Calendar.YEAR)) }
    var month by remember { mutableStateOf(Calendar.getInstance().get(Calendar.MONTH)) }
    var day by remember { mutableStateOf(Calendar.getInstance().get(Calendar.DAY_OF_MONTH)) }
    var notesText by remember { mutableStateOf("") }
    var showAddForm by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Patient Medications",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            IconButton(
                onClick = { showAddForm = !showAddForm },
                modifier = Modifier.testTag("meds_toggle_form_btn")
            ) {
                Icon(
                    imageVector = if (showAddForm) Icons.Default.Close else Icons.Default.AddCircle,
                    contentDescription = "Toggle Add Med Form",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        AnimatedVisibility(visible = showAddForm) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Log New Prescription",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Patient Select
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedCard(
                            onClick = { expandedDropdown = !expandedDropdown },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("med_select_patient_card")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = selectedPatient?.name ?: "Select Patient *",
                                    color = if (selectedPatient != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown")
                            }
                        }

                        DropdownMenu(
                            expanded = expandedDropdown,
                            onDismissRequest = { expandedDropdown = false },
                            modifier = Modifier.fillMaxWidth(0.9f)
                        ) {
                            if (patients.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("No patients registered yet") },
                                    onClick = { expandedDropdown = false }
                                )
                            } else {
                                patients.forEach { patient ->
                                    DropdownMenuItem(
                                        text = { Text(patient.name) },
                                        onClick = {
                                            selectedPatient = patient
                                            expandedDropdown = false
                                        },
                                        modifier = Modifier.testTag("med_dropdown_item_${patient.id}")
                                    )
                                }
                            }
                        }
                    }

                    // Med details
                    OutlinedTextField(
                        value = medName,
                        onValueChange = { medName = it },
                        label = { Text("Medication Name *") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("med_name_input"),
                        singleLine = true,
                        colors = TextFieldDefaults.colors(focusedContainerColor = MaterialTheme.colorScheme.surface, unfocusedContainerColor = MaterialTheme.colorScheme.surface)
                    )

                    OutlinedTextField(
                        value = dosage,
                        onValueChange = { dosage = it },
                        label = { Text("Dosage & Instructions *") },
                        placeholder = { Text("e.g. 1 tablet daily with lunch") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("med_dosage_input"),
                        singleLine = true,
                        colors = TextFieldDefaults.colors(focusedContainerColor = MaterialTheme.colorScheme.surface, unfocusedContainerColor = MaterialTheme.colorScheme.surface)
                    )

                    // Date inputs
                    Text("Start Date (D/M/Y):", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = day.toString(),
                            onValueChange = { it.toIntOrNull()?.let { d -> if (d in 1..31) day = d } },
                            label = { Text("Day") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = (month + 1).toString(),
                            onValueChange = { it.toIntOrNull()?.let { m -> if (m in 1..12) month = m - 1 } },
                            label = { Text("Month") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = year.toString(),
                            onValueChange = { it.toIntOrNull()?.let { y -> if (y in 2020..2040) year = y } },
                            label = { Text("Year") },
                            modifier = Modifier.weight(1.5f)
                        )
                    }

                    OutlinedTextField(
                        value = notesText,
                        onValueChange = { notesText = it },
                        label = { Text("Clinical Indication / Additional Notes") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .testTag("med_notes_input"),
                        colors = TextFieldDefaults.colors(focusedContainerColor = MaterialTheme.colorScheme.surface, unfocusedContainerColor = MaterialTheme.colorScheme.surface)
                    )

                    Button(
                        onClick = {
                            val patient = selectedPatient
                            if (patient != null && medName.isNotBlank() && dosage.isNotBlank()) {
                                val cal = Calendar.getInstance().apply {
                                    set(Calendar.YEAR, year)
                                    set(Calendar.MONTH, month)
                                    set(Calendar.DAY_OF_MONTH, day)
                                }
                                onAddMed(patient.id, medName.trim(), dosage.trim(), cal.timeInMillis, notesText.trim())
                                medName = ""
                                dosage = ""
                                notesText = ""
                                selectedPatient = null
                                showAddForm = false
                            }
                        },
                        enabled = selectedPatient != null && medName.isNotBlank() && dosage.isNotBlank(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("save_med_btn")
                    ) {
                        Text("Save Prescription")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (medications.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "No prescriptions",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(60.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "No prescriptions logged yet",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "Click the '+' pill to issue a medication log.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(medications, key = { it.id }) { med ->
                    val matchingPatient = patients.find { it.id == med.patientId }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = med.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "Patient: ${matchingPatient?.name ?: "Unknown"}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                IconButton(
                                    onClick = { onDeleteMed(med) },
                                    modifier = Modifier.testTag("delete_med_${med.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Stop Medication",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Dosage: ${med.dosage}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.weight(1.3f)
                                )
                                Text(
                                    text = "Started: ${PregnancyUtils.formatDate(med.startDate)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.End
                                )
                            }

                            if (med.notes.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Row {
                                    Icon(
                                        Icons.Default.Info,
                                        contentDescription = "Note Icon",
                                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                        modifier = Modifier
                                            .size(16.dp)
                                            .align(Alignment.CenterVertically)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = med.notes,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// CLINICAL STATS & BAR CHARTS SCREEN
// ==========================================
@Composable
fun StatsSubScreen(
    patients: List<Patient>,
    notes: List<ClinicalNote>,
    medications: List<Medication>,
    tasks: List<MidwifeTask>
) {
    val totalPatients = patients.size
    val totalNotes = notes.size
    val totalMeds = medications.size

    // High risk calculation:
    // Preeclampsia warning or notes containing explicit concern, or systolic bp >= 140 or diastolic bp >= 90 (simulating from active reports),
    // or gestational category / notes with "Concern" / "Referral"
    val concernNotesIds = notes.filter { it.category in listOf("Concern", "Referral") }.map { it.patientId }.toSet()
    val highRiskPatients = patients.filter { patient ->
        patient.id in concernNotesIds || patient.age >= 35 || patient.gravida >= 5
    }
    val highRiskCount = highRiskPatients.size

    // Average Gestational age math for active patients
    val activePatients = patients.filter { it.status.equals("Active", true) }
    val avgGestationWeeks = if (activePatients.isNotEmpty()) {
        val totalDays = activePatients.map {
            val diffMs = System.currentTimeMillis() - it.lmpDate
            (diffMs / (1000 * 60 * 60 * 24)).toInt()
        }.sum()
        val avgDays = totalDays / activePatients.size
        avgDays / 7
    } else {
        0
    }

    // Gestational progress distribution
    // Trimester 1 (0 to 12 weeks)
    // Trimester 2 (13 to 26 weeks)
    // Trimester 3 (27 to 40+ weeks)
    var trim1 = 0
    var trim2 = 0
    var trim3 = 0
    var postpartum = patients.count { it.status.equals("Postpartum", true) }
    var delivered = patients.count { it.status.equals("Delivered", true) }

    activePatients.forEach { patient ->
        val diffMs = System.currentTimeMillis() - patient.lmpDate
        val weeks = (diffMs / (1000 * 60 * 60 * 24 * 7)).toInt()
        when {
            weeks in 0..12 -> trim1++
            weeks in 13..26 -> trim2++
            else -> trim3++
        }
    }

    // Risk distribution indices (just for plotting)
    val lowRiskCount = totalPatients - highRiskCount

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Midwife Insights Center",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        // At-A-Glance Stat Grid
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatGridItem(label = "Total Census", value = "$totalPatients Mothers", icon = Icons.Default.AccountBox, color = MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f))
                StatGridItem(label = "High Risk Profile", value = "$highRiskCount Patients", icon = Icons.Default.Warning, color = MaterialTheme.colorScheme.error, modifier = Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (activePatients.isNotEmpty()) {
                    StatGridItem(label = "Avg Gestation", value = "$avgGestationWeeks Weeks", icon = Icons.Default.DateRange, color = MaterialTheme.colorScheme.secondary, modifier = Modifier.weight(1f))
                } else {
                    StatGridItem(label = "Avg Gestation", value = "N/A", icon = Icons.Default.DateRange, color = MaterialTheme.colorScheme.secondary, modifier = Modifier.weight(1f))
                }
                StatGridItem(label = "Active Obs Notes", value = "$totalNotes Entries", icon = Icons.Default.Edit, color = MaterialTheme.colorScheme.tertiary, modifier = Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatGridItem(label = "Active Medications", value = "$totalMeds Prescriptions", icon = Icons.Default.Star, color = Color(0xFF673AB7), modifier = Modifier.weight(1f))
            }
        }

        // Section: Risk Level Chart
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Patient Risk Distribution Profile",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Custom Graphical bar chart
                if (totalPatients == 0) {
                    Text("No clinical files loaded to evaluate risks.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    Text(
                        "Risk assessment based on age over 35, high parity, and clinically flagged Concerns.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // Low Risk Bar Row
                    ChartProgressBar(
                        label = "Low Clinical Risk",
                        count = lowRiskCount,
                        total = totalPatients,
                        color = Color(0xFF4CAF50)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // High Risk Bar Row
                    ChartProgressBar(
                        label = "High Clinical Risk / Alert Flags",
                        count = highRiskCount,
                        total = totalPatients,
                        color = Color(0xFFD32F2F)
                    )
                }
            }
        }

        // Section: Gestational progress bar charts
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Gestation & Status Milestones Progress",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (totalPatients == 0) {
                    Text("No clinical records loaded to evaluate milestones.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        ChartProgressBar(label = "Trimester 1 (0-12wks)", count = trim1, total = totalPatients, color = Color(0xFF03A9F4))
                        ChartProgressBar(label = "Trimester 2 (13-26wks)", count = trim2, total = totalPatients, color = Color(0xFFFF9800))
                        ChartProgressBar(label = "Trimester 3 (27wks+)", count = trim3, total = totalPatients, color = Color(0xFF9C27B0))
                        ChartProgressBar(label = "Postpartum Focus", count = postpartum, total = totalPatients, color = Color(0xFFE91E63))
                        ChartProgressBar(label = "Completed / Delivered", count = delivered, total = totalPatients, color = Color(0xFF4CAF50))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun StatGridItem(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = label, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

@Composable
fun ChartProgressBar(
    label: String,
    count: Int,
    total: Int,
    color: Color
) {
    val fraction = if (total > 0) count.toFloat() / total.toFloat() else 0f
    val percentText = if (total > 0) "${(fraction * 100).toInt()}%" else "0%"

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Text(text = "$count ($percentText)", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Simple real progress bar drawable in Jetpack Compose
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction = if (fraction > 0f) fraction else 0.001f)
                    .clip(RoundedCornerShape(5.dp))
                    .background(color)
            )
        }
    }
}
