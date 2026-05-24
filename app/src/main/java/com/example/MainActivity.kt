package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.AppDatabase
import com.example.data.PatientRepository
import com.example.ui.*
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Thread-safe Singleton Database & Repository Intakes
        val database = AppDatabase.getDatabase(this)
        val repository = PatientRepository(database.patientDao())

        setContent {
            MyApplicationTheme {
                val vm: PatientViewModel = viewModel(
                    factory = PatientViewModelFactory(repository)
                )

                val currentScreen by vm.currentScreen.collectAsState()

                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    when (val screen = currentScreen) {
                        is Screen.Dashboard -> {
                            DashboardScreen(viewModel = vm)
                        }
                        is Screen.PatientDetails -> {
                            PatientDetailsScreen(viewModel = vm, patientId = screen.patientId)
                        }
                        is Screen.AddEditPatient -> {
                            AddEditPatientScreen(viewModel = vm, patientId = screen.patientId)
                        }
                        is Screen.AddVisitLog -> {
                            AddVisitLogScreen(viewModel = vm, patientId = screen.patientId)
                        }
                    }
                }
            }
        }
    }
}
