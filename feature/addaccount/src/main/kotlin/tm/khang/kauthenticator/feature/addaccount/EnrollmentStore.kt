package tm.khang.kauthenticator.feature.addaccount

import tm.khang.kauthenticator.core.database.AccountRepositoryResult
import tm.khang.kauthenticator.core.database.EncryptedAccountRepository
import tm.khang.kauthenticator.core.model.TotpAccount

interface EnrollmentStore {
    fun getAll(): AccountRepositoryResult<List<TotpAccount>>
    fun insert(account: TotpAccount, plaintextSecret: ByteArray, nowEpochMillis: Long): AccountRepositoryResult<Unit>
    fun update(account: TotpAccount, plaintextSecret: ByteArray, nowEpochMillis: Long): AccountRepositoryResult<Unit>
}

class RepositoryEnrollmentStore(
    private val repository: EncryptedAccountRepository,
) : EnrollmentStore {
    override fun getAll() = repository.getAll()

    override fun insert(
        account: TotpAccount,
        plaintextSecret: ByteArray,
        nowEpochMillis: Long,
    ) = repository.insert(account, plaintextSecret, nowEpochMillis)

    override fun update(
        account: TotpAccount,
        plaintextSecret: ByteArray,
        nowEpochMillis: Long,
    ) = repository.update(account, plaintextSecret, nowEpochMillis)
}
