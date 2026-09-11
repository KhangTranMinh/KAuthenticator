package tm.khang.kauthenticator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tm.khang.kauthenticator.core.database.EncryptedAccountRepository
import tm.khang.kauthenticator.core.database.KAuthenticatorDatabase
import tm.khang.kauthenticator.core.model.TotpAccount
import tm.khang.kauthenticator.core.security.AndroidDeviceCredentialGate
import tm.khang.kauthenticator.core.security.AndroidKeystoreSecretCipher
import tm.khang.kauthenticator.core.security.ScreenshotProtection
import tm.khang.kauthenticator.feature.accounts.AccountListUiState
import tm.khang.kauthenticator.feature.accounts.AccountListScreen
import tm.khang.kauthenticator.feature.accounts.AccountSort
import tm.khang.kauthenticator.feature.accounts.EncryptedAccountListStore
import tm.khang.kauthenticator.feature.addaccount.AddAccountUseCase
import tm.khang.kauthenticator.feature.addaccount.DuplicateDecision
import tm.khang.kauthenticator.feature.addaccount.EnrollmentResult
import tm.khang.kauthenticator.feature.addaccount.ManualAccountForm
import tm.khang.kauthenticator.feature.addaccount.QrScanner
import tm.khang.kauthenticator.feature.addaccount.RepositoryEnrollmentStore
import tm.khang.kauthenticator.feature.settings.AndroidAutomaticTimeStatusReader
import tm.khang.kauthenticator.feature.settings.AppSettings
import tm.khang.kauthenticator.feature.settings.AppTheme
import tm.khang.kauthenticator.feature.settings.SettingsScreen
import tm.khang.kauthenticator.feature.settings.SettingsUiState
import tm.khang.kauthenticator.feature.settings.SharedPreferencesSettingsStore
import tm.khang.kauthenticator.ui.theme.KAuthenticatorTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KAuthenticatorRoot(activity = this)
        }
    }
}

private enum class AppDestination {
    ACCOUNTS,
    ADD_ACCOUNT,
    SETTINGS,
}

@Composable
private fun KAuthenticatorRoot(activity: ComponentActivity) {
    val context = LocalContext.current.applicationContext
    val settingsStore = remember(context) { SharedPreferencesSettingsStore(context) }
    var settings by remember { mutableStateOf(settingsStore.read()) }

    val darkTheme = when (settings.theme) {
        AppTheme.SYSTEM -> isSystemInDarkTheme()
        AppTheme.LIGHT -> false
        AppTheme.DARK -> true
    }

    LaunchedEffect(settings.screenshotProtectionEnabled) {
        ScreenshotProtection.apply(activity, settings.screenshotProtectionEnabled)
    }

    KAuthenticatorTheme(darkTheme = darkTheme) {
        KAuthenticatorApp(
            settings = settings,
            onSettingsChanged = {
                settingsStore.write(it)
                settings = it
            },
        )
    }
}

@Composable
fun KAuthenticatorApp(
    settings: AppSettings = AppSettings(),
    onSettingsChanged: (AppSettings) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current.applicationContext
    val scope = rememberCoroutineScope()
    val database = remember(context) { KAuthenticatorDatabase.create(context) }
    val repository = remember(database) {
        EncryptedAccountRepository(database.accountDao(), AndroidKeystoreSecretCipher())
    }
    val accountStore = remember(repository) { EncryptedAccountListStore(repository) }
    val addAccountUseCase = remember(repository) {
        AddAccountUseCase(RepositoryEnrollmentStore(repository))
    }
    val credentialGate = remember(context) { AndroidDeviceCredentialGate(context) }
    val automaticTimeReader = remember(context) { AndroidAutomaticTimeStatusReader(context) }

    var destination by remember { mutableStateOf(AppDestination.ACCOUNTS) }
    var sourceAccounts by remember { mutableStateOf<List<TotpAccount>>(emptyList()) }
    var accountState by remember { mutableStateOf(AccountListUiState()) }
    var duplicate by remember { mutableStateOf<EnrollmentResult.Duplicate?>(null) }
    var enrollmentMessage by remember { mutableStateOf<String?>(null) }

    fun filteredAccounts(
        accounts: List<TotpAccount> = sourceAccounts,
        query: String = accountState.query,
        sort: AccountSort = accountState.sort,
    ): List<TotpAccount> {
        val filtered = if (query.isBlank()) {
            accounts
        } else {
            accounts.filter {
                it.issuer.contains(query, ignoreCase = true) ||
                    it.accountName.contains(query, ignoreCase = true)
            }
        }
        return when (sort) {
            AccountSort.ISSUER -> filtered.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.issuer })
            AccountSort.ACCOUNT_NAME -> filtered.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.accountName })
        }
    }

    fun reloadAccounts() {
        accountState = accountState.copy(isLoading = true, error = null)
        scope.launch {
            val result = withContext(Dispatchers.IO) { accountStore.load() }
            result.fold(
                onSuccess = {
                    sourceAccounts = it
                    accountState = accountState.copy(
                        accounts = filteredAccounts(accounts = it),
                        isLoading = false,
                        error = null,
                    )
                },
                onFailure = {
                    accountState = accountState.copy(isLoading = false)
                },
            )
        }
    }

    LaunchedEffect(Unit) {
        reloadAccounts()
    }

    fun handleEnrollmentResult(result: EnrollmentResult) {
        when (result) {
            is EnrollmentResult.Added -> {
                duplicate = null
                enrollmentMessage = null
                destination = AppDestination.ACCOUNTS
                reloadAccounts()
            }
            is EnrollmentResult.Duplicate -> duplicate = result
            is EnrollmentResult.Invalid -> enrollmentMessage = result.errors.joinToString("\n") { it.message }
            EnrollmentResult.InvalidQr -> enrollmentMessage = "Invalid authenticator QR code."
            EnrollmentResult.StorageFailure -> enrollmentMessage = "Unable to save authenticator account."
        }
    }

    Scaffold(modifier = modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            AppHeader(
                destination = destination,
                onAccounts = { destination = AppDestination.ACCOUNTS },
                onAdd = {
                    enrollmentMessage = null
                    destination = AppDestination.ADD_ACCOUNT
                },
                onSettings = { destination = AppDestination.SETTINGS },
            )

            when (destination) {
                AppDestination.ACCOUNTS -> AccountListScreen(
                    state = accountState,
                    otpProvider = { account, epochSeconds ->
                        withContext(Dispatchers.IO) { accountStore.otp(account, epochSeconds) }
                    },
                    onQueryChange = { query ->
                        accountState = accountState.copy(
                            query = query,
                            accounts = filteredAccounts(query = query),
                        )
                    },
                    onSortChange = { sort ->
                        accountState = accountState.copy(
                            sort = sort,
                            accounts = filteredAccounts(sort = sort),
                        )
                    },
                    onEdit = { id, issuer, accountName ->
                        sourceAccounts.firstOrNull { it.id == id }?.let { current ->
                            val updated = current.copy(issuer = issuer.trim(), accountName = accountName.trim())
                            scope.launch {
                                withContext(Dispatchers.IO) {
                                    accountStore.updateMetadata(updated, System.currentTimeMillis())
                                }.onSuccess { reloadAccounts() }
                            }
                        }
                    },
                    onDeleteRequest = { id -> accountState = accountState.copy(pendingDeleteId = id) },
                    onDeleteConfirm = {
                        accountState.pendingDeleteId?.let { id ->
                            accountState = accountState.copy(pendingDeleteId = null)
                            scope.launch {
                                withContext(Dispatchers.IO) { accountStore.delete(id) }
                                    .onSuccess { reloadAccounts() }
                            }
                        }
                    },
                    onDeleteCancel = { accountState = accountState.copy(pendingDeleteId = null) },
                    modifier = Modifier.fillMaxSize(),
                )

                AppDestination.ADD_ACCOUNT -> AddAccountScreen(
                    message = enrollmentMessage,
                    onManualSubmit = { input ->
                        scope.launch {
                            handleEnrollmentResult(withContext(Dispatchers.IO) { addAccountUseCase.fromManual(input) })
                        }
                    },
                    onQrScanned = { rawValue ->
                        scope.launch {
                            handleEnrollmentResult(withContext(Dispatchers.IO) { addAccountUseCase.fromQr(rawValue) })
                        }
                    },
                )

                AppDestination.SETTINGS -> SettingsScreen(
                    state = SettingsUiState(
                        settings = settings,
                        deviceSecure = credentialGate.isDeviceSecure(),
                        automaticTimeEnabled = automaticTimeReader.isAutomaticTimeEnabled(),
                    ),
                    onBiometricLockChange = { enabled ->
                        if (!enabled || credentialGate.isDeviceSecure()) {
                            onSettingsChanged(settings.copy(biometricLockEnabled = enabled))
                        }
                    },
                    onScreenshotProtectionChange = { enabled ->
                        onSettingsChanged(settings.copy(screenshotProtectionEnabled = enabled))
                    },
                    onThemeChange = { theme -> onSettingsChanged(settings.copy(theme = theme)) },
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }

    duplicate?.let { pending ->
        AlertDialog(
            onDismissRequest = { duplicate = null },
            title = { Text("Account already exists") },
            text = { Text("Replace the existing account or keep both accounts?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            handleEnrollmentResult(
                                withContext(Dispatchers.IO) {
                                    addAccountUseCase.resolveDuplicate(pending, DuplicateDecision.Replace)
                                },
                            )
                        }
                    },
                ) { Text("Replace") }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = { duplicate = null }) { Text("Cancel") }
                    TextButton(
                        onClick = {
                            scope.launch {
                                handleEnrollmentResult(
                                    withContext(Dispatchers.IO) {
                                        addAccountUseCase.resolveDuplicate(pending, DuplicateDecision.KeepBoth)
                                    },
                                )
                            }
                        },
                    ) { Text("Keep both") }
                }
            },
        )
    }
}

@Composable
private fun AppHeader(
    destination: AppDestination,
    onAccounts: () -> Unit,
    onAdd: () -> Unit,
    onSettings: () -> Unit,
) {
    Surface(
        tonalElevation = 2.dp,
        shadowElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (destination != AppDestination.ACCOUNTS) {
                FilledTonalIconButton(onClick = onAccounts) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = when (destination) {
                        AppDestination.ACCOUNTS -> "KAuthenticator"
                        AppDestination.ADD_ACCOUNT -> "Add account"
                        AppDestination.SETTINGS -> "Settings"
                    },
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
                if (destination == AppDestination.ACCOUNTS) {
                    Text(
                        "Your verification codes",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            if (destination == AppDestination.ACCOUNTS) {
                FilledTonalButton(onClick = onSettings, shape = RoundedCornerShape(14.dp)) {
                    Text("Settings")
                }
                Button(onClick = onAdd, shape = RoundedCornerShape(14.dp)) {
                    Text("Add account")
                }
            }
        }
    }
}

@Composable
private fun AddAccountScreen(
    message: String?,
    onManualSubmit: (tm.khang.kauthenticator.feature.addaccount.ManualAccountInput) -> Unit,
    onQrScanned: (String) -> Unit,
) {
    var showScanner by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FilterChip(
                selected = !showScanner,
                onClick = { showScanner = false },
                label = { Text("Setup key") },
            )
            FilterChip(
                selected = showScanner,
                onClick = { showScanner = true },
                label = { Text("Scan QR") },
            )
        }

        message?.let {
            Surface(
                color = MaterialTheme.colorScheme.errorContainer,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
            ) {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(14.dp),
                )
            }
        }

        if (showScanner) {
            Column(
                modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        "Scan authenticator QR",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        "Position the QR code inside the camera view. It will be detected automatically.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                ) {
                    QrScanner(onQrScanned = onQrScanned, modifier = Modifier.fillMaxSize())
                }
            }
        } else {
            ManualAccountForm(onSubmit = onManualSubmit, modifier = Modifier.fillMaxSize())
        }
    }
}
