package assignment.testytradetest.rest

import android.os.SystemClock
import android.util.Log
import android.widget.Toast
import assignment.testytradetest.App
import assignment.testytradetest.R
import assignment.testytradetest.RestHelper
import assignment.testytradetest.models.Quote
import kotlinx.coroutines.*

class ContinuousQuoteGetter(private val clientCallback: Callback) {
    private val DELAY = 5000L
    private val trackingSymbols = mutableListOf<String>()
    private var runningJob : Job? = null
    private var quoteCallback : QuoteCallback? = null

    fun start() {
        if (runningJob != null) {
            return
        }
        Log.d("atf", "Quote getter started")

        quoteCallback = QuoteCallback()
        runningJob = CoroutineScope(Dispatchers.Default).launch {
            while (isActive) {
                CoroutineScope(Dispatchers.Main).launch {
                    for (symbol in trackingSymbols) {
                        RestHelper.getQuote(symbol, quoteCallback!!)
                    }
                    Log.d("atf", "Quotes requested")
                }.join()

                if (isActive) {
                    delay(DELAY)
                }
            }
        }
    }

    fun stop() {
        if (runningJob == null) {
            return
        }
        runningJob!!.cancel()
        runningJob = null
        quoteCallback = null
        Log.d("atf", "Quote getter stopped")
    }

    fun addTrackingSymbols(vararg symbols:String) {
        trackingSymbols.addAll(symbols)
    }

    fun removeSymbolsFromTracking(vararg symbols: String) {
        trackingSymbols.removeAll(symbols)
    }

    fun clearAllTrackingSymbols() {
        trackingSymbols.clear()
    }

    private inner class QuoteCallback : RestHelper.Callback<Array<Quote>> {

        override fun onResponse(result: Array<Quote>?, error: Throwable?) {
            if (quoteCallback != this) {
                return
            }

            if (result != null && result.size > 0) {
                val quote = result[0]
                if (!quote.symbol.isEmpty()) {
                    Log.d("atf", "Quote received: "+quote.symbol+" "+Thread.currentThread().name)
                    clientCallback.onQuoteResult(quote, null)
                }
            } else if (error != null) {
                clientCallback.onQuoteResult(null, error)
            }
        }
    }

    interface Callback {
        fun onQuoteResult(quote: Quote?, error: Throwable?)
    }

    abstract class ErrorHandledCallback : Callback {
        private val errorShowInterval = 5000L
        private var lastErrorTime = 0L

        override fun onQuoteResult(quote: Quote?, error: Throwable?) {
            if (quote != null) {
                onQuoteResult(quote)
            } else if (error != null){
                val currentTime = SystemClock.elapsedRealtime()
                if (lastErrorTime + errorShowInterval < currentTime) {
                    Toast.makeText(App.getInstance().baseContext, R.string.internet_connection, Toast.LENGTH_SHORT).show()
                    lastErrorTime = currentTime
                }
            }
        }

        abstract fun onQuoteResult(quote: Quote)
    }
}