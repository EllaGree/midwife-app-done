package com.example.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "patients")
data class Patient(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val age: Int,
    val phone: String,
    val email: String,
    val bloodType: String, // e.g. A+, O-, etc.
    val gravida: Int, // times pregnant
    val para: Int, // births
    val lmpDate: Long, // timestamp for Last Menstrual Period
    val emergencyContactName: String,
    val emergencyContactPhone: String,
    val notes: String,
    val status: String, // "Active", "Delivered", "Postpartum"
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "visit_logs",
    foreignKeys = [
        ForeignKey(
            entity = Patient::class,
            parentColumns = ["id"],
            childColumns = ["patientId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["patientId"])]
)
data class VisitLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val patientId: Int,
    val visitDate: Long,
    val gestationalWeeks: Int,
    val gestationalDays: Int,
    val systolicBp: Int,
    val diastolicBp: Int,
    val weightLb: Double,
    val fetalHeartRate: Int, // Fetal Heart Rate in bpm
    val symphysisFundalHeight: Int, // Symphysis-Fundal Height in cm
    val urineProtein: String, // e.g. "Negative", "Trace", "+1", "+2", etc.
    val urineGlucose: String, // e.g. "Negative", "Trace", "+1", "+2", etc.
    val presentation: String, // e.g. "Cephalic", "Breech", "Transverse", "Unknown"
    val notes: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "clinical_notes",
    foreignKeys = [
        ForeignKey(
            entity = Patient::class,
            parentColumns = ["id"],
            childColumns = ["patientId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["patientId"])]
)
data class ClinicalNote(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val patientId: Int,
    val category: String, // BP, Glucose, Scan, Routine, Labour, Concern, Referral
    val noteText: String,
    val date: Long = System.currentTimeMillis()
)

@Entity(tableName = "midwife_tasks")
data class MidwifeTask(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val description: String,
    val priority: String, // High, Medium, Low
    val dueDate: Long,
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "medications",
    foreignKeys = [
        ForeignKey(
            entity = Patient::class,
            parentColumns = ["id"],
            childColumns = ["patientId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["patientId"])]
)
data class Medication(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val patientId: Int,
    val name: String,
    val dosage: String,
    val startDate: Long,
    val notes: String,
    val createdAt: Long = System.currentTimeMillis()
)
