package tm.khang.kauthenticator.feature.addaccount

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
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

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                "Enter setup key",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                "Use the information provided by the service you want to protect.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        ElevatedCard(shape = RoundedCornerShape(22.dp)) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                OutlinedTextField(
                    value = issuer,
                    onValueChange = { issuer = it },
                    label = { Text("Issuer") },
                    placeholder = { Text("Example: GitHub") },
                    singleLine = true,
                    isError = error(ManualField.Issuer) != null,
                    supportingText = { error(ManualField.Issuer)?.let { Text(it) } },
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = accountName,
                    onValueChange = { accountName = it },
                    label = { Text("Account") },
                    placeholder = { Text("you@example.com") },
                    singleLine = true,
                    isError = error(ManualField.AccountName) != null,
                    supportingText = { error(ManualField.AccountName)?.let { Text(it) } },
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = secret,
                    onValueChange = { secret = it.uppercase().replace(" ", "") },
                    label = { Text("Setup key") },
                    placeholder = { Text("Base32 secret") },
                    singleLine = true,
                    isError = error(ManualField.Secret) != null,
                    supportingText = { error(ManualField.Secret)?.let { Text(it) } },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        Text("Advanced", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)

        ElevatedCard(shape = RoundedCornerShape(22.dp)) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text("Algorithm", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TotpAlgorithm.entries.forEach { option ->
                        FilterChip(
                            selected = algorithm == option,
                            onClick = { algorithm = option },
                            label = { Text(option.name) },
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = digits,
                        onValueChange = { digits = it.filter(Char::isDigit) },
                        label = { Text("Digits") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = error(ManualField.Digits) != null,
                        supportingText = { error(ManualField.Digits)?.let { Text(it) } },
                        modifier = Modifier.weight(1f),
                    )
                    OutlinedTextField(
                        value = period,
                        onValueChange = { period = it.filter(Char::isDigit) },
                        label = { Text("Period") },
                        suffix = { Text("sec") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = error(ManualField.Period) != null,
                        supportingText = { error(ManualField.Period)?.let { Text(it) } },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }

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
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
        ) {
            Text("Add account", modifier = Modifier.padding(vertical = 6.dp))
        }
    }
}
