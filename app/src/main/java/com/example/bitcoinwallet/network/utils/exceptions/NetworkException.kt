package com.example.bitcoinwallet.network.utils.exceptions

class NetworkException(
    override val message: String?,
    override val cause: Throwable,
    val code: Int,
) : Exception(message, cause)

class NoNetworkException(
    override val message: String?,
    override val cause: Throwable
) : Exception(message, cause)
