package tm.khang.kauthenticator.core.otp

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class Base32Test {
    @Test
    fun `decodes RFC 4648 vectors with or without padding`() {
        val cases = listOf(
            "MY======" to "f",
            "MZXQ====" to "fo",
            "MZXW6===" to "foo",
            "MZXW6YQ=" to "foob",
            "MZXW6YTB" to "fooba",
            "MZXW6YTBOI======" to "foobar",
            "mzxw6ytboi" to "foobar",
        )

        cases.forEach { (encoded, expected) ->
            val result = Base32.decode(encoded)
            assertTrue(result is OtpResult.Success)
            assertArrayEquals(expected.toByteArray(), (result as OtpResult.Success).value)
        }
    }

    @Test
    fun `rejects whitespace invalid alphabet empty input and malformed padding`() {
        assertTrue(Base32.decode("") is OtpResult.Failure)
        assertTrue(Base32.decode("MZXW 6===") is OtpResult.Failure)
        assertTrue(Base32.decode("MZXW1===") is OtpResult.Failure)
        assertTrue(Base32.decode("MZXW6=") is OtpResult.Failure)
        assertTrue(Base32.decode("MZXW6===A") is OtpResult.Failure)
    }

    @Test
    fun `rejects non zero trailing bits`() {
        val result = Base32.decode("MZ")
        assertEquals(OtpError.InvalidBase32Length, (result as OtpResult.Failure).error)
    }
}
