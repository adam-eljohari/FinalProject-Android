package com.example.fatless.data


data class Sport(val name: String,
                 val duration: Int,
                 val calories: Int,
                 val imageResId: Int ,
                 var isFavorite: Boolean = false,
                 var isInCurrent: Boolean = false)
