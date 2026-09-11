package tm.khang.kauthenticator.feature.settings

import android.content.Context
import android.provider.Settings

fun interface AutomaticTimeStatusReader {
    fun isAutomaticTimeEnabled(): Boolean
}

class AndroidAutomaticTimeStatusReader(
    private val context: Context,
) : AutomaticTimeStatusReader {
    override fun isAutomaticTimeEnabled(): Boolean = runCatching {
        Settings.Global.getInt(context.contentResolver, Settings.Global.AUTO_TIME) == 1
    }.getOrDefault(false)
}
