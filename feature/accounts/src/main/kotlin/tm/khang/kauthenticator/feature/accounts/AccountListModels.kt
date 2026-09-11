package tm.khang.kauthenticator.feature.accounts

import tm.khang.kauthenticator.core.model.TotpAccount

enum class AccountSort {
    ISSUER,
    ACCOUNT_NAME,
}

data class AccountListUiState(
    val accounts: List<TotpAccount> = emptyList(),
    val query: String = "",
    val sort: AccountSort = AccountSort.ISSUER,
    val isLoading: Boolean = true,
    val error: AccountListError? = null,
    val pendingDeleteId: String? = null,
)

enum class AccountListError {
    LOAD_FAILED,
    UPDATE_FAILED,
    DELETE_FAILED,
    OTP_UNAVAILABLE,
}

data class AccountOtp(
    val code: String,
    val secondsRemaining: Int,
)
