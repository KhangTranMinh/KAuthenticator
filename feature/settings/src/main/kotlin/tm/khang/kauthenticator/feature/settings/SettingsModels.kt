package tm.khang.kauthenticator.feature.settings

enum class AppTheme {
    SYSTEM,
    LIGHT,
    DARK,
}

data class AppSettings(
    val biometricLockEnabled: Boolean = false,
    val screenshotProtectionEnabled: Boolean = false,
    val theme: AppTheme = AppTheme.SYSTEM,
)

data class SettingsUiState(
    val settings: AppSettings = AppSettings(),
    val deviceSecure: Boolean = true,
    val automaticTimeEnabled: Boolean = true,
)
