package com.example.cashgrab.ui.dashboard

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.cashgrab.R
import com.example.cashgrab.databinding.FragmentDashboardBinding
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.lang.Float
import java.lang.Long.parseLong
import java.util.*
import kotlin.math.truncate

class DashboardFragment : Fragment() {

    private lateinit var dashboardViewModel: DashboardViewModel
    private lateinit var binding: FragmentDashboardBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dashboardViewModel =
            ViewModelProvider(this).get(DashboardViewModel::class.java)
//        val root = inflater.inflate(R.layout.fragment_dashboard, container, false)
//        val textView: TextView = root.findViewById(R.id.text_dashboard)
//        dashboardViewModel.text.observe(viewLifecycleOwner, Observer {
////            textView.text = it
//        })

        binding = FragmentDashboardBinding.inflate(layoutInflater)

        auth = Firebase.auth
        val currentUser = auth.currentUser

        var id: String = "Test"

        val db = Firebase.firestore

        if (currentUser == null) {
            failureToast()
        } else {
            Log.d("RESULT", currentUser?.uid.toString())
        }

        db.collection("users").whereEqualTo("user_id", currentUser.uid).get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    var result = task.result?.documents?.get(0)
                    id = result?.id!!
                    Log.d("RESULT", result?.data?.get("user_name").toString())

                    binding.textHello.text = "Hello, " + result?.data?.get("user_name").toString()
                    binding.textBalance.text = "€" + result?.data?.get("balance").toString() + ",-"
                    binding.textCash.text = "Cash: €" + result?.data?.get("cash").toString() + ",-"

                    val lastWorked: Timestamp = result?.data?.get("last_worked") as Timestamp
                    val currentTime = Date()
                    val workCooldownOver = lastWorked.toDate().time + 3600000

                    if (currentTime.time >= workCooldownOver) {
                        binding.buttonWork.text = "Work\nReady"
                        binding.buttonWork.isEnabled = true
                    } else {
                        val timeToGo = workCooldownOver - currentTime.time
                        val hours = truncate((timeToGo / 1000 / 60 / 60).toDouble()).toInt()
                        val minutes = truncate((timeToGo / 1000 / 60 - (hours * 60)).toDouble()).toInt()
                        binding.buttonWork.text =
                            "Work\n" + hours.toString() + "h" + minutes.toString() + "m"
                    }

                    val lastPM: Timestamp = result?.data?.get("last_pm") as Timestamp
                    val pmCooldownOver = lastPM.toDate().time + 21600000

                    if (currentTime.time >= pmCooldownOver) {
                        binding.buttonPM.text = "Pocket money\nReady"
                        binding.buttonPM.isEnabled = true
                    } else {
                        val timeToGo = pmCooldownOver - currentTime.time
                        val hours = truncate((timeToGo / 1000 / 60 / 60).toDouble()).toInt()
                        val minutes =
                            truncate((timeToGo / 1000 / 60 - (hours * 60)).toDouble()).toInt()
                        binding.buttonPM.text =
                            "Pocket money\n" + hours.toString() + "h" + minutes.toString() + "m"
                    }
                } else {
                    failureToast()
                }
            }

        binding.buttonDeposit.setOnClickListener {
            var myDialog: Dialog
            myDialog = Dialog(this.requireContext())
            myDialog.setContentView(R.layout.popup_deposit);
            myDialog.show()

            val textAmount = myDialog.findViewById<EditText>(R.id.textDepAmount)
            val buttonAll = myDialog.findViewById<Button>(R.id.buttonDepAll)
            val buttonBack = myDialog.findViewById<ImageButton>(R.id.buttonBack)
            val buttonConfirmDeposit = myDialog.findViewById<Button>(R.id.buttonConfirmDeposit)

            var cash: Long = 0
            var balance: Long = 0
            val userRef = db.collection("users").document(id)
            userRef.get()
                .addOnSuccessListener { document ->
                    cash = document.data?.get("cash") as Long
                    balance = document.data?.get("balance") as Long
                }

            textAmount.addTextChangedListener { text ->
                if (text != null && text.toString().isNotEmpty()) {
                    val input: Long = parseLong(text.toString())
                    if (input > cash) {
                        textAmount.setText(cash.toString())
                    }
                }
            }

            buttonAll.setOnClickListener {
                textAmount.setText(cash.toString())
            }

            buttonBack.setOnClickListener {
                myDialog.dismiss()
            }

            buttonConfirmDeposit.setOnClickListener {
                val text = textAmount.text.toString()
                var amount: Long = 0
                if (text.isNotEmpty()) {
                    amount = parseLong(text)
                }

                userRef
                    .update("cash", cash - amount, "balance", balance + amount)
                    .addOnSuccessListener {
                        Toast.makeText(
                            this.context,
                            "Succesfully deposited €" + amount.toString() + ",- to your bank.",
                            Toast.LENGTH_SHORT
                        ).show();
                        binding.textBalance.text = "€" + (balance + amount).toString() + ",-"
                        binding.textCash.text = "Cash: €" + (cash - amount).toString() + ",-"
                        myDialog.dismiss()
                    }
                    .addOnFailureListener {
                        failureToast()
                    }
            }
        }

        binding.buttonWithdraw.setOnClickListener {
            var myDialog: Dialog
            myDialog = Dialog(this.requireContext())
            myDialog.setContentView(R.layout.popup_withdraw);
            myDialog.show()

            val textAmount = myDialog.findViewById<EditText>(R.id.textWithAmount)
            val buttonAll = myDialog.findViewById<Button>(R.id.buttonWithAll)
            val buttonBack = myDialog.findViewById<ImageButton>(R.id.buttonBack2)
            val buttonConfirmWithdraw = myDialog.findViewById<Button>(R.id.buttonConfirmWithdraw)

            var cash: Long = 0
            var balance: Long = 0
            val userRef = db.collection("users").document(id)
            userRef.get()
                .addOnSuccessListener { document ->
                    cash = document.data?.get("cash") as Long
                    balance = document.data?.get("balance") as Long
                }

            textAmount.addTextChangedListener { text ->
                if (text != null && text.toString().isNotEmpty()) {
                    val input: Long = parseLong(text.toString())
                    if (input > balance) {
                        textAmount.setText(balance.toString());
                    }
                }
            }

            buttonAll.setOnClickListener {
                textAmount.setText("$balance")
            }

            buttonBack.setOnClickListener {
                myDialog.dismiss()
            }

            buttonConfirmWithdraw.setOnClickListener {
                val text = textAmount.text.toString()
                var amount: Long = 0
                if (text.isNotEmpty()) {
                    amount = parseLong(text)
                }

                userRef
                    .update(
                        "cash", cash + amount, "balance", balance - amount
                    )
                    .addOnSuccessListener {
                        Toast.makeText(
                            this.context,
                            "Succesfully withdrew €" + amount.toString() + ",- from your bank.",
                            Toast.LENGTH_SHORT
                        ).show();
                        binding.textBalance.text = "€" + (balance - amount).toString() + ",-"
                        binding.textCash.text = "Cash: €" + (cash + amount).toString() + ",-"
                        myDialog.dismiss()
                    }
                    .addOnFailureListener {
                        failureToast()
                    }
            }
        }

        binding.buttonWork.setOnClickListener {
            var cash: Long = 0
            var earned: Long = ((0..50).random() * 1000).toLong()

            Toast.makeText(
                this.context, "You earned €" + earned.toString() + ",-",
                Toast.LENGTH_SHORT
            ).show();

            val userRef = db.collection("users").document(id)
            userRef.get()
                .addOnSuccessListener { document ->
                    cash = document.data?.get("cash") as Long

                    userRef
                        .update("last_worked", Timestamp.now(), "cash", cash + earned)
                        .addOnSuccessListener {
                            binding.textCash.text = "Cash: €" + (cash + earned)
                            binding.buttonWork.isEnabled = false
                            binding.buttonWork.text = "Work\n1h0m"
                        }
                        .addOnFailureListener {
                            failureToast()
                        }
                }
        }

        binding.buttonPM.setOnClickListener {
            var cash: Long = 0
            var earned: Long = ((0..100).random() * 1000).toLong()

            Toast.makeText(
                this.context, "You earned €" + earned.toString() + ",-",
                Toast.LENGTH_SHORT
            ).show();

            val userRef = db.collection("users").document(id)
            userRef.get()
                .addOnSuccessListener { document ->
                    cash = document.data?.get("cash") as Long


                    userRef
                        .update("last_pm", Timestamp.now(), "cash", cash + earned)
                        .addOnSuccessListener {
                            binding.textCash.text = "Cash: €" + (cash + earned)
                            binding.buttonPM.isEnabled = false
                            binding.buttonPM.text = "Pocket money\n6h0m"
                        }
                        .addOnFailureListener {
                            failureToast()
                        }
                }
        }

        return binding.root
    }

    private fun failureToast() {
        Toast.makeText(
            this.context, "Something went wrong...",
            Toast.LENGTH_SHORT
        ).show();
    }
}