package tm.khang.kauthenticator.core.otp

enum class TotpAlgorithm(val jcaName: String) {
    SHA1("HmacSHA1"),
    SHA256("HmacSHA256"),
    SHA512("HmacSHA512"),
}
