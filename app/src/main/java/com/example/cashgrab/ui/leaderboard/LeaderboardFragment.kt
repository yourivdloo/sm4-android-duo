package com.example.cashgrab.ui.leaderboard

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.view.forEach
import androidx.core.view.get
import androidx.fragment.app.Fragment
import com.example.cashgrab.R
import com.example.cashgrab.databinding.FragmentLeaderboardBinding
import com.example.cashgrab.models.ColoredName
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class LeaderboardFragment : Fragment() {
    private lateinit var binding: FragmentLeaderboardBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentLeaderboardBinding.inflate(layoutInflater)

        auth = Firebase.auth
        val currentUser = auth.currentUser
        val uid = currentUser.uid
        val db = Firebase.firestore

        if (currentUser == null) {
            failureToast()
        } else {
            Log.d("RESULT", currentUser?.uid.toString())
        }

        var users = arrayListOf<String>()

        var coloredNames = arrayListOf<ColoredName>()

        db.collection("users").orderBy("balance", Query.Direction.DESCENDING).get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    var rank = 1
                    var index = 0
                    for (document in task.result!!.documents) {
                        val userString = rank.toString()+ ". " + document.data?.get("user_name").toString() + " - €" + document.data?.get("balance").toString()+ ",-"
                        if(document.data?.get("user_id") == uid){
                            binding.textRank.text = "You are ranked #"+ rank + " worldwide!"
                            binding.textYou.text = userString
                        }

                        if(document.data?.get("role") != ""){
                            coloredNames.add(ColoredName(index, document.data?.get("role").toString()))
                        }

                        Log.d("DATA", "${document.id} => ${document.data}")
                        users.add(userString)
                        rank++
                        index++
                    }

                    val adapter = ArrayAdapter<String>(
                        this.requireContext(),
                        android.R.layout.simple_list_item_1,
                        users
                    )

                    binding.listLb.adapter = adapter
                }
            }

        binding.switchRole.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked){
                for (coloredName in coloredNames){
                    println(coloredName.index.toString())
                    println(coloredName.role)

                    if(coloredName.role == "laguna"){
                        binding.listLb[coloredName.index].setBackgroundColor(resources.getColor(R.color.laguna))
                    } else if (coloredName.role == "galactic"){
                        binding.listLb[coloredName.index].setBackgroundColor(resources.getColor(R.color.galactic))
                    } else if (coloredName.role == "acid"){
                        binding.listLb[coloredName.index].setBackgroundColor(resources.getColor(R.color.acid))
                    } else if (coloredName.role == "metallic"){
                        binding.listLb[coloredName.index].setBackgroundColor(resources.getColor(R.color.metallic))
                    }
                }
            } else {
                binding.listLb.forEach{
                    it.setBackgroundColor(Color.TRANSPARENT)
                }
            }
        }

        return binding.root
    }

    private fun failureToast(){
        Toast.makeText(
            this.context, "Something went wrong...",
            Toast.LENGTH_SHORT
        ).show();
    }
}