package com.ayah.smarthomemonitor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.ayah.smarthomemonitor.ui.MonitorScreen
import com.ayah.smarthomemonitor.ui.theme.SmartHomeMonitorTheme

/**
 * Deliberately thin: sets up Compose and shows [MonitorScreen]. No network
 * calls, no detection logic, no polling, no auth logic — all of that lives
 * in the ViewModel/Repository/domain layers.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SmartHomeMonitorTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MonitorScreen()
                }
            }
        }
    }
}
