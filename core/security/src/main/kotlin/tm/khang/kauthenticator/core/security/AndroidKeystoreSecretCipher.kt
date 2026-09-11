package tm.khang.kauthenticator.core.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.AEADBadTagException
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

class AndroidKeystoreSecretCipher(
    private val keyAlias: String = DEFAULT_KEY_ALIAS,
) : SecretCipher {
    override fun encrypt(plaintext: ByteArray): SecretCipherResult<EncryptedSecretEnvelope> {
        if (plaintext.isEmpty()) return SecretCipherResult.Failure
        return try {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
            SecretCipherResult.Success(
                EncryptedSecretEnvelope(
                    ciphertext = cipher.doFinal(plaintext),
                    iv = cipher.iv.copyOf(),
                ),
            )
        } catch (_: KeyPermanentlyInvalidatedException) {
            SecretCipherResult.KeyInvalidated
        } catch (_: Exception) {
            SecretCipherResult.Failure
        }
    }

    override fun decrypt(envelope: EncryptedSecretEnvelope): SecretCipherResult<ByteArray> {
        if (envelope.schemaVersion != EncryptedSecretEnvelope.CURRENT_SCHEMA_VERSION) {
            return SecretCipherResult.Failure
        }
        return try {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            val params = javax.crypto.spec.GCMParameterSpec(TAG_LENGTH_BITS, envelope.iv)
            cipher.init(Cipher.DECRYPT_MODE, getExistingKey() ?: return SecretCipherResult.Failure, params)
            SecretCipherResult.Success(cipher.doFinal(envelope.ciphertext))
        } catch (_: KeyPermanentlyInvalidatedException) {
            SecretCipherResult.KeyInvalidated
        } catch (_: AEADBadTagException) {
            SecretCipherResult.AuthenticationFailed
        } catch (_: Exception) {
            SecretCipherResult.Failure
        }
    }

    private fun getExistingKey(): SecretKey? {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        return keyStore.getKey(keyAlias, null) as? SecretKey
    }

    private fun getOrCreateKey(): SecretKey {
        getExistingKey()?.let { return it }
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        generator.init(
            KeyGenParameterSpec.Builder(
                keyAlias,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
            )
                .setKeySize(256)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setRandomizedEncryptionRequired(true)
                .build(),
        )
        return generator.generateKey()
    }

    companion object {
        const val DEFAULT_KEY_ALIAS = "kauthenticator.secret.v1"
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val TAG_LENGTH_BITS = 128
    }
}
