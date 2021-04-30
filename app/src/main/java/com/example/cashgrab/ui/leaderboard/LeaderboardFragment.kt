package com.example.cashgrab.ui.leaderboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.cashgrab.R
import com.example.cashgrab.ui.market.MarketViewModel

class LeaderboardFragment : Fragment() {

    private lateinit var leaderboardViewModel: LeaderboardViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        leaderboardViewModel =
            ViewModelProvider(this).get(LeaderboardViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_leaderboard, container, false)
        val textView: TextView = root.findViewById(R.id.text_leaderboard)
        leaderboardViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })
        return root
    }
}