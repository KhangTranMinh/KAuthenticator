package tm.khang.kauthenticator.core.otp

sealed interface OtpResult<out T> {
    data class Success<T>(val value: T) : OtpResult<T>
    data class Failure(val error: OtpError) : OtpResult<Nothing>
}

sealed interface OtpError {
    data object EmptySecret : OtpError
    data class InvalidBase32Character(val character: Char, val index: Int) : OtpError
    data object InvalidBase32Length : OtpError
    data object InvalidBase32Padding : OtpError

    data object InvalidOtpAuthUri : OtpError
    data class UnsupportedOtpType(val type: String?) : OtpError
    data object MissingAccountName : OtpError
    data object MissingSecret : OtpError
    data class IssuerMismatch(val labelIssuer: String, val queryIssuer: String) : OtpError
    data class UnsupportedAlgorithm(val value: String) : OtpError
    data class UnsupportedDigits(val value: Int?) : OtpError
    data class InvalidPeriod(val value: Int?) : OtpError

    data object InvalidCounter : OtpError
}
