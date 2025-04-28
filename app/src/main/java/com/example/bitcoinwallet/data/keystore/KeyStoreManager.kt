package com.example.bitcoinwallet.data.keystore

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator.getInstance
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject

class KeyStoreManager @Inject constructor(
   private val keyAlias: String
) {
    private val provider = "AndroidKeyStore"
    private val ks = KeyStore.getInstance(provider).apply { load(null) }

    init {
        if (!ks.containsAlias(keyAlias)) {
            generateAesKey()
        }
    }

    private fun generateAesKey() {
        val keyGen = getInstance(
            KeyProperties.KEY_ALGORITHM_AES, provider
        )
        val spec = KeyGenParameterSpec.Builder(
            keyAlias,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .build()
        keyGen.init(spec)
        keyGen.generateKey()
    }

    fun encrypt(plain: ByteArray): ByteArray {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(
            Cipher.ENCRYPT_MODE,
            ks.getKey(keyAlias, null) as SecretKey
        )
        val cipherText = cipher.doFinal(plain)
        return cipher.iv + cipherText
    }

    fun decrypt(data: ByteArray): ByteArray {
        val iv = data.sliceArray(0 until 12)
        val cipherText = data.sliceArray(12 until data.size)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val spec = GCMParameterSpec(128, iv)
        cipher.init(
            Cipher.DECRYPT_MODE,
            ks.getKey(keyAlias, null) as SecretKey,
            spec
        )
        return cipher.doFinal(cipherText)
    }
}