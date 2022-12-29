package assignment.testytradetest.rest

import assignment.testytradetest.models.HistoryPrice
import assignment.testytradetest.models.Quote
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface IexApi {

    @GET("data/core/quote/{symbol}")
    fun getQuote(@Path("symbol") symbol:String) : Call<Array<Quote>>

    @GET("data/core/historical_prices/{symbol}")
    fun getHistoricalPrices(@Path("symbol") symbol: String, @Query("last") count:Int) : Call<Array<HistoryPrice>>
}