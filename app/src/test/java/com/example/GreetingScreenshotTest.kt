package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.data.Patient
import com.example.ui.PatientListItem
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun greeting_screenshot() {
    val dummyPatient = Patient(
        id = 1,
        name = "Jane Doe",
        age = 29,
        phone = "+1 (555) 019-2834",
        email = "jane.doe@example.com",
        bloodType = "O+",
        gravida = 2,
        para = 1,
        lmpDate = System.currentTimeMillis() - 14 * 24 * 60 * 60 * 1000L * 7L, // ~14 weeks ago
        emergencyContactName = "John Doe (Partner)",
        emergencyContactPhone = "+1 (555) 019-9999",
        notes = "No known clinical allergies. Keep active monitoring on blood pressure indices.",
        status = "Active"
    )

    composeTestRule.setContent {
      MyApplicationTheme {
        PatientListItem(
            patient = dummyPatient,
            onDetailClick = {}
        )
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
  }
}
