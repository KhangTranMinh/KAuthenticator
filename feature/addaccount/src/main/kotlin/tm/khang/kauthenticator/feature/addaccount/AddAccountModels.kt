package tm.khang.kauthenticator.feature.addaccount

import tm.khang.kauthenticator.core.otp.OtpSecret
import tm.khang.kauthenticator.core.otp.TotpAlgorithm

data class ManualAccountInput(
    val issuer: String,
    val accountName: String,
    val secretBase32: String,
    val algorithm: TotpAlgorithm = TotpAlgorithm.SHA1,
    val digits: Int = 6,
    val periodSeconds: Int = 30,
)

enum class ManualField {
    Issuer,
    AccountName,
    Secret,
    Digits,
    Period,
}

data class ManualValidationError(
    val field: ManualField,
    val message: String,
)

enum class DuplicateDecision {
    Replace,
    KeepBoth,
}

sealed interface EnrollmentResult {
    data class Added(val accountId: String) : EnrollmentResult
    data class Duplicate(
        val existingAccountId: String,
        val candidate: EnrollmentCandidate,
    ) : EnrollmentResult
    data class Invalid(val errors: List<ManualValidationError>) : EnrollmentResult
    data object InvalidQr : EnrollmentResult
    data object StorageFailure : EnrollmentResult
}

data class EnrollmentCandidate(
    val issuer: String,
    val accountName: String,
    val secret: OtpSecret,
    val algorithm: TotpAlgorithm,
    val digits: Int,
    val periodSeconds: Int,
)
