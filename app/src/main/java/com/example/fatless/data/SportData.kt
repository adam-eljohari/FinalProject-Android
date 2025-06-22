package com.example.fatless.data

import com.example.fatless.R

object SportData {
    fun getLocalSports(): List<Sport> {
        return listOf(
            Sport("Jumping Jacks", 30, 200, R.drawable.jump_jacks),
            Sport("Push Ups", 20, 150, R.drawable.push_ups),
            Sport("Plank", 10, 90, R.drawable.plank),
            Sport("Squats", 25, 180, R.drawable.squats),
            Sport("Lunges", 20, 160, R.drawable.lunges),
            Sport("Mountain Climbers", 15, 190, R.drawable.mountain_climbers),
            Sport("Burpees", 20, 220, R.drawable.burpees),
            Sport("Yoga", 40, 140, R.drawable.yoga),
            Sport("Shadow Boxing", 25, 210, R.drawable.shadow_boxing),
            Sport("Skipping Rope", 30, 300, R.drawable.skipping_rope),
            Sport("Wall Sit", 10, 80, R.drawable.wall_sit),
            Sport("Sit Ups", 20, 130, R.drawable.sit_ups),
            Sport("Running", 20, 200, R.drawable.running)
        )
    }
}