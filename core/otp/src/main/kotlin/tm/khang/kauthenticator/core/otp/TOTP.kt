package tm.khang.kauthenticator.core.otp

class TotpGenerator(
    private val clock: Clock = SystemClock,
) {
    fun generate(
        secret: OtpSecret,
        algorithm: TotpAlgorithm = TotpAlgorithm.SHA1,
        digits: Int = 6,
        periodSeconds: Int = 30,
    ): OtpResult<String> {
        if (periodSeconds <= 0) return OtpResult.Failure(OtpError.InvalidPeriod(periodSeconds))
        val now = clock.epochSeconds()
        if (now < 0L) return OtpResult.Failure(OtpError.InvalidCounter)
        return Hotp.generate(
            secret = secret,
            counter = now / periodSeconds,
            digits = digits,
            algorithm = algorithm,
        )
    }

    fun secondsRemaining(periodSeconds: Int = 30): OtpResult<Int> {
        if (periodSeconds <= 0) return OtpResult.Failure(OtpError.InvalidPeriod(periodSeconds))
        val now = clock.epochSeconds()
        if (now < 0L) return OtpResult.Failure(OtpError.InvalidCounter)
        val elapsed = (now % periodSeconds).toInt()
        return OtpResult.Success(periodSeconds - elapsed)
    }
}
