package tm.khang.kauthenticator.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
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
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        if (!state.automaticTimeEnabled) {
            Surface(
                color = MaterialTheme.colorScheme.errorContainer,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        "Check device time",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                    )
                    Text(
                        "Automatic date and time is off. Authentication codes may be rejected.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                    )
                }
            }
        }

        SettingsSection(title = "Security") {
            SettingSwitch(
                title = "App lock",
                description = if (state.deviceSecure) {
                    "Require device authentication when reopening KAuthenticator."
                } else {
                    "Set a secure screen lock on this device before enabling app lock."
                },
                checked = state.settings.biometricLockEnabled,
                enabled = state.deviceSecure,
                onCheckedChange = onBiometricLockChange,
            )
            SettingSwitch(
                title = "Block screenshots",
                description = "Hide sensitive codes from screenshots and recent-app previews.",
                checked = state.settings.screenshotProtectionEnabled,
                onCheckedChange = onScreenshotProtectionChange,
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Appearance", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            ElevatedCard(shape = RoundedCornerShape(22.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    AppTheme.entries.forEach { theme ->
                        FilterChip(
                            selected = state.settings.theme == theme,
                            onClick = { onThemeChange(theme) },
                            label = { Text(theme.displayName()) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        ElevatedCard(shape = RoundedCornerShape(22.dp)) {
            Column(Modifier.padding(vertical = 4.dp)) { content() }
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
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(Modifier.weight(1f).padding(end = 16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
            Text(
                description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            modifier = Modifier.semantics { stateDescription = if (checked) "On" else "Off" },
        )
    }
}

private fun AppTheme.displayName(): String = when (this) {
    AppTheme.SYSTEM -> "System"
    AppTheme.LIGHT -> "Light"
    AppTheme.DARK -> "Dark"
}
