package tm.khang.kauthenticator.feature.accounts

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import tm.khang.kauthenticator.core.model.TotpAccount
import tm.khang.kauthenticator.core.security.SensitiveClipboard

@Composable
fun AccountListScreen(
    state: AccountListUiState,
    otpProvider: (TotpAccount, Long) -> Result<AccountOtp>,
    onQueryChange: (String) -> Unit,
    onSortChange: (AccountSort) -> Unit,
    onEdit: (String, String, String) -> Unit,
    onDeleteRequest: (String) -> Unit,
    onDeleteConfirm: () -> Unit,
    onDeleteCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.padding(16.dp)) {
        OutlinedTextField(
            value = state.query,
            onValueChange = onQueryChange,
            label = { Text("Search") },
            modifier = Modifier.fillMaxWidth(),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = { onSortChange(AccountSort.ISSUER) }) { Text("Issuer") }
            TextButton(onClick = { onSortChange(AccountSort.ACCOUNT_NAME) }) { Text("Account") }
        }

        when {
            state.isLoading -> CircularProgressIndicator()
            state.accounts.isEmpty() -> EmptyAccounts()
            else -> LazyColumn {
                items(items = state.accounts, key = { it.id }) { account ->
                    AccountRow(
                        account = account,
                        otpProvider = otpProvider,
                        onEdit = onEdit,
                        onDeleteRequest = onDeleteRequest,
                    )
                }
            }
        }
    }

    if (state.pendingDeleteId != null) {
        AlertDialog(
            onDismissRequest = onDeleteCancel,
            title = { Text("Delete authenticator?") },
            text = { Text("This removes the account and its encrypted secret from this device.") },
            confirmButton = { TextButton(onClick = onDeleteConfirm) { Text("Delete") } },
            dismissButton = { TextButton(onClick = onDeleteCancel) { Text("Cancel") } },
        )
    }
}

@Composable
private fun EmptyAccounts() {
    Column(Modifier.padding(top = 32.dp)) {
        Text("No authenticator accounts", style = MaterialTheme.typography.titleMedium)
        Text("Scan a QR code or enter a secret manually to add one.")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AccountRow(
    account: TotpAccount,
    otpProvider: (TotpAccount, Long) -> Result<AccountOtp>,
    onEdit: (String, String, String) -> Unit,
    onDeleteRequest: (String) -> Unit,
) {
    val context = LocalContext.current
    val clipboard = remember(context) { SensitiveClipboard(context.applicationContext) }
    var epochSeconds by remember(account.id) { mutableStateOf(System.currentTimeMillis() / 1_000L) }
    var editing by remember(account.id) { mutableStateOf(false) }
    var issuer by remember(account.id, account.issuer) { mutableStateOf(account.issuer) }
    var accountName by remember(account.id, account.accountName) { mutableStateOf(account.accountName) }

    LaunchedEffect(account.id) {
        while (true) {
            epochSeconds = System.currentTimeMillis() / 1_000L
            delay(1_000L - (System.currentTimeMillis() % 1_000L))
        }
    }

    val timeStep = epochSeconds / account.periodSeconds
    val otp = remember(account, timeStep) {
        otpProvider(account, epochSeconds).getOrNull()
    }
    val secondsRemaining = secondsRemainingAt(epochSeconds, account.periodSeconds)

    Card(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Column(Modifier.padding(16.dp)) {
            Text(account.issuer.ifBlank { "Authenticator" }, style = MaterialTheme.typography.titleMedium)
            Text(account.accountName)
            Spacer(Modifier.height(8.dp))
            Text(
                text = otp?.code ?: "------",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier
                    .semantics { contentDescription = "One-time password ${otp?.code ?: "unavailable"}" }
                    .clickable(enabled = otp != null) {
                        otp?.let { clipboard.copyAndScheduleClear("TOTP code", it.code) }
                    },
            )
            Text(
                text = "${secondsRemaining}s",
                modifier = Modifier.semantics {
                    contentDescription = "$secondsRemaining seconds until code refresh"
                },
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = { editing = true }) { Text("Edit") }
                TextButton(onClick = { onDeleteRequest(account.id) }) { Text("Delete") }
            }
        }
    }

    if (editing) {
        AlertDialog(
            onDismissRequest = { editing = false },
            title = { Text("Edit account") },
            text = {
                Column {
                    OutlinedTextField(value = issuer, onValueChange = { issuer = it }, label = { Text("Issuer") })
                    OutlinedTextField(value = accountName, onValueChange = { accountName = it }, label = { Text("Account name") })
                }
            },
            confirmButton = {
                Button(
                    enabled = accountName.isNotBlank(),
                    onClick = {
                        onEdit(account.id, issuer, accountName)
                        editing = false
                    },
                ) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { editing = false }) { Text("Cancel") } },
        )
    }
}
