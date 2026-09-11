package tm.khang.kauthenticator.core.database

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import tm.khang.kauthenticator.core.model.SecretReference
import tm.khang.kauthenticator.core.model.TotpAccount
import tm.khang.kauthenticator.core.otp.TotpAlgorithm
import tm.khang.kauthenticator.core.security.EncryptedSecretEnvelope
import tm.khang.kauthenticator.core.security.SecretCipher
import tm.khang.kauthenticator.core.security.SecretCipherResult

class EncryptedAccountRepositoryTest {
    @Test
    fun insert_encryptsSecretBeforePersisting() {
        val dao = FakeDao()
        val cipher = FakeCipher()
        val repository = EncryptedAccountRepository(dao, cipher)
        val secret = byteArrayOf(1, 2, 3, 4)

        val result = repository.insert(sampleAccount(), secret, nowEpochMillis = 100L)

        assertTrue(result is AccountRepositoryResult.Success)
        assertArrayEquals(byteArrayOf(9, 9, 9), dao.saved?.secretCiphertext)
        assertArrayEquals(secret, cipher.lastPlaintext)
    }

    @Test
    fun withDecryptedSecret_wipesPlaintextAfterUse() {
        val dao = FakeDao().apply { saved = sampleEntity() }
        val decrypted = byteArrayOf(7, 8, 9)
        val cipher = FakeCipher(decrypted = decrypted)
        val repository = EncryptedAccountRepository(dao, cipher)

        val result = repository.withDecryptedSecret("id-1") { bytes -> bytes.copyOf() }

        assertTrue(result is AccountRepositoryResult.Success)
        assertArrayEquals(byteArrayOf(7, 8, 9), (result as AccountRepositoryResult.Success).value)
        assertArrayEquals(byteArrayOf(0, 0, 0), decrypted)
    }

    @Test
    fun withDecryptedSecret_mapsAuthenticationFailureToCorruptedSecret() {
        val dao = FakeDao().apply { saved = sampleEntity() }
        val repository = EncryptedAccountRepository(dao, FakeCipher(decryptResult = SecretCipherResult.AuthenticationFailed))

        assertEquals(AccountRepositoryResult.CorruptedSecret, repository.withDecryptedSecret("id-1") { Unit })
    }

    private fun sampleAccount() = TotpAccount(
        id = "id-1",
        issuer = "GitHub",
        accountName = "user@example.com",
        secret = SecretReference("id-1"),
        algorithm = TotpAlgorithm.SHA1,
        digits = 6,
        periodSeconds = 30,
    )

    private fun sampleEntity() = AuthenticatorAccountEntity(
        id = "id-1",
        issuer = "GitHub",
        accountName = "user@example.com",
        algorithm = "SHA1",
        digits = 6,
        periodSeconds = 30,
        secretCiphertext = byteArrayOf(9, 9, 9),
        secretIv = ByteArray(12),
        secretSchemaVersion = 1,
        createdAtEpochMillis = 1L,
        updatedAtEpochMillis = 1L,
    )

    private class FakeDao : AuthenticatorAccountDao {
        var saved: AuthenticatorAccountEntity? = null

        override fun getAll(): List<AuthenticatorAccountEntity> = listOfNotNull(saved)
        override fun getById(id: String): AuthenticatorAccountEntity? = saved?.takeIf { it.id == id }
        override fun insert(entity: AuthenticatorAccountEntity) { saved = entity }
        override fun update(entity: AuthenticatorAccountEntity) { saved = entity }
        override fun delete(entity: AuthenticatorAccountEntity) { if (saved == entity) saved = null }
    }

    private class FakeCipher(
        private val decrypted: ByteArray = byteArrayOf(1, 2, 3),
        private val decryptResult: SecretCipherResult<ByteArray>? = null,
    ) : SecretCipher {
        var lastPlaintext: ByteArray? = null

        override fun encrypt(plaintext: ByteArray): SecretCipherResult<EncryptedSecretEnvelope> {
            lastPlaintext = plaintext.copyOf()
            return SecretCipherResult.Success(
                EncryptedSecretEnvelope(
                    ciphertext = byteArrayOf(9, 9, 9),
                    iv = ByteArray(12),
                ),
            )
        }

        override fun decrypt(envelope: EncryptedSecretEnvelope): SecretCipherResult<ByteArray> =
            decryptResult ?: SecretCipherResult.Success(decrypted)
    }
}
