package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Patient
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: PatientViewModel,
    modifier: Modifier = Modifier
) {
    val patients by viewModel.patients.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val statusFilter by viewModel.statusFilter.collectAsState()

    var currentTab by remember { mutableStateOf(0) } // 0 = Mothers list, 1 = Tools
    var selectedSubTab by remember { mutableStateOf(0) } // 0 = Notes, 1 = Tasks, 2 = Meds, 3 = Stats

    // Calculate count statuses from total state
    val totalPatientsMap by viewModel.patients.collectAsState()
    var showCalculatorDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (currentTab == 0) "Midwife Records" else "Midwife Private Tools",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                actions = {
                    IconButton(
                        onClick = { showCalculatorDialog = true },
                        modifier = Modifier.testTag("calculator_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Calculators",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier.testTag("main_bottom_bar")
            ) {
                NavigationBarItem(
                    selected = currentTab == 0,
                    onClick = { currentTab = 0 },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Mothers List") },
                    label = { Text("Mothers") },
                    modifier = Modifier.testTag("tab_button_mothers")
                )
                NavigationBarItem(
                    selected = currentTab == 1,
                    onClick = { currentTab = 1 },
                    icon = { Icon(Icons.Default.Build, contentDescription = "Midwife Tools") },
                    label = { Text("Tools") },
                    modifier = Modifier.testTag("tab_button_tools")
                )
            }
        },
        floatingActionButton = {
            if (currentTab == 0) {
                FloatingActionButton(
                    onClick = { viewModel.navigateTo(Screen.AddEditPatient()) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier
                        .navigationBarsPadding()
                        .testTag("add_patient_fab")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Mother")
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        if (currentTab == 0) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
            ) {
                // Dashboard Metrics Cards
                Card(
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Clinical Caseload Overview",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            MetricCountItem(
                                label = "Active Prenatal",
                                count = patients.count { it.status.equals("Active", true) }.toString(),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(
                                modifier = Modifier
                                    .height(40.dp)
                                    .width(1.dp)
                                    .align(Alignment.CenterVertically)
                                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
                            )
                            MetricCountItem(
                                label = "Postpartum Focus",
                                count = patients.count { it.status.equals("Postpartum", true) }.toString(),
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Spacer(
                                modifier = Modifier
                                    .height(40.dp)
                                    .width(1.dp)
                                    .align(Alignment.CenterVertically)
                                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
                            )
                            MetricCountItem(
                                label = "Delivered Total",
                                count = patients.count { it.status.equals("Delivered", true) }.toString(),
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    }
                }

                // Interactive Search Field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    placeholder = { Text("Search patient name, phone, or email...") },
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .testTag("search_input")
                )

                // Status Filter Tabs
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val filters = listOf(
                        Pair("Active", "Active"),
                        Pair("Postpartum", "Postpartum"),
                        Pair("Delivered", "Delivered"),
                        Pair("All Care", null)
                    )

                    filters.forEach { (label, value) ->
                        val isSelected = statusFilter == value
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.setStatusFilter(value) },
                            label = { Text(label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier.testTag("filter_$label")
                        )
                    }
                }

                // Injected Pending Tasks Summary Card (As Requested!)
                val tasks by viewModel.midwifeTasks.collectAsState()
                val pendingTasks = tasks.filter { !it.isCompleted }

                AnimatedVisibility(visible = pendingTasks.isNotEmpty()) {
                    Card(
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .testTag("tasks_summary_card")
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Pending Tasks",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Pending Tasks (${pendingTasks.size})",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }

                                Text(
                                    text = "View All",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .clickable {
                                            currentTab = 1
                                            selectedSubTab = 1 // Switch to Tasks subtab
                                        }
                                        .testTag("tasks_view_all_btn")
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            pendingTasks.take(2).forEach { task ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.FavoriteBorder,
                                        contentDescription = "Quick Check Off",
                                        tint = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f),
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clickable { viewModel.toggleTaskCompletion(task) }
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = task.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    val diffDay = ((task.dueDate - System.currentTimeMillis()) / (1000 * 60 * 60 * 24)).toInt()
                                    Text(
                                        text = if (diffDay < 0) "Overdue" else "Due ${PregnancyUtils.formatDate(task.dueDate)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (diffDay < 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }

                // Patients List
                if (patients.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = "No patients logo",
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                modifier = Modifier.size(72.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No maternal records found",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (searchQuery.isNotEmpty()) {
                                    "Try adjusting your search queries or filters."
                                } else {
                                    "Tap the '+' floating action button below to create your very first patient intake profile!"
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                modifier = Modifier.fillMaxWidth(0.85f),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(patients, key = { it.id }) { patient ->
                            PatientListItem(
                                patient = patient,
                                onDetailClick = { viewModel.navigateTo(Screen.PatientDetails(patient.id)) }
                            )
                        }
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                ToolsTabSection(
                    viewModel = viewModel,
                    selectedSubTab = selectedSubTab,
                    onSubTabChange = { selectedSubTab = it }
                )
            }
        }
    }

    // Pregnancy Calculator Intake Dialog
    if (showCalculatorDialog) {
        IntakeCalculatorDialog(
            onDismiss = { showCalculatorDialog = false },
            onAddPatient = { lmpTime ->
                showCalculatorDialog = false
                viewModel.navigateTo(Screen.AddEditPatient())
            }
        )
    }
}

@Composable
fun MetricCountItem(
    label: String,
    count: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        Text(
            text = count,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = color
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientListItem(
    patient: Patient,
    onDetailClick: () -> Unit
) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onDetailClick() }
            .testTag("patient_item_${patient.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Patient Avatar Initial Circle
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
            ) {
                Text(
                    text = patient.name.take(1).uppercase(),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Patient textual snapshot
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = patient.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    // Blood Type Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = patient.bloodType,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.tertiary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Gestational Age Or postpartum label
                    if (patient.status.equals("Active", true)) {
                        val gestStr = PregnancyUtils.getGestationalAgeString(patient.lmpDate)
                        Text(
                            text = "GA: $gestStr",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    } else {
                        Text(
                            text = "Status: ${patient.status}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // EDD date
                    if (patient.status.equals("Active", true)) {
                        val eddDate = PregnancyUtils.calculateEdd(patient.lmpDate)
                        Text(
                            text = "EDD: ${PregnancyUtils.formatDate(eddDate)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text(
                            text = "Age: ${patient.age} yrs",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "View clinical files",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
            )
        }
    }
}

@Composable
fun IntakeCalculatorDialog(
    onDismiss: () -> Unit,
    onAddPatient: (Long) -> Unit
) {
    var lmpInputDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var selectedCalendar = remember { Calendar.getInstance() }

    var year by remember { mutableStateOf(selectedCalendar.get(Calendar.YEAR)) }
    var month by remember { mutableStateOf(selectedCalendar.get(Calendar.MONTH)) }
    var day by remember { mutableStateOf(selectedCalendar.get(Calendar.DAY_OF_MONTH)) }

    // Re-evaluate calculation values
    val eddTimestamp = remember(year, month, day) {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, day)
        }
        PregnancyUtils.calculateEdd(cal.timeInMillis)
    }

    val gestStr = remember(year, month, day) {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, day)
        }
        PregnancyUtils.getGestationalAgeString(cal.timeInMillis)
    }

    val daysLeft = remember(eddTimestamp) {
        PregnancyUtils.getDaysUntilDue(eddTimestamp)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Pregnancy Intake Calculator",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Select Last Menstrual Period (LMP) to calculate active gestational age and Estimated Due Date (EDD).",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Simulating compact inputs for the date
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Day Input
                    OutlinedTextField(
                        value = day.toString(),
                        onValueChange = {
                            it.toIntOrNull()?.let { d -> if (d in 1..31) day = d }
                        },
                        label = { Text("Day") },
                        modifier = Modifier.weight(1f)
                    )

                    // Month dropdown substitute for reliability
                    OutlinedTextField(
                        value = (month + 1).toString(),
                        onValueChange = {
                            it.toIntOrNull()?.let { m -> if (m in 1..12) month = m - 1 }
                        },
                        label = { Text("Month") },
                        modifier = Modifier.weight(1f)
                    )

                    // Year Input
                    OutlinedTextField(
                        value = year.toString(),
                        onValueChange = {
                            it.toIntOrNull()?.let { y -> if (y in 2020..2040) year = y }
                        },
                        label = { Text("Year") },
                        modifier = Modifier.weight(1.5f)
                    )
                }

                // Calculations Output Cards
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Estimated Due Date:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            Text(PregnancyUtils.formatDate(eddTimestamp), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        }
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Gestational Age:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            Text(gestStr, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.ExtraBold)
                        }
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Countdown Remaining:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            Text("$daysLeft days left", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cal = Calendar.getInstance().apply {
                        set(Calendar.YEAR, year)
                        set(Calendar.MONTH, month)
                        set(Calendar.DAY_OF_MONTH, day)
                    }
                    onAddPatient(cal.timeInMillis)
                }
            ) {
                Text("Add Patient using this date")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}
