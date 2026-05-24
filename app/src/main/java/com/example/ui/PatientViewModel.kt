package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.Patient
import com.example.data.PatientRepository
import com.example.data.VisitLog
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface Screen {
    object Dashboard : Screen
    data class PatientDetails(val patientId: Int) : Screen
    data class AddEditPatient(val patientId: Int? = null) : Screen
    data class AddVisitLog(val patientId: Int) : Screen
}

class PatientViewModel(private val repository: PatientRepository) : ViewModel() {

    // View navigation flow
    private val _currentScreen = MutableStateFlow<Screen>(Screen.Dashboard)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    // Query filters
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _statusFilter = MutableStateFlow<String?>("Active") // "Active", "Delivered", "Postpartum", null (All)
    val statusFilter: StateFlow<String?> = _statusFilter.asStateFlow()

    // Patients filtered list
    val patients: StateFlow<List<Patient>> = repository.allPatients
        .combine(_searchQuery) { list, query ->
            if (query.isBlank()) {
                list
            } else {
                list.filter {
                    it.name.contains(query, ignoreCase = true) ||
                    it.phone.contains(query) ||
                    it.email.contains(query, ignoreCase = true)
                }
            }
        }
        .combine(_statusFilter) { list, filter ->
            if (filter == null) {
                list
            } else {
                list.filter { it.status.equals(filter, ignoreCase = true) }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Selection tracking
    private val _selectedPatientId = MutableStateFlow<Int?>(null)
    val selectedPatientId: StateFlow<Int?> = _selectedPatientId.asStateFlow()

    val selectedPatient: StateFlow<Patient?> = _selectedPatientId
        .flatMapLatest { id ->
            if (id == null) flowOf(null) else repository.getPatientById(id)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val selectedPatientVisits: StateFlow<List<VisitLog>> = _selectedPatientId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList()) else repository.getVisitsForPatient(id)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Navigation actions
    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
        when (screen) {
            is Screen.PatientDetails -> _selectedPatientId.value = screen.patientId
            is Screen.AddVisitLog -> _selectedPatientId.value = screen.patientId
            is Screen.AddEditPatient -> {
                screen.patientId?.let { _selectedPatientId.value = it }
            }
            Screen.Dashboard -> {
                _selectedPatientId.value = null
            }
        }
    }

    fun navigateBack() {
        when (val current = _currentScreen.value) {
            Screen.Dashboard -> { /* Root screen */ }
            is Screen.PatientDetails -> {
                _currentScreen.value = Screen.Dashboard
                _selectedPatientId.value = null
            }
            is Screen.AddEditPatient -> {
                val pid = current.patientId
                if (pid != null) {
                    _currentScreen.value = Screen.PatientDetails(pid)
                    _selectedPatientId.value = pid
                } else {
                    _currentScreen.value = Screen.Dashboard
                    _selectedPatientId.value = null
                }
            }
            is Screen.AddVisitLog -> {
                _currentScreen.value = Screen.PatientDetails(current.patientId)
                _selectedPatientId.value = current.patientId
            }
        }
    }

    // Filters update
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setStatusFilter(filter: String?) {
        _statusFilter.value = filter
    }

    // Patient transactions
    fun savePatient(
        id: Int = 0,
        name: String,
        age: Int,
        phone: String,
        email: String,
        bloodType: String,
        gravida: Int,
        para: Int,
        lmpDate: Long,
        emergencyContactName: String,
        emergencyContactPhone: String,
        notes: String,
        status: String,
        onSuccess: (Int) -> Unit
    ) {
        viewModelScope.launch {
            val patient = Patient(
                id = id,
                name = name,
                age = age,
                phone = phone,
                email = email,
                bloodType = bloodType,
                gravida = gravida,
                para = para,
                lmpDate = lmpDate,
                emergencyContactName = emergencyContactName,
                emergencyContactPhone = emergencyContactPhone,
                notes = notes,
                status = status
            )
            val resultId = repository.insertPatient(patient)
            val finalId = if (id == 0) resultId.toInt() else id
            onSuccess(finalId)
        }
    }

    fun deletePatient(patient: Patient) {
        viewModelScope.launch {
            repository.deletePatient(patient)
            _selectedPatientId.value = null
            _currentScreen.value = Screen.Dashboard
        }
    }

    // Visit logs transactions
    fun saveVisitLog(
        patientId: Int,
        visitDate: Long,
        gestationalWeeks: Int,
        gestationalDays: Int,
        systolicBp: Int,
        diastolicBp: Int,
        weightLb: Double,
        fetalHeartRate: Int,
        symphysisFundalHeight: Int,
        urineProtein: String,
        urineGlucose: String,
        presentation: String,
        notes: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val log = VisitLog(
                patientId = patientId,
                visitDate = visitDate,
                gestationalWeeks = gestationalWeeks,
                gestationalDays = gestationalDays,
                systolicBp = systolicBp,
                diastolicBp = diastolicBp,
                weightLb = weightLb,
                fetalHeartRate = fetalHeartRate,
                symphysisFundalHeight = symphysisFundalHeight,
                urineProtein = urineProtein,
                urineGlucose = urineGlucose,
                presentation = presentation,
                notes = notes
            )
            repository.insertVisitLog(log)
            onSuccess()
        }
    }

    fun deleteVisitLog(log: VisitLog) {
        viewModelScope.launch {
            repository.deleteVisitLog(log)
        }
    }

    // New State Flows for Tools tab
    val clinicalNotes: StateFlow<List<com.example.data.ClinicalNote>> = repository.allClinicalNotes
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val midwifeTasks: StateFlow<List<com.example.data.MidwifeTask>> = repository.allTasks
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val medications: StateFlow<List<com.example.data.Medication>> = repository.allMedications
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // New Data Persistence Operations
    fun insertClinicalNote(patientId: Int, category: String, noteText: String) {
        viewModelScope.launch {
            repository.insertClinicalNote(
                com.example.data.ClinicalNote(patientId = patientId, category = category, noteText = noteText)
            )
        }
    }

    fun deleteClinicalNote(note: com.example.data.ClinicalNote) {
        viewModelScope.launch {
            repository.deleteClinicalNote(note)
        }
    }

    fun deleteClinicalNoteById(id: Int) {
        viewModelScope.launch {
            repository.deleteClinicalNoteById(id)
        }
    }

    fun insertTask(description: String, priority: String, dueDate: Long) {
        viewModelScope.launch {
            repository.insertTask(
                com.example.data.MidwifeTask(description = description, priority = priority, dueDate = dueDate)
            )
        }
    }

    fun toggleTaskCompletion(task: com.example.data.MidwifeTask) {
        viewModelScope.launch {
            repository.insertTask(task.copy(isCompleted = !task.isCompleted))
        }
    }

    fun deleteTask(task: com.example.data.MidwifeTask) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }

    fun deleteTaskById(id: Int) {
        viewModelScope.launch {
            repository.deleteTaskById(id)
        }
    }

    fun insertMedication(patientId: Int, name: String, dosage: String, startDate: Long, notes: String) {
        viewModelScope.launch {
            repository.insertMedication(
                com.example.data.Medication(
                    patientId = patientId,
                    name = name,
                    dosage = dosage,
                    startDate = startDate,
                    notes = notes
                )
            )
        }
    }

    fun deleteMedication(med: com.example.data.Medication) {
        viewModelScope.launch {
            repository.deleteMedication(med)
        }
    }

    fun deleteMedicationById(id: Int) {
        viewModelScope.launch {
            repository.deleteMedicationById(id)
        }
    }
}

class PatientViewModelFactory(private val repository: PatientRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PatientViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PatientViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
