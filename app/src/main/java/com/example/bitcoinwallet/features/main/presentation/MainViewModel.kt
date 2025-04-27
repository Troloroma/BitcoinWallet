package com.example.bitcoinwallet.features.main.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bitcoinwallet.common.Entity
import com.example.bitcoinwallet.features.main.domain.MainInteractor
import com.example.bitcoinwallet.features.main.presentation.model.MainEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import javax.inject.Inject

class MainViewModel @Inject constructor(
    private val interactor: MainInteractor
) : ViewModel() {
    private val _state = MutableStateFlow<MainScreenState>(MainScreenState.Loading)
    val state: StateFlow<MainScreenState> = _state

    private val _txEvent = MutableSharedFlow<TransactionEvent>()
    val txEvent: SharedFlow<TransactionEvent> = _txEvent

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
                interactor.getBalance(address)
                    .combine(flowOf(address)) { balanceEntity, addr ->
                        when (balanceEntity) {
                            is Entity.Success -> {
                                Log.d("TAG", "Success")
                                MainScreenState.Success(
                                    MainEntity(
                                        address = addr,
                                        balance = balanceEntity.data
                                    )
                                )
                            }
                            is Entity.Error -> {
                                Log.d("TAG", "Error")
                                MainScreenState.Error(balanceEntity.message)
                            }
                        }
                    }
                    .collect { screenState ->
                        Log.d("TAG", screenState.toString())
                        _state.value = screenState
                    }
            } catch (e: Exception) {
                _state.value = MainScreenState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun sendCoins(amountBtcToSend: String, addressToSend: String) {
        viewModelScope.launch(Dispatchers.IO) {
            when(val result = interactor.sendCoins(amountBtcToSend, addressToSend)){
                is Entity.Success -> {
                    _txEvent.emit(TransactionEvent.Success(result.data))
                }
                is Entity.Error -> {
                    _txEvent.emit(TransactionEvent.Failure(result.message))
                }
            }
        }
    }
}
