package assignment.testytradetest.viewModels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import assignment.testytradetest.InAppMessageDispatcher
import assignment.testytradetest.RestHelper
import assignment.testytradetest.models.HistoryPrice
import assignment.testytradetest.models.Quote
import assignment.testytradetest.rest.ContinuousQuoteGetter

class ViewModelChart : ViewModel() {
    private val quoteGetter = ContinuousQuoteGetter(QuoteCallback())

    val symbol:String = InAppMessageDispatcher.getAndDispose(this::class.simpleName!!)!!
    val prices = mutableStateListOf<HistoryPrice>()
    var quote by mutableStateOf(Quote())
    var showErrorDialog by mutableStateOf(false)

    init {
        quoteGetter.addTrackingSymbols(symbol)
        getPrices()
    }

    fun getPrices() {
        RestHelper.getHistoricalPrices(symbol = symbol, callback = PricesCallback())
    }

    fun startQuoteGetter() {
        quoteGetter.start()
    }

    fun stopQuoteGetter() {
        quoteGetter.stop()
    }

    private inner class PricesCallback : RestHelper.Callback<Array<HistoryPrice>> {

        override fun onResponse(result: Array<HistoryPrice>?, error: Throwable?) {
            if (result != null) {
                prices.addAll(result)
            } else if (error != null) {
                showErrorDialog = true
            }
        }
    }

    private inner class QuoteCallback : ContinuousQuoteGetter.ErrorHandledCallback() {
        override fun onQuoteResult(quote: Quote) {
            this@ViewModelChart.quote = quote
        }
    }
}