package com.example.fatless.ui.LeaderBoard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class LeaderboardViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is Leader Board Fragment"
    }
    val text: LiveData<String> = _text
}