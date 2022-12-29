package assignment.testytradetest.models

import java.io.Serializable

class WatchList(val name : String) : Serializable {
    val symbols = mutableListOf<String>()

    companion object {
        private const val serialVersionUID = 0L

        fun createDefault() : WatchList {
            val watchList = WatchList("My first list")
            watchList.symbols.addAll(arrayOf("AAPL", "MSFT", "GOOG"))
            return watchList
        }
    }
}