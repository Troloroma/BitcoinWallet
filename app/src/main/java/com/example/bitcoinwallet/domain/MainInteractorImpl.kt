package com.example.bitcoinwallet.domain

import android.util.Log
import com.example.bitcoinwallet.common.Entity
import com.example.bitcoinwallet.features.main.domain.MainInteractor
import com.example.bitcoinwallet.features.main.domain.MainRepository
import com.example.bitcoinwallet.features.main.domain.WalletRepository
import com.example.bitcoinwallet.features.main.presentation.model.BalanceEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
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
import java.math.BigDecimal
import java.math.RoundingMode

class MainInteractorImpl(
    private val mainRepository: MainRepository,
    private val walletRepository: WalletRepository
) : MainInteractor {
    override suspend fun getAddress(): Entity<String> {
        return when (val wallet = walletRepository.getWallet()) {
            is Entity.Success -> {
                Entity.Success(wallet.data.address.toString())
            }

            is Entity.Error -> {
                Entity.Error(wallet.message)
            }
        }
    }

    override suspend fun getBalance(address: String): Flow<Entity<BalanceEntity>> {
        return mainRepository.getUtxoList(address)
            .map { entity ->
                when (entity) {
                    is Entity.Success -> {
                        val (confirmedUtxos, unconfirmedUtxos) = entity.data.partition {
                            it.status?.confirmed == true
                        }

                        val confirmedSats = confirmedUtxos.sumOf { it.value }
                        val unconfirmedSats = unconfirmedUtxos.sumOf { it.value }

                        fun Long.toBtcString(): String {
                            return BigDecimal(this)
                                .divide(BigDecimal(100_000_000), 8, RoundingMode.DOWN)
                                .stripTrailingZeros().toPlainString()
                        }

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
                    val scriptType = ScriptType.P2WPKH
                    val network = BitcoinNetwork.SIGNET

                    // From address and private key
                    val fromAddress = wallet.data.address!!
                    val privateKeyWif =
                        DumpedPrivateKey.fromBase58(network, wallet.data.privateKeyWif).key

                    // Amount to send
                    val sendAmount = Coin.valueOf(
                        (amountBtcToSend.toBigDecimal() *
                                100_000_000.toBigDecimal()).toLong()
                    )

                    // To address
                    val addressParser = AddressParser.getDefault(network)
                    val toAddress = addressParser.parseAddress(addressToSend)

                    // Utxo
                    val utxoList = mainRepository
                        .getUtxoList(fromAddress)
                        .first()
                    if (utxoList is Entity.Error)
                        return Entity.Error(utxoList.message)
                    val utxos = (utxoList as Entity.Success).data

                    // All utxos sum
                    var totalInput = Coin.ZERO
                    utxos.forEach { utxo ->
                        totalInput += Coin.valueOf(utxo.value)
                    }
                    val fee = Coin.valueOf(380L) // TODO


                    val tx = Transaction()

                    // Recipient output
                    tx.addOutput(sendAmount, toAddress)

                    // Change output
                    val changeSat = totalInput.subtract(sendAmount).subtract(fee)
                    Log.d("123", "$sendAmount $fee $changeSat")
                    if (changeSat.isPositive) {
                        tx.addOutput(changeSat, privateKeyWif.toAddress(scriptType, network))
                    }

                    // Utxos inputs
                    utxos.forEach { u ->
                        val txid = u.txid
                        val vout = u.vout.toLong()
                        val valSat = u.value

                        val outPoint = TransactionOutPoint(
                            vout, Sha256Hash.wrap(txid)
                        )

                        val scriptPubKey = ScriptBuilder
                            .createOutputScript(addressParser.parseAddress(fromAddress))
                        tx.addSignedInput(
                            outPoint,
                            scriptPubKey,
                            Coin.valueOf(valSat),
                            privateKeyWif,
                            Transaction.SigHash.ALL,
                            false
                        )
                    }

                    // Final hex
                    val hex = formatHex(tx.serialize())
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
}
