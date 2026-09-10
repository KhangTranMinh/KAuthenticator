package tm.khang.kauthenticator

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test
import tm.khang.kauthenticator.ui.theme.KAuthenticatorTheme

class KAuthenticatorAppTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun appName_isDisplayed() {
        composeTestRule.setContent {
            KAuthenticatorTheme {
                KAuthenticatorApp()
            }
        }

        composeTestRule.onNodeWithText("KAuthenticator").assertIsDisplayed()
    }
}
