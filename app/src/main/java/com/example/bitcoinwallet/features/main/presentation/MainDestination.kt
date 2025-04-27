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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bitcoinwallet.R
import com.example.bitcoinwallet.common.theme.BitcoinWalletTheme
import com.example.bitcoinwallet.features.main.presentation.model.BalanceEntity
import com.example.bitcoinwallet.features.main.presentation.model.MainEntity
import kotlinx.coroutines.Dispatchers

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun MainDestination(
    viewModelFactory: ViewModelProvider.Factory,
) {
    val viewModel: MainViewModel = viewModel(factory = viewModelFactory)
    val state by viewModel.state.collectAsState(Dispatchers.Main.immediate)

    var dialogEvent by remember { mutableStateOf<TransactionEvent?>(null) }
    var isRefreshing by remember { mutableStateOf(false) }

    val pullRefreshState = rememberPullRefreshState(isRefreshing, {
        isRefreshing = true
        viewModel.refresh()
        isRefreshing = false
    })

    LaunchedEffect(Unit) {
        viewModel.txEvent.collect { event ->
            dialogEvent = event
        }
    }

    Scaffold(topBar = {
        CenterAlignedTopAppBar(
            title = { Text("Bitcoin Wallet", style = MaterialTheme.typography.headlineSmall) },
            colors = TopAppBarDefaults.topAppBarColors(
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
                onSendClick = { amountBtcToSend, addressToSend ->
                    viewModel.sendCoins(amountBtcToSend, addressToSend)
                },
                dialogEvent = dialogEvent,
                onDialogClose = { dialogEvent = null },
            )
            PullRefreshIndicator(
                refreshing = isRefreshing,
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
    onSendClick: (amountBtcToSend: String, addressToSend: String) -> Unit,
    dialogEvent: TransactionEvent?,
    onDialogClose: () -> Unit,
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
                    Text("History", style = MaterialTheme.typography.labelLarge)
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
                        TransactionPager()
                    }
                }
            }
        }
        dialogEvent?.let { event ->
            TxIdDialog(event, onDialogClose)
        }
    }

}

@Composable
fun TxIdDialog(
    event: TransactionEvent,
    onDialogClose: () -> Unit,
) {
    val uriHandler = LocalUriHandler.current
    AlertDialog(onDismissRequest = onDialogClose, title = {
        Text(
            text = when (event) {
                is TransactionEvent.Success -> "Transaction sent!"
                is TransactionEvent.Failure -> "Error while sending transaction"
            }
        )
    }, text = {
        when (event) {
            is TransactionEvent.Success -> {
                Column {
                    Text("Your transaction ID is ")
                    Spacer(Modifier.height(4.dp))
                    Text(text = event.txId, style = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.primary,
                        textDecoration = TextDecoration.Underline
                    ), modifier = Modifier
                        .clickable {
                            val url = "https://mempool.space/signet/tx/${event.txId}"
                            uriHandler.openUri(url)
                        }
                        .padding(4.dp))
                }
            }

            is TransactionEvent.Failure -> Text(event.message)
        }
    }, confirmButton = {
        TextButton(onClick = onDialogClose) {
            Text("Ok")
        }
    })
}

@Composable
fun TransactionPager() {
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 6 })

    Spacer(modifier = Modifier.height(16.dp))

    VerticalPager(
        state = pagerState, modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
    ) { page ->
        TransactionHistoryPage(pageIndex = page)
    }
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

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surface)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Balance", style = MaterialTheme.typography.headlineSmall)

        Image(
            painter = painterResource(id = R.drawable.ic_bitcoin),
            contentDescription = "Bitcoin Icon",
            modifier = Modifier.size(80.dp)
        )

        Text(
            text = "${state.data.balance?.confirmedBalance} tBTC",
            style = MaterialTheme.typography.displayLarge
        )
        if (state.data.balance?.unconfirmedBalance.equals("")) {
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
            placeholder = { Text("0.0001") },
            label = { Text("Amount to send") },
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
            label = { Text("Address to send") },
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
            enabled = amountBtcToSend.isNotBlank() && addressToSend.isNotBlank(),
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
                    context, "Address was copied", Toast.LENGTH_SHORT
                ).show()
            },
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .height(32.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            Text("Receive", style = MaterialTheme.typography.labelSmall)
        }
    }
}


@Composable
fun TransactionHistoryPage(pageIndex: Int) {
    val items2 = remember(pageIndex) { sampleTxFor(pageIndex) }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(items2) { tx ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(tx.type, style = MaterialTheme.typography.labelLarge)
                        Text(tx.address, style = MaterialTheme.typography.bodyMedium)
                    }
                    Text(
                        tx.amount, style = MaterialTheme.typography.labelLarge.copy(
                            color = if (tx.isIncoming) Color(0xFF4CAF50) else Color(0xFFD32F2F)
                        )
                    )
                }
            }
        }
    }
}

data class Tx(val type: String, val address: String, val amount: String, val isIncoming: Boolean)

fun sampleTxFor(i: Int): List<Tx> = listOf(
    Tx("Received", "bc1…${123 + i}", "+0.001 BTC", true),
    Tx("Sent", "bc1…${456 + i}", "-0.0003 BTC", false),
    Tx("Received", "bc1…${789 + i}", "+0.0220 BTC", true),
)

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    BitcoinWalletTheme {
        MainScreen(modifier = Modifier.fillMaxSize(),
            state = MainScreenState.Success(
                MainEntity(
                    address = "123...asd",
                    balance = BalanceEntity(
                        "1",
                        "0.2",
                    )
                )
            ),
            onSendClick = { _, _ -> },
            dialogEvent = null,
            {})
    }
}