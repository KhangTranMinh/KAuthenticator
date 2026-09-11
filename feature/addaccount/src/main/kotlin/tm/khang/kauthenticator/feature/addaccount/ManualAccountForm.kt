package tm.khang.kauthenticator.feature.addaccount

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import tm.khang.kauthenticator.core.otp.TotpAlgorithm

@Composable
fun ManualAccountForm(
    onSubmit: (ManualAccountInput) -> Unit,
    modifier: Modifier = Modifier,
) {
    var issuer by remember { mutableStateOf("") }
    var accountName by remember { mutableStateOf("") }
    var secret by remember { mutableStateOf("") }
    var digits by remember { mutableStateOf("6") }
    var period by remember { mutableStateOf("30") }
    var algorithm by remember { mutableStateOf(TotpAlgorithm.SHA1) }
    var errors by remember { mutableStateOf(emptyList<ManualValidationError>()) }

    fun error(field: ManualField): String? = errors.firstOrNull { it.field == field }?.message

    Column(modifier = modifier.padding(16.dp)) {
        OutlinedTextField(
            value = issuer,
            onValueChange = { issuer = it },
            label = { Text("Issuer") },
            isError = error(ManualField.Issuer) != null,
            supportingText = { error(ManualField.Issuer)?.let { Text(it) } },
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = accountName,
            onValueChange = { accountName = it },
            label = { Text("Account name") },
            isError = error(ManualField.AccountName) != null,
            supportingText = { error(ManualField.AccountName)?.let { Text(it) } },
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = secret,
            onValueChange = { secret = it },
            label = { Text("Base32 secret") },
            isError = error(ManualField.Secret) != null,
            supportingText = { error(ManualField.Secret)?.let { Text(it) } },
            modifier = Modifier.fillMaxWidth(),
        )
        Text("Algorithm")
        Row {
            TotpAlgorithm.entries.forEach { option ->
                FilterChip(
                    selected = algorithm == option,
                    onClick = { algorithm = option },
                    label = { Text(option.name) },
                    modifier = Modifier.padding(end = 8.dp),
                )
            }
        }
        OutlinedTextField(
            value = digits,
            onValueChange = { digits = it.filter(Char::isDigit) },
            label = { Text("Digits (6 or 8)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = error(ManualField.Digits) != null,
            supportingText = { error(ManualField.Digits)?.let { Text(it) } },
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = period,
            onValueChange = { period = it.filter(Char::isDigit) },
            label = { Text("Period seconds") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = error(ManualField.Period) != null,
            supportingText = { error(ManualField.Period)?.let { Text(it) } },
            modifier = Modifier.fillMaxWidth(),
        )
        Button(
            onClick = {
                val input = ManualAccountInput(
                    issuer = issuer,
                    accountName = accountName,
                    secretBase32 = secret,
                    algorithm = algorithm,
                    digits = digits.toIntOrNull() ?: 0,
                    periodSeconds = period.toIntOrNull() ?: 0,
                )
                errors = ManualAccountValidator.validate(input)
                if (errors.isEmpty()) onSubmit(input)
            },
        ) {
            Text("Add account")
        }
    }
}
