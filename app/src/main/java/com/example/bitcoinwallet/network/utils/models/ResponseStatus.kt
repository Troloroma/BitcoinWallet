package com.example.bitcoinwallet.network.utils.models

import com.example.bitcoinwallet.network.utils.exceptions.NetworkException

sealed class ResponseStatus<out T : Any> {
    data class Success<out T : Any>(val data: T?, val code: Int) : ResponseStatus<T>() {
        override fun <K : Any> map(mapper: (oldValue: T?) -> K?): Success<K> {
            return Success(mapper.invoke(data), code)
        }
    }

    data class ServerError<out T : Any>(val exception: NetworkException, val localData: T? = null) :
        ResponseStatus<T>() {
        override fun <K : Any> map(mapper: (oldValue: T?) -> K?): ServerError<K> {
            return ServerError(exception, mapper.invoke(localData))
        }
    }

    data class LocalError<out T : Any>(
        val localData: T?,
        val exception: Exception,
        val code: Int,
        val message: String = ""
    ) :
        ResponseStatus<T>() {
        override fun <K : Any> map(mapper: (oldValue: T?) -> K?): LocalError<K> {
            return LocalError(mapper.invoke(localData), exception, code)
        }
    }

    fun getSuccessData(): T? {
        return when (this) {
            is Success -> data
            else -> null
        }
    }

    abstract fun <K : Any> map(mapper: (oldValue: T?) -> K?): ResponseStatus<K>
}
