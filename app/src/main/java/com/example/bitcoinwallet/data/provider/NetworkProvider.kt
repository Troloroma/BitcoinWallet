package com.example.bitcoinwallet.data.provider

import com.example.bitcoinwallet.data.factories.EnumConverterFactory
import com.example.bitcoinwallet.data.factories.PlainTextConverterFactory
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class NetworkProvider(
    private val host: String,
) {
    private val client = OkHttpClient.Builder().apply {
        readTimeout(READ_TIME_OUT, TimeUnit.SECONDS)
        connectTimeout(
            CONNECT_TIME_OUT, TimeUnit.SECONDS
        ).addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
    }.build()

    fun <T> provideRetrofit(
        clazz: Class<T>,
    ): T {
        return Retrofit.Builder().baseUrl(host).client(client)
            .addConverterFactory(PlainTextConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().setLenient().create()))
            .addConverterFactory(EnumConverterFactory())
            .build().create(clazz)
    }

    companion object {
        private const val CONNECT_TIME_OUT: Long = 30
        private const val READ_TIME_OUT: Long = 15
    }
}
