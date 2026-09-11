package tm.khang.kauthenticator.core.security

enum class AppLockState {
    DISABLED,
    LOCKED,
    UNLOCKED,
}

class AppLockCoordinator(
    private val credentialGate: DeviceCredentialGate,
) {
    private var state: AppLockState = AppLockState.DISABLED

    fun updateEnabled(enabled: Boolean): AppLockState {
        state = when {
            !enabled -> AppLockState.DISABLED
            !credentialGate.isDeviceSecure() -> AppLockState.DISABLED
            else -> AppLockState.LOCKED
        }
        return state
    }

    fun onAuthenticationSucceeded(): AppLockState {
        if (state == AppLockState.LOCKED) state = AppLockState.UNLOCKED
        return state
    }

    fun onAppBackgrounded(): AppLockState {
        if (state == AppLockState.UNLOCKED) state = AppLockState.LOCKED
        return state
    }

    fun currentState(): AppLockState = state
}
