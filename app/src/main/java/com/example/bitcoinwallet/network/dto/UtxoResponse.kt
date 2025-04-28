package com.example.bitcoinwallet.network.dto

import com.google.gson.annotations.SerializedName

data class UtxoResponse(
    @SerializedName("txid") var txid: String,
    @SerializedName("vout") var vout: Int,
    @SerializedName("status") var status: StatusDTO? = StatusDTO(),
    @SerializedName("value") var value: Long
)