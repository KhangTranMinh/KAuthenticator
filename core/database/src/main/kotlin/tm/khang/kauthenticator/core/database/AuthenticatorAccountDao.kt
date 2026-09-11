package tm.khang.kauthenticator.core.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface AuthenticatorAccountDao {
    @Query("SELECT * FROM authenticator_accounts ORDER BY issuer COLLATE NOCASE, account_name COLLATE NOCASE")
    fun getAll(): List<AuthenticatorAccountEntity>

    @Query("SELECT * FROM authenticator_accounts WHERE id = :id LIMIT 1")
    fun getById(id: String): AuthenticatorAccountEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insert(entity: AuthenticatorAccountEntity)

    @Update
    fun update(entity: AuthenticatorAccountEntity)

    @Delete
    fun delete(entity: AuthenticatorAccountEntity)
}
