package tm.khang.kauthenticator.core.model

import tm.khang.kauthenticator.core.otp.TotpAlgorithm

@JvmInline
value class SecretReference(val value: String) {
    init {
        require(value.isNotBlank()) { "Secret reference must not be blank" }
    }

    override fun toString(): String = "SecretReference(**redacted**)"
}

data class TotpAccount(
    val id: String,
    val issuer: String,
    val accountName: String,
    val secret: SecretReference,
    val algorithm: TotpAlgorithm,
    val digits: Int,
    val periodSeconds: Int,
)
