package com.example.bitcoinwallet.data.repository

import android.util.Log
import com.example.bitcoinwallet.common.Entity
import com.example.bitcoinwallet.data.base.BaseRepository
import com.example.bitcoinwallet.features.main.domain.MainRepository
import com.example.bitcoinwallet.network.api.Api
import com.example.bitcoinwallet.network.dto.UtxoResponse
import com.example.bitcoinwallet.network.utils.models.ResponseStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class MainRepositoryImpl(
    private val apiService: Api
) : MainRepository, BaseRepository() {

    override suspend fun getUtxoList(address: String): Flow<Entity<List<UtxoResponse>>> {
        return flow {
            when (val response = safeApiSuspendResultNoResponse {
                apiService.getUtxo(address = address)
            }) {
                is ResponseStatus.Success -> {
                    val utxos: List<UtxoResponse> = response.data ?: emptyList()
                    emit(Entity.Success(utxos))
                }

                is ResponseStatus.LocalError -> {
                    emit(Entity.Error(response.message.ifEmpty { "Error" }))
                }

                is ResponseStatus.ServerError -> {
                    emit(
                        Entity.Error(
                            response.exception.message ?: "Error"
                        )
                    )
                }
            }
        }
    }

    override suspend fun sendCoins(hex: String): Entity<String> {
        return when (val response = safeApiSuspendResultNoResponse {
            apiService.sendTx(txHex = hex)
        }) {
            is ResponseStatus.Success ->{
                Entity.Success(response.data as String)
            }

            is ResponseStatus.LocalError -> {
                Entity.Error(response.message.ifEmpty { "Error" })
            }
            is ResponseStatus.ServerError -> {
                Log.d("MainRepositoryImpl", hex)
                Entity.Error(
                    response.exception.message ?: "Error"
                )
            }
        }
    }
}