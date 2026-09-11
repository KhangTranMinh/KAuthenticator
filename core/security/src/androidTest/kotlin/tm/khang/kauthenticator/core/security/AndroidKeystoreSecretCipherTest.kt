package tm.khang.kauthenticator.core.security

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AndroidKeystoreSecretCipherTest {
    private val cipher = AndroidKeystoreSecretCipher("kauthenticator.test.${System.nanoTime()}")

    @Test
    fun encryptDecrypt_roundTrip() {
        val plaintext = "JBSWY3DPEHPK3PXP".encodeToByteArray()
        val encrypted = cipher.encrypt(plaintext) as SecretCipherResult.Success
        val decrypted = cipher.decrypt(encrypted.value) as SecretCipherResult.Success
        try {
            assertArrayEquals(plaintext, decrypted.value)
        } finally {
            plaintext.fill(0)
            decrypted.value.fill(0)
        }
    }

    @Test
    fun decrypt_rejectsTamperedCiphertext() {
        val plaintext = "JBSWY3DPEHPK3PXP".encodeToByteArray()
        val encrypted = (cipher.encrypt(plaintext) as SecretCipherResult.Success).value
        plaintext.fill(0)
        val tampered = encrypted.ciphertext.copyOf().also { it[0] = (it[0].toInt() xor 1).toByte() }

        val result = cipher.decrypt(encrypted.copy(ciphertext = tampered))

        assertTrue(result is SecretCipherResult.AuthenticationFailed)
    }
}
