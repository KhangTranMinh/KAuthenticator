package tm.khang.kauthenticator.feature.settings

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import tm.khang.kauthenticator.core.security.DeviceCredentialGate

class SettingsViewModel(
    private val store: SettingsStore,
    private val deviceCredentialGate: DeviceCredentialGate,
    private val automaticTimeStatusReader: AutomaticTimeStatusReader,
) : ViewModel() {
    private val _state = MutableStateFlow(loadState())
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    fun refreshSystemState() {
        _state.value = _state.value.copy(
            deviceSecure = deviceCredentialGate.isDeviceSecure(),
            automaticTimeEnabled = automaticTimeStatusReader.isAutomaticTimeEnabled(),
        )
    }

    fun setBiometricLockEnabled(enabled: Boolean) {
        val canEnable = !enabled || deviceCredentialGate.isDeviceSecure()
        if (!canEnable) {
            _state.value = _state.value.copy(deviceSecure = false)
            return
        }
        updateSettings { it.copy(biometricLockEnabled = enabled) }
    }

    fun setScreenshotProtectionEnabled(enabled: Boolean) {
        updateSettings { it.copy(screenshotProtectionEnabled = enabled) }
    }

    fun setTheme(theme: AppTheme) {
        updateSettings { it.copy(theme = theme) }
    }

    private fun updateSettings(transform: (AppSettings) -> AppSettings) {
        val updated = transform(_state.value.settings)
        store.write(updated)
        _state.value = _state.value.copy(settings = updated)
    }

    private fun loadState(): SettingsUiState = SettingsUiState(
        settings = store.read(),
        deviceSecure = deviceCredentialGate.isDeviceSecure(),
        automaticTimeEnabled = automaticTimeStatusReader.isAutomaticTimeEnabled(),
    )
}
