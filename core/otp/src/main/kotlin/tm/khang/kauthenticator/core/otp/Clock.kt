package tm.khang.kauthenticator.core.otp

fun interface Clock {
    fun epochSeconds(): Long
}

object SystemClock : Clock {
    override fun epochSeconds(): Long = System.currentTimeMillis() / 1_000L
}
