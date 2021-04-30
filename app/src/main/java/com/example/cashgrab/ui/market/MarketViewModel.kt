package com.example.cashgrab.ui.market

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MarketViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is market Fragment"
    }
    val text: LiveData<String> = _text
}