package tm.khang.kauthenticator.core.database

import tm.khang.kauthenticator.core.model.SecretReference
import tm.khang.kauthenticator.core.model.TotpAccount
import tm.khang.kauthenticator.core.otp.TotpAlgorithm
import tm.khang.kauthenticator.core.security.EncryptedSecretEnvelope
import tm.khang.kauthenticator.core.security.SecretCipher
import tm.khang.kauthenticator.core.security.SecretCipherResult

sealed interface AccountRepositoryResult<out T> {
    data class Success<T>(val value: T) : AccountRepositoryResult<T>
    data object NotFound : AccountRepositoryResult<Nothing>
    data object KeyInvalidated : AccountRepositoryResult<Nothing>
    data object CorruptedSecret : AccountRepositoryResult<Nothing>
    data object Failure : AccountRepositoryResult<Nothing>
}

class EncryptedAccountRepository(
    private val dao: AuthenticatorAccountDao,
    private val cipher: SecretCipher,
) {
    fun insert(account: TotpAccount, plaintextSecret: ByteArray, nowEpochMillis: Long): AccountRepositoryResult<Unit> {
        val encrypted = when (val result = cipher.encrypt(plaintextSecret)) {
            is SecretCipherResult.Success -> result.value
            SecretCipherResult.KeyInvalidated -> return AccountRepositoryResult.KeyInvalidated
            SecretCipherResult.AuthenticationFailed, SecretCipherResult.Failure -> return AccountRepositoryResult.Failure
        }
        return try {
            dao.insert(account.toEntity(encrypted, nowEpochMillis, nowEpochMillis))
            AccountRepositoryResult.Success(Unit)
        } catch (_: Exception) {
            AccountRepositoryResult.Failure
        }
    }

    fun update(account: TotpAccount, plaintextSecret: ByteArray, nowEpochMillis: Long): AccountRepositoryResult<Unit> {
        val existing = try {
            dao.getById(account.id)
        } catch (_: Exception) {
            return AccountRepositoryResult.Failure
        } ?: return AccountRepositoryResult.NotFound

        val encrypted = when (val result = cipher.encrypt(plaintextSecret)) {
            is SecretCipherResult.Success -> result.value
            SecretCipherResult.KeyInvalidated -> return AccountRepositoryResult.KeyInvalidated
            SecretCipherResult.AuthenticationFailed, SecretCipherResult.Failure -> return AccountRepositoryResult.Failure
        }
        return try {
            dao.update(account.toEntity(encrypted, existing.createdAtEpochMillis, nowEpochMillis))
            AccountRepositoryResult.Success(Unit)
        } catch (_: Exception) {
            AccountRepositoryResult.Failure
        }
    }

    fun getAll(): AccountRepositoryResult<List<TotpAccount>> = try {
        AccountRepositoryResult.Success(dao.getAll().map { it.toDomain() })
    } catch (_: Exception) {
        AccountRepositoryResult.Failure
    }

    fun updateMetadata(account: TotpAccount, nowEpochMillis: Long): AccountRepositoryResult<Unit> {
        val existing = try {
            dao.getById(account.id)
        } catch (_: Exception) {
            return AccountRepositoryResult.Failure
        } ?: return AccountRepositoryResult.NotFound

        return try {
            dao.update(
                existing.copy(
                    issuer = account.issuer,
                    accountName = account.accountName,
                    algorithm = account.algorithm.name,
                    digits = account.digits,
                    periodSeconds = account.periodSeconds,
                    updatedAtEpochMillis = nowEpochMillis,
                ),
            )
            AccountRepositoryResult.Success(Unit)
        } catch (_: Exception) {
            AccountRepositoryResult.Failure
        }
    }

    fun delete(id: String): AccountRepositoryResult<Unit> {
        val existing = try {
            dao.getById(id)
        } catch (_: Exception) {
            return AccountRepositoryResult.Failure
        } ?: return AccountRepositoryResult.NotFound

        return try {
            dao.delete(existing)
            AccountRepositoryResult.Success(Unit)
        } catch (_: Exception) {
            AccountRepositoryResult.Failure
        }
    }

    fun <T> withDecryptedSecret(id: String, block: (ByteArray) -> T): AccountRepositoryResult<T> {
        val entity = try {
            dao.getById(id)
        } catch (_: Exception) {
            return AccountRepositoryResult.Failure
        } ?: return AccountRepositoryResult.NotFound

        val envelope = EncryptedSecretEnvelope(
            ciphertext = entity.secretCiphertext,
            iv = entity.secretIv,
            schemaVersion = entity.secretSchemaVersion,
        )
        return when (val result = cipher.decrypt(envelope)) {
            is SecretCipherResult.Success -> {
                try {
                    AccountRepositoryResult.Success(block(result.value))
                } finally {
                    result.value.fill(0)
                }
            }
            SecretCipherResult.KeyInvalidated -> AccountRepositoryResult.KeyInvalidated
            SecretCipherResult.AuthenticationFailed -> AccountRepositoryResult.CorruptedSecret
            SecretCipherResult.Failure -> AccountRepositoryResult.Failure
        }
    }

    private fun TotpAccount.toEntity(
        encrypted: EncryptedSecretEnvelope,
        createdAtEpochMillis: Long,
        updatedAtEpochMillis: Long,
    ) = AuthenticatorAccountEntity(
        id = id,
        issuer = issuer,
        accountName = accountName,
        algorithm = algorithm.name,
        digits = digits,
        periodSeconds = periodSeconds,
        secretCiphertext = encrypted.ciphertext,
        secretIv = encrypted.iv,
        secretSchemaVersion = encrypted.schemaVersion,
        createdAtEpochMillis = createdAtEpochMillis,
        updatedAtEpochMillis = updatedAtEpochMillis,
    )

    private fun AuthenticatorAccountEntity.toDomain() = TotpAccount(
        id = id,
        issuer = issuer,
        accountName = accountName,
        secret = SecretReference(id),
        algorithm = TotpAlgorithm.valueOf(algorithm),
        digits = digits,
        periodSeconds = periodSeconds,
    )
}
