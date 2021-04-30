package com.example.cashgrab.models

import java.util.*

internal class User(var user_id: String, var role: String, var last_worked: Date, var last_steal: Date, var last_pm: Date, var last_gift: Date,
                    var games_left: Int, var first_game: Date, var extra_games: Boolean, var double_pm: Boolean, var cash: Int, var balance: Int,
                    var apple_stocks: Int, var tesla_stocks: Int, var microsoft_stocks: Int, var user_name: String) {
}