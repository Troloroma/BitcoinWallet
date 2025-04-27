package com.example.bitcoinwallet.network.dto

import com.google.gson.annotations.SerializedName

data class UtxoResponse(
    @SerializedName("txid") var txid: String,
    @SerializedName("vout") var vout: Int,
    @SerializedName("status") var status: Status? = Status(),
    @SerializedName("value") var value: Long
)

data class Status(
    @SerializedName("confirmed") var confirmed: Boolean? = null,
    @SerializedName("block_height") var blockHeight: Int? = null,
    @SerializedName("block_hash") var blockHash: String? = null,
    @SerializedName("block_time") var blockTime: Int? = null
)