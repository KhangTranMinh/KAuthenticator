package tm.khang.kauthenticator.core.security

data class EncryptedSecretEnvelope(
    val ciphertext: ByteArray,
    val iv: ByteArray,
    val schemaVersion: Int = CURRENT_SCHEMA_VERSION,
) {
    companion object {
        const val CURRENT_SCHEMA_VERSION = 1
    }

    override fun toString(): String = "EncryptedSecretEnvelope(**redacted**)"
}

sealed interface SecretCipherResult<out T> {
    data class Success<T>(val value: T) : SecretCipherResult<T>
    data object KeyInvalidated : SecretCipherResult<Nothing>
    data object AuthenticationFailed : SecretCipherResult<Nothing>
    data object Failure : SecretCipherResult<Nothing>
}

interface SecretCipher {
    fun encrypt(plaintext: ByteArray): SecretCipherResult<EncryptedSecretEnvelope>
    fun decrypt(envelope: EncryptedSecretEnvelope): SecretCipherResult<ByteArray>
}
