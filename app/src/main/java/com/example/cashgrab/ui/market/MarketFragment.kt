package com.example.cashgrab.ui.market

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
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
    private lateinit var binding : FragmentMarketBinding
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

        val db = Firebase.firestore
        db.collection("users").whereEqualTo("user_id", currentUser.uid).get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    var result = task.result?.documents?.get(0)
                    if (result != null) {
                        cash = result.data?.get("cash") as Long
                        binding.textMarketCash.text = "Cash: €" + cash + ",-"
                        val id = result.id
                        userRef = db.collection("users").document(id)
                    }
                }
            }

        binding.buttonApple.setOnClickListener {
            var myDialog: Dialog
            myDialog = Dialog(this.requireContext())
            myDialog.setContentView(R.layout.apple_stocks);
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

            var price= 0.0

            val url =
                "https://financialmodelingprep.com/api/v3/quote-short/AAPL?apikey=623f97eb5ad18c553db91f3135f9d0bd"

//            Fuel.get(url).response { _, _, result ->
//                val (data, _) = result
//                if(data!=null) {
//                    println(String(data))
//                }
//            }
            Fuel.get(url).responseObject(Stock.Deserializer()) { _, _, result ->
                val (stock, _) = result

                if(stock?.get(0) !=null){
                    price = stock.get(0).price
                    println(price.toString())
                    textStocks.text = "1 : " + price.toString()
                }
//
//                price = stock?.get(0)?.price!!
//                println(price.toString())
//
//                textStocks.text = "1 : " + price
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
                textStocks.text = "1 : " + price.toString()
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
                textStocks.text = "1 : " + price.toString()
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

                var cost = truncate(((amount * appleStocks).toDouble()))
                println(cost.toString())
                cash = (cash - cost).toLong()
                println(cash.toString())
                appleStocks = appleStocks + amount

//                userRef!!
//                    .update(
//                        "cash", cash, "apple_stocks", appleStocks
//                    )

//                binding.textMarketCash.text = "Cash: €" + cash + ",-"
                myDialog.dismiss()
            }

            buttonSell.setOnClickListener {
                val text = textSell.text.toString()
                var amount: Long = 0
                if (text.isNotEmpty()) {
                    amount = java.lang.Long.parseLong(text)
                }

                var earnings = truncate(((amount * appleStocks).toDouble()))
                cash = (cash + earnings).toLong()
                appleStocks = appleStocks - amount

                userRef!!
                    .update(
                        "cash", cash, "apple_stocks", appleStocks
                    )

                binding.textMarketCash.text = "Cash: €" + cash + ",-"
                myDialog.dismiss()
            }
        }

        return binding.root
    }
}