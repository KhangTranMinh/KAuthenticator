package tm.khang.kauthenticator.feature.settings

import android.content.Context

interface SettingsStore {
    fun read(): AppSettings
    fun write(settings: AppSettings)
}

class SharedPreferencesSettingsStore(
    context: Context,
) : SettingsStore {
    private val preferences = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)

    override fun read(): AppSettings = AppSettings(
        biometricLockEnabled = preferences.getBoolean(KEY_BIOMETRIC_LOCK, false),
        screenshotProtectionEnabled = preferences.getBoolean(KEY_SCREENSHOT_PROTECTION, false),
        theme = preferences.getString(KEY_THEME, null)
            ?.let(AppTheme::fromPersistenceValue)
            ?: AppTheme.SYSTEM,
    )

    override fun write(settings: AppSettings) {
        preferences.edit()
            .putBoolean(KEY_BIOMETRIC_LOCK, settings.biometricLockEnabled)
            .putBoolean(KEY_SCREENSHOT_PROTECTION, settings.screenshotProtectionEnabled)
            .putString(KEY_THEME, settings.theme.persistenceValue)
            .apply()
    }

    private companion object {
        const val FILE_NAME = "app_settings"
        const val KEY_BIOMETRIC_LOCK = "biometric_lock_enabled"
        const val KEY_SCREENSHOT_PROTECTION = "screenshot_protection_enabled"
        const val KEY_THEME = "theme"
    }
}
