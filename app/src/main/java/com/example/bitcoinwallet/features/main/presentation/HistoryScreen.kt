package com.example.bitcoinwallet.features.main.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.bitcoinwallet.R
import com.example.bitcoinwallet.common.toBtcString
import com.example.bitcoinwallet.features.main.presentation.model.Direction
import com.example.bitcoinwallet.features.main.presentation.model.TxHistoryItem
import com.example.bitcoinwallet.features.main.presentation.states.HistoryUiState
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import java.text.DateFormat
import java.util.Date

@Composable
fun HistoryScreen(
    historyState: HistoryUiState, onLoadMore: () -> Unit
) {
    val listState = rememberLazyListState()

    LaunchedEffect(historyState.items.size) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }.filterNotNull()
            .distinctUntilChanged().collect { lastVisible ->
                if (lastVisible >= historyState.items.size - 1 && !historyState.isLoading) {
                    onLoadMore()
                }
            }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        itemsIndexed(historyState.items) { _, tx ->
            HistoryRow(tx)
        }

        item {
            when {
                historyState.isLoading -> {
                    Box(Modifier.fillMaxWidth()) {
                        CircularProgressIndicator(Modifier.align(Alignment.Center))
                    }
                }

                historyState.error != null -> {
                    Text(
                        text = historyState.error,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun HistoryRow(
    tx: TxHistoryItem,
) {
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        onClick = {
            val url = context.getString(R.string.https_mempool_space_signet_tx, tx.txId)
            uriHandler.openUri(url)
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = if (tx.direction == Direction.IN) stringResource(R.string.received) else stringResource(
                        R.string.sent
                    ), style = MaterialTheme.typography.labelLarge
                )
                Text(
                    text = if (tx.direction == Direction.IN) stringResource(
                        R.string.from, tx.counterparty
                    ) else stringResource(R.string.to, tx.counterparty),
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (tx.confirmed) {
                    Text(
                        text = DateFormat.getDateTimeInstance().format(Date(tx.timestamp * 1000)),
                        style = MaterialTheme.typography.bodySmall
                    )
                } else {
                    Text(
                        text = "Unconfirmed",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Text(
                text = if (tx.direction == Direction.IN) "+${tx.amount.toBtcString()}" else "-${tx.amount.toBtcString()}",
                style = MaterialTheme.typography.labelLarge.copy(
                    color = if (tx.direction == Direction.IN) Color(0xFF4CAF50) else Color(
                        0xFFD32F2F
                    )
                )
            )
        }
    }
}
