package assignment.testytradetest.viewModels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import assignment.testytradetest.InAppMessageDispatcher
import assignment.testytradetest.Utils
import assignment.testytradetest.models.Quote
import assignment.testytradetest.models.WatchList
import assignment.testytradetest.removeIfCompat
import assignment.testytradetest.rest.ContinuousQuoteGetter

class ViewModelWatchlist : ViewModel() {

    companion object {
        private const val WATCHLIST_FILE = "watchlists.ttf"
    }

    val watchLists = mutableStateListOf<WatchList>()
    var selectedWatchListIndex by mutableStateOf(-1)
    var quotes = mutableStateListOf<Quote>()
    var showCreateWatchlistDialog by mutableStateOf(false)

    private val quoteGetter = ContinuousQuoteGetter(QuoteCallback())

    init {
        var restoredWatchLists : MutableList<WatchList>? = Utils.getObjectFromFile(WATCHLIST_FILE)
        if (restoredWatchLists != null) {
            watchLists.addAll(restoredWatchLists)
        } else {
            watchLists.add(WatchList.createDefault())
        }
        if (watchLists.size > 0) {
            selectedWatchListIndex = 0
        }
    }

    fun getSelectedWatchList() : WatchList? {
        if (selectedWatchListIndex >= 0) {
            return watchLists[selectedWatchListIndex]
        } else {
            return null
        }
    }

    fun removeQuote(quote: Quote) {
        quotes.remove(quote)
        val watchList = getSelectedWatchList()
        watchList?.symbols?.removeIfCompat { it == quote.symbol }
        quoteGetter.removeSymbolsFromTracking(quote.symbol)
        if (watchList == null || watchList.symbols.isEmpty()) {
            stopGettingQuotes()
        }
    }

    fun createWatchList(name:String) {
        if (name.isEmpty()) {
            return
        }

        var watchList = watchLists.firstOrNull { it.name == name }
        if (watchList == null) {
            watchList = WatchList(name)
            watchLists.add(watchList)
        }
        selectWatchList(watchList)
        showCreateWatchlistDialog = false
    }

    fun selectWatchList(watchList: WatchList) {
        selectWatchList(watchLists.indexOf(watchList))
    }

    fun selectWatchList(index : Int) {
        selectedWatchListIndex = index
        startGettingQuotes()
    }

    fun startGettingQuotes() {
        stopGettingQuotes()

        quotes.clear()
        val symbols = getSelectedWatchList()?.symbols?.toTypedArray()
        if (symbols != null && symbols.isNotEmpty()) {
            quoteGetter.addTrackingSymbols(*symbols)
            quoteGetter.start()
        }
    }

    fun stopGettingQuotes() {
        quoteGetter.stop()
        quoteGetter.clearAllTrackingSymbols()
    }

    fun saveState() {
        val sWatchLists = ArrayList<WatchList>()
        sWatchLists.addAll(watchLists)
        Utils.writeObjectToFile(WATCHLIST_FILE, sWatchLists)
    }

    fun requestMoreQuotes() {
        val watchList = getSelectedWatchList()
        if (watchList != null) {
            val message = MessageAddQuotes(watchList)
            InAppMessageDispatcher.broadcastMessage(message, this::class.simpleName!!)
        }
    }

    fun showChart(quote: Quote) {
        InAppMessageDispatcher.broadcastMessage(MessageShowChart(quote.symbol), this::class.simpleName!!)
    }

    fun deleteWatchlist() {
        if (selectedWatchListIndex == -1) {
            return
        }

        watchLists.removeAt(selectedWatchListIndex)
        if (selectedWatchListIndex > watchLists.lastIndex) {
            selectedWatchListIndex --
        }
        selectWatchList(selectedWatchListIndex)
    }

    private inner class QuoteCallback : ContinuousQuoteGetter.ErrorHandledCallback() {

        override fun onQuoteResult(quote: Quote) {
            val indexOfExistingQuote = quotes.indexOfFirst { it.symbol == quote.symbol }
            if (indexOfExistingQuote != -1) {
                quotes[indexOfExistingQuote] = quote
            } else if (getSelectedWatchList()?.symbols?.contains(quote.symbol) ?: false) {
                quotes.add(quote)
                quotes.sortBy {it.symbol}
            }
        }
    }

    data class MessageAddQuotes(val watchList: WatchList)
    data class MessageShowChart(val symbol: String)
}