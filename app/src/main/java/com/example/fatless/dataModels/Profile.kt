package com.example.fatless.dataModels

data class Profile(val name: String, val age: Int, val profileCompleted: Boolean,
                   val caloriesBurned: Int? = null, val favoriteSport: List<String>? = null){

    data class Builder(
        private var name: String = "", private var age: Int = 0,
        private var profileCompleted: Boolean = false,
        private var caloriesBurned: Int? = null,
        private var favoriteSport: List<String>? = null)
    {
        fun name(name: String) = apply {
            this.name = name
        }
        fun age(age: Int) = apply {
            this.age = age
        }
        fun profileComplete(profileCompleted: Boolean) = apply {
            this.profileCompleted = profileCompleted
        }
        fun caloriesBurned(caloriesBurned: Int?) = apply {
            this.caloriesBurned = caloriesBurned
        }
        fun favoriteWorkouts(favoriteSport: List<String>?) = apply {
            this.favoriteSport = favoriteSport
        }

        fun build() = Profile(name, age, profileCompleted, caloriesBurned, favoriteSport)
    }

}
