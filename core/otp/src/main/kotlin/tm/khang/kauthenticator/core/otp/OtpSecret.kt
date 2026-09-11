package tm.khang.kauthenticator.core.otp

class OtpSecret private constructor(private val bytes: ByteArray) {
    internal fun copyBytes(): ByteArray = bytes.copyOf()

    fun <T> useBytes(block: (ByteArray) -> T): T {
        val copy = bytes.copyOf()
        return try {
            block(copy)
        } finally {
            copy.fill(0)
        }
    }

    override fun toString(): String = "OtpSecret(**redacted**)"

    override fun equals(other: Any?): Boolean =
        other is OtpSecret && bytes.contentEquals(other.bytes)

    override fun hashCode(): Int = bytes.contentHashCode()

    companion object {
        fun fromBase32(value: String): OtpResult<OtpSecret> = when (val result = Base32.decode(value)) {
            is OtpResult.Success -> OtpResult.Success(OtpSecret(result.value.copyOf()))
            is OtpResult.Failure -> result
        }

        internal fun fromBytes(value: ByteArray): OtpSecret = OtpSecret(value.copyOf())
    }
}
