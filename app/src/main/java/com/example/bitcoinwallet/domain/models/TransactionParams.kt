package com.example.bitcoinwallet.domain.models

import com.example.bitcoinwallet.network.dto.UtxoResponse
import org.bitcoinj.base.Address
import org.bitcoinj.base.BitcoinNetwork
import org.bitcoinj.base.Coin
import org.bitcoinj.crypto.ECKey

data class TransactionParams (
    val privateKeyWif: ECKey,
    val toAddress: Address,
    val fromAddress: Address,
    val sendAmount: Coin,
    val fee: Coin,
    val totalInput: Coin,
    val selectedUtxos: List<UtxoResponse>,
    val network: BitcoinNetwork
)