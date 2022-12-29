package assignment.testytradetest.rest

import assignment.testytradetest.models.SymbolDescription
import retrofit2.Call
import retrofit2.http.GET

interface CloudIexApi {

    @GET("beta/ref-data/symbols")
    fun getAllSymbolDescriptions() : Call<Array<SymbolDescription>>
}