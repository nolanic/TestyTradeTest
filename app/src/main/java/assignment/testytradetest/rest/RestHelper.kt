package assignment.testytradetest

import assignment.testytradetest.models.HistoryPrice
import assignment.testytradetest.models.Quote
import assignment.testytradetest.models.SymbolDescription
import assignment.testytradetest.rest.CloudIexApi
import assignment.testytradetest.rest.IexApi
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.reflect.KFunction

object RestHelper {
    private val token : String
    private val iexApi : IexApi
    private val cloudIexApi : CloudIexApi
    private val callsInProgress = mutableSetOf<BaseCallback<*>>()

    init {
        token = App.getInstance().getString(R.string.iex_token)
        val httpClient = OkHttpClient.Builder()
            .addInterceptor(CallInterceptor())
            .build()

        iexApi = Retrofit.Builder()
            .baseUrl("https://mytastytradetest.iex.cloud/v1/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(httpClient)
            .build()
            .create(IexApi::class.java)

        cloudIexApi = Retrofit.Builder()
            .baseUrl("https://cloud.iexapis.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(httpClient)
            .build()
            .create(CloudIexApi::class.java)
    }

    private fun getCallInProgress(apiDescription:String) : BaseCallback<*>? {
        for (baseCallback in callsInProgress) {
            if (baseCallback.apiDescription == apiDescription) {
                return baseCallback
            }
        }
        return null
    }

    private fun <T>invokeApiCall(apiDescription: String, clientCallback : Callback<T>, apiFunction : KFunction<Call<T>>, vararg apiParameters : Any) {
        val baseCallbackInProgress = getCallInProgress(apiDescription)
        if (baseCallbackInProgress != null) {
            baseCallbackInProgress.addClient(clientCallback)
            return
        }

        val baseCallback = BaseCallback(apiDescription, clientCallback)
        callsInProgress.add(baseCallback)
        apiFunction.call(*apiParameters).enqueue(baseCallback)
    }

    fun getQuote(symbol : String, callback : Callback<Array<Quote>>) {
        val apiDescription = "getQuote|"+symbol
        invokeApiCall(apiDescription, callback, iexApi::getQuote, symbol)
    }

    fun getHistoricalPrices(symbol:String, count:Int = 30, callback: Callback<Array<HistoryPrice>>) {
        val apiDescription = "getHistoricalPrices|$symbol|$count"
        invokeApiCall(apiDescription, callback, iexApi::getHistoricalPrices, symbol, count)
    }

    fun getAllSymbolDescriptions(callback: Callback<Array<SymbolDescription>>) {
        val apiDescription = "allSymbolDescriptions"
        invokeApiCall(apiDescription, callback, cloudIexApi::getAllSymbolDescriptions)
    }

    private class CallInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
            val newUrl = chain.request().url().newBuilder().addQueryParameter("token", token).build()
            val newRequest = chain.request().newBuilder().url(newUrl).build()
            return chain.proceed(newRequest)
        }
    }

    private class BaseCallback<T> : retrofit2.Callback<T> {
        val apiDescription : String
        val clientCallbacks = mutableSetOf<Callback<T>>()

        constructor(apiDescription: String, callback: Callback<T>) {
            this.apiDescription = apiDescription;
            clientCallbacks.add(callback)
        }

        private fun notifyClientsAndDispose(result: T?, error: Throwable?) {
            for (callback in clientCallbacks) {
                callback.onResponse(result, error)
            }
            callsInProgress.remove(this)
        }

        override fun onResponse(call: Call<T>, response: Response<T>) {
            val result = response.body()
            if (result != null) {
                notifyClientsAndDispose(result, null)
                return
            }

            val error = response.errorBody()
            if (error != null) {
                val responseError = Throwable(error.toString())
                notifyClientsAndDispose(null, responseError)
                return
            }

            for (callback in clientCallbacks) {
                val error = Throwable("God knows what happened")
                notifyClientsAndDispose(null, error)
            }
        }

        override fun onFailure(call: Call<T>, t: Throwable) {
            notifyClientsAndDispose(null, t)
        }

        fun addClient(callback : Callback<*>) {
            clientCallbacks.add(callback as Callback<T>)
        }
    }

    interface Callback<T> {
        fun onResponse(result:T?, error:Throwable?)
    }
}