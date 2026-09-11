package tm.khang.kauthenticator.core.database

import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performBlocking
import androidx.sqlite.SQLiteStatement
import javax.`annotation`.processing.Generated
import kotlin.ByteArray
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.mutableListOf
import kotlin.reflect.KClass

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class AuthenticatorAccountDao_Impl(
  __db: RoomDatabase,
) : AuthenticatorAccountDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfAuthenticatorAccountEntity:
      EntityInsertAdapter<AuthenticatorAccountEntity>

  private val __deleteAdapterOfAuthenticatorAccountEntity:
      EntityDeleteOrUpdateAdapter<AuthenticatorAccountEntity>

  private val __updateAdapterOfAuthenticatorAccountEntity:
      EntityDeleteOrUpdateAdapter<AuthenticatorAccountEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfAuthenticatorAccountEntity = object :
        EntityInsertAdapter<AuthenticatorAccountEntity>() {
      protected override fun createQuery(): String =
          "INSERT OR ABORT INTO `authenticator_accounts` (`id`,`issuer`,`account_name`,`algorithm`,`digits`,`period_seconds`,`secret_ciphertext`,`secret_iv`,`secret_schema_version`,`created_at_epoch_millis`,`updated_at_epoch_millis`) VALUES (?,?,?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: AuthenticatorAccountEntity) {
        statement.bindText(1, entity.id)
        statement.bindText(2, entity.issuer)
        statement.bindText(3, entity.accountName)
        statement.bindText(4, entity.algorithm)
        statement.bindLong(5, entity.digits.toLong())
        statement.bindLong(6, entity.periodSeconds.toLong())
        statement.bindBlob(7, entity.secretCiphertext)
        statement.bindBlob(8, entity.secretIv)
        statement.bindLong(9, entity.secretSchemaVersion.toLong())
        statement.bindLong(10, entity.createdAtEpochMillis)
        statement.bindLong(11, entity.updatedAtEpochMillis)
      }
    }
    this.__deleteAdapterOfAuthenticatorAccountEntity = object :
        EntityDeleteOrUpdateAdapter<AuthenticatorAccountEntity>() {
      protected override fun createQuery(): String =
          "DELETE FROM `authenticator_accounts` WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: AuthenticatorAccountEntity) {
        statement.bindText(1, entity.id)
      }
    }
    this.__updateAdapterOfAuthenticatorAccountEntity = object :
        EntityDeleteOrUpdateAdapter<AuthenticatorAccountEntity>() {
      protected override fun createQuery(): String =
          "UPDATE OR ABORT `authenticator_accounts` SET `id` = ?,`issuer` = ?,`account_name` = ?,`algorithm` = ?,`digits` = ?,`period_seconds` = ?,`secret_ciphertext` = ?,`secret_iv` = ?,`secret_schema_version` = ?,`created_at_epoch_millis` = ?,`updated_at_epoch_millis` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: AuthenticatorAccountEntity) {
        statement.bindText(1, entity.id)
        statement.bindText(2, entity.issuer)
        statement.bindText(3, entity.accountName)
        statement.bindText(4, entity.algorithm)
        statement.bindLong(5, entity.digits.toLong())
        statement.bindLong(6, entity.periodSeconds.toLong())
        statement.bindBlob(7, entity.secretCiphertext)
        statement.bindBlob(8, entity.secretIv)
        statement.bindLong(9, entity.secretSchemaVersion.toLong())
        statement.bindLong(10, entity.createdAtEpochMillis)
        statement.bindLong(11, entity.updatedAtEpochMillis)
        statement.bindText(12, entity.id)
      }
    }
  }

  public override fun insert(entity: AuthenticatorAccountEntity): Unit = performBlocking(__db,
      false, true) { _connection ->
    __insertAdapterOfAuthenticatorAccountEntity.insert(_connection, entity)
  }

  public override fun delete(entity: AuthenticatorAccountEntity): Unit = performBlocking(__db,
      false, true) { _connection ->
    __deleteAdapterOfAuthenticatorAccountEntity.handle(_connection, entity)
  }

  public override fun update(entity: AuthenticatorAccountEntity): Unit = performBlocking(__db,
      false, true) { _connection ->
    __updateAdapterOfAuthenticatorAccountEntity.handle(_connection, entity)
  }

  public override fun getAll(): List<AuthenticatorAccountEntity> {
    val _sql: String =
        "SELECT * FROM authenticator_accounts ORDER BY issuer COLLATE NOCASE, account_name COLLATE NOCASE"
    return performBlocking(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfIssuer: Int = getColumnIndexOrThrow(_stmt, "issuer")
        val _columnIndexOfAccountName: Int = getColumnIndexOrThrow(_stmt, "account_name")
        val _columnIndexOfAlgorithm: Int = getColumnIndexOrThrow(_stmt, "algorithm")
        val _columnIndexOfDigits: Int = getColumnIndexOrThrow(_stmt, "digits")
        val _columnIndexOfPeriodSeconds: Int = getColumnIndexOrThrow(_stmt, "period_seconds")
        val _columnIndexOfSecretCiphertext: Int = getColumnIndexOrThrow(_stmt, "secret_ciphertext")
        val _columnIndexOfSecretIv: Int = getColumnIndexOrThrow(_stmt, "secret_iv")
        val _columnIndexOfSecretSchemaVersion: Int = getColumnIndexOrThrow(_stmt,
            "secret_schema_version")
        val _columnIndexOfCreatedAtEpochMillis: Int = getColumnIndexOrThrow(_stmt,
            "created_at_epoch_millis")
        val _columnIndexOfUpdatedAtEpochMillis: Int = getColumnIndexOrThrow(_stmt,
            "updated_at_epoch_millis")
        val _result: MutableList<AuthenticatorAccountEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: AuthenticatorAccountEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpIssuer: String
          _tmpIssuer = _stmt.getText(_columnIndexOfIssuer)
          val _tmpAccountName: String
          _tmpAccountName = _stmt.getText(_columnIndexOfAccountName)
          val _tmpAlgorithm: String
          _tmpAlgorithm = _stmt.getText(_columnIndexOfAlgorithm)
          val _tmpDigits: Int
          _tmpDigits = _stmt.getLong(_columnIndexOfDigits).toInt()
          val _tmpPeriodSeconds: Int
          _tmpPeriodSeconds = _stmt.getLong(_columnIndexOfPeriodSeconds).toInt()
          val _tmpSecretCiphertext: ByteArray
          _tmpSecretCiphertext = _stmt.getBlob(_columnIndexOfSecretCiphertext)
          val _tmpSecretIv: ByteArray
          _tmpSecretIv = _stmt.getBlob(_columnIndexOfSecretIv)
          val _tmpSecretSchemaVersion: Int
          _tmpSecretSchemaVersion = _stmt.getLong(_columnIndexOfSecretSchemaVersion).toInt()
          val _tmpCreatedAtEpochMillis: Long
          _tmpCreatedAtEpochMillis = _stmt.getLong(_columnIndexOfCreatedAtEpochMillis)
          val _tmpUpdatedAtEpochMillis: Long
          _tmpUpdatedAtEpochMillis = _stmt.getLong(_columnIndexOfUpdatedAtEpochMillis)
          _item =
              AuthenticatorAccountEntity(_tmpId,_tmpIssuer,_tmpAccountName,_tmpAlgorithm,_tmpDigits,_tmpPeriodSeconds,_tmpSecretCiphertext,_tmpSecretIv,_tmpSecretSchemaVersion,_tmpCreatedAtEpochMillis,_tmpUpdatedAtEpochMillis)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getById(id: String): AuthenticatorAccountEntity? {
    val _sql: String = "SELECT * FROM authenticator_accounts WHERE id = ? LIMIT 1"
    return performBlocking(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, id)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfIssuer: Int = getColumnIndexOrThrow(_stmt, "issuer")
        val _columnIndexOfAccountName: Int = getColumnIndexOrThrow(_stmt, "account_name")
        val _columnIndexOfAlgorithm: Int = getColumnIndexOrThrow(_stmt, "algorithm")
        val _columnIndexOfDigits: Int = getColumnIndexOrThrow(_stmt, "digits")
        val _columnIndexOfPeriodSeconds: Int = getColumnIndexOrThrow(_stmt, "period_seconds")
        val _columnIndexOfSecretCiphertext: Int = getColumnIndexOrThrow(_stmt, "secret_ciphertext")
        val _columnIndexOfSecretIv: Int = getColumnIndexOrThrow(_stmt, "secret_iv")
        val _columnIndexOfSecretSchemaVersion: Int = getColumnIndexOrThrow(_stmt,
            "secret_schema_version")
        val _columnIndexOfCreatedAtEpochMillis: Int = getColumnIndexOrThrow(_stmt,
            "created_at_epoch_millis")
        val _columnIndexOfUpdatedAtEpochMillis: Int = getColumnIndexOrThrow(_stmt,
            "updated_at_epoch_millis")
        val _result: AuthenticatorAccountEntity?
        if (_stmt.step()) {
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpIssuer: String
          _tmpIssuer = _stmt.getText(_columnIndexOfIssuer)
          val _tmpAccountName: String
          _tmpAccountName = _stmt.getText(_columnIndexOfAccountName)
          val _tmpAlgorithm: String
          _tmpAlgorithm = _stmt.getText(_columnIndexOfAlgorithm)
          val _tmpDigits: Int
          _tmpDigits = _stmt.getLong(_columnIndexOfDigits).toInt()
          val _tmpPeriodSeconds: Int
          _tmpPeriodSeconds = _stmt.getLong(_columnIndexOfPeriodSeconds).toInt()
          val _tmpSecretCiphertext: ByteArray
          _tmpSecretCiphertext = _stmt.getBlob(_columnIndexOfSecretCiphertext)
          val _tmpSecretIv: ByteArray
          _tmpSecretIv = _stmt.getBlob(_columnIndexOfSecretIv)
          val _tmpSecretSchemaVersion: Int
          _tmpSecretSchemaVersion = _stmt.getLong(_columnIndexOfSecretSchemaVersion).toInt()
          val _tmpCreatedAtEpochMillis: Long
          _tmpCreatedAtEpochMillis = _stmt.getLong(_columnIndexOfCreatedAtEpochMillis)
          val _tmpUpdatedAtEpochMillis: Long
          _tmpUpdatedAtEpochMillis = _stmt.getLong(_columnIndexOfUpdatedAtEpochMillis)
          _result =
              AuthenticatorAccountEntity(_tmpId,_tmpIssuer,_tmpAccountName,_tmpAlgorithm,_tmpDigits,_tmpPeriodSeconds,_tmpSecretCiphertext,_tmpSecretIv,_tmpSecretSchemaVersion,_tmpCreatedAtEpochMillis,_tmpUpdatedAtEpochMillis)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
