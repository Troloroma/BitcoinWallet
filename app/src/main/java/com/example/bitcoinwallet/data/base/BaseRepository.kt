package com.example.bitcoinwallet.data.base

import android.util.Log
import com.example.bitcoinwallet.network.utils.exceptions.LOCAL_ERROR_CODE
import com.example.bitcoinwallet.network.utils.exceptions.NO_ERROR_CODE
import com.example.bitcoinwallet.network.utils.exceptions.NO_NETWORK_ERROR_CODE
import com.example.bitcoinwallet.network.utils.exceptions.PARSE_ERROR
import com.example.bitcoinwallet.network.utils.exceptions.PARSE_ERROR_CODE
import com.example.bitcoinwallet.network.utils.models.ServerResponse
import com.example.bitcoinwallet.network.utils.models.ResponseStatus
import com.example.bitcoinwallet.network.utils.exceptions.NetworkException
import com.example.bitcoinwallet.network.utils.exceptions.NoNetworkException
import com.example.bitcoinwallet.network.utils.models.Error
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import retrofit2.HttpException
import retrofit2.Response
import java.net.UnknownHostException

typealias Request<T> = suspend () -> Response<ServerResponse<T>>

open class BaseRepository {

    protected suspend fun <K : Any> safeApiSuspendResultNoResponse(call: suspend () -> Response<K>?): ResponseStatus<K> {
        val response: Response<K>?
        try {
            response = call.invoke()
            if (response != null && response.isSuccessful) {
                return ResponseStatus.Success(response.body(), response.code())
            }
            val errorBody: Error? = response?.errorBody()?.parseError()
            return ResponseStatus.ServerError(
                NetworkException(
                    errorBody?.message, Throwable(REPOSITORY), errorBody?.code ?: NO_ERROR_CODE
                )
            )
        } catch (e: Exception) {
            Log.e("BaseRepository: ", e.message.toString())
            when (e) {
                is UnknownHostException -> return ResponseStatus.LocalError(
                    null,
                    NoNetworkException(cause = Throwable(REPOSITORY), message = e.message),
                    NO_NETWORK_ERROR_CODE
                )

                is JsonSyntaxException -> {
                    Log.e("BaseRepository: ", e.message.toString())
                    return ResponseStatus.ServerError(
                        NetworkException(
                            e.message, Throwable(REPOSITORY), PARSE_ERROR_CODE
                        )
                    )
                }

                is HttpException -> return ResponseStatus.LocalError(
                    null,
                    e.response()?.errorBody()?.parseError() ?: NullPointerException(),
                    e.code()
                )

                else -> return ResponseStatus.LocalError(null, e, LOCAL_ERROR_CODE)
            }
        }
    }

    protected suspend fun <G : Any> map(call: suspend () -> G): G? {
        return withContext(Dispatchers.Default) {
            try {
                call.invoke()
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    companion object {
        private const val REPOSITORY = "REPOSITORY"
    }
}

fun ResponseBody.parseError(): Error {
    return try {
        val gson = Gson()
        val type = object : TypeToken<Error>() {}.type
        gson.fromJson(charStream(), type)
    } catch (e: JsonSyntaxException) {
        Error("", PARSE_ERROR, e.message)
    }
}
