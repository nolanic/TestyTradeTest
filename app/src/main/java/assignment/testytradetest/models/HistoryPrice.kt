package assignment.testytradetest.models

class HistoryPrice {
    val symbol = ""
    val low : Float? = null
    val high : Float? = null
    val open : Float? = null
    val close : Float? = null
    val priceDate = ""

    fun topPrice() : Float {
        if (open?:0f > close?:0f)
            return open?:0f
        else
            return close?:0f
    }

    fun bottomPrice() : Float {
        if (open?:0f < close?:0f)
            return open?:0f
        else
            return close?:0f
    }

    fun highestPrice() : Float {
        var maxPrice = 0f
        if (high != null && high > maxPrice) {
            maxPrice = high
        }
        if (topPrice() > maxPrice) {
            maxPrice = topPrice()
        }
        if (low != null && low > maxPrice) {
            maxPrice = low
        }
        return maxPrice
    }

    fun lowestPrice() : Float {
        var minPrice = Float.POSITIVE_INFINITY
        if (high != null && high < minPrice) {
            minPrice = high
        }
        if (bottomPrice() < minPrice) {
            minPrice = bottomPrice()
        }
        if (low != null && low < minPrice) {
            minPrice = low
        }
        return minPrice
    }

    fun getType() : Type? {
        if (open != null && close != null) {
            if (close > open) {
                return Type.ASCENDING
            } else if (close < open) {
                return Type.DESCENDING
            } else {
                return Type.STEADY
            }
        } else if (open != null || close != null) {
            return Type.STEADY
        } else {
            return null
        }
    }

    enum class Type {
        ASCENDING, DESCENDING, STEADY
    }
}