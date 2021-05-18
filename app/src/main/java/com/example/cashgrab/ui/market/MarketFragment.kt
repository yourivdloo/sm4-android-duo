package com.example.cashgrab.ui.market

import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.cashgrab.R
import com.example.cashgrab.databinding.FragmentMarketBinding
import kotlin.math.truncate
import com.example.cashgrab.models.Stock
import com.github.kittinunf.fuel.Fuel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlin.math.floor

class MarketFragment : Fragment() {

    private lateinit var marketViewModel: MarketViewModel
    private lateinit var binding: FragmentMarketBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        marketViewModel =
            ViewModelProvider(this).get(MarketViewModel::class.java)
        binding = FragmentMarketBinding.inflate(layoutInflater)

        auth = Firebase.auth
        val currentUser = auth.currentUser
        var userRef: DocumentReference? = null

        var cash: Long = 0
        var doublePM: Boolean = false
        var extraGames: Boolean = false
        var role: String = ""

        val db = Firebase.firestore
        db.collection("users").whereEqualTo("user_id", currentUser.uid).get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    var result = task.result?.documents?.get(0)
                    if (result != null) {
                        cash = result.data?.get("cash") as Long
                        doublePM = result.data?.get("double_pm") as Boolean
                        extraGames = result.data?.get("extra_games") as Boolean
                        role = result.data?.get("role") as String
                        binding.textMarketCash.text = "Cash: €" + cash + ",-"
                        val id = result.id
                        userRef = db.collection("users").document(id)
                    }
                }
            }

        var items = arrayListOf<String>(
            "Upgrade: +10 extra games (€5.000.000,-)",
            "Upgrade: pocket money x2 (€2.500.000,-)",
            "Role: Cobalt (€10.000.000,-)",
            "Role: Crimson (€20.000.000,-)",
            "Role: Titanium White (€50.000.000,-)",
            "Role: ALL CAPS (€50.000.000,-)"
        )

        val adapter = ArrayAdapter<String>(
            this.requireContext(),
            android.R.layout.simple_list_item_1,
            items
        )

        binding.listShop.adapter = adapter

        binding.listShop.setOnItemClickListener { _, _, i, _ ->
            val builder = AlertDialog.Builder(this.requireContext())
            builder.setTitle("Are you sure?")

            var insufficient = false
            var owned = false
            when (i) {
                0 -> {
                    if (!extraGames) {
                        if (cash >= 5000000) {
                            builder.setMessage("Do you want to buy +10 extra games for €5.000.000,-?")
                            builder.setPositiveButton("Yes") { dialogInterface: DialogInterface, i: Int ->
                                cash -= 5000000
                                userRef!!.update(
                                    "extra_games",
                                    true,
                                    "cash",
                                    cash,
                                    "games_left",
                                    15
                                ).addOnSuccessListener {
                                    binding.textMarketCash.text = "Cash: €" + cash + ",-"
                                    extraGames = true
                                }
                            }
                        } else {
                            insufficient = true
                        }
                    } else {
                        owned = true
                    }
                }
                1 -> {
                    if (!doublePM) {
                        if (cash >= 2500000) {
                            builder.setMessage("Do you want to buy double pocket money for €2.500.000,-?")
                            builder.setPositiveButton("Yes") { dialogInterface: DialogInterface, i: Int ->
                                cash -= 2500000
                                userRef!!.update("double_pm", true, "cash", cash)
                                    .addOnSuccessListener {
                                        binding.textMarketCash.text = "Cash: €" + cash + ",-"
                                        doublePM = true
                                    }
                            }
                        } else {
                            insufficient = true
                        }
                    } else {
                        owned = true
                    }
                }
                2 -> {
                    if (role == "") {
                        if (cash >= 10000000) {
                            builder.setMessage("Do you want to buy the cobalt role for €10.000.000,-?")
                            builder.setPositiveButton("Yes") { dialogInterface: DialogInterface, i: Int ->
                                cash -= 10000000
                                userRef!!.update("role", "cobalt", "cash", cash)
                                    .addOnSuccessListener {
                                        binding.textMarketCash.text = "Cash: €" + cash + ",-"
                                        role = "cobalt"
                                    }
                            }
                        } else {
                            insufficient = true
                        }
                    } else {
                        owned = true
                    }
                }
                3 -> {
                    if (role == "cobalt" || role == "") {
                        if (cash >= 20000000) {
                            builder.setMessage("Do you want to buy the crimson role for €20.000.000,-?")
                            builder.setPositiveButton("Yes") { dialogInterface: DialogInterface, i: Int ->
                                cash -= 20000000
                                userRef!!.update("role", "crimson", "cash", cash)
                                    .addOnSuccessListener {
                                        binding.textMarketCash.text = "Cash: €" + cash + ",-"
                                        role = "crimson"
                                    }
                            }
                        } else {
                            insufficient = true
                        }
                    } else {
                        owned = true
                    }
                }
                4 -> {
                    if (role !== "white" && role !== "caps") {
                        if (cash >= 40000000) {
                            builder.setMessage("Do you want to buy the titanium white role for €40.000.000,-?")
                            builder.setPositiveButton("Yes") { dialogInterface: DialogInterface, i: Int ->
                                cash -= 40000000
                                userRef!!.update("role", "white", "cash", cash)
                                    .addOnSuccessListener {
                                        binding.textMarketCash.text = "Cash: €" + cash + ",-"
                                        role = "white"
                                    }
                            }
                        } else {
                            insufficient = true
                        }
                    } else {
                        owned = true
                    }
                }
                5 -> {
                    if (role !== "caps") {
                        if (cash >= 50000000) {
                            builder.setMessage("Do you want to buy the all caps role for €50.000.000,-?")
                            builder.setPositiveButton("Yes") { dialogInterface: DialogInterface, i: Int ->
                                cash -= 50000000
                                userRef!!.update("role", "caps", "cash", cash)
                                    .addOnSuccessListener {
                                        binding.textMarketCash.text = "Cash: €" + cash + ",-"
                                        role = "caps"
                                    }
                            }
                        } else {
                            insufficient = true
                        }
                    } else {
                        owned = true
                    }
                }
            }

            if (!insufficient && !owned) {
                builder.setNegativeButton("No") { dialogInterface: DialogInterface, i: Int -> }
                builder.show()
            } else if (owned) {
                Toast.makeText(
                    this.context,
                    "You already own this role/upgrade or a better one",
                    Toast.LENGTH_SHORT
                ).show();
            } else if (insufficient) {
                Toast.makeText(
                    this.context,
                    "You don't have enough money for this purchase",
                    Toast.LENGTH_SHORT
                ).show();
            }

            owned = false
            insufficient = false
        }

        binding.buttonApple.setOnClickListener {
            var myDialog: Dialog
            myDialog = Dialog(this.requireContext())
            myDialog.setContentView(R.layout.apple_stocks);
            myDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            myDialog.show()

            val textStocks = myDialog.findViewById<TextView>(R.id.textAppleStocks)
            val textBuy = myDialog.findViewById<EditText>(R.id.textAppleBuy)
            val textCost = myDialog.findViewById<TextView>(R.id.textAppleCost)
            val buttonBuy = myDialog.findViewById<Button>(R.id.buttonAppleBuy)
            val textShares = myDialog.findViewById<TextView>(R.id.textAppleShares)
            val textEarnings = myDialog.findViewById<TextView>(R.id.textAppleEarnings)
            val textSell = myDialog.findViewById<EditText>(R.id.textAppleSell)
            val buttonSell = myDialog.findViewById<Button>(R.id.buttonAppleSell)
            val buttonBack = myDialog.findViewById<ImageButton>(R.id.buttonBack6)
            val buttonBuyMax = myDialog.findViewById<Button>(R.id.buttonAppleBuyMax)
            val buttonSellMax = myDialog.findViewById<Button>(R.id.buttonAppleSellMax)

            var price = 0.0

            val url =
                "https://financialmodelingprep.com/api/v3/quote-short/AAPL?apikey=623f97eb5ad18c553db91f3135f9d0bd"

            Fuel.get(url).responseObject(Stock.Deserializer()) { _, _, result ->
                val (stock, _) = result

                if (stock?.get(0) != null) {
                    price = stock.get(0).price
                    println(price.toString())
                    textStocks.text = "1 : " + price.toString()
                }
//
//                price = stock?.get(0)?.price!!
//                println(price.toString())

                textStocks.text = "1 : " + price


            }
            var appleStocks: Long = 0
            val db = Firebase.firestore

            db.collection("users").whereEqualTo("user_id", currentUser.uid).get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        var result = task.result?.documents?.get(0)
                        if (result != null) {
                            appleStocks = result.data?.get("apple_stocks") as Long
                            textShares.text = "You have " + appleStocks + " shares"
                        }
                    }
                }

            buttonBack.setOnClickListener {
                myDialog.dismiss()
            }

            buttonBuyMax.setOnClickListener {
                val maxBuyable = floor(((cash.toDouble() / price)))
                textBuy.setText(maxBuyable.toInt().toString())
                val cost = truncate((maxBuyable * price))
                textCost.text = "Cost: €" + cost.toInt().toString() + ",-"
            }

            buttonSellMax.setOnClickListener {
                textSell.setText(appleStocks.toInt().toString())
                var earnings = truncate(((appleStocks.toDouble() * price)))
                textEarnings.text = "Earnings: €" + earnings.toInt().toString() + ",-"
            }

            textBuy.addTextChangedListener { text ->
//                    textStocks.text = "1 : " + price.toString()
                if (text != null && text.toString().isNotEmpty()) {
                    val amount: Long = java.lang.Long.parseLong(text.toString())

                    var cost = truncate((amount.toDouble() * price))
                    val maxBuyable = floor(((cash.toDouble() / price)))
                    println(maxBuyable.toString())

                    if (cost > cash) {
                        textBuy.setText(maxBuyable.toInt().toString())
                        cost = truncate((maxBuyable * price))
                    } else {
                        buttonBuy.isEnabled = true
                    }

                    textCost.text = "Cost: €" + cost.toInt().toString() + ",-"
                } else {
                    textCost.text = ""
                }
            }

            textSell.addTextChangedListener { text ->
//                    textStocks.text = "1 : " + price.toString()
                if (text != null && text.toString().isNotEmpty()) {
                    val amount: Long = java.lang.Long.parseLong(text.toString())

                    var earnings = truncate(((amount.toDouble() * price)))

                    if (amount > appleStocks) {
                        textSell.setText(appleStocks.toString())
                        earnings = truncate(((appleStocks * price)))
                    } else {
                        buttonSell.isEnabled = true
                    }

                    textEarnings.text = "Earnings: €" + earnings.toInt().toString() + ",-"
                } else {
                    textEarnings.text = ""
                }
            }

            buttonBuy.setOnClickListener {
                val text = textBuy.text.toString()
                var amount: Long = 0
                if (text.isNotEmpty()) {
                    amount = java.lang.Long.parseLong(text)
                }

                var cost = truncate(((amount * price)))
                println("cash old " + cash.toString())
                println("cost " + cost.toString())
                cash = (cash - cost).toLong()
                println("cash new " + cash.toString())
                appleStocks = appleStocks + amount

                userRef!!
                    .update(
                        "cash", cash, "apple_stocks", appleStocks
                    ).addOnCompleteListener {
                        binding.textMarketCash.text = "Cash: €" + cash + ",-"
                        Toast.makeText(
                            this.context,
                            "Successfully bought " + amount.toString() + "Apple stocks for €" + cost.toString() + ",-",
                            Toast.LENGTH_SHORT
                        ).show();
                        myDialog.dismiss()
                    }
            }

            buttonSell.setOnClickListener {
                val text = textSell.text.toString()
                var amount: Long = 0
                if (text.isNotEmpty()) {
                    amount = java.lang.Long.parseLong(text)
                }

                var earnings = truncate(((amount * price)))
                cash = (cash + earnings).toLong()
                appleStocks = appleStocks - amount

                userRef!!
                    .update(
                        "cash", cash, "apple_stocks", appleStocks
                    ).addOnCompleteListener {
                        binding.textMarketCash.text = "Cash: €" + cash + ",-"
                        Toast.makeText(
                            this.context,
                            "Successfully sold " + amount.toString() + "Apple stocks for €" + earnings.toString() + ",-",
                            Toast.LENGTH_SHORT
                        ).show();
                        myDialog.dismiss()
                    }
            }
        }

        binding.buttonTesla.setOnClickListener {
            var myDialog: Dialog
            myDialog = Dialog(this.requireContext())
            myDialog.setContentView(R.layout.tesla_stocks);
            myDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            myDialog.show()

            val textStocks = myDialog.findViewById<TextView>(R.id.textTeslaStocks)
            val textBuy = myDialog.findViewById<EditText>(R.id.textTeslaBuy)
            val textCost = myDialog.findViewById<TextView>(R.id.textTeslaCost)
            val buttonBuy = myDialog.findViewById<Button>(R.id.buttonTeslaBuy)
            val textShares = myDialog.findViewById<TextView>(R.id.textTeslaShares)
            val textEarnings = myDialog.findViewById<TextView>(R.id.textTeslaEarnings)
            val textSell = myDialog.findViewById<EditText>(R.id.textTeslaSell)
            val buttonSell = myDialog.findViewById<Button>(R.id.buttonTeslaSell)
            val buttonBack = myDialog.findViewById<ImageButton>(R.id.buttonBack7)
            val buttonBuyMax = myDialog.findViewById<Button>(R.id.buttonTeslaBuyMax)
            val buttonSellMax = myDialog.findViewById<Button>(R.id.buttonTeslaSellMax)

            var price = 0.0

            val url =
                "https://financialmodelingprep.com/api/v3/quote-short/TSLA?apikey=623f97eb5ad18c553db91f3135f9d0bd"

            Fuel.get(url).responseObject(Stock.Deserializer()) { _, _, result ->
                val (stock, _) = result

                if (stock?.get(0) != null) {
                    price = stock.get(0).price
                    println(price.toString())
                    textStocks.text = "1 : " + price.toString()
                }
//
//                price = stock?.get(0)?.price!!
//                println(price.toString())

                textStocks.text = "1 : " + price


            }
            var teslaStocks: Long = 0
            val db = Firebase.firestore

            db.collection("users").whereEqualTo("user_id", currentUser.uid).get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        var result = task.result?.documents?.get(0)
                        if (result != null) {
                            teslaStocks = result.data?.get("tesla_stocks") as Long
                            textShares.text = "You have " + teslaStocks + " shares"
                        }
                    }
                }

            buttonBack.setOnClickListener {
                myDialog.dismiss()
            }

            buttonBuyMax.setOnClickListener {
                val maxBuyable = floor(((cash.toDouble() / price)))
                textBuy.setText(maxBuyable.toInt().toString())
                val cost = truncate((maxBuyable * price))
                textCost.text = "Cost: €" + cost.toInt().toString() + ",-"
            }

            buttonSellMax.setOnClickListener {
                textSell.setText(teslaStocks.toInt().toString())
                var earnings = truncate(((teslaStocks.toDouble() * price)))
                textEarnings.text = "Earnings: €" + earnings.toInt().toString() + ",-"
            }

            textBuy.addTextChangedListener { text ->
//                    textStocks.text = "1 : " + price.toString()
                if (text != null && text.toString().isNotEmpty()) {
                    val amount: Long = java.lang.Long.parseLong(text.toString())

                    var cost = truncate((amount.toDouble() * price))
                    val maxBuyable = floor(((cash.toDouble() / price)))
                    println(maxBuyable.toString())

                    if (cost > cash) {
                        textBuy.setText(maxBuyable.toInt().toString())
                        cost = truncate((maxBuyable * price))
                    } else {
                        buttonBuy.isEnabled = true
                    }

                    textCost.text = "Cost: €" + cost.toInt().toString() + ",-"
                } else {
                    textCost.text = ""
                }
            }

            textSell.addTextChangedListener { text ->
//                    textStocks.text = "1 : " + price.toString()
                if (text != null && text.toString().isNotEmpty()) {
                    val amount: Long = java.lang.Long.parseLong(text.toString())

                    var earnings = truncate(((amount.toDouble() * price)))

                    if (amount > teslaStocks) {
                        textSell.setText(teslaStocks.toString())
                        earnings = truncate(((teslaStocks * price)))
                    } else {
                        buttonSell.isEnabled = true
                    }

                    textEarnings.text = "Earnings: €" + earnings.toInt().toString() + ",-"
                } else {
                    textEarnings.text = ""
                }
            }

            buttonBuy.setOnClickListener {
                val text = textBuy.text.toString()
                var amount: Long = 0
                if (text.isNotEmpty()) {
                    amount = java.lang.Long.parseLong(text)
                }

                var cost = truncate(((amount * price)))
                println("cash old " + cash.toString())
                println("cost " + cost.toString())
                cash = (cash - cost).toLong()
                println("cash new " + cash.toString())
                teslaStocks = teslaStocks + amount

                userRef!!
                    .update(
                        "cash", cash, "tesla_stocks", teslaStocks
                    ).addOnCompleteListener {
                        binding.textMarketCash.text = "Cash: €" + cash + ",-"
                        Toast.makeText(
                            this.context,
                            "Successfully bought " + amount.toString() + "Tesla stocks for €" + cost.toString() + ",-",
                            Toast.LENGTH_SHORT
                        ).show();
                        myDialog.dismiss()
                    }
            }

            buttonSell.setOnClickListener {
                val text = textSell.text.toString()
                var amount: Long = 0
                if (text.isNotEmpty()) {
                    amount = java.lang.Long.parseLong(text)
                }

                var earnings = truncate(((amount * price)))
                cash = (cash + earnings).toLong()
                teslaStocks = teslaStocks - amount

                userRef!!
                    .update(
                        "cash", cash, "tesla_stocks", teslaStocks
                    ).addOnCompleteListener {
                        binding.textMarketCash.text = "Cash: €" + cash + ",-"
                        Toast.makeText(
                            this.context,
                            "Successfully sold " + amount.toString() + "Tesla stocks for €" + earnings.toString() + ",-",
                            Toast.LENGTH_SHORT
                        ).show();
                        myDialog.dismiss()
                    }
            }
        }

        binding.buttonMicrosoft.setOnClickListener {
            var myDialog: Dialog
            myDialog = Dialog(this.requireContext())
            myDialog.setContentView(R.layout.microsoft_stocks);
            myDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            myDialog.show()

            val textStocks = myDialog.findViewById<TextView>(R.id.textMicrosoftStocks)
            val textBuy = myDialog.findViewById<EditText>(R.id.textMicrosoftBuy)
            val textCost = myDialog.findViewById<TextView>(R.id.textMicrosoftCost)
            val buttonBuy = myDialog.findViewById<Button>(R.id.buttonMicrosoftBuy)
            val textShares = myDialog.findViewById<TextView>(R.id.textMicrosoftShares)
            val textEarnings = myDialog.findViewById<TextView>(R.id.textMicrosoftEarnings)
            val textSell = myDialog.findViewById<EditText>(R.id.textMicrosoftSell)
            val buttonSell = myDialog.findViewById<Button>(R.id.buttonMicrosoftSell)
            val buttonBack = myDialog.findViewById<ImageButton>(R.id.buttonBack8)
            val buttonBuyMax = myDialog.findViewById<Button>(R.id.buttonMicrosoftBuyMax)
            val buttonSellMax = myDialog.findViewById<Button>(R.id.buttonMicrosoftSellMax)

            var price = 0.0

            val url =
                "https://financialmodelingprep.com/api/v3/quote-short/MSFT?apikey=623f97eb5ad18c553db91f3135f9d0bd"

            Fuel.get(url).responseObject(Stock.Deserializer()) { _, _, result ->
                val (stock, _) = result

                if (stock?.get(0) != null) {
                    price = stock.get(0).price
                    println(price.toString())
                    textStocks.text = "1 : " + price.toString()
                }
//
//                price = stock?.get(0)?.price!!
//                println(price.toString())

                textStocks.text = "1 : " + price


            }
            var microsoftStocks: Long = 0
            val db = Firebase.firestore

            db.collection("users").whereEqualTo("user_id", currentUser.uid).get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        var result = task.result?.documents?.get(0)
                        if (result != null) {
                            microsoftStocks = result.data?.get("microsoft_stocks") as Long
                            textShares.text = "You have " + microsoftStocks + " shares"
                        }
                    }
                }

            buttonBack.setOnClickListener {
                myDialog.dismiss()
            }

            buttonBuyMax.setOnClickListener {
                val maxBuyable = floor(((cash.toDouble() / price)))
                textBuy.setText(maxBuyable.toInt().toString())
                val cost = truncate((maxBuyable * price))
                textCost.text = "Cost: €" + cost.toInt().toString() + ",-"
            }

            buttonSellMax.setOnClickListener {
                textSell.setText(microsoftStocks.toInt().toString())
                var earnings = truncate(((microsoftStocks.toDouble() * price)))
                textEarnings.text = "Earnings: €" + earnings.toInt().toString() + ",-"
            }

            textBuy.addTextChangedListener { text ->
//                    textStocks.text = "1 : " + price.toString()
                if (text != null && text.toString().isNotEmpty()) {
                    val amount: Long = java.lang.Long.parseLong(text.toString())

                    var cost = truncate((amount.toDouble() * price))
                    val maxBuyable = floor(((cash.toDouble() / price)))
                    println(maxBuyable.toString())

                    if (cost > cash) {
                        textBuy.setText(maxBuyable.toInt().toString())
                        cost = truncate((maxBuyable * price))
                    } else {
                        buttonBuy.isEnabled = true
                    }

                    textCost.text = "Cost: €" + cost.toInt().toString() + ",-"
                } else {
                    textCost.text = ""
                }
            }

            textSell.addTextChangedListener { text ->
//                    textStocks.text = "1 : " + price.toString()
                if (text != null && text.toString().isNotEmpty()) {
                    val amount: Long = java.lang.Long.parseLong(text.toString())

                    var earnings = truncate(((amount.toDouble() * price)))

                    if (amount > microsoftStocks) {
                        textSell.setText(microsoftStocks.toString())
                        earnings = truncate(((microsoftStocks * price)))
                    } else {
                        buttonSell.isEnabled = true
                    }

                    textEarnings.text = "Earnings: €" + earnings.toInt().toString() + ",-"
                } else {
                    textEarnings.text = ""
                }
            }

            buttonBuy.setOnClickListener {
                val text = textBuy.text.toString()
                var amount: Long = 0
                if (text.isNotEmpty()) {
                    amount = java.lang.Long.parseLong(text)
                }

                var cost = truncate(((amount * price)))
                println("cash old " + cash.toString())
                println("cost " + cost.toString())
                cash = (cash - cost).toLong()
                println("cash new " + cash.toString())
                microsoftStocks = microsoftStocks + amount

                userRef!!
                    .update(
                        "cash", cash, "microsoft_stocks", microsoftStocks
                    ).addOnCompleteListener {
                        binding.textMarketCash.text = "Cash: €" + cash + ",-"
                        Toast.makeText(
                            this.context,
                            "Successfully bought " + amount.toString() + "Microsoft stocks for €" + cost.toString() + ",-",
                            Toast.LENGTH_SHORT
                        ).show();
                        myDialog.dismiss()
                    }
            }

            buttonSell.setOnClickListener {
                val text = textSell.text.toString()
                var amount: Long = 0
                if (text.isNotEmpty()) {
                    amount = java.lang.Long.parseLong(text)
                }

                var earnings = truncate(((amount * price)))
                cash = (cash + earnings).toLong()
                microsoftStocks = microsoftStocks - amount

                userRef!!
                    .update(
                        "cash", cash, "microsoft_stocks", microsoftStocks
                    ).addOnCompleteListener {
                        binding.textMarketCash.text = "Cash: €" + cash + ",-"
                        Toast.makeText(
                            this.context,
                            "Successfully sold " + amount.toString() + "Microsoft stocks for €" + earnings.toString() + ",-",
                            Toast.LENGTH_SHORT
                        ).show();
                        myDialog.dismiss()
                    }
            }
        }


        return binding.root
    }
}