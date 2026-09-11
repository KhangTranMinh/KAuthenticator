package tm.khang.kauthenticator.feature.addaccount

import java.util.UUID
import tm.khang.kauthenticator.core.database.AccountRepositoryResult
import tm.khang.kauthenticator.core.model.SecretReference
import tm.khang.kauthenticator.core.model.TotpAccount
import tm.khang.kauthenticator.core.otp.OtpAuthUriParser
import tm.khang.kauthenticator.core.otp.OtpResult
import tm.khang.kauthenticator.core.otp.OtpSecret

class AddAccountUseCase(
    private val store: EnrollmentStore,
    private val idFactory: () -> String = { UUID.randomUUID().toString() },
    private val nowEpochMillis: () -> Long = System::currentTimeMillis,
) {
    fun fromQr(rawValue: String): EnrollmentResult {
        val parsed = when (val result = OtpAuthUriParser.parse(rawValue)) {
            is OtpResult.Success -> result.value
            is OtpResult.Failure -> return EnrollmentResult.InvalidQr
        }
        if (parsed.issuer.isBlank()) return EnrollmentResult.InvalidQr
        val candidate = EnrollmentCandidate(
            issuer = parsed.issuer,
            accountName = parsed.accountName,
            secret = parsed.secret,
            algorithm = parsed.algorithm,
            digits = parsed.digits,
            periodSeconds = parsed.periodSeconds,
        )
        return enroll(candidate, parsed.secret)
    }

    fun fromManual(input: ManualAccountInput): EnrollmentResult {
        val errors = ManualAccountValidator.validate(input)
        if (errors.isNotEmpty()) return EnrollmentResult.Invalid(errors)

        val secret = when (val result = OtpSecret.fromBase32(input.secretBase32)) {
            is OtpResult.Success -> result.value
            is OtpResult.Failure -> return EnrollmentResult.Invalid(
                listOf(ManualValidationError(ManualField.Secret, "Secret must be valid Base32")),
            )
        }
        val candidate = EnrollmentCandidate(
            issuer = input.issuer.trim(),
            accountName = input.accountName.trim(),
            secret = secret,
            algorithm = input.algorithm,
            digits = input.digits,
            periodSeconds = input.periodSeconds,
        )
        return enroll(candidate, secret)
    }

    fun resolveDuplicate(
        duplicate: EnrollmentResult.Duplicate,
        decision: DuplicateDecision,
    ): EnrollmentResult {
        val secret = duplicate.candidate.secret
        return when (decision) {
            DuplicateDecision.Replace -> persist(
                candidate = duplicate.candidate,
                secret = secret,
                accountId = duplicate.existingAccountId,
                replace = true,
            )
            DuplicateDecision.KeepBoth -> persist(
                candidate = duplicate.candidate,
                secret = secret,
                accountId = idFactory(),
                replace = false,
            )
        }
    }

    private fun enroll(candidate: EnrollmentCandidate, secret: OtpSecret): EnrollmentResult {
        val accounts = when (val result = store.getAll()) {
            is AccountRepositoryResult.Success -> result.value
            else -> return EnrollmentResult.StorageFailure
        }
        val duplicate = accounts.firstOrNull {
            it.issuer.equals(candidate.issuer, ignoreCase = true) &&
                it.accountName.equals(candidate.accountName, ignoreCase = true)
        }
        if (duplicate != null) {
            return EnrollmentResult.Duplicate(duplicate.id, candidate)
        }
        return persist(candidate, secret, idFactory(), replace = false)
    }

    private fun persist(
        candidate: EnrollmentCandidate,
        secret: OtpSecret,
        accountId: String,
        replace: Boolean,
    ): EnrollmentResult {
        val account = TotpAccount(
            id = accountId,
            issuer = candidate.issuer,
            accountName = candidate.accountName,
            secret = SecretReference(accountId),
            algorithm = candidate.algorithm,
            digits = candidate.digits,
            periodSeconds = candidate.periodSeconds,
        )
        return secret.useBytes { bytes ->
            val result = if (replace) {
                store.update(account, bytes, nowEpochMillis())
            } else {
                store.insert(account, bytes, nowEpochMillis())
            }
            when (result) {
                is AccountRepositoryResult.Success -> EnrollmentResult.Added(accountId)
                else -> EnrollmentResult.StorageFailure
            }
        }
    }
}
