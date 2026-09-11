package tm.khang.kauthenticator.core.security

import org.junit.Assert.assertEquals
import org.junit.Test

class AppLockCoordinatorTest {
    @Test
    fun `enabled secure device locks until authentication succeeds`() {
        val coordinator = AppLockCoordinator(FakeGate(true))

        assertEquals(AppLockState.LOCKED, coordinator.updateEnabled(true))
        assertEquals(AppLockState.UNLOCKED, coordinator.onAuthenticationSucceeded())
        assertEquals(AppLockState.LOCKED, coordinator.onAppBackgrounded())
    }

    @Test
    fun `cannot enable lock when device has no secure credential`() {
        val coordinator = AppLockCoordinator(FakeGate(false))

        assertEquals(AppLockState.DISABLED, coordinator.updateEnabled(true))
    }

    private class FakeGate(private val secure: Boolean) : DeviceCredentialGate {
        override fun isDeviceSecure(): Boolean = secure
    }
}
