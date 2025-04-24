package com.example.bitcoinwallet.features.main.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bitcoinwallet.R
import com.example.bitcoinwallet.common.theme.BitcoinWalletTheme
import kotlinx.coroutines.Dispatchers

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainDestination(
    modifier: Modifier = Modifier,
    viewModelFactory: ViewModelProvider.Factory,
    navigateOnHistory: () -> Unit
) {
    val viewModel: MainViewModel = viewModel(factory = viewModelFactory)
    val state by viewModel.state.collectAsState(Dispatchers.Main.immediate)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Bitcoin Wallet") },
                actions = {
                    TextButton(onClick = { navigateOnHistory() }) {
                        Text("History", color = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
            )
        },
    ) { innerPadding ->
        MainScreen(
            modifier = Modifier.padding(innerPadding),
            state = state,
            onSendClick = viewModel::send
        )
    }
}

@Composable
fun MainScreen(
    modifier: Modifier = Modifier, state: MainScreenState, onSendClick: () -> Unit
) {
    var amountToSend by remember { mutableStateOf("") }
    var addressToSend by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
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
                        "Error",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }

            is MainScreenState.Success -> {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Text(
                        text = "Balance", style = MaterialTheme.typography.headlineSmall
                    )

                    Image(
                        painter = painterResource(id = R.drawable.ic_bitcoin),
                        contentDescription = "Bitcoin Icon",
                        modifier = Modifier.size(100.dp)
                    )

                    Text(
                        text = "0.001 BTC", style = MaterialTheme.typography.displayMedium.copy(
                            fontSize = 36.sp
                        )
                    )

                    OutlinedTextField(
                        value = amountToSend,
                        onValueChange = { amountToSend = it },
                        label = { Text("Amount to send") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = addressToSend,
                        onValueChange = { addressToSend = it },
                        label = { Text("Address to send") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = onSendClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        enabled = amountToSend.isNotEmpty() && addressToSend.isNotEmpty()
                    ) {
                        Text("Send", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    BitcoinWalletTheme {
        MainScreen(
            modifier = Modifier.fillMaxSize(), state = MainScreenState.Success
        ) {

        }
    }
}