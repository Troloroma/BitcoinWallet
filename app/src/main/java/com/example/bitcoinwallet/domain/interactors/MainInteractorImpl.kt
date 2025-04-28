package com.example.bitcoinwallet.domain.interactors

import android.util.Log
import com.example.bitcoinwallet.common.Entity
import com.example.bitcoinwallet.common.toBtcString
import com.example.bitcoinwallet.common.toSatoshiLong
import com.example.bitcoinwallet.domain.models.TransactionParams
import com.example.bitcoinwallet.features.main.domain.MainInteractor
import com.example.bitcoinwallet.features.main.domain.MainRepository
import com.example.bitcoinwallet.features.main.domain.WalletRepository
import com.example.bitcoinwallet.features.main.presentation.model.BalanceEntity
import com.example.bitcoinwallet.features.main.presentation.model.Direction
import com.example.bitcoinwallet.features.main.presentation.model.TxHistoryItem
import com.example.bitcoinwallet.network.dto.TransactionResponse
import com.example.bitcoinwallet.network.dto.UtxoResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import org.bitcoinj.base.AddressParser
import org.bitcoinj.base.BitcoinNetwork
import org.bitcoinj.base.Coin
import org.bitcoinj.base.ScriptType
import org.bitcoinj.base.Sha256Hash
import org.bitcoinj.base.internal.ByteUtils.formatHex
import org.bitcoinj.core.Transaction
import org.bitcoinj.core.TransactionOutPoint
import org.bitcoinj.crypto.DumpedPrivateKey
import org.bitcoinj.script.ScriptBuilder
import kotlin.math.ceil

class MainInteractorImpl(
    private val mainRepository: MainRepository, private val walletRepository: WalletRepository
) : MainInteractor {
    override suspend fun getAddress(): Entity<String> {
        return when (val wallet = walletRepository.getWallet()) {
            is Entity.Success -> {
                Entity.Success(wallet.data.address!!)
            }

            is Entity.Error -> {
                Entity.Error(wallet.message)
            }
        }
    }

    override suspend fun getBalance(address: String): Flow<Entity<BalanceEntity>> {
        return mainRepository.getUtxoList(address).map { entity ->
            when (entity) {
                is Entity.Success -> {
                    val (confirmedUtxos, unconfirmedUtxos) = entity.data.partition {
                        it.status?.confirmed == true
                    }

                    val confirmedSats = confirmedUtxos.sumOf { it.value }
                    val unconfirmedSats = unconfirmedUtxos.sumOf { it.value }
                    Log.d("confirmedUtxos", confirmedSats.toString())
                    Log.d("unconfirmedUtxos", unconfirmedSats.toString())

                    Entity.Success(
                        BalanceEntity(
                            confirmedBalance = confirmedSats.toBtcString(),
                            unconfirmedBalance = unconfirmedSats.toBtcString(),
                        )
                    )
                }

                is Entity.Error -> {
                    Entity.Error(entity.message)
                }
            }
        }
    }

    override suspend fun sendCoins(amountBtcToSend: String, addressToSend: String): Entity<String> {
        return when (val wallet = walletRepository.getWallet()) {
            is Entity.Error -> return Entity.Error(wallet.message)
            is Entity.Success -> {
                try {
                    // Network settings
                    val network = BitcoinNetwork.SIGNET

                    // From address and private key
                    val fromAddress = wallet.data.address!!
                    val privateKeyWif =
                        DumpedPrivateKey.fromBase58(network, wallet.data.privateKeyWif).key

                    // Amount to send
                    val sendAmount = Coin.valueOf(
                        (amountBtcToSend.toSatoshiLong())
                    )

                    // To address
                    val addressParser = AddressParser.getDefault(network)
                    val toAddress = addressParser.parseAddress(addressToSend)

                    // Utxo
                    val utxoList = mainRepository.getUtxoList(fromAddress).first()
                    if (utxoList is Entity.Error) return Entity.Error(utxoList.message)
                    val utxos = (utxoList as Entity.Success).data

                    // Fee rates
                    val feeRates = mainRepository.getFeeEstimates()

                    // first 6 blocks or the smallest
                    val feeRateSatPerVb = feeRates[6] ?: feeRates.values.minOrNull() ?: 1.0
                    // Raw fee just to get utxos
                    val roughFee1In = ceil(feeRateSatPerVb * (68 + 31 + 10)).toLong()

                    val selectedUtxos =
                        selectUtxos(utxos, (amountBtcToSend.toSatoshiLong() + roughFee1In))
                    // All selected utxos sum
                    var totalInput = Coin.ZERO
                    selectedUtxos.forEach { utxo ->
                        totalInput += Coin.valueOf(utxo.value)
                    }

                    val inputCount = selectedUtxos.size
                    val outputCount =
                        if (totalInput.value - sendAmount.value - roughFee1In > 0) 2 else 1
                    val vsize = inputCount * 68 + outputCount * 31 + 50

                    // Final fee
                    val fee = Coin.valueOf(ceil(feeRateSatPerVb * vsize).toLong())

                    if (totalInput.subtract(sendAmount) < Coin.valueOf(roughFee1In)) return Entity.Error(
                        "Not enough coins to send transaction with fee"
                    )

                    // Final hex
                    val hex = generateTransaction(
                        TransactionParams(
                            privateKeyWif = privateKeyWif,
                            toAddress = toAddress,
                            fromAddress = addressParser.parseAddress(fromAddress),
                            sendAmount = sendAmount,
                            fee = fee,
                            totalInput = totalInput,
                            selectedUtxos = selectedUtxos,
                            network = network
                        )
                    )
                    Log.d("MainInteractorImpl", hex)

                    // Api
                    when (val txResult = mainRepository.sendCoins(hex)) {
                        is Entity.Error -> return Entity.Error(txResult.message)
                        is Entity.Success -> return Entity.Success(txResult.data)
                    }
                } catch (e: Exception) {
                    Entity.Error(e.message ?: "Error while creating transaction")
                }
            }
        }
    }

    /***
     * Because Bitcoin nodes reject transactions that use multiple outputs of a single parent transaction as inputs.
     * ***/
    private fun selectUtxos(utxos: List<UtxoResponse>, amountNeeded: Long): List<UtxoResponse> {
        val sorted = utxos.sortedByDescending { it.value }

        val selected = mutableListOf<UtxoResponse>()
        var total = 0L
        for (utxo in sorted) {
            if (selected.any { it.txid == utxo.txid }) continue

            selected.add(utxo)
            total += utxo.value

            if (total >= amountNeeded) break
        }

        return if (total >= amountNeeded) selected else emptyList()
    }

    private fun generateTransaction(transactionParams: TransactionParams): String {

        val scriptType = ScriptType.P2WPKH
        val tx = Transaction()

        // Recipient output
        tx.addOutput(transactionParams.sendAmount, transactionParams.toAddress)

        // Change output
        val changeSat = transactionParams.totalInput.subtract(transactionParams.sendAmount)
            .subtract(transactionParams.fee)
        if (changeSat.isPositive) {
            tx.addOutput(
                changeSat,
                transactionParams.privateKeyWif.toAddress(scriptType, transactionParams.network)
            )
        }

        // Utxos inputs
        transactionParams.selectedUtxos.forEach { u ->
            val txid = u.txid
            val vout = u.vout.toLong()
            val valSat = u.value

            val outPoint = TransactionOutPoint(
                vout, Sha256Hash.wrap(txid)
            )

            val scriptPubKey = ScriptBuilder.createOutputScript(transactionParams.fromAddress)
            tx.addSignedInput(
                outPoint,
                scriptPubKey,
                Coin.valueOf(valSat),
                transactionParams.privateKeyWif,
                Transaction.SigHash.ALL,
                false
            )
        }

        // Final hex
        val hex = formatHex(tx.serialize())
        return hex
    }

    override suspend fun getTxHistory(lastTxId: String?): Flow<Entity<List<TxHistoryItem>>> {
        return flow {
            when (val wallet = walletRepository.getWallet()) {
                is Entity.Error -> emit(Entity.Error(wallet.message))
                is Entity.Success -> {
                    val myAddress = wallet.data.address
                    mainRepository.getTxHistory(myAddress!!, lastTxId).collect { rawTxHistory ->
                        when (rawTxHistory) {
                            is Entity.Error -> emit(Entity.Error("Can't get transactions history"))
                            is Entity.Success -> {
                                try {
                                    val txHistory =
                                        mapToHistoryList(rawTxHistory.data, myAddress = myAddress)
                                    Log.d("txHistory", txHistory.toString())
                                    emit(Entity.Success(txHistory))
                                } catch (e: Exception) {
                                    emit(Entity.Error("Can't get transactions history"))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun mapToHistoryList(
        txs: List<TransactionResponse>, myAddress: String
    ): List<TxHistoryItem> = txs.map { tx ->
        val confirmed = tx.status?.confirmed
        val timestamp = tx.status?.blockTime

        // Our address is in input?
        val isOutgoing = tx.vin.any { it.prevout?.scriptpubkeyAddress == myAddress }

        // amount and counterparty
        val (amount, counterparty) = if (isOutgoing) {
            // OUT
            val outsOther = tx.vout.filter { it.scriptpubkeyAddress != myAddress }
            if (outsOther.isNotEmpty()) {
                // Sent to other
                val sum = outsOther.sumOf { it.value }
                val counter = outsOther.firstOrNull()?.scriptpubkeyAddress ?: "Unknown"
                sum to counter
            } else {
                // Sent to myself
                val sendVal = tx.vout.firstOrNull()?.value
                sendVal to myAddress
            }
        } else {
            // IN
            val outs = tx.vout.filter { it.scriptpubkeyAddress == myAddress }
            val sum = outs.sumOf { it.value }

            val counter =
                tx.vin.firstOrNull { it.prevout?.scriptpubkeyAddress != myAddress }?.prevout?.scriptpubkeyAddress
                    ?: "Unknown"
            sum to counter
        }

        TxHistoryItem(
            txId = tx.txid.toString(),
            timestamp = timestamp ?: 0L,
            confirmed = confirmed ?: false,
            direction = if (isOutgoing) Direction.OUT else Direction.IN,
            counterparty = counterparty,
            amount = amount ?: 0L
        )
    }
}
