package tm.khang.kauthenticator.feature.accounts

import tm.khang.kauthenticator.core.database.AccountRepositoryResult
import tm.khang.kauthenticator.core.database.EncryptedAccountRepository
import tm.khang.kauthenticator.core.model.TotpAccount
import tm.khang.kauthenticator.core.otp.Clock
import tm.khang.kauthenticator.core.otp.OtpResult
import tm.khang.kauthenticator.core.otp.OtpSecret
import tm.khang.kauthenticator.core.otp.TotpGenerator

interface AccountListStore {
    fun load(): Result<List<TotpAccount>>
    fun updateMetadata(account: TotpAccount, nowEpochMillis: Long): Result<Unit>
    fun delete(id: String): Result<Unit>
    fun otp(account: TotpAccount, epochSeconds: Long): Result<AccountOtp>
}

class EncryptedAccountListStore(
    private val repository: EncryptedAccountRepository,
) : AccountListStore {
    override fun load(): Result<List<TotpAccount>> = repository.getAll().toResult()

    override fun updateMetadata(account: TotpAccount, nowEpochMillis: Long): Result<Unit> =
        repository.updateMetadata(account, nowEpochMillis).toResult()

    override fun delete(id: String): Result<Unit> = repository.delete(id).toResult()

    override fun otp(account: TotpAccount, epochSeconds: Long): Result<AccountOtp> {
        val result = repository.withDecryptedSecret(account.id) { bytes ->
            val secret = OtpSecret.fromBytes(bytes)
            val generator = TotpGenerator(Clock { epochSeconds })
            val code = generator.generate(
                secret = secret,
                algorithm = account.algorithm,
                digits = account.digits,
                periodSeconds = account.periodSeconds,
            )
            if (code is OtpResult.Success) {
                AccountOtp(code.value, secondsRemainingAt(epochSeconds, account.periodSeconds))
            } else {
                null
            }
        }
        return when (result) {
            is AccountRepositoryResult.Success -> result.value?.let { Result.success(it) }
                ?: Result.failure(IllegalStateException("OTP generation failed"))
            else -> Result.failure(IllegalStateException("Secret unavailable"))
        }
    }

    private fun <T> AccountRepositoryResult<T>.toResult(): Result<T> = when (this) {
        is AccountRepositoryResult.Success -> Result.success(value)
        else -> Result.failure(IllegalStateException("Repository operation failed"))
    }
}
