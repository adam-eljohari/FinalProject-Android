package com.example.fatless.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.fatless.R
import com.example.fatless.data.User
import androidx.core.graphics.toColorInt

class LeaderboardAdapter (private val context: Context)
    : RecyclerView.Adapter<LeaderboardAdapter.LeaderboardViewHolder>() {

    private var userList: List<User> = emptyList()

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(newList: List<User>) {
        userList = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeaderboardViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_leaderboard, parent, false)
        return LeaderboardViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: LeaderboardViewHolder, position: Int) {
        val user = userList[position]

        holder.userName.text = user.name
        holder.userAge.text = user.age.toString()
        holder.userCalories.text = user.caloriesBurned.toString()
        holder.userRank.text = (position + 1).toString()

        // Set rank background color dynamically
        val rankColor = when (position) {
            0 -> "#FFD700".toColorInt() // Gold
            1 -> "#C0C0C0".toColorInt() // Silver
            2 -> "#CD7F32".toColorInt() // Bronze
            else -> ContextCompat.getColor(context, R.color.default_rank_color)
        }

        val bgDrawable = ContextCompat.getDrawable(context, R.drawable.rank_circle)?.mutate()
        bgDrawable?.setTint(rankColor)
        holder.userRank.background = bgDrawable

    }

    override fun getItemCount(): Int = userList.size

    class LeaderboardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userRank: TextView = itemView.findViewById(R.id.leaderboard_LBL_rank)
        val userName: TextView = itemView.findViewById(R.id.leaderboard_LBL_UserName)
        val userAge: TextView = itemView.findViewById(R.id.leaderboard_LBL_age)
        val userCalories: TextView = itemView.findViewById(R.id.leaderboard_LBL_Calories)
    }
}