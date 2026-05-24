package com.example.data

import kotlinx.coroutines.flow.Flow

class PatientRepository(private val patientDao: PatientDao) {
    val allPatients: Flow<List<Patient>> = patientDao.getAllPatients()

    fun getPatientById(id: Int): Flow<Patient?> = patientDao.getPatientById(id)

    suspend fun insertPatient(patient: Patient): Long = patientDao.insertPatient(patient)

    suspend fun deletePatient(patient: Patient) = patientDao.deletePatient(patient)

    suspend fun deletePatientById(id: Int) = patientDao.deletePatientById(id)

    fun getVisitsForPatient(patientId: Int): Flow<List<VisitLog>> = patientDao.getVisitsForPatient(patientId)

    suspend fun insertVisitLog(visit: VisitLog): Long = patientDao.insertVisitLog(visit)

    suspend fun deleteVisitLog(visit: VisitLog) = patientDao.deleteVisitLog(visit)

    // Clinical Notes
    val allClinicalNotes: Flow<List<ClinicalNote>> = patientDao.getAllClinicalNotes()
    fun getClinicalNotesForPatient(patientId: Int): Flow<List<ClinicalNote>> = patientDao.getClinicalNotesForPatient(patientId)
    suspend fun insertClinicalNote(note: ClinicalNote): Long = patientDao.insertClinicalNote(note)
    suspend fun deleteClinicalNote(note: ClinicalNote) = patientDao.deleteClinicalNote(note)
    suspend fun deleteClinicalNoteById(id: Int) = patientDao.deleteClinicalNoteById(id)

    // Midwife Tasks
    val allTasks: Flow<List<MidwifeTask>> = patientDao.getAllTasks()
    suspend fun insertTask(task: MidwifeTask): Long = patientDao.insertTask(task)
    suspend fun deleteTask(task: MidwifeTask) = patientDao.deleteTask(task)
    suspend fun deleteTaskById(id: Int) = patientDao.deleteTaskById(id)

    // Medications
    val allMedications: Flow<List<Medication>> = patientDao.getAllMedications()
    fun getMedicationsForPatient(patientId: Int): Flow<List<Medication>> = patientDao.getMedicationsForPatient(patientId)
    suspend fun insertMedication(med: Medication): Long = patientDao.insertMedication(med)
    suspend fun deleteMedication(med: Medication) = patientDao.deleteMedication(med)
    suspend fun deleteMedicationById(id: Int) = patientDao.deleteMedicationById(id)
}
