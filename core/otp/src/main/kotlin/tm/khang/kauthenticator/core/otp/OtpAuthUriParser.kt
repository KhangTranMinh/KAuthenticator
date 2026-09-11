package tm.khang.kauthenticator.core.otp

import java.net.URI
import java.net.URLDecoder
import java.nio.charset.StandardCharsets


data class ParsedTotpAccount(
    val issuer: String,
    val accountName: String,
    val secret: OtpSecret,
    val algorithm: TotpAlgorithm,
    val digits: Int,
    val periodSeconds: Int,
)

object OtpAuthUriParser {
    fun parse(value: String): OtpResult<ParsedTotpAccount> {
        val uri = try {
            URI(value)
        } catch (_: Exception) {
            return OtpResult.Failure(OtpError.InvalidOtpAuthUri)
        }

        if (!uri.scheme.equals("otpauth", ignoreCase = true)) {
            return OtpResult.Failure(OtpError.InvalidOtpAuthUri)
        }
        if (!uri.host.equals("totp", ignoreCase = true)) {
            return OtpResult.Failure(OtpError.UnsupportedOtpType(uri.host))
        }

        val rawLabel = uri.rawPath?.removePrefix("/").orEmpty()
        if (rawLabel.isEmpty()) return OtpResult.Failure(OtpError.MissingAccountName)
        val label = decodeComponent(rawLabel) ?: return OtpResult.Failure(OtpError.InvalidOtpAuthUri)
        val (labelIssuer, accountName) = splitLabel(label)
        if (accountName.isBlank()) return OtpResult.Failure(OtpError.MissingAccountName)

        val query = parseQuery(uri.rawQuery) ?: return OtpResult.Failure(OtpError.InvalidOtpAuthUri)
        val secretText = query["secret"]?.lastOrNull()?.takeIf { it.isNotBlank() }
            ?: return OtpResult.Failure(OtpError.MissingSecret)
        val secret = when (val decoded = OtpSecret.fromBase32(secretText)) {
            is OtpResult.Success -> decoded.value
            is OtpResult.Failure -> return decoded
        }

        val queryIssuer = query["issuer"]?.lastOrNull()?.trim().orEmpty()
        if (labelIssuer.isNotBlank() && queryIssuer.isNotBlank() && labelIssuer != queryIssuer) {
            return OtpResult.Failure(OtpError.IssuerMismatch(labelIssuer, queryIssuer))
        }
        val issuer = queryIssuer.ifBlank { labelIssuer }

        val algorithmText = query["algorithm"]?.lastOrNull()?.ifBlank { "SHA1" } ?: "SHA1"
        val algorithm = when (algorithmText.uppercase().replace("-", "")) {
            "SHA1" -> TotpAlgorithm.SHA1
            "SHA256" -> TotpAlgorithm.SHA256
            "SHA512" -> TotpAlgorithm.SHA512
            else -> return OtpResult.Failure(OtpError.UnsupportedAlgorithm(algorithmText))
        }

        val digits = parsePositiveInt(query["digits"]?.lastOrNull(), default = 6)
            ?: return OtpResult.Failure(OtpError.UnsupportedDigits(null))
        if (digits != 6 && digits != 8) {
            return OtpResult.Failure(OtpError.UnsupportedDigits(digits))
        }

        val period = parsePositiveInt(query["period"]?.lastOrNull(), default = 30)
            ?: return OtpResult.Failure(OtpError.InvalidPeriod(null))
        if (period <= 0) return OtpResult.Failure(OtpError.InvalidPeriod(period))

        return OtpResult.Success(
            ParsedTotpAccount(
                issuer = issuer,
                accountName = accountName,
                secret = secret,
                algorithm = algorithm,
                digits = digits,
                periodSeconds = period,
            ),
        )
    }

    private fun splitLabel(label: String): Pair<String, String> {
        val separator = label.indexOf(':')
        if (separator < 0) return "" to label.trim()
        return label.substring(0, separator).trim() to label.substring(separator + 1).trim()
    }

    private fun parseQuery(rawQuery: String?): Map<String, List<String>>? {
        if (rawQuery.isNullOrEmpty()) return emptyMap()
        val result = linkedMapOf<String, MutableList<String>>()
        for (part in rawQuery.split('&')) {
            if (part.isEmpty()) continue
            val separator = part.indexOf('=')
            val rawKey = if (separator >= 0) part.substring(0, separator) else part
            val rawValue = if (separator >= 0) part.substring(separator + 1) else ""
            val key = decodeComponent(rawKey)?.lowercase() ?: return null
            val value = decodeComponent(rawValue) ?: return null
            result.getOrPut(key) { mutableListOf() }.add(value)
        }
        return result
    }

    private fun parsePositiveInt(value: String?, default: Int): Int? {
        if (value == null) return default
        return value.toIntOrNull()
    }

    private fun decodeComponent(value: String): String? = try {
        URLDecoder.decode(value.replace("+", "%2B"), StandardCharsets.UTF_8.name())
    } catch (_: IllegalArgumentException) {
        null
    }
}
