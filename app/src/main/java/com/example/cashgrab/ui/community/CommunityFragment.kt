package com.example.cashgrab.ui.community

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.cashgrab.R

class CommunityFragment : Fragment() {
    private lateinit var communityViewModel: CommunityViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        communityViewModel =
            ViewModelProvider(this).get(CommunityViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_community, container, false)
        val textView: TextView = root.findViewById(R.id.text_community)
        communityViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })
        return root
    }
}