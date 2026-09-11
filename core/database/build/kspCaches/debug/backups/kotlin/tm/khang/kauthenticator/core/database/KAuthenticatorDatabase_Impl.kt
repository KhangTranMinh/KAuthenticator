package tm.khang.kauthenticator.core.database

import androidx.room.InvalidationTracker
import androidx.room.RoomOpenDelegate
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.room.util.TableInfo
import androidx.room.util.TableInfo.Companion.read
import androidx.room.util.dropFtsSyncTriggers
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import javax.`annotation`.processing.Generated
import kotlin.Lazy
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.MutableList
import kotlin.collections.MutableMap
import kotlin.collections.MutableSet
import kotlin.collections.Set
import kotlin.collections.mutableListOf
import kotlin.collections.mutableMapOf
import kotlin.collections.mutableSetOf
import kotlin.reflect.KClass

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class KAuthenticatorDatabase_Impl : KAuthenticatorDatabase() {
  private val _authenticatorAccountDao: Lazy<AuthenticatorAccountDao> = lazy {
    AuthenticatorAccountDao_Impl(this)
  }

  protected override fun createOpenDelegate(): RoomOpenDelegate {
    val _openDelegate: RoomOpenDelegate = object : RoomOpenDelegate(2,
        "fce61ac5bc7b6d12fb1a7d4cfa97607a", "16514323de7460b3ed7263f192f3b642") {
      public override fun createAllTables(connection: SQLiteConnection) {
        connection.execSQL("CREATE TABLE IF NOT EXISTS `authenticator_accounts` (`id` TEXT NOT NULL, `issuer` TEXT NOT NULL, `account_name` TEXT NOT NULL, `algorithm` TEXT NOT NULL, `digits` INTEGER NOT NULL, `period_seconds` INTEGER NOT NULL, `secret_ciphertext` BLOB NOT NULL, `secret_iv` BLOB NOT NULL, `secret_schema_version` INTEGER NOT NULL, `created_at_epoch_millis` INTEGER NOT NULL, `updated_at_epoch_millis` INTEGER NOT NULL DEFAULT 0, PRIMARY KEY(`id`))")
        connection.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)")
        connection.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, 'fce61ac5bc7b6d12fb1a7d4cfa97607a')")
      }

      public override fun dropAllTables(connection: SQLiteConnection) {
        connection.execSQL("DROP TABLE IF EXISTS `authenticator_accounts`")
      }

      public override fun onCreate(connection: SQLiteConnection) {
      }

      public override fun onOpen(connection: SQLiteConnection) {
        internalInitInvalidationTracker(connection)
      }

      public override fun onPreMigrate(connection: SQLiteConnection) {
        dropFtsSyncTriggers(connection)
      }

      public override fun onPostMigrate(connection: SQLiteConnection) {
      }

      public override fun onValidateSchema(connection: SQLiteConnection):
          RoomOpenDelegate.ValidationResult {
        val _columnsAuthenticatorAccounts: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsAuthenticatorAccounts.put("id", TableInfo.Column("id", "TEXT", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsAuthenticatorAccounts.put("issuer", TableInfo.Column("issuer", "TEXT", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsAuthenticatorAccounts.put("account_name", TableInfo.Column("account_name", "TEXT",
            true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsAuthenticatorAccounts.put("algorithm", TableInfo.Column("algorithm", "TEXT", true,
            0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsAuthenticatorAccounts.put("digits", TableInfo.Column("digits", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsAuthenticatorAccounts.put("period_seconds", TableInfo.Column("period_seconds",
            "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsAuthenticatorAccounts.put("secret_ciphertext", TableInfo.Column("secret_ciphertext",
            "BLOB", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsAuthenticatorAccounts.put("secret_iv", TableInfo.Column("secret_iv", "BLOB", true,
            0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsAuthenticatorAccounts.put("secret_schema_version",
            TableInfo.Column("secret_schema_version", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsAuthenticatorAccounts.put("created_at_epoch_millis",
            TableInfo.Column("created_at_epoch_millis", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsAuthenticatorAccounts.put("updated_at_epoch_millis",
            TableInfo.Column("updated_at_epoch_millis", "INTEGER", true, 0, "0",
            TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysAuthenticatorAccounts: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesAuthenticatorAccounts: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoAuthenticatorAccounts: TableInfo = TableInfo("authenticator_accounts",
            _columnsAuthenticatorAccounts, _foreignKeysAuthenticatorAccounts,
            _indicesAuthenticatorAccounts)
        val _existingAuthenticatorAccounts: TableInfo = read(connection, "authenticator_accounts")
        if (!_infoAuthenticatorAccounts.equals(_existingAuthenticatorAccounts)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |authenticator_accounts(tm.khang.kauthenticator.core.database.AuthenticatorAccountEntity).
              | Expected:
              |""".trimMargin() + _infoAuthenticatorAccounts + """
              |
              | Found:
              |""".trimMargin() + _existingAuthenticatorAccounts)
        }
        return RoomOpenDelegate.ValidationResult(true, null)
      }
    }
    return _openDelegate
  }

  protected override fun createInvalidationTracker(): InvalidationTracker {
    val _shadowTablesMap: MutableMap<String, String> = mutableMapOf()
    val _viewTables: MutableMap<String, Set<String>> = mutableMapOf()
    return InvalidationTracker(this, _shadowTablesMap, _viewTables, "authenticator_accounts")
  }

  public override fun clearAllTables() {
    super.performClear(false, "authenticator_accounts")
  }

  protected override fun getRequiredTypeConverterClasses(): Map<KClass<*>, List<KClass<*>>> {
    val _typeConvertersMap: MutableMap<KClass<*>, List<KClass<*>>> = mutableMapOf()
    _typeConvertersMap.put(AuthenticatorAccountDao::class,
        AuthenticatorAccountDao_Impl.getRequiredConverters())
    return _typeConvertersMap
  }

  public override fun getRequiredAutoMigrationSpecClasses(): Set<KClass<out AutoMigrationSpec>> {
    val _autoMigrationSpecsSet: MutableSet<KClass<out AutoMigrationSpec>> = mutableSetOf()
    return _autoMigrationSpecsSet
  }

  public override
      fun createAutoMigrations(autoMigrationSpecs: Map<KClass<out AutoMigrationSpec>, AutoMigrationSpec>):
      List<Migration> {
    val _autoMigrations: MutableList<Migration> = mutableListOf()
    return _autoMigrations
  }

  public override fun accountDao(): AuthenticatorAccountDao = _authenticatorAccountDao.value
}
