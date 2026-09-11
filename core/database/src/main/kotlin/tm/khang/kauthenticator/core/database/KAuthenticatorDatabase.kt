package tm.khang.kauthenticator.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [AuthenticatorAccountEntity::class],
    version = 2,
    exportSchema = false,
)
abstract class KAuthenticatorDatabase : RoomDatabase() {
    abstract fun accountDao(): AuthenticatorAccountDao

    companion object {
        const val DEFAULT_NAME = "kauthenticator.db"

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE authenticator_accounts " +
                        "ADD COLUMN updated_at_epoch_millis INTEGER NOT NULL DEFAULT 0",
                )
            }
        }

        fun create(context: Context, name: String = DEFAULT_NAME): KAuthenticatorDatabase =
            Room.databaseBuilder(context, KAuthenticatorDatabase::class.java, name)
                .addMigrations(MIGRATION_1_2)
                .build()
    }
}
