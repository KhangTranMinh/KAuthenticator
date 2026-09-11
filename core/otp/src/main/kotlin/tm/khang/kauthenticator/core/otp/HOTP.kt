package tm.khang.kauthenticator.core.otp

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object Hotp {
    fun generate(
        secret: OtpSecret,
        counter: Long,
        digits: Int = 6,
        algorithm: TotpAlgorithm = TotpAlgorithm.SHA1,
    ): OtpResult<String> {
        if (counter < 0L) return OtpResult.Failure(OtpError.InvalidCounter)
        if (digits != 6 && digits != 8) return OtpResult.Failure(OtpError.UnsupportedDigits(digits))

        val message = ByteArray(8)
        var value = counter
        for (index in 7 downTo 0) {
            message[index] = (value and 0xff).toByte()
            value = value ushr 8
        }

        val keyBytes = secret.copyBytes()
        return try {
            val mac = Mac.getInstance(algorithm.jcaName)
            mac.init(SecretKeySpec(keyBytes, algorithm.jcaName))
            val digest = mac.doFinal(message)
            val offset = digest.last().toInt() and 0x0f
            val binary = ((digest[offset].toInt() and 0x7f) shl 24) or
                ((digest[offset + 1].toInt() and 0xff) shl 16) or
                ((digest[offset + 2].toInt() and 0xff) shl 8) or
                (digest[offset + 3].toInt() and 0xff)
            val modulus = if (digits == 6) 1_000_000 else 100_000_000
            OtpResult.Success((binary % modulus).toString().padStart(digits, '0'))
        } finally {
            keyBytes.fill(0)
        }
    }
}
