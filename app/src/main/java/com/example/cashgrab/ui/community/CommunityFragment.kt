package com.example.cashgrab.ui.community

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
import com.example.cashgrab.databinding.FragmentCommunityBinding
import com.example.cashgrab.models.Event
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.*
import kotlin.math.truncate

class CommunityFragment : Fragment() {
    private lateinit var communityViewModel: CommunityViewModel
    private lateinit var binding: FragmentCommunityBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        communityViewModel =
            ViewModelProvider(this).get(CommunityViewModel::class.java)

        binding = FragmentCommunityBinding.inflate(layoutInflater)

        val db = Firebase.firestore
        auth = Firebase.auth
        val currentUser = auth.currentUser
        var id: String = ""
        var userRef: DocumentReference? = null
        var myCash: Long = 0
        var myName: String = ""

        db.collection("users").whereEqualTo("user_id", currentUser.uid).get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    var result = task.result?.documents?.get(0)
                    myCash = result?.data?.get("cash") as Long
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
                        val minutes = truncate((timeToGo / 1000 / 60 - (hours * 60)).toDouble()).toInt()
                        binding.buttonSteal.text =
                            "Steal\n" + hours.toString() + "h" + minutes.toString() + "m"
                    }

                    val lastGift: Timestamp = result?.data?.get("last_gift") as Timestamp
                    val giftCooldownOver = lastGift.toDate().time + 10800000

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


        binding.buttonSteal.setOnClickListener {
            var myDialog: Dialog
            myDialog = Dialog(this.requireContext())
            myDialog.setContentView(R.layout.popup_steal);
            myDialog.show()

            val textSearch = myDialog.findViewById<EditText>(R.id.textSearch)
            val listPlayers = myDialog.findViewById<ListView>(R.id.listPlayers)
            val buttonBack = myDialog.findViewById<ImageButton>(R.id.buttonBack3)
            val buttonSearch = myDialog.findViewById<ImageButton>(R.id.buttonSearch)


            var users = arrayListOf<String>()
            var ids = arrayListOf<String>()

            db.collection("users").whereNotEqualTo("user_id", currentUser.uid).get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        for (document in task.result!!.documents) {

                            Log.d("DATA", "${document.id} => ${document.data}")
                            ids.add(document.id)
                            users.add(
                                document.data?.get("user_name")
                                    .toString() + " - €" + document.data?.get("cash")
                                    .toString() + ",- cash"
                            )
                        }

                        val adapter = ArrayAdapter<String>(
                            this.requireContext(),
                            android.R.layout.simple_list_item_1,
                            users
                        )

                        listPlayers.adapter = adapter
                    }
                }

            listPlayers.setOnItemClickListener { parent, view, position, id ->

                val user = db.collection("users").document(ids[position])
                var story: String = ""
                var amount: Long = 0
                var cash: Long = 0
                var userName: String = ""
                var userId: String = ""
                user.get()
                    .addOnSuccessListener { document ->
                        userName = document.data?.get("user_name").toString()
                        cash = document.data?.get("cash") as Long
                        amount = (-(cash)..cash).random().toLong()
                        userId = document.data?.get("user_id").toString()


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

                        if(amount>0){
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
                    }
            }

            textSearch.addTextChangedListener { text ->

            }

            buttonBack.setOnClickListener {
                myDialog.dismiss()
            }
        }

        return binding.root
    }
}