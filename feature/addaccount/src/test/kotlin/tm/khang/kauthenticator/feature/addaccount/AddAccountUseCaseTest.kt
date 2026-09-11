package tm.khang.kauthenticator.feature.addaccount

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import tm.khang.kauthenticator.core.database.AccountRepositoryResult
import tm.khang.kauthenticator.core.model.TotpAccount

class AddAccountUseCaseTest {
    private val secret = "JBSWY3DPEHPK3PXP"

    @Test
    fun `valid QR supports SHA1 SHA256 and SHA512`() {
        listOf("SHA1", "SHA256", "SHA512").forEach { algorithm ->
            val store = FakeStore()
            val useCase = AddAccountUseCase(store, idFactory = { "id-$algorithm" }, nowEpochMillis = { 123L })

            val result = useCase.fromQr(
                "otpauth://totp/Example:user@example.com?secret=$secret&issuer=Example&algorithm=$algorithm",
            )

            assertEquals(EnrollmentResult.Added("id-$algorithm"), result)
            assertEquals(1, store.inserted.size)
            assertTrue(store.lastSecretWasNonEmpty)
        }
    }

    @Test
    fun `invalid QR never writes storage`() {
        val store = FakeStore()
        val useCase = AddAccountUseCase(store)

        val result = useCase.fromQr("https://example.com/not-otp")

        assertEquals(EnrollmentResult.InvalidQr, result)
        assertTrue(store.inserted.isEmpty())
        assertTrue(store.updated.isEmpty())
    }

    @Test
    fun `manual validation reports errors per field`() {
        val errors = ManualAccountValidator.validate(
            ManualAccountInput(
                issuer = "",
                accountName = "",
                secretBase32 = "***",
                digits = 7,
                periodSeconds = 0,
            ),
        )

        assertEquals(
            setOf(ManualField.Issuer, ManualField.AccountName, ManualField.Secret, ManualField.Digits, ManualField.Period),
            errors.map { it.field }.toSet(),
        )
    }

    @Test
    fun `duplicate can replace existing account`() {
        val store = FakeStore(
            accounts = mutableListOf(existingAccount("existing")),
        )
        val useCase = AddAccountUseCase(store, idFactory = { "new" }, nowEpochMillis = { 55L })
        val duplicate = useCase.fromManual(validManual()) as EnrollmentResult.Duplicate

        val result = useCase.resolveDuplicate(duplicate, DuplicateDecision.Replace)

        assertEquals(EnrollmentResult.Added("existing"), result)
        assertEquals("existing", store.updated.single().id)
        assertTrue(store.inserted.isEmpty())
    }

    @Test
    fun `duplicate can keep both with new id`() {
        val store = FakeStore(
            accounts = mutableListOf(existingAccount("existing")),
        )
        val useCase = AddAccountUseCase(store, idFactory = { "new" }, nowEpochMillis = { 55L })
        val duplicate = useCase.fromManual(validManual()) as EnrollmentResult.Duplicate

        val result = useCase.resolveDuplicate(duplicate, DuplicateDecision.KeepBoth)

        assertEquals(EnrollmentResult.Added("new"), result)
        assertEquals("new", store.inserted.single().id)
        assertTrue(store.updated.isEmpty())
    }

    private fun validManual() = ManualAccountInput(
        issuer = "Example",
        accountName = "user@example.com",
        secretBase32 = secret,
    )

    private fun existingAccount(id: String) = TotpAccount(
        id = id,
        issuer = "Example",
        accountName = "user@example.com",
        secret = tm.khang.kauthenticator.core.model.SecretReference(id),
        algorithm = tm.khang.kauthenticator.core.otp.TotpAlgorithm.SHA1,
        digits = 6,
        periodSeconds = 30,
    )

    private class FakeStore(
        private val accounts: MutableList<TotpAccount> = mutableListOf(),
    ) : EnrollmentStore {
        val inserted = mutableListOf<TotpAccount>()
        val updated = mutableListOf<TotpAccount>()
        var lastSecretWasNonEmpty = false

        override fun getAll(): AccountRepositoryResult<List<TotpAccount>> =
            AccountRepositoryResult.Success(accounts.toList())

        override fun insert(
            account: TotpAccount,
            plaintextSecret: ByteArray,
            nowEpochMillis: Long,
        ): AccountRepositoryResult<Unit> {
            lastSecretWasNonEmpty = plaintextSecret.isNotEmpty()
            inserted += account
            accounts += account
            return AccountRepositoryResult.Success(Unit)
        }

        override fun update(
            account: TotpAccount,
            plaintextSecret: ByteArray,
            nowEpochMillis: Long,
        ): AccountRepositoryResult<Unit> {
            lastSecretWasNonEmpty = plaintextSecret.isNotEmpty()
            updated += account
            accounts.replaceAll { if (it.id == account.id) account else it }
            return AccountRepositoryResult.Success(Unit)
        }
    }
}
