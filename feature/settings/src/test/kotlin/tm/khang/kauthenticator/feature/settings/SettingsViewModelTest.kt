package tm.khang.kauthenticator.feature.settings

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import tm.khang.kauthenticator.core.security.DeviceCredentialGate

class SettingsViewModelTest {
    @Test
    fun `loads persisted settings and system state`() {
        val store = FakeStore(AppSettings(theme = AppTheme.DARK))
        val viewModel = SettingsViewModel(store, FakeGate(true), AutomaticTimeStatusReader { false })

        assertEquals(AppTheme.DARK, viewModel.state.value.settings.theme)
        assertTrue(viewModel.state.value.deviceSecure)
        assertFalse(viewModel.state.value.automaticTimeEnabled)
    }

    @Test
    fun `does not enable app lock without secure device credential`() {
        val store = FakeStore()
        val viewModel = SettingsViewModel(store, FakeGate(false), AutomaticTimeStatusReader { true })

        viewModel.setBiometricLockEnabled(true)

        assertFalse(viewModel.state.value.settings.biometricLockEnabled)
        assertFalse(store.value.biometricLockEnabled)
    }

    @Test
    fun `persists security and theme changes`() {
        val store = FakeStore()
        val viewModel = SettingsViewModel(store, FakeGate(true), AutomaticTimeStatusReader { true })

        viewModel.setBiometricLockEnabled(true)
        viewModel.setScreenshotProtectionEnabled(true)
        viewModel.setTheme(AppTheme.LIGHT)

        assertTrue(store.value.biometricLockEnabled)
        assertTrue(store.value.screenshotProtectionEnabled)
        assertEquals(AppTheme.LIGHT, store.value.theme)
    }

    private class FakeStore(initial: AppSettings = AppSettings()) : SettingsStore {
        var value = initial
        override fun read(): AppSettings = value
        override fun write(settings: AppSettings) { value = settings }
    }

    private class FakeGate(private val secure: Boolean) : DeviceCredentialGate {
        override fun isDeviceSecure(): Boolean = secure
    }
}
