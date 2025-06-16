package com.example.fatless.ui.currentSession

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class currentSessionViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is current Session Fragment"
    }
    val text: LiveData<String> = _text
}