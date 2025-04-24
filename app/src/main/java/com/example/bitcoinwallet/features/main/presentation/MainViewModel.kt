package com.example.bitcoinwallet.features.main.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.bitcoinwallet.features.main.domain.MainInteractor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class MainViewModel @Inject constructor(
    val interactor: MainInteractor
) : ViewModel() {
    private val _state = MutableStateFlow<MainScreenState>(MainScreenState.Success)
    val state: StateFlow<MainScreenState> = _state

    init {
        Log.d("ViewModel", "CREATED")
    }

    fun send(){

    }

}
