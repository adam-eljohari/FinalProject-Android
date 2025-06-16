package com.example.fatless.ui.favorite

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class favoriteViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is progress Fragment"
    }
    val text: LiveData<String> = _text
}