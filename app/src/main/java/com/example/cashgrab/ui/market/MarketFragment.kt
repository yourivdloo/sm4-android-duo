package com.example.cashgrab.ui.market

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.cashgrab.R

class MarketFragment : Fragment() {

    private lateinit var marketViewModel: MarketViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        marketViewModel =
                ViewModelProvider(this).get(MarketViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_market, container, false)
        val textView: TextView = root.findViewById(R.id.text_market)
        marketViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })
        return root
    }
}