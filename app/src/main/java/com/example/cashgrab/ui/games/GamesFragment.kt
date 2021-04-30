package com.example.cashgrab.ui.games

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.cashgrab.R

class GamesFragment : Fragment() {

    private lateinit var gameViewModel: GamesViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        gameViewModel =
                ViewModelProvider(this).get(GamesViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_games, container, false)
        val textView: TextView = root.findViewById(R.id.text_games)
        gameViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })
        return root
    }
}