package assignment.testytradetest.viewModels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import assignment.testytradetest.InAppMessageDispatcher
import assignment.testytradetest.RestHelper
import assignment.testytradetest.models.SymbolDescription
import assignment.testytradetest.models.WatchList

class ViewModelAddQuotes : ViewModel() {
    private val symbolDescriptions = mutableListOf<SymbolDescription>()
    private val watchlist = InAppMessageDispatcher.getAndDispose<WatchList>(this::class.simpleName!!)!!
    val filteredSymbolDescriptions = mutableStateListOf<SymbolDescription>()
    val selectedSymbols = mutableStateListOf<SymbolDescription>()

    var showInternetError by mutableStateOf(false)
    var searchSymbol by mutableStateOf("")
    var searchName by mutableStateOf("")

    init {
        requestData()
    }

    fun applySearchFilter(searchSymbol:String = this.searchSymbol, searchName:String = this.searchName) {
        this.searchSymbol = searchSymbol
        this.searchName = searchName

        filteredSymbolDescriptions.clear()
        filteredSymbolDescriptions.addAll(
            symbolDescriptions.filter {
                it.symbol.contains(searchSymbol, true) && it.name.contains(searchName, true)
        })
    }

    fun changeSelection(symbolDescription: SymbolDescription, selected:Boolean) {
        if (selected) {
            selectedSymbols.add(symbolDescription)
        } else {
            selectedSymbols.remove(symbolDescription)
        }
    }

    fun addSymbolsToTrack() {
        watchlist.symbols.addAll(selectedSymbols.map { it.symbol })
        InAppMessageDispatcher.broadcastMessage(MessageSymbolsAdded(), this::class.simpleName!!)
    }

    fun requestData() {
        showInternetError = false
        RestHelper.getAllSymbolDescriptions(SymbolDescriptionsCallback())
    }

    private inner class SymbolDescriptionsCallback : RestHelper.Callback<Array<SymbolDescription>> {

        override fun onResponse(result: Array<SymbolDescription>?, error: Throwable?) {
            if (result != null) {
                symbolDescriptions.clear()
                symbolDescriptions.addAll(result.filter {
                    !it.symbol.isEmpty() && !watchlist.symbols.contains(it.symbol)
                })

                applySearchFilter()
            } else if (error != null) {
                showInternetError = true
            }
        }
    }

    class MessageSymbolsAdded
}