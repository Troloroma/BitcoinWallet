package com.example.bitcoinwallet.network.dto

import com.google.gson.annotations.SerializedName

data class TransactionResponse(
    @SerializedName("txid")
    var txid: String? = null,
    @SerializedName("version")
    var version: Int? = null,
    @SerializedName("locktime")
    var locktime: Int? = null,
    @SerializedName("vin")
    var vin: ArrayList<Vin> = arrayListOf(),
    @SerializedName("vout")
    var vout: ArrayList<Vout> = arrayListOf(),
    @SerializedName("size")
    var size: Int? = null,
    @SerializedName("weight")
    var weight: Int? = null,
    @SerializedName("fee")
    var fee: Int? = null,
    @SerializedName("status")
    var status: StatusDTO? = StatusDTO()
)

data class Prevout(
    @SerializedName("scriptpubkey")
    var scriptpubkey: String? = null,
    @SerializedName("scriptpubkey_asm")
    var scriptpubkeyAsm: String? = null,
    @SerializedName("scriptpubkey_type")
    var scriptpubkeyType: String? = null,
    @SerializedName("scriptpubkey_address")
    var scriptpubkeyAddress: String? = null,
    @SerializedName("value") var value: Long
)

data class Vin(

    @SerializedName("txid")
    var txid: String? = null,
    @SerializedName("vout")
    var vout: Int? = null,
    @SerializedName("prevout")
    var prevout: Prevout? = Prevout(value = 0),
    @SerializedName("scriptsig")
    var scriptsig: String? = null,
    @SerializedName("scriptsig_asm")
    var scriptsigAsm: String? = null,
    @SerializedName("witness")
    var witness: ArrayList<String> = arrayListOf(),
    @SerializedName("is_coinbase")
    var isCoinbase: Boolean? = null,
    @SerializedName("sequence")
    var sequence: Long? = null

)

data class Vout(
    @SerializedName("scriptpubkey")
    var scriptpubkey: String? = null,
    @SerializedName("scriptpubkey_asm")
    var scriptpubkeyAsm: String? = null,
    @SerializedName("scriptpubkey_type")
    var scriptpubkeyType: String? = null,
    @SerializedName("scriptpubkey_address")
    var scriptpubkeyAddress: String? = null,
    @SerializedName("value")
    var value: Long
)


data class StatusDTO(
    @SerializedName("confirmed") var confirmed: Boolean? = null,
    @SerializedName("block_height") var blockHeight: Long? = null,
    @SerializedName("block_hash") var blockHash: String? = null,
    @SerializedName("block_time") var blockTime: Long? = null
)