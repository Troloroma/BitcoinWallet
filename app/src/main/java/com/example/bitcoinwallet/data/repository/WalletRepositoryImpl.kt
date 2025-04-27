package com.example.bitcoinwallet.data.repository

import android.util.Base64
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.bitcoinwallet.common.Entity
import com.example.bitcoinwallet.data.model.WalletModel
import com.example.bitcoinwallet.features.main.domain.WalletRepository
import kotlinx.coroutines.flow.first
import org.bitcoinj.base.BitcoinNetwork
import org.bitcoinj.base.ScriptType
import org.bitcoinj.crypto.DumpedPrivateKey
import org.bitcoinj.crypto.ECKey
import javax.inject.Inject

class WalletRepositoryImpl @Inject constructor(
    private val keyStoreManager: KeyStoreManager,
    private val dataStore: DataStore<Preferences>
) : WalletRepository {

    private val network = BitcoinNetwork.SIGNET
    private val wifKey = stringPreferencesKey("encrypted_wif")

    override suspend fun getWallet(): Entity<WalletModel> {
        try {
            val prefs = dataStore.data.first()
            val encrypted = prefs[wifKey]
            val wif: String = if (encrypted.isNullOrEmpty()) {
                val ecKey = ECKey()
                val rawWif = ecKey.getPrivateKeyAsWiF(network)
                val cipherText = keyStoreManager.encrypt(rawWif.toByteArray())
                dataStore.edit { settings ->
                    settings[wifKey] = Base64.encodeToString(cipherText, Base64.DEFAULT)
                }

                rawWif
            } else {
                val cipherBytes = Base64.decode(encrypted, Base64.DEFAULT)

                String(keyStoreManager.decrypt(cipherBytes))
            }

            val dumpedPriv = DumpedPrivateKey.fromBase58(network, wif)
            val ecKey = dumpedPriv.key

            val address = ecKey
                .toAddress(ScriptType.P2WPKH, network)
                .toString()
            Log.d("address", address)

            return Entity.Success(WalletModel(address = address, privateKeyWif = wif))
        } catch (e: Exception) {
            return Entity.Error(message = "Error while getting wallet")
        }
    }
}