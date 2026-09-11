package tm.khang.kauthenticator.feature.accounts

import androidx.lifecycle.SavedStateHandle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import tm.khang.kauthenticator.core.model.SecretReference
import tm.khang.kauthenticator.core.model.TotpAccount
import tm.khang.kauthenticator.core.otp.TotpAlgorithm

class AccountListViewModelTest {
    @Test
    fun `empty state loads successfully`() {
        val viewModel = AccountListViewModel(SavedStateHandle(), FakeStore(emptyList()))
        assertEquals(emptyList<TotpAccount>(), viewModel.state.accounts)
        assertEquals(false, viewModel.state.isLoading)
    }

    @Test
    fun `search matches issuer and account name ignoring case`() {
        val viewModel = AccountListViewModel(SavedStateHandle(), FakeStore(accounts()))
        viewModel.setQuery("WORK")
        assertEquals(listOf("2"), viewModel.state.accounts.map { it.id })
        viewModel.setQuery("alice")
        assertEquals(listOf("1"), viewModel.state.accounts.map { it.id })
    }

    @Test
    fun `sort persists in saved state`() {
        val handle = SavedStateHandle()
        val viewModel = AccountListViewModel(handle, FakeStore(accounts()))
        viewModel.setSort(AccountSort.ACCOUNT_NAME)
        assertEquals(AccountSort.ACCOUNT_NAME.persistenceValue, handle.get<String>("account_list_sort"))
        assertEquals(listOf("1", "2"), viewModel.state.accounts.map { it.id })
    }


    @Test
    fun `query and sort survive recreation through saved state`() {
        val handle = SavedStateHandle()
        val first = AccountListViewModel(handle, FakeStore(accounts()))
        first.setQuery("work")
        first.setSort(AccountSort.ACCOUNT_NAME)

        val recreated = AccountListViewModel(handle, FakeStore(accounts()))

        assertEquals("work", recreated.state.query)
        assertEquals(AccountSort.ACCOUNT_NAME, recreated.state.sort)
        assertEquals(listOf("2"), recreated.state.accounts.map { it.id })
    }

    @Test
    fun `delete requires confirmation`() {
        val store = FakeStore(accounts())
        val viewModel = AccountListViewModel(SavedStateHandle(), store)
        viewModel.requestDelete("1")
        assertEquals("1", viewModel.state.pendingDeleteId)
        assertEquals(0, store.deleted.size)
        viewModel.confirmDelete()
        assertEquals(listOf("1"), store.deleted)
        assertNull(viewModel.state.pendingDeleteId)
        assertEquals(listOf("2"), viewModel.state.accounts.map { it.id })
    }

    @Test
    fun `edit updates metadata without requesting plaintext secret`() {
        val store = FakeStore(accounts())
        val viewModel = AccountListViewModel(SavedStateHandle(), store, nowEpochMillis = { 123L })
        viewModel.editLabel("1", "Personal", "alice@example.com")
        assertEquals("Personal", store.updated.single().issuer)
        assertEquals(123L, store.updatedAt)
    }

    private fun accounts() = listOf(
        account("1", "GitHub", "alice@example.com"),
        account("2", "Work", "zoe@example.com"),
    )

    private fun account(id: String, issuer: String, accountName: String) = TotpAccount(
        id = id,
        issuer = issuer,
        accountName = accountName,
        secret = SecretReference(id),
        algorithm = TotpAlgorithm.SHA1,
        digits = 6,
        periodSeconds = 30,
    )

    private class FakeStore(initial: List<TotpAccount>) : AccountListStore {
        private var accounts = initial
        val deleted = mutableListOf<String>()
        val updated = mutableListOf<TotpAccount>()
        var updatedAt: Long? = null

        override fun load(): Result<List<TotpAccount>> = Result.success(accounts)

        override fun updateMetadata(account: TotpAccount, nowEpochMillis: Long): Result<Unit> {
            updated += account
            updatedAt = nowEpochMillis
            accounts = accounts.map { if (it.id == account.id) account else it }
            return Result.success(Unit)
        }

        override fun delete(id: String): Result<Unit> {
            deleted += id
            accounts = accounts.filterNot { it.id == id }
            return Result.success(Unit)
        }

        override fun otp(account: TotpAccount, epochSeconds: Long): Result<AccountOtp> =
            Result.success(AccountOtp("123456", account.periodSeconds - (epochSeconds % account.periodSeconds).toInt()))
    }
}
