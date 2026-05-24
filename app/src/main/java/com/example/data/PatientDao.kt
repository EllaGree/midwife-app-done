package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PatientDao {
    @Query("SELECT * FROM patients ORDER BY name ASC")
    fun getAllPatients(): Flow<List<Patient>>

    @Query("SELECT * FROM patients WHERE id = :id")
    fun getPatientById(id: Int): Flow<Patient?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPatient(patient: Patient): Long

    @Delete
    suspend fun deletePatient(patient: Patient)

    @Query("DELETE FROM patients WHERE id = :id")
    suspend fun deletePatientById(id: Int)

    @Query("SELECT * FROM visit_logs WHERE patientId = :patientId ORDER BY visitDate DESC")
    fun getVisitsForPatient(patientId: Int): Flow<List<VisitLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVisitLog(visit: VisitLog): Long

    @Delete
    suspend fun deleteVisitLog(visit: VisitLog)

    // Clinical Notes
    @Query("SELECT * FROM clinical_notes ORDER BY date DESC")
    fun getAllClinicalNotes(): Flow<List<ClinicalNote>>

    @Query("SELECT * FROM clinical_notes WHERE patientId = :patientId ORDER BY date DESC")
    fun getClinicalNotesForPatient(patientId: Int): Flow<List<ClinicalNote>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClinicalNote(note: ClinicalNote): Long

    @Delete
    suspend fun deleteClinicalNote(note: ClinicalNote)

    @Query("DELETE FROM clinical_notes WHERE id = :id")
    suspend fun deleteClinicalNoteById(id: Int)

    // Midwife Tasks
    @Query("SELECT * FROM midwife_tasks ORDER BY dueDate ASC, createdAt DESC")
    fun getAllTasks(): Flow<List<MidwifeTask>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: MidwifeTask): Long

    @Delete
    suspend fun deleteTask(task: MidwifeTask)

    @Query("DELETE FROM midwife_tasks WHERE id = :id")
    suspend fun deleteTaskById(id: Int)

    // Medications
    @Query("SELECT * FROM medications ORDER BY createdAt DESC")
    fun getAllMedications(): Flow<List<Medication>>

    @Query("SELECT * FROM medications WHERE patientId = :patientId ORDER BY createdAt DESC")
    fun getMedicationsForPatient(patientId: Int): Flow<List<Medication>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedication(med: Medication): Long

    @Delete
    suspend fun deleteMedication(med: Medication)

    @Query("DELETE FROM medications WHERE id = :id")
    suspend fun deleteMedicationById(id: Int)
}
