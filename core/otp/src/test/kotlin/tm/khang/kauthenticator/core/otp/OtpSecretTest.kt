package tm.khang.kauthenticator.core.otp

import org.junit.Assert.assertTrue
import org.junit.Test

class OtpSecretTest {
    @Test
    fun `useBytes wipes temporary copy after callback`() {
        val secret = (OtpSecret.fromBase32("JBSWY3DPEHPK3PXP") as OtpResult.Success).value
        lateinit var exposedCopy: ByteArray

        secret.useBytes { bytes ->
            exposedCopy = bytes
            assertTrue(bytes.any { it.toInt() != 0 })
        }

        assertTrue(exposedCopy.all { it.toInt() == 0 })
    }
}
