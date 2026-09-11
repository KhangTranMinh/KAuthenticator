package tm.khang.kauthenticator.core.otp

object Base32 {
    private const val ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"

    fun decode(input: String): OtpResult<ByteArray> {
        if (input.isEmpty()) return OtpResult.Failure(OtpError.EmptySecret)

        val normalized = input.uppercase()
        val paddingStart = normalized.indexOf('=')
        val dataPart = if (paddingStart >= 0) normalized.substring(0, paddingStart) else normalized
        val paddingCount = if (paddingStart >= 0) normalized.length - paddingStart else 0

        if (dataPart.isEmpty()) return OtpResult.Failure(OtpError.EmptySecret)

        for ((index, char) in dataPart.withIndex()) {
            if (char !in ALPHABET) {
                return OtpResult.Failure(OtpError.InvalidBase32Character(char, index))
            }
        }

        if (paddingStart >= 0) {
            if (normalized.substring(paddingStart).any { it != '=' }) {
                return OtpResult.Failure(OtpError.InvalidBase32Padding)
            }
            if (normalized.length % 8 != 0) {
                return OtpResult.Failure(OtpError.InvalidBase32Padding)
            }
            val expectedPadding = when (dataPart.length % 8) {
                0 -> 0
                2 -> 6
                4 -> 4
                5 -> 3
                7 -> 1
                else -> -1
            }
            if (paddingCount != expectedPadding) {
                return OtpResult.Failure(OtpError.InvalidBase32Padding)
            }
        } else if (dataPart.length % 8 !in setOf(0, 2, 4, 5, 7)) {
            return OtpResult.Failure(OtpError.InvalidBase32Length)
        }

        val output = ByteArray((dataPart.length * 5) / 8)
        var buffer = 0
        var bitsInBuffer = 0
        var outputIndex = 0

        for (char in dataPart) {
            buffer = (buffer shl 5) or ALPHABET.indexOf(char)
            bitsInBuffer += 5
            if (bitsInBuffer >= 8) {
                bitsInBuffer -= 8
                output[outputIndex++] = ((buffer shr bitsInBuffer) and 0xff).toByte()
            }
        }

        if (bitsInBuffer > 0) {
            val remainderMask = (1 shl bitsInBuffer) - 1
            if ((buffer and remainderMask) != 0) {
                return OtpResult.Failure(OtpError.InvalidBase32Length)
            }
        }

        return OtpResult.Success(output)
    }
}
