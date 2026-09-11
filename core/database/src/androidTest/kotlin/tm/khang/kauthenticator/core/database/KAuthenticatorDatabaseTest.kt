package tm.khang.kauthenticator.core.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class KAuthenticatorDatabaseTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val dbName = "kauthenticator-test-${System.nanoTime()}.db"

    @After
    fun cleanup() {
        context.deleteDatabase(dbName)
    }

    @Test
    fun account_survivesDatabaseReopen() {
        var db = Room.databaseBuilder(context, KAuthenticatorDatabase::class.java, dbName)
            .addMigrations(KAuthenticatorDatabase.MIGRATION_1_2)
            .allowMainThreadQueries()
            .build()
        db.accountDao().insert(sampleEntity())
        db.close()

        db = Room.databaseBuilder(context, KAuthenticatorDatabase::class.java, dbName)
            .addMigrations(KAuthenticatorDatabase.MIGRATION_1_2)
            .allowMainThreadQueries()
            .build()
        val restored = db.accountDao().getById("id-1")
        db.close()

        assertEquals("GitHub", restored?.issuer)
        assertEquals(1, restored?.secretSchemaVersion)
    }

    @Test
    fun migration1To2_addsUpdatedTimestampWithoutLosingData() {
        val legacy = context.openOrCreateDatabase(dbName, Context.MODE_PRIVATE, null)
        legacy.execSQL(
            """
            CREATE TABLE IF NOT EXISTS authenticator_accounts (
                id TEXT NOT NULL PRIMARY KEY,
                issuer TEXT NOT NULL,
                account_name TEXT NOT NULL,
                algorithm TEXT NOT NULL,
                digits INTEGER NOT NULL,
                period_seconds INTEGER NOT NULL,
                secret_ciphertext BLOB NOT NULL,
                secret_iv BLOB NOT NULL,
                secret_schema_version INTEGER NOT NULL,
                created_at_epoch_millis INTEGER NOT NULL
            )
            """.trimIndent(),
        )
        legacy.execSQL(
            """
            INSERT INTO authenticator_accounts (
                id, issuer, account_name, algorithm, digits, period_seconds,
                secret_ciphertext, secret_iv, secret_schema_version, created_at_epoch_millis
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """.trimIndent(),
            arrayOf(
                "legacy-id", "GitHub", "legacy@example.com", "SHA1", 6, 30,
                byteArrayOf(1, 2, 3), ByteArray(12), 1, 42L,
            ),
        )
        legacy.version = 1
        legacy.close()

        val db = Room.databaseBuilder(context, KAuthenticatorDatabase::class.java, dbName)
            .addMigrations(KAuthenticatorDatabase.MIGRATION_1_2)
            .allowMainThreadQueries()
            .build()
        val migrated = db.accountDao().getById("legacy-id")
        db.close()

        assertEquals("legacy@example.com", migrated?.accountName)
        assertEquals(0L, migrated?.updatedAtEpochMillis)
    }

    private fun sampleEntity() = AuthenticatorAccountEntity(
        id = "id-1",
        issuer = "GitHub",
        accountName = "user@example.com",
        algorithm = "SHA1",
        digits = 6,
        periodSeconds = 30,
        secretCiphertext = byteArrayOf(1, 2, 3),
        secretIv = ByteArray(12) { it.toByte() },
        secretSchemaVersion = 1,
        createdAtEpochMillis = 1L,
        updatedAtEpochMillis = 1L,
    )
}
