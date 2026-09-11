package tm.khang.kauthenticator.core.otp

enum class TotpAlgorithm(
    val jcaName: String,
    val persistenceValue: String,
) {
    SHA1("HmacSHA1", "sha1"),
    SHA256("HmacSHA256", "sha256"),
    SHA512("HmacSHA512", "sha512"),
    ;

    companion object {
        fun fromPersistenceValue(value: String): TotpAlgorithm? = entries.firstOrNull {
            it.persistenceValue == value.lowercase() || it.name == value
        }
    }
}
