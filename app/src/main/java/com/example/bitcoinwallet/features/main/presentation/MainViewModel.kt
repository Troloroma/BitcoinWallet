package com.example.bitcoinwallet.features.main.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bitcoinwallet.common.Entity
import com.example.bitcoinwallet.features.main.domain.MainInteractor
import com.example.bitcoinwallet.features.main.presentation.model.MainEntity
import com.example.bitcoinwallet.features.main.presentation.states.HistoryUiState
import com.example.bitcoinwallet.features.main.presentation.states.MainScreenState
import com.example.bitcoinwallet.features.main.presentation.states.TransactionEventState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

class MainViewModel @Inject constructor(
    private val interactor: MainInteractor
) : ViewModel() {
    private val _state = MutableStateFlow<MainScreenState>(MainScreenState.Loading)
    val state: StateFlow<MainScreenState> = _state

    private val _txEvent = MutableSharedFlow<TransactionEventState>()
    val txEvent: SharedFlow<TransactionEventState> = _txEvent

    private val _historyState = MutableStateFlow(HistoryUiState())
    val historyState: StateFlow<HistoryUiState> = _historyState

    private var lastTxId: String? = null

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _state.value = MainScreenState.Loading
                // address
                val addressResult = interactor.getAddress()
                if (addressResult is Entity.Error) {
                    _state.value = MainScreenState.Error(addressResult.message)
                    return@launch
                }
                val address = (addressResult as Entity.Success).data
                // balance
                interactor.getBalance(address).combine(flowOf(address)) { balanceEntity, addr ->
                    when (balanceEntity) {
                        is Entity.Success -> {
                            MainScreenState.Success(
                                MainEntity(
                                    address = addr, balance = balanceEntity.data
                                )
                            )
                        }

                        is Entity.Error -> {
                            MainScreenState.Error(balanceEntity.message)
                        }
                    }
                }.collect { screenState ->
                    _state.value = screenState
                }
            } catch (e: Exception) {
                _state.value = MainScreenState.Error(e.message ?: "Unknown error")
            }
            refreshHistory()
        }
    }

    fun sendCoins(amountBtcToSend: String, addressToSend: String) {
        viewModelScope.launch(Dispatchers.IO) {
            when (val result = interactor.sendCoins(amountBtcToSend, addressToSend)) {
                is Entity.Success -> {
                    _txEvent.emit(TransactionEventState.Success(result.data))
                }

                is Entity.Error -> {
                    _txEvent.emit(TransactionEventState.Failure(result.message))
                }
            }
        }
    }

    fun refreshHistory() {
        _historyState.update { it.copy(items = emptyList(), isLoading = false, error = null) }
        lastTxId = null
        getHistory()
    }

    fun getHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            interactor.getTxHistory(lastTxId).collect { result ->
                when (result) {
                    is Entity.Success -> {
                        val newItems = result.data
                        if (newItems.isNotEmpty()) {
                            lastTxId = newItems.last().txId
                        }
                        _historyState.update {
                            it.copy(
                                items = it.items + newItems, isLoading = false, error = null
                            )
                        }
                    }

                    is Entity.Error -> {
                        _historyState.update {
                            it.copy(isLoading = false, error = result.message)
                        }
                    }
                }
            }
        }
    }
}
