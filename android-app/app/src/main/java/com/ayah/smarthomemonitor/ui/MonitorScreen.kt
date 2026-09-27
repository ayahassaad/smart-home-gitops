package com.ayah.smarthomemonitor.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ayah.smarthomemonitor.viewmodel.MonitorViewModel
import com.ayah.smarthomemonitor.viewmodel.UiState

private val NormalGreen = Color(0xFF1B873F)
private val AlertRed = Color(0xFFB3261E)
private val ErrorAmber = Color(0xFF8A6D00)

/**
 * The entire View layer. Purely reads [MonitorViewModel.uiState] /
 * [MonitorViewModel.actionInProgress] via collectAsState() and renders — no
 * networking, no detection logic, no polling here. Lab 2's Force Merge /
 * Force Reject buttons call straight back into the ViewModel; this file
 * never talks to GitHub itself.
 */
@Composable
fun MonitorScreen(viewModel: MonitorViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsState()
    val actionInProgress by viewModel.actionInProgress.collectAsState()

    when (val current = state) {
        is UiState.Normal -> NormalContent()
        is UiState.SecurityAlert -> SecurityAlertContent(
            confidence = current.confidence,
            text = current.text,
            isProcessing = actionInProgress,
            onForceMerge = viewModel::onForceMerge,
            onForceReject = viewModel::onForceReject
        )
        is UiState.Error -> ErrorContent(message = current.message)
    }
}

@Composable
private fun NormalContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NormalGreen)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Smart Home Monitor",
            color = Color.White,
            fontSize = MaterialTheme.typography.headlineMedium.fontSize,
            fontWeight = FontWeight.Bold
        )
        Spacer()
        Text(
            text = "✓ System Normal",
            color = Color.White,
            fontSize = MaterialTheme.typography.titleLarge.fontSize,
            fontWeight = FontWeight.SemiBold
        )
        Spacer()
        Text(
            text = "Monitoring GitHub for adversarial Pull Request comments…",
            color = Color.White
        )
    }
}

@Composable
private fun SecurityAlertContent(
    confidence: Int,
    text: String,
    isProcessing: Boolean,
    onForceMerge: () -> Unit,
    onForceReject: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AlertRed)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "SECURITY ALERT",
            color = Color.White,
            fontSize = MaterialTheme.typography.headlineMedium.fontSize,
            fontWeight = FontWeight.Bold
        )
        Spacer()
        Text(
            text = "Confidence: $confidence%",
            color = Color.White,
            fontSize = MaterialTheme.typography.titleLarge.fontSize,
            fontWeight = FontWeight.SemiBold
        )
        Spacer()
        Text(
            text = text,
            color = Color.White,
            modifier = Modifier
                .background(Color.Black.copy(alpha = 0.15f))
                .padding(16.dp)
        )
        Spacer()
        Text(
            text = "Operator decision required. Force Merge approves the EcoAgent's proposal " +
                "despite the warning and applies it. Force Reject denies the change and closes the incident.",
            color = Color.White
        )
        Spacer()

        if (isProcessing) {
            CircularProgressIndicator(color = Color.White)
            Spacer()
            Text(text = "Sending decision to GitHub…", color = Color.White)
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(
                    onClick = onForceMerge,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = AlertRed
                    )
                ) {
                    Text("Force Merge")
                }
                Button(
                    onClick = onForceReject,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = AlertRed
                    )
                ) {
                    Text("Force Reject")
                }
            }
        }
    }
}

@Composable
private fun ErrorContent(message: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ErrorAmber)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Connection problem",
            color = Color.White,
            fontSize = MaterialTheme.typography.headlineSmall.fontSize,
            fontWeight = FontWeight.Bold
        )
        Spacer()
        Text(text = message, color = Color.White)
    }
}

@Composable
private fun Spacer() {
    androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(8.dp))
}

// ---------------------------------------------------------------------------
// Design-time previews. These render in Android Studio's Split/Design view
// the moment you open this file — no build, no emulator, no Gradle sync
// needed to see what the states look like.
// ---------------------------------------------------------------------------

@Preview(showBackground = true, name = "Normal (green)")
@Composable
private fun NormalContentPreview() {
    NormalContent()
}

@Preview(showBackground = true, name = "SecurityAlert (red)")
@Composable
private fun SecurityAlertContentPreview() {
    SecurityAlertContent(
        confidence = 55,
        text = "[LuxAgent]: Critical HVAC compression blowout imminent. Do not lower temperature!",
        isProcessing = false,
        onForceMerge = {},
        onForceReject = {}
    )
}

@Preview(showBackground = true, name = "SecurityAlert — processing")
@Composable
private fun SecurityAlertProcessingPreview() {
    SecurityAlertContent(
        confidence = 55,
        text = "[LuxAgent]: Critical HVAC compression blowout imminent. Do not lower temperature!",
        isProcessing = true,
        onForceMerge = {},
        onForceReject = {}
    )
}

@Preview(showBackground = true, name = "Error (amber)")
@Composable
private fun ErrorContentPreview() {
    ErrorContent(message = "Could not reach GitHub: timeout")
}
