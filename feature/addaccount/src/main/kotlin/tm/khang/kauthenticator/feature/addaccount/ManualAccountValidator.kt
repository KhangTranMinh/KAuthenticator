package tm.khang.kauthenticator.feature.addaccount

import tm.khang.kauthenticator.core.otp.OtpResult
import tm.khang.kauthenticator.core.otp.OtpSecret

object ManualAccountValidator {
    fun validate(input: ManualAccountInput): List<ManualValidationError> = buildList {
        if (input.issuer.isBlank()) {
            add(ManualValidationError(ManualField.Issuer, "Issuer is required"))
        }
        if (input.accountName.isBlank()) {
            add(ManualValidationError(ManualField.AccountName, "Account name is required"))
        }
        if (input.secretBase32.isBlank()) {
            add(ManualValidationError(ManualField.Secret, "Secret is required"))
        } else if (OtpSecret.fromBase32(input.secretBase32) is OtpResult.Failure) {
            add(ManualValidationError(ManualField.Secret, "Secret must be valid Base32"))
        }
        if (input.digits != 6 && input.digits != 8) {
            add(ManualValidationError(ManualField.Digits, "Digits must be 6 or 8"))
        }
        if (input.periodSeconds <= 0) {
            add(ManualValidationError(ManualField.Period, "Period must be greater than zero"))
        }
    }
}
