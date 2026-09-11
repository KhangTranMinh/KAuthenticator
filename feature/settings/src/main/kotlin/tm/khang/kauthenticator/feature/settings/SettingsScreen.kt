package tm.khang.kauthenticator.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen(
    state: SettingsUiState,
    onBiometricLockChange: (Boolean) -> Unit,
    onScreenshotProtectionChange: (Boolean) -> Unit,
    onThemeChange: (AppTheme) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("Security", style = MaterialTheme.typography.titleLarge)

        SettingSwitch(
            title = "App lock",
            description = if (state.deviceSecure) {
                "Require device authentication when reopening the app."
            } else {
                "Set a secure screen lock on this device before enabling app lock."
            },
            checked = state.settings.biometricLockEnabled,
            enabled = state.deviceSecure,
            onCheckedChange = onBiometricLockChange,
        )

        SettingSwitch(
            title = "Block screenshots",
            description = "Prevent screenshots and recent-app previews while KAuthenticator is visible.",
            checked = state.settings.screenshotProtectionEnabled,
            onCheckedChange = onScreenshotProtectionChange,
        )

        if (!state.automaticTimeEnabled) {
            Text(
                text = "Automatic date and time is off. TOTP codes may be rejected until device time is corrected.",
                color = MaterialTheme.colorScheme.error,
            )
        }

        Text("Theme", style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AppTheme.entries.forEach { theme ->
                TextButton(onClick = { onThemeChange(theme) }) {
                    val marker = if (state.settings.theme == theme) "✓ " else ""
                    Text(marker + theme.name.lowercase().replaceFirstChar { it.uppercase() })
                }
            }
        }
    }
}

@Composable
private fun SettingSwitch(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(Modifier.weight(1f).padding(end = 16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(description, style = MaterialTheme.typography.bodyMedium)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
        )
    }
}
