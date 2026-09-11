package tm.khang.kauthenticator.feature.accounts

import androidx.lifecycle.SavedStateHandle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import tm.khang.kauthenticator.core.model.TotpAccount

class AccountListViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val store: AccountListStore,
    private val nowEpochMillis: () -> Long = System::currentTimeMillis,
) : ViewModel() {
    private var sourceAccounts: List<TotpAccount> = emptyList()

    var state: AccountListUiState by mutableStateOf(AccountListUiState(
        query = savedStateHandle[KEY_QUERY] ?: "",
        sort = savedStateHandle.get<String>(KEY_SORT)?.let { runCatching { AccountSort.valueOf(it) }.getOrNull() }
            ?: AccountSort.ISSUER,
    ))
        private set

    init {
        reload()
    }

    fun reload() {
        state = state.copy(isLoading = true, error = null)
        store.load().fold(
            onSuccess = {
                sourceAccounts = it
                state = state.copy(accounts = filteredAccounts(), isLoading = false)
            },
            onFailure = {
                state = state.copy(isLoading = false, error = AccountListError.LOAD_FAILED)
            },
        )
    }

    fun setQuery(query: String) {
        savedStateHandle[KEY_QUERY] = query
        state = state.copy(query = query, accounts = filteredAccounts(query = query))
    }

    fun setSort(sort: AccountSort) {
        savedStateHandle[KEY_SORT] = sort.name
        state = state.copy(sort = sort, accounts = filteredAccounts(sort = sort))
    }

    fun editLabel(id: String, issuer: String, accountName: String) {
        val current = sourceAccounts.firstOrNull { it.id == id } ?: return
        val updated = current.copy(issuer = issuer.trim(), accountName = accountName.trim())
        if (updated.accountName.isBlank()) return
        store.updateMetadata(updated, nowEpochMillis()).fold(
            onSuccess = {
                sourceAccounts = sourceAccounts.map { if (it.id == id) updated else it }
                state = state.copy(accounts = filteredAccounts(), error = null)
            },
            onFailure = { state = state.copy(error = AccountListError.UPDATE_FAILED) },
        )
    }

    fun requestDelete(id: String) {
        state = state.copy(pendingDeleteId = id)
    }

    fun cancelDelete() {
        state = state.copy(pendingDeleteId = null)
    }

    fun confirmDelete() {
        val id = state.pendingDeleteId ?: return
        store.delete(id).fold(
            onSuccess = {
                sourceAccounts = sourceAccounts.filterNot { it.id == id }
                state = state.copy(accounts = filteredAccounts(), pendingDeleteId = null, error = null)
            },
            onFailure = { state = state.copy(pendingDeleteId = null, error = AccountListError.DELETE_FAILED) },
        )
    }

    fun otp(account: TotpAccount, epochSeconds: Long): Result<AccountOtp> = store.otp(account, epochSeconds)

    private fun filteredAccounts(
        query: String = state.query,
        sort: AccountSort = state.sort,
    ): List<TotpAccount> {
        val normalized = query.trim()
        val filtered = if (normalized.isEmpty()) {
            sourceAccounts
        } else {
            sourceAccounts.filter {
                it.issuer.contains(normalized, ignoreCase = true) ||
                    it.accountName.contains(normalized, ignoreCase = true)
            }
        }
        return when (sort) {
            AccountSort.ISSUER -> filtered.sortedWith(
                compareBy<TotpAccount> { it.issuer.lowercase() }
                    .thenBy { it.accountName.lowercase() },
            )
            AccountSort.ACCOUNT_NAME -> filtered.sortedWith(
                compareBy<TotpAccount> { it.accountName.lowercase() }
                    .thenBy { it.issuer.lowercase() },
            )
        }
    }

    companion object {
        private const val KEY_QUERY = "account_list_query"
        private const val KEY_SORT = "account_list_sort"
    }
}
