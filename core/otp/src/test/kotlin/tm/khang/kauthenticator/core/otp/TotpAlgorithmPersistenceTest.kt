package tm.khang.kauthenticator.core.otp

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TotpAlgorithmPersistenceTest {
    @Test
    fun persistenceValues_areStableAndBackwardCompatible() {
        assertEquals(TotpAlgorithm.SHA1, TotpAlgorithm.fromPersistenceValue("sha1"))
        assertEquals(TotpAlgorithm.SHA256, TotpAlgorithm.fromPersistenceValue("sha256"))
        assertEquals(TotpAlgorithm.SHA512, TotpAlgorithm.fromPersistenceValue("sha512"))
        assertEquals(TotpAlgorithm.SHA256, TotpAlgorithm.fromPersistenceValue("SHA256"))
        assertNull(TotpAlgorithm.fromPersistenceValue("md5"))
    }
}
