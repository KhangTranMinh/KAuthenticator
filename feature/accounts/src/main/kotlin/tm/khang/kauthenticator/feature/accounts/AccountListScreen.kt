package tm.khang.kauthenticator.feature.accounts

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import tm.khang.kauthenticator.core.model.TotpAccount
import tm.khang.kauthenticator.core.security.SensitiveClipboard

@Composable
fun AccountListScreen(
    state: AccountListUiState,
    otpProvider: suspend (TotpAccount, Long) -> Result<AccountOtp>,
    onEdit: (String, String, String) -> Unit,
    onDeleteRequest: (String) -> Unit,
    onDeleteConfirm: () -> Unit,
    onDeleteCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
    ) {
        Spacer(Modifier.height(12.dp))

        when {
            state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }

            state.accounts.isEmpty() -> EmptyAccounts(
                modifier = Modifier.fillMaxSize(),
            )

            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(items = state.accounts, key = { it.id }) { account ->
                    AccountRow(
                        account = account,
                        otpProvider = otpProvider,
                        onEdit = onEdit,
                        onDeleteRequest = onDeleteRequest,
                    )
                }
                item { Spacer(Modifier.height(88.dp)) }
            }
        }
    }

    if (state.pendingDeleteId != null) {
        AlertDialog(
            onDismissRequest = onDeleteCancel,
            title = { Text("Delete authenticator?") },
            text = { Text("This account and its encrypted secret will be permanently removed from this device.") },
            confirmButton = { TextButton(onClick = onDeleteConfirm) { Text("Delete") } },
            dismissButton = { TextButton(onClick = onDeleteCancel) { Text("Cancel") } },
        )
    }
}

@Composable
private fun EmptyAccounts(
    modifier: Modifier = Modifier,
) {
    Box(modifier, contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(32.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "•••",
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
            }
            Text(
                text = "No accounts yet",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "Tap the add button to scan a QR code or enter a setup key.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun AccountRow(
    account: TotpAccount,
    otpProvider: suspend (TotpAccount, Long) -> Result<AccountOtp>,
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
    val otp by produceState<AccountOtp?>(initialValue = null, account.id, timeStep) {
        value = otpProvider(account, epochSeconds).getOrNull()
    }
    val secondsRemaining = secondsRemainingAt(epochSeconds, account.periodSeconds)
    val progress = secondsRemaining.toFloat() / account.periodSeconds.toFloat()

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
    ) {
        Column(Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        account.issuer.ifBlank { "Authenticator" },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        account.accountName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                FilledTonalIconButton(onClick = { editing = true }) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit account",
                    )
                }
            }

            Spacer(Modifier.height(18.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = otp?.code?.chunked(3)?.joinToString(" ") ?: "--- ---",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 34.sp,
                    lineHeight = 38.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .semantics { contentDescription = "One-time password ${otp?.code ?: "unavailable"}" }
                        .clickable(enabled = otp != null) {
                            otp?.let { clipboard.copyAndScheduleClear("TOTP code", it.code) }
                        },
                )
                Text(
                    text = "${secondsRemaining}s",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.semantics {
                        contentDescription = "$secondsRemaining seconds until code refresh"
                    },
                )
            }

            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 14.dp),
            )

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                horizontalArrangement = Arrangement.End,
            ) {
                FilledTonalIconButton(onClick = { onDeleteRequest(account.id) }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete account",
                    )
                }
            }
        }
    }

    if (editing) {
        AlertDialog(
            onDismissRequest = { editing = false },
            title = { Text("Edit account") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = issuer,
                        onValueChange = { issuer = it },
                        label = { Text("Issuer") },
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = accountName,
                        onValueChange = { accountName = it },
                        label = { Text("Account name") },
                        singleLine = true,
                    )
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
