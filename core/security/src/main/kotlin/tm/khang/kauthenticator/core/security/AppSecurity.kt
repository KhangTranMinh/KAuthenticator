package tm.khang.kauthenticator.core.security

import android.app.Activity
import android.app.KeyguardManager
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.WindowManager

interface DeviceCredentialGate {
    fun isDeviceSecure(): Boolean
}

class AndroidDeviceCredentialGate(
    context: Context,
) : DeviceCredentialGate {
    private val keyguardManager = context.getSystemService(KeyguardManager::class.java)

    override fun isDeviceSecure(): Boolean = keyguardManager?.isDeviceSecure == true
}

object ScreenshotProtection {
    fun apply(activity: Activity, enabled: Boolean) {
        if (enabled) {
            activity.window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        } else {
            activity.window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }
}

class SensitiveClipboard(
    private val context: Context,
    private val handler: Handler = Handler(Looper.getMainLooper()),
) {
    private val clipboard = context.getSystemService(ClipboardManager::class.java)

    fun copyAndScheduleClear(
        label: String,
        value: String,
        clearAfterMillis: Long = DEFAULT_CLEAR_AFTER_MILLIS,
    ) {
        require(clearAfterMillis >= 0L) { "clearAfterMillis must be non-negative" }
        val clip = android.content.ClipData.newPlainText(label, value)
        clipboard?.setPrimaryClip(clip)
        handler.postDelayed({ clearIfUnchanged(value) }, clearAfterMillis)
    }

    private fun clearIfUnchanged(expected: String) {
        val manager = clipboard ?: return
        val current = manager.primaryClip
            ?.takeIf { it.itemCount > 0 }
            ?.getItemAt(0)
            ?.coerceToText(context)
            ?.toString()
        if (current == expected) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                manager.clearPrimaryClip()
            } else {
                manager.setPrimaryClip(android.content.ClipData.newPlainText("", ""))
            }
        }
    }

    companion object {
        const val DEFAULT_CLEAR_AFTER_MILLIS = 30_000L
    }
}
