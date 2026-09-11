package tm.khang.kauthenticator.feature.settings

import org.junit.Assert.assertEquals
import org.junit.Test

class AppThemePersistenceTest {
    @Test
    fun persistenceValues_areStableAndBackwardCompatible() {
        assertEquals(AppTheme.SYSTEM, AppTheme.fromPersistenceValue("system"))
        assertEquals(AppTheme.LIGHT, AppTheme.fromPersistenceValue("light"))
        assertEquals(AppTheme.DARK, AppTheme.fromPersistenceValue("dark"))
        assertEquals(AppTheme.DARK, AppTheme.fromPersistenceValue("DARK"))
    }
}
