package tm.khang.kauthenticator.feature.accounts

internal fun secondsRemainingAt(epochSeconds: Long, periodSeconds: Int): Int {
    require(epochSeconds >= 0L) { "Epoch seconds must be non-negative" }
    require(periodSeconds > 0) { "Period must be positive" }
    val elapsed = (epochSeconds % periodSeconds).toInt()
    return periodSeconds - elapsed
}
