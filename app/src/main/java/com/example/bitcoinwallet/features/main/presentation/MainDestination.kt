package com.example.bitcoinwallet.features.main.presentation

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bitcoinwallet.R
import com.example.bitcoinwallet.common.theme.BitcoinWalletTheme
import com.example.bitcoinwallet.features.main.presentation.model.BalanceEntity
import com.example.bitcoinwallet.features.main.presentation.model.MainEntity
import com.example.bitcoinwallet.features.main.presentation.states.HistoryUiState
import com.example.bitcoinwallet.features.main.presentation.states.MainScreenState
import com.example.bitcoinwallet.features.main.presentation.states.TransactionEvent
import com.example.bitcoinwallet.features.main.presentation.states.TransactionEventState
import kotlinx.coroutines.Dispatchers

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun MainDestination(
    viewModel: MainViewModel
) {
    val state by viewModel.state.collectAsState(Dispatchers.Main.immediate)
    val historyState by viewModel.historyState.collectAsState(Dispatchers.Main.immediate)

    var isRefreshingScreen by remember { mutableStateOf(false) }

    val pullRefreshState = rememberPullRefreshState(isRefreshingScreen, {
        isRefreshingScreen = true
        viewModel.refresh()
        isRefreshingScreen = false
    })

    val eventState by viewModel.txEventState.collectAsState()

    when (val currEventState = eventState) {
        is TransactionEventState.Triggered -> {
            TxIdDialog(event = currEventState.event, onDialogClose = {
                viewModel.markTransactionEventHandled()
            })
        }

        is TransactionEventState.Handled -> {}
    }

    Scaffold(topBar = {
        CenterAlignedTopAppBar(
            title = {
                Text(
                    stringResource(R.string.bitcoin_wallet),
                    style = MaterialTheme.typography.headlineSmall
                )
            }, colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primary,
                titleContentColor = MaterialTheme.colorScheme.onPrimary
            )
        )
    }) { contentPadding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .pullRefresh(pullRefreshState)
                .verticalScroll(rememberScrollState())
        ) {
            MainScreen(
                state = state,
                historyState = historyState,
                onSendClick = { amountBtcToSend, addressToSend ->
                    viewModel.sendCoins(amountBtcToSend, addressToSend)
                },
                onLoadHistory = viewModel::getHistory
            )
            PullRefreshIndicator(
                refreshing = isRefreshingScreen,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    state: MainScreenState,
    historyState: HistoryUiState,
    onSendClick: (amountBtcToSend: String, addressToSend: String) -> Unit,
    onLoadHistory: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween
    ) {
        when (state) {
            is MainScreenState.Loading -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                }
            }

            is MainScreenState.Error -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    Text(
                        state.message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }

            is MainScreenState.Success -> {
                MainForm(state = state, onSendClick = { amountBtcToSend, addressToSend ->
                    onSendClick(amountBtcToSend, addressToSend)
                })
                TextButton(modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                    onClick = { showBottomSheet = true }) {
                    Text(
                        stringResource(R.string.history),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
                if (showBottomSheet) {
                    ModalBottomSheet(
                        onDismissRequest = { showBottomSheet = false },
                        sheetState = sheetState,
                        dragHandle = {
                            Box(
                                Modifier
                                    .padding(vertical = 8.dp)
                                    .size(width = 40.dp, height = 4.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(Color.Gray)
                                    .align(Alignment.CenterHorizontally)
                            )
                        },
                        modifier = Modifier.fillMaxSize(),
                        shape = MaterialTheme.shapes.large
                    ) {
                        HistoryScreen(
                            historyState = historyState, onLoadMore = onLoadHistory
                        )
                    }
                }
            }
        }
    }

}

@Composable
fun TxIdDialog(
    event: TransactionEvent,
    onDialogClose: () -> Unit,
) {
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current
    AlertDialog(onDismissRequest = onDialogClose, title = {
        Text(
            text = when (event.type) {
                is TransactionEvent.EventType.Success -> stringResource(R.string.transaction_sent)
                is TransactionEvent.EventType.Failure -> stringResource(R.string.error_while_sending_transaction)
            }
        )
    }, text = {
        when (event.type) {
            is TransactionEvent.EventType.Success -> {
                Column {
                    Text(text = stringResource(R.string.your_transaction_id_is))
                    Spacer(Modifier.height(4.dp))
                    Text(text = event.txId ?: stringResource(R.string.unknown),
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.primary,
                            textDecoration = TextDecoration.Underline
                        ),
                        modifier = Modifier
                            .clickable {
                                val url = context.getString(
                                    R.string.https_mempool_space_signet_tx, event.txId
                                )
                                uriHandler.openUri(url)
                            }
                            .padding(4.dp))
                }
            }

            is TransactionEvent.EventType.Failure -> Text(
                event.message ?: stringResource(R.string.unknown)
            )
        }
    }, confirmButton = {
        TextButton(onClick = onDialogClose) {
            Text(stringResource(R.string.send_more))
        }
    })
}

@Composable
fun MainForm(
    state: MainScreenState.Success,
    onSendClick: (amountBtcToSend: String, addressToSend: String) -> Unit,
) {
    var amountBtcToSend by remember { mutableStateOf("") }
    var addressToSend by remember { mutableStateOf("") }
    val context = LocalContext.current
    val clipboardManager =
        remember { context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager }

    val isSentButtonEnabled by remember {
        derivedStateOf {
            amountBtcToSend.isNotBlank() && addressToSend.isNotBlank()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surface)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.balance), style = MaterialTheme.typography.headlineSmall
        )

        Image(
            painter = painterResource(id = R.drawable.ic_bitcoin),
            contentDescription = "Bitcoin Icon",
            modifier = Modifier.size(80.dp)
        )

        Text(
            text = stringResource(
                R.string.tbtc, state.data.balance?.confirmedBalance.toString()
            ), style = MaterialTheme.typography.displayLarge
        )
        if (!state.data.balance?.unconfirmedBalance.equals("0")) {
            Text(
                text = "Unconfirmed: ${state.data.balance?.unconfirmedBalance}",
                style = MaterialTheme.typography.labelMedium,
                color = Color.Gray
            )
        }

        Box(
            modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center
        ) {
            Text(
                text = state.data.address.toString(),
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                color = Color.Gray
            )
        }

        OutlinedTextField(
            value = amountBtcToSend,
            onValueChange = { amountBtcToSend = it },
            placeholder = { Text(stringResource(R.string._0_0001)) },
            label = { Text(stringResource(R.string.amount_to_send)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                cursorColor = MaterialTheme.colorScheme.primary
            ),
            shape = MaterialTheme.shapes.medium
        )

        OutlinedTextField(
            value = addressToSend,
            onValueChange = { addressToSend = it },
            label = { Text(stringResource(R.string.address_to_send)) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                cursorColor = MaterialTheme.colorScheme.primary
            ),
            shape = MaterialTheme.shapes.medium
        )

        Button(
            onClick = {
                onSendClick(
                    amountBtcToSend, addressToSend
                )
            },
            enabled = isSentButtonEnabled,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = MaterialTheme.shapes.small
        ) {
            Text("Send", style = MaterialTheme.typography.labelLarge)
        }
        Button(
            onClick = {
                val clip = ClipData.newPlainText("label", state.data.address)
                clipboardManager.setPrimaryClip(clip)
                Toast.makeText(
                    context, context.getString(R.string.address_was_copied), Toast.LENGTH_SHORT
                ).show()
            },
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .height(32.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            Text(
                text = stringResource(R.string.receive), style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    BitcoinWalletTheme {
        MainScreen(modifier = Modifier.fillMaxSize(),
            state = MainScreenState.Success(
                MainEntity(
                    address = "123...asd", balance = BalanceEntity(
                        "1",
                        "0.2",
                    )
                )
            ),
            historyState = HistoryUiState(),
            onSendClick = { _, _ -> },
            {}
        )
    }
}