package tm.khang.kauthenticator.feature.accounts

import org.junit.Assert.assertEquals
import org.junit.Test

class AccountSortPersistenceTest {
    @Test
    fun persistenceValues_areStableAndBackwardCompatible() {
        assertEquals(AccountSort.ISSUER, AccountSort.fromPersistenceValue("issuer"))
        assertEquals(AccountSort.ACCOUNT_NAME, AccountSort.fromPersistenceValue("account_name"))
        assertEquals(AccountSort.ACCOUNT_NAME, AccountSort.fromPersistenceValue("ACCOUNT_NAME"))
    }
}
