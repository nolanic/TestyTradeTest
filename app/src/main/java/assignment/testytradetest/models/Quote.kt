package assignment.testytradetest.models

import com.google.gson.annotations.SerializedName

class Quote {
    val symbol = ""

    @SerializedName("iexBidPrice")
    val bidPrice : Float? = null

    @SerializedName("iexAskPrice")
    val askPrice : Float? = null

    @SerializedName("latestPrice")
    val lastPrice : Float? = null
}