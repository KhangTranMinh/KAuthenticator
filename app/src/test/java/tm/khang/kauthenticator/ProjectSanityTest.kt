package tm.khang.kauthenticator

import org.junit.Assert.assertEquals
import org.junit.Test

class ProjectSanityTest {
    @Test
    fun packageName_isConfiguredAsExpected() {
        assertEquals("tm.khang.kauthenticator", BuildConfig.APPLICATION_ID)
    }
}
