package com.example.cashgrab.ui.community

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import com.example.cashgrab.R
import com.example.cashgrab.databinding.FragmentCommunityBinding
import com.example.cashgrab.models.Event
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.*
import kotlin.math.ceil
import kotlin.math.truncate

class CommunityFragment : Fragment() {
    private lateinit var binding: FragmentCommunityBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentCommunityBinding.inflate(layoutInflater)

        val db = Firebase.firestore
        auth = Firebase.auth
        val currentUser = auth.currentUser
        var id: String = ""
        var userRef: DocumentReference? = null
        var myCash: Long = 0
        var myBalance: Long = 0
        var myName: String = ""
        var stealAdapter: ArrayAdapter<String>? = null
        var eventAdapter: ArrayAdapter<String>? = null
        var giftAdapter: ArrayAdapter<String>? = null

        db.collection("users").whereEqualTo("user_id", currentUser.uid).get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    var result = task.result?.documents?.get(0)
                    myCash = result?.data?.get("cash") as Long
                    myBalance = result?.data?.get("balance") as Long
                    myName = result?.data?.get("user_name").toString()
                    id = result?.id!!
                    userRef = db.collection("users").document(id)
                    Log.d("RESULT", result?.data?.get("user_name").toString())

                    val lastSteal: Timestamp = result?.data?.get("last_steal") as Timestamp
                    val currentTime = Date()
                    val stealCooldownOver = lastSteal.toDate().time + 10800000

                    if (currentTime.time >= stealCooldownOver) {
                        binding.buttonSteal.text = "Steal\nReady"
                        binding.buttonSteal.isEnabled = true
                    } else {
                        val timeToGo = stealCooldownOver - currentTime.time
                        val hours = truncate((timeToGo / 1000 / 60 / 60).toDouble()).toInt()
                        val minutes =
                            truncate((timeToGo / 1000 / 60 - (hours * 60)).toDouble()).toInt()
                        binding.buttonSteal.text =
                            "Steal\n" + hours.toString() + "h" + minutes.toString() + "m"
                    }

                    val lastGift: Timestamp = result?.data?.get("last_gift") as Timestamp
                    val giftCooldownOver = lastGift.toDate().time + 3600000

                    if (currentTime.time >= giftCooldownOver) {
                        binding.buttonGift.text = "Gift\nReady"
                        binding.buttonGift.isEnabled = true
                    } else {
                        val timeToGo = giftCooldownOver - currentTime.time
                        val hours = truncate((timeToGo / 1000 / 60 / 60).toDouble()).toInt()
                        val minutes =
                            truncate((timeToGo / 1000 / 60 - (hours * 60)).toDouble()).toInt()
                        binding.buttonGift.text =
                            "Gift\n" + hours.toString() + "h" + minutes.toString() + "m"
                    }
                }
            }

        var events = arrayListOf<String>()

        db.collection("events").orderBy("timestamp", Query.Direction.DESCENDING).get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    for (document in task.result!!.documents) {
                        if (document.data?.get("to_user_id")
                                .toString() == currentUser.uid || document.data?.get("from_user_id")
                                .toString() == currentUser.uid
                        ) {
                            val timestamp: Timestamp = document.data?.get("timestamp") as Timestamp
                            val timeSince = Date().time - timestamp.toDate().time
                            val hours = (timeSince / 1000 / 60 / 60).toDouble()
                            Log.d("TIME", hours.toString())
                            var time = "\n(less than an hour ago)"

                            if (hours >= 1) {
                                time = "\n(" + ceil(hours).toInt() + " hours ago)"
                            }

                            if (hours >= 24) {
                                val days = ceil((hours / 24)).toInt()
                                time = "\n($days days ago)"

                                if (days >= 7) {
                                    val weeks = ceil((days / 7).toDouble()).toInt()
                                    time = "\n($weeks weeks ago)"
                                }
                            }

                            events.add(document.data?.get("event").toString() + time)
                        }
                    }

                    val adapter = ArrayAdapter<String>(
                        this.requireContext(),
                        android.R.layout.simple_list_item_1,
                        events
                    )

                    binding.listEvents.adapter = adapter
                    eventAdapter = adapter
                }
            }


        binding.buttonSteal.setOnClickListener {
            var myDialog: Dialog
            myDialog = Dialog(this.requireContext())
            myDialog.setContentView(R.layout.popup_steal);
            myDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            myDialog.show()

            val textSearch = myDialog.findViewById<EditText>(R.id.textSearch)
            val listPlayers = myDialog.findViewById<ListView>(R.id.listPlayers)
            val buttonBack = myDialog.findViewById<ImageButton>(R.id.buttonBack3)

            var allUsers = arrayListOf<String>()
            var allIds = arrayListOf<String>()
            var users = arrayListOf<String>()
            var ids = arrayListOf<String>()

            db.collection("users").orderBy("cash", Query.Direction.DESCENDING).get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        for (document in task.result!!.documents) {
                            if (document.data?.get("user_id").toString() != currentUser.uid) {
                                Log.d("DATA", "${document.id} => ${document.data}")
                                allIds.add(document.id)
                                ids.add(document.id)
                                allUsers.add(
                                    document.data?.get("user_name")
                                        .toString() + " - €" + document.data?.get("cash")
                                        .toString() + ",- cash"
                                )
                                users.add(
                                    document.data?.get("user_name")
                                        .toString() + " - €" + document.data?.get("cash")
                                        .toString() + ",- cash"
                                )
                            }
                        }

                        val adapter = ArrayAdapter<String>(
                            this.requireContext(),
                            android.R.layout.simple_list_item_1,
                            users
                        )

                        listPlayers.adapter = adapter
                        stealAdapter = adapter
                    }
                }

            listPlayers.setOnItemClickListener { _, _, position, _ ->

                val user = db.collection("users").document(ids[position])

                user.get()
                    .addOnSuccessListener { document ->
                        var userName = document.data?.get("user_name").toString()
                        var cash = document.data?.get("cash") as Long

                        if (cash > 0) {
                            var story= ""
                            var myMoney = myCash + myBalance
                            var amount = (-(cash)..cash).random()
                            if(amount < -(myMoney)) { amount = -(myMoney) }
                            var userId = document.data?.get("user_id").toString()

                            if (amount < 0) {
                                story =
                                    myName + " Tried to steal from " + userName + ", but was caught. They were fined €" + Math.abs(
                                        amount
                                    ).toString() + ",-"
                            } else {
                                story =
                                    myName + " Successfully stole €" + amount + ",- from " + userName
                            }


                            userRef!!
                                .update("cash", myCash + amount, "last_steal", Date())
                                .addOnSuccessListener {
                                    Toast.makeText(
                                        this.context,
                                        story,
                                        Toast.LENGTH_SHORT
                                    ).show();
                                }

                            if (amount > 0) {
                                user
                                    .update("cash", cash - amount)
                                    .addOnSuccessListener {
                                        Log.d("DEBUG", "updated 2nd user")
                                    }
                            }

                            var event = Event(story, currentUser.uid, userId, Date())
                            db.collection("events").add(event)

                            binding.buttonSteal.isEnabled = false
                            binding.buttonSteal.text = "Steal\n3h0m"
                            myDialog.dismiss()

                            events.add(0, story + " (just now)")
                            eventAdapter?.notifyDataSetChanged()
                        } else {
                            Toast.makeText(
                                this.context,
                                "This player has no cash. You cannot steal from them.",
                                Toast.LENGTH_SHORT
                            ).show();
                        }
                    }
            }

            textSearch.addTextChangedListener { text ->
                users.clear()
                ids.clear()

                if (text != null && text.toString().isNotEmpty()) {
                    allUsers.forEachIndexed { index, user ->
                        if (user.toLowerCase().contains(text.toString().toLowerCase())) {
                            users.add(user)
                            ids.add(allIds[index])
                        }
                    }
                } else {
                    allUsers.forEachIndexed { index, user ->
                        users.add(user)
                        ids.add(allIds[index])
                    }
                }

                stealAdapter?.notifyDataSetChanged()
            }

            buttonBack.setOnClickListener {
                myDialog.dismiss()
            }
        }

        binding.buttonGift.setOnClickListener {
            var myDialog: Dialog
            myDialog = Dialog(this.requireContext())
            myDialog.setContentView(R.layout.popup_gift);
            myDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            myDialog.show()

            val textSearch = myDialog.findViewById<EditText>(R.id.textFind)
            val listPlayers = myDialog.findViewById<ListView>(R.id.listUsers)
            val buttonBack = myDialog.findViewById<ImageButton>(R.id.buttonBack4)
            val textAmount = myDialog.findViewById<EditText>(R.id.textGiftAmount)
            val buttonAll = myDialog.findViewById<Button>(R.id.buttonGiftAll)

            var allUsers = arrayListOf<String>()
            var allIds = arrayListOf<String>()
            var users = arrayListOf<String>()
            var ids = arrayListOf<String>()

            db.collection("users").whereNotEqualTo("user_id", currentUser.uid).get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        for (document in task.result!!.documents) {
                            Log.d("DATA", "${document.id} => ${document.data}")
                            allIds.add(document.id)
                            ids.add(document.id)
                            allUsers.add(
                                document.data?.get("user_name").toString()
                            )
                            users.add(
                                document.data?.get("user_name").toString()
                            )
                        }

                        val adapter = ArrayAdapter<String>(
                            this.requireContext(),
                            android.R.layout.simple_list_item_1,
                            users
                        )

                        listPlayers.adapter = adapter
                        giftAdapter = adapter
                    }
                }

            listPlayers.setOnItemClickListener { _, _, position, _ ->
                val user = db.collection("users").document(ids[position])
                user.get()
                    .addOnSuccessListener { document ->
                        val text = textAmount.text.toString()
                        if (text.isNotEmpty() && text != "0") {
                            var amount: Long = java.lang.Long.parseLong(text)

                            var userName = document.data?.get("user_name").toString()
                            var userId = document.data?.get("user_id").toString()
                            var cash = document.data?.get("cash") as Long

                            var story =
                                myName + " gifted €" + amount + ",- to " + userName + ". How nice of them!"

                            userRef!!
                                .update("cash", myCash - amount, "last_gift", Date())
                                .addOnSuccessListener {
                                    Toast.makeText(
                                        this.context,
                                        story,
                                        Toast.LENGTH_SHORT
                                    ).show();
                                }

                            user
                                .update("cash", cash + amount)
                                .addOnSuccessListener {
                                    Log.d("DEBUG", "updated 2nd user")
                                }

                            var event = Event(story, currentUser.uid, userId, Date())
                            db.collection("events").add(event)

                            binding.buttonGift.isEnabled = false
                            binding.buttonGift.text = "Gift\n1h0m"
                            myDialog.dismiss()

                            events.add(0, story + " (just now)")
                            eventAdapter?.notifyDataSetChanged()
                        } else {
                            Toast.makeText(
                                this.context,
                                "You have not provided an amount to gift. Please fill in the field.",
                                Toast.LENGTH_SHORT
                            ).show();
                        }
                    }
            }

            textSearch.addTextChangedListener { text ->
                users.clear()
                ids.clear()

                if (text != null && text.toString().isNotEmpty()) {
                    allUsers.forEachIndexed { index, user ->
                        if (user.toLowerCase().contains(text.toString().toLowerCase())) {
                            users.add(user)
                            ids.add(allIds[index])
                        }
                    }
                } else {
                    allUsers.forEachIndexed { index, user ->
                        users.add(user)
                        ids.add(allIds[index])
                    }
                }

                giftAdapter?.notifyDataSetChanged()
            }

            textAmount.addTextChangedListener { text ->
                if (text != null && text.toString().isNotEmpty()) {
                    val input: Long = java.lang.Long.parseLong(text.toString())
                    if (input > myCash) {
                        textAmount.setText(myCash.toString())
                        textAmount.setSelection(textAmount.text.toString().length)
                    }
                }
            }

            buttonAll.setOnClickListener {
                textAmount.setText(myCash.toString())
            }

            buttonBack.setOnClickListener {
                myDialog.dismiss()
            }
        }

        return binding.root
    }
}