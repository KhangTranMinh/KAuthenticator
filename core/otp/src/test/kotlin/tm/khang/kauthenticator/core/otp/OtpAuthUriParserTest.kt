package tm.khang.kauthenticator.core.otp

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OtpAuthUriParserTest {
    @Test
    fun `parses percent encoded TOTP URI and normalizes defaults`() {
        val result = OtpAuthUriParser.parse(
            "otpauth://totp/Example%20Inc:alice%40example.com?secret=JBSWY3DPEHPK3PXP&issuer=Example%20Inc",
        )

        assertTrue(result is OtpResult.Success)
        val account = (result as OtpResult.Success).value
        assertEquals("Example Inc", account.issuer)
        assertEquals("alice@example.com", account.accountName)
        assertEquals(TotpAlgorithm.SHA1, account.algorithm)
        assertEquals(6, account.digits)
        assertEquals(30, account.periodSeconds)
        assertEquals("OtpSecret(**redacted**)", account.secret.toString())
    }

    @Test
    fun `parses supported algorithms digits and custom period`() {
        val result = OtpAuthUriParser.parse(
            "otpauth://totp/alice?secret=JBSWY3DPEHPK3PXP&algorithm=SHA-512&digits=8&period=45",
        )
        val account = (result as OtpResult.Success).value
        assertEquals(TotpAlgorithm.SHA512, account.algorithm)
        assertEquals(8, account.digits)
        assertEquals(45, account.periodSeconds)
    }

    @Test
    fun `rejects unsupported type missing secret and issuer mismatch`() {
        assertFailure< OtpError.UnsupportedOtpType >(
            OtpAuthUriParser.parse("otpauth://hotp/alice?secret=JBSWY3DPEHPK3PXP"),
        )
        assertEquals(
            OtpResult.Failure(OtpError.MissingSecret),
            OtpAuthUriParser.parse("otpauth://totp/alice?issuer=Example"),
        )
        assertFailure<OtpError.IssuerMismatch>(
            OtpAuthUriParser.parse("otpauth://totp/IssuerA:alice?secret=JBSWY3DPEHPK3PXP&issuer=IssuerB"),
        )
    }

    @Test
    fun `rejects unsupported algorithm digits and invalid period`() {
        assertFailure<OtpError.UnsupportedAlgorithm>(
            OtpAuthUriParser.parse("otpauth://totp/alice?secret=JBSWY3DPEHPK3PXP&algorithm=MD5"),
        )
        assertFailure<OtpError.UnsupportedDigits>(
            OtpAuthUriParser.parse("otpauth://totp/alice?secret=JBSWY3DPEHPK3PXP&digits=7"),
        )
        assertFailure<OtpError.InvalidPeriod>(
            OtpAuthUriParser.parse("otpauth://totp/alice?secret=JBSWY3DPEHPK3PXP&period=0"),
        )
    }

    @Test
    fun `does not silently accept whitespace in secret`() {
        assertFailure<OtpError.InvalidBase32Character>(
            OtpAuthUriParser.parse("otpauth://totp/alice?secret=JBSW%20Y3DP"),
        )
    }

    private inline fun <reified E : OtpError> assertFailure(result: OtpResult<*>) {
        assertTrue(result is OtpResult.Failure)
        assertTrue((result as OtpResult.Failure).error is E)
    }
}
