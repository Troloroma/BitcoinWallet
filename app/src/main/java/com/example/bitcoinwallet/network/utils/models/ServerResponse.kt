package com.example.bitcoinwallet.network.utils.models

import com.google.gson.annotations.SerializedName

data class ServerResponse<T>(
    @SerializedName("message") val message: String?,
    @SerializedName("error") var isError: Boolean?,
    @SerializedName("code") val code: Int?,
    @SerializedName("data") val data: T?
)
