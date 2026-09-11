package tm.khang.kauthenticator.core.otp

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HotpTest {
    @Test
    fun `matches RFC 4226 test vectors`() {
        val secret = OtpSecret.fromBytes("12345678901234567890".toByteArray())
        val expected = listOf(
            "755224", "287082", "359152", "969429", "338314",
            "254676", "287922", "162583", "399871", "520489",
        )

        expected.forEachIndexed { counter, otp ->
            assertEquals(
                OtpResult.Success(otp),
                Hotp.generate(secret, counter.toLong()),
            )
        }
    }

    @Test
    fun `supports eight digits and rejects invalid input`() {
        val secret = OtpSecret.fromBytes("12345678901234567890".toByteArray())
        val result = Hotp.generate(secret, counter = 0, digits = 8)
        assertEquals(OtpResult.Success("84755224"), result)
        assertTrue(Hotp.generate(secret, counter = -1) is OtpResult.Failure)
        assertTrue(Hotp.generate(secret, counter = 0, digits = 7) is OtpResult.Failure)
    }
}
