package tm.khang.kauthenticator.core.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "authenticator_accounts")
data class AuthenticatorAccountEntity(
    @PrimaryKey val id: String,
    val issuer: String,
    @ColumnInfo(name = "account_name") val accountName: String,
    val algorithm: String,
    val digits: Int,
    @ColumnInfo(name = "period_seconds") val periodSeconds: Int,
    @ColumnInfo(name = "secret_ciphertext", typeAffinity = ColumnInfo.BLOB) val secretCiphertext: ByteArray,
    @ColumnInfo(name = "secret_iv", typeAffinity = ColumnInfo.BLOB) val secretIv: ByteArray,
    @ColumnInfo(name = "secret_schema_version") val secretSchemaVersion: Int,
    @ColumnInfo(name = "created_at_epoch_millis") val createdAtEpochMillis: Long,
    @ColumnInfo(name = "updated_at_epoch_millis", defaultValue = "0") val updatedAtEpochMillis: Long,
)
