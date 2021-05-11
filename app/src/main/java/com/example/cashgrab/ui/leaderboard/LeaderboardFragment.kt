package com.example.cashgrab.ui.leaderboard

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.cashgrab.databinding.FragmentLeaderboardBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
class LeaderboardFragment : Fragment() {

    private lateinit var leaderboardViewModel: LeaderboardViewModel
    private lateinit var binding: FragmentLeaderboardBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        leaderboardViewModel =
            ViewModelProvider(this).get(LeaderboardViewModel::class.java)

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

        db.collection("users").orderBy("balance", Query.Direction.DESCENDING).get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    var rank = 1
                    for (document in task.result!!.documents) {
                        if(document.data?.get("user_id") == uid){
                            binding.textRank.text = "You are ranked #"+ rank + " worldwide!"
                        }

                        Log.d("DATA", "${document.id} => ${document.data}")
                        users.add(rank.toString()+ ". " + document.data?.get("user_name").toString() + " - €" + document.data?.get("balance").toString()+ ",-")
                        rank++
                    }

                    val adapter = ArrayAdapter<String>(
                        this.requireContext(),
                        android.R.layout.simple_list_item_1,
                        users
                    )

                    binding.listLb.adapter = adapter
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