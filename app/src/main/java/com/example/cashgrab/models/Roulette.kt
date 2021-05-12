package com.example.cashgrab.models

import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

data class Roulette(
    @SerializedName("success") val success: Boolean,
    @SerializedName("roll") val roll: Roll,
    @SerializedName("bet") val bet : Bet
){
    class Deserializer: ResponseDeserializable<Roulette> {
        override fun deserialize(content: String): Roulette? = Gson().fromJson(content, Roulette::class.java)
    }
}

data class Roll(
    @SerializedName("number") val number : Int,
    @SerializedName("color") val color : String,
    @SerializedName("parity") val parity: String
){
    class Deserializer: ResponseDeserializable<Roll> {
        override fun deserialize(content: String): Roll? = Gson().fromJson(content, Roll::class.java)
    }
}

data class Bet(
    @SerializedName("bet") val bet : String,
    @SerializedName("wager") val wager : Int,
    @SerializedName("win") val win : Boolean,
    @SerializedName("payout_rate") val payout_rate : Int,
    @SerializedName("payout") val payout : Double
){
    class Deserializer: ResponseDeserializable<Bet> {
        override fun deserialize(content: String): Bet? = Gson().fromJson(content, Bet::class.java)
    }
}