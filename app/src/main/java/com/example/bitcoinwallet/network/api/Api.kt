package com.example.bitcoinwallet.network.api

import com.example.bitcoinwallet.network.dto.UtxoResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface Api {

    @GET("address/{address}/utxo")
    suspend fun getUtxo(
        @Path("address") address: String
    ): Response<List<UtxoResponse>>

    @POST("tx")
    suspend fun sendTx(
        @Body txHex: String
    ) : Response<String>
}