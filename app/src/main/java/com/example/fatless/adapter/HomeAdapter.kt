package com.example.fatless.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.fatless.R
import com.example.fatless.data.Sport
import com.example.fatless.databinding.ItemSportBinding
import com.example.fatless.utilities.constants


class HomeAdapter (
    private var sportList: List<Sport>,
    private val onPlayClick: (Sport) -> Unit,
    private val onFavoriteClick: (Sport) -> Unit) : RecyclerView.Adapter<HomeAdapter.HomeViewHolder>() {

    inner class HomeViewHolder(val binding: ItemSportBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeViewHolder {
        val binding = ItemSportBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HomeViewHolder(binding)
    }



    override fun onBindViewHolder(holder: HomeViewHolder, position: Int) {
        val sport = sportList[position]
        val b = holder.binding

        // Set texts
        b.sportLBLSportName.text = sport.name
        b.sportLBLTime.text = sport.duration.toString()
        b.tvSportDuration.text = constants.DB.minRef
        b.sportLBLSportCalories.text = sport.calories.toString()
        b.tvSportCalories.text = constants.DB.kcalRef

        // Set image
        b.imgSport.setImageResource(sport.imageResId)


        // Favorite button
        val favIcon = if (sport.isFavorite) R.drawable.favorite_bnt_pic else R.drawable.unfavorite_bnt_pic
        b.sportBTNFavorite.setImageResource(favIcon)

        b.sportBTNFavorite.setOnClickListener {
            onFavoriteClick(sport)
        }

        // Background color for current sport
        val cardColor = if (sport.isInCurrent) R.color.progress_yellow else R.color.white
        b.cardSport.setCardBackgroundColor(ContextCompat.getColor(b.root.context, cardColor))

        b.sportBTNPlay.setOnClickListener {
            onPlayClick(sport)
        }

    }

    override fun getItemCount(): Int = sportList.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(newList: List<Sport>) {
        sportList = newList
        notifyDataSetChanged()
    }
}