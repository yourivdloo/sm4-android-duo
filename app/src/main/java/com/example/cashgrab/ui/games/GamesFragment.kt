package com.example.cashgrab.ui.games

import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import com.example.cashgrab.R
import com.example.cashgrab.databinding.FragmentGamesBinding
import com.example.cashgrab.models.Roulette
import com.example.cashgrab.ui.MainActivity
import com.github.kittinunf.fuel.Fuel
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.lang.Long.parseLong
import java.util.*
import kotlin.math.truncate


class GamesFragment : Fragment() {
    private lateinit var gameViewModel: GamesViewModel
    private lateinit var binding: FragmentGamesBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        gameViewModel =
            ViewModelProvider(this).get(GamesViewModel::class.java)

        binding = FragmentGamesBinding.inflate(layoutInflater)
        auth = Firebase.auth
        val currentUser = auth.currentUser

        var firstGame: Timestamp = Timestamp.now()
        var gamesLeft: Long = 0
        var cash: Long = 0
        var userRef: DocumentReference? = null

        val db = Firebase.firestore
        db.collection("users").whereEqualTo("user_id", currentUser.uid).get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    var result = task.result?.documents?.get(0)
                    if (result != null) {
                        cash = result.data?.get("cash") as Long
                        val id = result.id
                        userRef = db.collection("users").document(id)
                        Log.d("RESULT", result.data?.get("user_name").toString())

                        firstGame = result.data?.get("first_game") as Timestamp
                        gamesLeft = result.data?.get("games_left") as Long
                        val currentTime = Date().time
                        val newGames = firstGame.toDate().time + 1800000
                        val timeUntilReset = currentTime - newGames

                        println(currentTime.toString())
                        println(newGames.toString())
                        println(timeUntilReset.toString())

                        if (timeUntilReset > 0) {
                            gamesLeft = 5
                            binding.textGamesLeft.text = "You have " + gamesLeft + " games left!"
                            binding.textCooldown.text = "Reset in 30m after next game"
                            binding.buttonRoulette.isEnabled = true
                            userRef!!.update("games_left", gamesLeft)
                        } else {
                            val minUntilReset =
                                Math.abs(truncate((timeUntilReset / 1000 / 60).toDouble()).toInt())
                            binding.textGamesLeft.text = "You have " + gamesLeft + " games left!"
                            binding.textCooldown.text = "Reset in " + minUntilReset + "m"
                        }

                        if (gamesLeft.toInt() == 5) {
                            binding.textCooldown.text = "Reset in 30m after next game"
                        }
                        binding.textGamesCash.text = "Cash: €" + cash + ",-"
                        if (gamesLeft > 0) {
                            binding.buttonRoulette.isEnabled = true
                        }
                    }

                }
            }

        binding.buttonRoulette.setOnClickListener {
            var myDialog: Dialog
            myDialog = Dialog(this.requireContext())
            myDialog.setContentView(R.layout.roulette_game);
            myDialog.show()

            val textStakes = myDialog.findViewById<EditText>(R.id.textStakes)
            val buttonAll = myDialog.findViewById<Button>(R.id.buttonBetAll)
            val buttonRed = myDialog.findViewById<Button>(R.id.buttonRed)
            val buttonBlack = myDialog.findViewById<Button>(R.id.buttonBlack)
            val buttonEven = myDialog.findViewById<Button>(R.id.buttonEven)
            val buttonOdd = myDialog.findViewById<Button>(R.id.buttonOdd)
            val button1to12 = myDialog.findViewById<Button>(R.id.button1to12)
            val button13to24 = myDialog.findViewById<Button>(R.id.button13to24)
            val button25to36 = myDialog.findViewById<Button>(R.id.button25to36)
            val buttonCustomBet = myDialog.findViewById<Button>(R.id.buttonCustomBet)
            val textCustomBet = myDialog.findViewById<EditText>(R.id.textCustomBet)
            val buttonBack = myDialog.findViewById<ImageButton>(R.id.buttonBack5)

            var check1 : Boolean = false
            var check2 : Boolean = false

            buttonBack.setOnClickListener {
                myDialog.dismiss()
            }

            textStakes.addTextChangedListener { text ->
                if (text != null && text.toString().isNotEmpty()) {
                    val input: Long = parseLong(text.toString())
                    if (input > cash) {
                        textStakes.setText(cash.toString())
                    } else {
                        buttonRed.isEnabled = true
                        buttonBlack.isEnabled = true
                        buttonEven.isEnabled = true
                        buttonOdd.isEnabled = true
                        button1to12.isEnabled = true
                        button13to24.isEnabled = true
                        button25to36.isEnabled = true
                        check1 = true
                        if(check1 && check2){ buttonCustomBet.isEnabled = true }
                    }
                } else {
                    buttonRed.isEnabled = false
                    buttonBlack.isEnabled = false
                    buttonEven.isEnabled = false
                    buttonOdd.isEnabled = false
                    button1to12.isEnabled = false
                    button13to24.isEnabled = false
                    button25to36.isEnabled = false
                    check1 = false
                    if(!check1 || !check2){ buttonCustomBet.isEnabled = false }
                }
            }

            textCustomBet.addTextChangedListener { text ->
                if (text != null && text.toString().isNotEmpty()) {
                    val input: Long = parseLong(text.toString())
                    if (input > 36) {
                        textStakes.setText(0)
                    } else {
                        check2 = true
                        if(check1 && check2) { buttonCustomBet.isEnabled = true }
                    }
                } else {
                    check2 = false
                    if(!check1 || !check2){ buttonCustomBet.isEnabled = false }
                }
            }

            buttonRed.setOnClickListener {
                if (textStakes.text.toString().isNotEmpty() && textStakes.text.toString() != "0") {
                    if (!textStakes.text.toString().contains("-")) {
                        val url =
                            "http://roulette.rip/api/play?bet=red&wager=" + textStakes.text.toString()
                        Fuel.get(url).responseObject(Roulette.Deserializer()) { _, _, result ->
                            val (roulette, _) = result

                            if (roulette != null) {
                                var payout = roulette?.bet?.payout
                                var outcome = "You have lost your bet."
                                if (roulette?.bet?.win == true) {
                                    outcome = "You have won €" + payout.toInt().toString() + ",-"
                                }
                                var finalResult =
                                    "The ball has landed on " + roulette?.roll?.color + " " + roulette?.roll?.number + ". " + outcome

                                if (gamesLeft.toInt() == 5) {
                                    firstGame = Timestamp.now()
                                }

                                cash =
                                    (cash.toInt() - roulette?.bet?.wager!! + payout!!.toInt()).toLong()
                                gamesLeft = (gamesLeft.toInt() - 1).toLong()

                                userRef!!
                                    .update(
                                        "cash",
                                        cash,
                                        "games_left",
                                        gamesLeft,
                                        "first_game",
                                        firstGame
                                    )
                                    .addOnSuccessListener {
                                        Log.d("update", "updated user")

                                        binding.textGamesCash.text =
                                            "Cash: €" + cash.toString() + ",-"
                                        binding.textGamesLeft.text =
                                            "You have " + gamesLeft.toString() + " games left!"
                                        val currentTime = Date().time
                                        val newGames = firstGame.toDate().time + 1800000
                                        val timeUntilReset = currentTime - newGames
                                        val minUntilReset =
                                            Math.abs(truncate((timeUntilReset / 1000 / 60).toDouble()).toInt())
                                        binding.textCooldown.text =
                                            "Reset in " + minUntilReset + "m"

                                        if (gamesLeft <= 0) {
                                            binding.buttonRoulette.isEnabled = false
                                        }
                                        resultToast(finalResult)
                                        myDialog.dismiss()
                                    }
                            }
                        }
                    } else {
                        poorToast()
                        myDialog.dismiss()
                    }
                } else {
                    noBetToast()
                }
            }

            buttonBlack.setOnClickListener {
                if (textStakes.text.toString().isNotEmpty() && textStakes.text.toString() != "0") {
                    if (!textStakes.text.toString().contains("-")) {
                        val url =
                            "http://roulette.rip/api/play?bet=black&wager=" + textStakes.text.toString()
                        Fuel.get(url).responseObject(Roulette.Deserializer()) { _, _, result ->
                            val (roulette, _) = result

                            if (roulette != null) {
                                var payout = roulette?.bet?.payout
                                var outcome = "You have lost your bet."
                                if (roulette?.bet?.win == true) {
                                    outcome = "You have won €" + payout.toInt().toString() + ",-"
                                }
                                var finalResult =
                                    "The ball has landed on " + roulette?.roll?.color + " " + roulette?.roll?.number + ". " + outcome

                                if (gamesLeft.toInt() == 5) {
                                    firstGame = Timestamp.now()
                                }

                                cash =
                                    (cash.toInt() - roulette?.bet?.wager!! + payout!!.toInt()).toLong()
                                gamesLeft = (gamesLeft.toInt() - 1).toLong()

                                userRef!!
                                    .update(
                                        "cash",
                                        cash,
                                        "games_left",
                                        gamesLeft,
                                        "first_game",
                                        firstGame
                                    )
                                    .addOnSuccessListener {
                                        Log.d("update", "updated user")

                                        binding.textGamesCash.text =
                                            "Cash: €" + cash.toString() + ",-"
                                        binding.textGamesLeft.text =
                                            "You have " + gamesLeft.toString() + " games left!"
                                        val currentTime = Date().time
                                        val newGames = firstGame.toDate().time + 1800000
                                        val timeUntilReset = currentTime - newGames
                                        val minUntilReset =
                                            Math.abs(truncate((timeUntilReset / 1000 / 60).toDouble()).toInt())
                                        binding.textCooldown.text =
                                            "Reset in " + minUntilReset + "m"

                                        if (gamesLeft <= 0) {
                                            binding.buttonRoulette.isEnabled = false
                                        }
                                        resultToast(finalResult)
                                        myDialog.dismiss()
                                    }
                            }
                        }
                    } else {
                        poorToast()
                        myDialog.dismiss()
                    }
                } else {
                    noBetToast()
                }
            }

            buttonEven.setOnClickListener {
                if (textStakes.text.toString().isNotEmpty() && textStakes.text.toString() != "0") {
                    if (!textStakes.text.toString().contains("-")) {
                        val url =
                            "http://roulette.rip/api/play?bet=even&wager=" + textStakes.text.toString()
                        Fuel.get(url).responseObject(Roulette.Deserializer()) { _, _, result ->
                            val (roulette, _) = result

                            if (roulette != null) {
                                var payout = roulette?.bet?.payout
                                var outcome = "You have lost your bet."
                                if (roulette?.bet?.win == true) {
                                    outcome = "You have won €" + payout.toInt().toString() + ",-"
                                }
                                var finalResult =
                                    "The ball has landed on " + roulette?.roll?.color + " " + roulette?.roll?.number + ". " + outcome

                                if (gamesLeft.toInt() == 5) {
                                    firstGame = Timestamp.now()
                                }

                                cash =
                                    (cash.toInt() - roulette?.bet?.wager!! + payout!!.toInt()).toLong()
                                gamesLeft = (gamesLeft.toInt() - 1).toLong()

                                userRef!!
                                    .update(
                                        "cash",
                                        cash,
                                        "games_left",
                                        gamesLeft,
                                        "first_game",
                                        firstGame
                                    )
                                    .addOnSuccessListener {
                                        Log.d("update", "updated user")

                                        binding.textGamesCash.text =
                                            "Cash: €" + cash.toString() + ",-"
                                        binding.textGamesLeft.text =
                                            "You have " + gamesLeft.toString() + " games left!"
                                        val currentTime = Date().time
                                        val newGames = firstGame.toDate().time + 1800000
                                        val timeUntilReset = currentTime - newGames
                                        val minUntilReset =
                                            Math.abs(truncate((timeUntilReset / 1000 / 60).toDouble()).toInt())
                                        binding.textCooldown.text =
                                            "Reset in " + minUntilReset + "m"

                                        if (gamesLeft <= 0) {
                                            binding.buttonRoulette.isEnabled = false
                                        }
                                        resultToast(finalResult)
                                        myDialog.dismiss()
                                    }
                            }
                        }
                    } else {
                        poorToast()
                        myDialog.dismiss()
                    }
                } else {
                    noBetToast()
                }
            }

            buttonOdd.setOnClickListener {
                if (textStakes.text.toString().isNotEmpty() && textStakes.text.toString() != "0") {
                    if (!textStakes.text.toString().contains("-")) {
                        val url =
                            "http://roulette.rip/api/play?bet=odd&wager=" + textStakes.text.toString()
                        Fuel.get(url).responseObject(Roulette.Deserializer()) { _, _, result ->
                            val (roulette, _) = result

                            if (roulette != null) {
                                var payout = roulette?.bet?.payout
                                var outcome = "You have lost your bet."
                                if (roulette?.bet?.win == true) {
                                    outcome = "You have won €" + payout.toInt().toString() + ",-"
                                }
                                var finalResult =
                                    "The ball has landed on " + roulette?.roll?.color + " " + roulette?.roll?.number + ". " + outcome

                                if (gamesLeft.toInt() == 5) {
                                    firstGame = Timestamp.now()
                                }

                                cash =
                                    (cash.toInt() - roulette?.bet?.wager!! + payout!!.toInt()).toLong()
                                gamesLeft = (gamesLeft.toInt() - 1).toLong()

                                userRef!!
                                    .update(
                                        "cash",
                                        cash,
                                        "games_left",
                                        gamesLeft,
                                        "first_game",
                                        firstGame
                                    )
                                    .addOnSuccessListener {
                                        Log.d("update", "updated user")

                                        binding.textGamesCash.text =
                                            "Cash: €" + cash.toString() + ",-"
                                        binding.textGamesLeft.text =
                                            "You have " + gamesLeft.toString() + " games left!"
                                        val currentTime = Date().time
                                        val newGames = firstGame.toDate().time + 1800000
                                        val timeUntilReset = currentTime - newGames
                                        val minUntilReset =
                                            Math.abs(truncate((timeUntilReset / 1000 / 60).toDouble()).toInt())
                                        binding.textCooldown.text =
                                            "Reset in " + minUntilReset + "m"

                                        if (gamesLeft <= 0) {
                                            binding.buttonRoulette.isEnabled = false
                                        }
                                        resultToast(finalResult)
                                        myDialog.dismiss()
                                    }
                            }
                        }
                    } else {
                        poorToast()
                        myDialog.dismiss()
                    }
                } else {
                    noBetToast()
                }
            }

            buttonCustomBet.setOnClickListener {
                if (textStakes.text.toString().isNotEmpty() && textStakes.text.toString() != "0") {
                    if (!textStakes.text.toString().contains("-")) {
                        if (textCustomBet.text.toString().isNotEmpty()) {
                            val url =
                                "http://roulette.rip/api/play?bet=" + textCustomBet.text.toString() + "&wager=" + textStakes.text.toString()
                            Fuel.get(url).responseObject(Roulette.Deserializer()) { _, _, result ->
                                val (roulette, _) = result

                                if (roulette != null) {
                                    var payout = roulette?.bet?.payout
                                    var outcome = "You have lost your bet."
                                    if (roulette?.bet?.win == true) {
                                        outcome =
                                            "You have won €" + payout.toInt().toString() + ",-"
                                    }
                                    var finalResult =
                                        "The ball has landed on " + roulette?.roll?.color + " " + roulette?.roll?.number + ". " + outcome

                                    if (gamesLeft.toInt() == 5) {
                                        firstGame = Timestamp.now()
                                    }

                                    cash =
                                        (cash.toInt() - roulette?.bet?.wager!! + payout!!.toInt()).toLong()
                                    gamesLeft = (gamesLeft.toInt() - 1).toLong()

                                    userRef!!
                                        .update(
                                            "cash",
                                            cash,
                                            "games_left",
                                            gamesLeft,
                                            "first_game",
                                            firstGame
                                        )
                                        .addOnSuccessListener {
                                            Log.d("update", "updated user")

                                            binding.textGamesCash.text =
                                                "Cash: €" + cash.toString() + ",-"
                                            binding.textGamesLeft.text =
                                                "You have " + gamesLeft.toString() + " games left!"
                                            val currentTime = Date().time
                                            val newGames = firstGame.toDate().time + 1800000
                                            val timeUntilReset = currentTime - newGames
                                            val minUntilReset =
                                                Math.abs(truncate((timeUntilReset / 1000 / 60).toDouble()).toInt())
                                            binding.textCooldown.text =
                                                "Reset in " + minUntilReset + "m"

                                            if (gamesLeft <= 0) {
                                                binding.buttonRoulette.isEnabled = false
                                            }
                                            resultToast(finalResult)
                                            myDialog.dismiss()
                                        }
                                }
                            }
                        } else {
                            Toast.makeText(
                                this.context,
                                "You have not provided an valid bet. Please check the bet field.",
                                Toast.LENGTH_SHORT
                            ).show();
                        }
                    } else {
                        poorToast()
                        myDialog.dismiss()
                    }
                } else {
                    noBetToast()
                }
            }

            button1to12.setOnClickListener {
                if (textStakes.text.toString().isNotEmpty() && textStakes.text.toString() != "0") {
                    if (!textStakes.text.toString().contains("-")) {
                        val url =
                            "http://roulette.rip/api/play?bet=red&wager=" + textStakes.text.toString()
                        Fuel.get(url).responseObject(Roulette.Deserializer()) { _, _, result ->
                            val (roulette, _) = result

                            if (roulette != null) {
                                var payout = 0
                                var outcome = "You have lost your bet."
                                if (roulette?.roll?.number >= 1 && roulette?.roll?.number <= 12) {
                                    payout = roulette?.bet?.wager!!.toInt() * 3
                                    println(payout.toString())
                                    outcome = "You have won €" + payout.toString() + ",-"
                                }

                                var finalResult =
                                    "The ball has landed on " + roulette?.roll?.color + " " + roulette?.roll?.number + ". " + outcome

                                if (gamesLeft.toInt() == 5) {
                                    firstGame = Timestamp.now()
                                }

                                cash =
                                    (cash.toInt() - roulette?.bet?.wager!! + payout!!.toInt()).toLong()
                                gamesLeft = (gamesLeft.toInt() - 1).toLong()

                                userRef!!
                                    .update(
                                        "cash",
                                        cash,
                                        "games_left",
                                        gamesLeft,
                                        "first_game",
                                        firstGame
                                    )
                                    .addOnSuccessListener {
                                        Log.d("update", "updated user")

                                        binding.textGamesCash.text =
                                            "Cash: €" + cash.toString() + ",-"
                                        binding.textGamesLeft.text =
                                            "You have " + gamesLeft.toString() + " games left!"
                                        val currentTime = Date().time
                                        val newGames = firstGame.toDate().time + 1800000
                                        val timeUntilReset = currentTime - newGames
                                        val minUntilReset =
                                            Math.abs(truncate((timeUntilReset / 1000 / 60).toDouble()).toInt())
                                        binding.textCooldown.text =
                                            "Reset in " + minUntilReset + "m"

                                        if (gamesLeft <= 0) {
                                            binding.buttonRoulette.isEnabled = false
                                        }
                                        resultToast(finalResult)
                                        myDialog.dismiss()
                                    }
                            }
                        }
                    } else {
                        poorToast()
                        myDialog.dismiss()
                    }
                } else {
                    noBetToast()
                }
            }

            button13to24.setOnClickListener {
                if (textStakes.text.toString().isNotEmpty() && textStakes.text.toString() != "0") {
                    if (!textStakes.text.toString().contains("-")) {
                        val url =
                            "http://roulette.rip/api/play?bet=red&wager=" + textStakes.text.toString()
                        Fuel.get(url).responseObject(Roulette.Deserializer()) { _, _, result ->
                            val (roulette, _) = result

                            if (roulette != null) {
                                var payout = 0
                                var outcome = "You have lost your bet."
                                if (roulette?.roll?.number >= 13 && roulette?.roll?.number <= 26) {
                                    payout = roulette?.bet?.wager!!.toInt() * 3
                                    outcome = "You have won €" + payout.toString() + ",-"
                                }
                                var finalResult =
                                    "The ball has landed on " + roulette?.roll?.color + " " + roulette?.roll?.number + ". " + outcome

                                if (gamesLeft.toInt() == 5) {
                                    firstGame = Timestamp.now()
                                }

                                cash =
                                    (cash.toInt() - roulette?.bet?.wager!! + payout!!.toInt()).toLong()
                                gamesLeft = (gamesLeft.toInt() - 1).toLong()

                                userRef!!
                                    .update(
                                        "cash",
                                        cash,
                                        "games_left",
                                        gamesLeft,
                                        "first_game",
                                        firstGame
                                    )
                                    .addOnSuccessListener {
                                        Log.d("update", "updated user")

                                        binding.textGamesCash.text =
                                            "Cash: €" + cash.toString() + ",-"
                                        binding.textGamesLeft.text =
                                            "You have " + gamesLeft.toString() + " games left!"
                                        val currentTime = Date().time
                                        val newGames = firstGame.toDate().time + 1800000
                                        val timeUntilReset = currentTime - newGames
                                        val minUntilReset =
                                            Math.abs(truncate((timeUntilReset / 1000 / 60).toDouble()).toInt())
                                        binding.textCooldown.text =
                                            "Reset in " + minUntilReset + "m"

                                        if (gamesLeft <= 0) {
                                            binding.buttonRoulette.isEnabled = false
                                        }
                                        resultToast(finalResult)
                                        myDialog.dismiss()
                                    }
                            }
                        }
                    } else {
                        poorToast()
                        myDialog.dismiss()
                    }
                } else {
                    noBetToast()
                }
            }

            button25to36.setOnClickListener {
                if (textStakes.text.toString().isNotEmpty() && textStakes.text.toString() != "0") {
                    if (!textStakes.text.toString().contains("-")) {
                        val url =
                            "http://roulette.rip/api/play?bet=red&wager=" + textStakes.text.toString()
                        Fuel.get(url).responseObject(Roulette.Deserializer()) { _, _, result ->
                            val (roulette, _) = result

                            if (roulette != null) {
                                var payout = 0
                                var outcome = "You have lost your bet."
                                if (roulette?.roll?.number >= 25 && roulette?.roll?.number <= 36) {
                                    payout = roulette?.bet?.wager!!.toInt() * 3
                                    outcome = "You have won €" + payout.toString() + ",-"
                                }

                                var finalResult =
                                    "The ball has landed on " + roulette?.roll?.color + " " + roulette?.roll?.number + ". " + outcome

                                if (gamesLeft.toInt() == 5) {
                                    firstGame = Timestamp.now()
                                }

                                cash =
                                    (cash.toInt() - roulette?.bet?.wager!! + payout!!.toInt()).toLong()
                                gamesLeft = (gamesLeft.toInt() - 1).toLong()

                                userRef!!
                                    .update(
                                        "cash",
                                        cash,
                                        "games_left",
                                        gamesLeft,
                                        "first_game",
                                        firstGame
                                    )
                                    .addOnSuccessListener {
                                        Log.d("update", "updated user")

                                        binding.textGamesCash.text =
                                            "Cash: €" + cash.toString() + ",-"
                                        binding.textGamesLeft.text =
                                            "You have " + gamesLeft.toString() + " games left!"
                                        val currentTime = Date().time
                                        val newGames = firstGame.toDate().time + 1800000
                                        val timeUntilReset = currentTime - newGames
                                        val minUntilReset =
                                            Math.abs(truncate((timeUntilReset / 1000 / 60).toDouble()).toInt())
                                        binding.textCooldown.text =
                                            "Reset in " + minUntilReset + "m"

                                        if (gamesLeft <= 0) {
                                            binding.buttonRoulette.isEnabled = false
                                        }
                                        resultToast(finalResult)
                                        myDialog.dismiss()
                                    }
                            }
                        }
                    } else {
                        poorToast()
                        myDialog.dismiss()
                    }
                } else {
                    noBetToast()
                }
            }

            buttonAll.setOnClickListener {
                textStakes.setText(cash.toString())
            }
        }

        return binding.root
    }

//    private fun updateDB(cash: Long, gamesLeft: Long) {
////        var id = ""
////        val db = Firebase.firestore
////
////        auth = Firebase.auth
////        val currentUser = auth.currentUser
////        db.collection("users").whereEqualTo("user_id", currentUser.uid).get()
////            .addOnCompleteListener { task ->
////                if (task.isSuccessful) {
////                    var user = task.result?.documents?.get(0)
////                    id = user!!.id
////                }
////
////                var userRef = db.collection("users").document(id)
////
////                userRef!!
////                    .update("cash", cash, "games_left", gamesLeft)
////                    .addOnSuccessListener {
////                        Log.d("update", "updated user")
////                    }
////            }
//    }

//    private fun bet(bet: String, amount: String): Roulette? {
////        var toReturn: Roulette? = null
////        val url = "http://roulette.rip/api/play?bet=" + bet + "&wager=" + amount
////        url.httpGet()
////            .responseObject(Roulette.Deserializer())
////            { _, _, result ->
////                val (data, _) = result
////                toReturn = data
////            }
//        return null
//    }

    private fun noBetToast() {
        Toast.makeText(
            this.context,
            "You have not provided an amount to bet. Please fill in the field.",
            Toast.LENGTH_SHORT
        ).show();
    }

    private fun poorToast() {
        Toast.makeText(
            this.context,
            "You cannot play games with negative cash. Please withdraw some money from your bank.",
            Toast.LENGTH_SHORT
        ).show();
    }

    private fun resultToast(result: String) {
        Toast.makeText(
            this.context,
            result,
            Toast.LENGTH_SHORT
        ).show();
    }
}