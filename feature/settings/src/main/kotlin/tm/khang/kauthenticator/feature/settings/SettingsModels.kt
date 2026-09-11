package tm.khang.kauthenticator.feature.settings

enum class AppTheme(val persistenceValue: String) {
    SYSTEM("system"),
    LIGHT("light"),
    DARK("dark"),
    ;

    companion object {
        fun fromPersistenceValue(value: String): AppTheme? = entries.firstOrNull {
            it.persistenceValue == value || it.name == value
        }
    }
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
