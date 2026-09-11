package tm.khang.kauthenticator.feature.accounts

import org.junit.Assert.assertEquals
import org.junit.Test

class CountdownTest {
    @Test
    fun `countdown is derived from timestamp around period boundary`() {
        assertEquals(2, secondsRemainingAt(epochSeconds = 28, periodSeconds = 30))
        assertEquals(1, secondsRemainingAt(epochSeconds = 29, periodSeconds = 30))
        assertEquals(30, secondsRemainingAt(epochSeconds = 30, periodSeconds = 30))
        assertEquals(29, secondsRemainingAt(epochSeconds = 31, periodSeconds = 30))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `invalid period is rejected`() {
        secondsRemainingAt(epochSeconds = 30, periodSeconds = 0)
    }
}
