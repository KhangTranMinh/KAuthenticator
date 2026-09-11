package tm.khang.kauthenticator.core.otp

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TotpGeneratorTest {
    @Test
    fun `matches RFC 6238 SHA1 SHA256 and SHA512 vectors`() {
        val timestamps = listOf(59L, 1_111_111_109L, 1_111_111_111L, 1_234_567_890L, 2_000_000_000L, 20_000_000_000L)
        val vectors = listOf(
            Triple(TotpAlgorithm.SHA1, "12345678901234567890", listOf("94287082", "07081804", "14050471", "89005924", "69279037", "65353130")),
            Triple(TotpAlgorithm.SHA256, "12345678901234567890123456789012", listOf("46119246", "68084774", "67062674", "91819424", "90698825", "77737706")),
            Triple(TotpAlgorithm.SHA512, "1234567890123456789012345678901234567890123456789012345678901234", listOf("90693936", "25091201", "99943326", "93441116", "38618901", "47863826")),
        )

        vectors.forEach { (algorithm, secretText, expected) ->
            timestamps.forEachIndexed { index, timestamp ->
                val generator = TotpGenerator(Clock { timestamp })
                val actual = generator.generate(
                    secret = OtpSecret.fromBytes(secretText.toByteArray()),
                    algorithm = algorithm,
                    digits = 8,
                    periodSeconds = 30,
                )
                assertEquals("$algorithm at $timestamp", OtpResult.Success(expected[index]), actual)
            }
        }
    }

    @Test
    fun `uses injected clock exactly across a period boundary`() {
        val secret = OtpSecret.fromBytes("12345678901234567890".toByteArray())
        val before = TotpGenerator(Clock { 59 }).generate(secret)
        val after = TotpGenerator(Clock { 60 }).generate(secret)

        assertEquals(Hotp.generate(secret, 1), before)
        assertEquals(Hotp.generate(secret, 2), after)
    }

    @Test
    fun `reports countdown from timestamp without decrementing state`() {
        assertEquals(OtpResult.Success(30), TotpGenerator(Clock { 60 }).secondsRemaining())
        assertEquals(OtpResult.Success(1), TotpGenerator(Clock { 89 }).secondsRemaining())
        assertTrue(TotpGenerator(Clock { -1 }).generate(OtpSecret.fromBytes(byteArrayOf(1))) is OtpResult.Failure)
    }
}
