package com.example.cashgrab.models

import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

data class Stock(
    @SerializedName("symbol") val symbol: String,
                 @SerializedName("price") val price: Double,
                 @SerializedName("volume") val volume : Int
) {
    class Deserializer: ResponseDeserializable<Array<Stock>> {
        override fun deserialize(content: String): Array<Stock>? = Gson().fromJson(content, Array<Stock>::class.java)
    }
}