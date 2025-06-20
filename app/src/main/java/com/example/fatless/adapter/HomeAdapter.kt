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
    private val onFavoriteClick: (Sport) -> Unit
) : RecyclerView.Adapter<HomeAdapter.HomeViewHolder>() {

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

        // Set favorite icon
        b.sportBTNFavorite.setImageResource(
            if (sport.isFavorite) R.drawable.favorite_bnt_pic
            else R.drawable.unfavorite_bnt_pic
        )

        // Set "in progress" visual (optional effect)
        if (sport.isInCurrent) {
            b.cardSport.setCardBackgroundColor(
                ContextCompat.getColor(b.root.context, R.color.progress_yellow)
            )

        } else {
            b.cardSport.setCardBackgroundColor(
                ContextCompat.getColor(b.root.context, R.color.white)
            )
            b.sportBTNPlay.setImageResource(R.drawable.play_arrow)
        }

        // Click: Play
        b.sportBTNPlay.setOnClickListener {
            onPlayClick(sport)
        }

        // Click: Favorite
        b.sportBTNFavorite.setOnClickListener {
            sport.isFavorite = !sport.isFavorite
            notifyItemChanged(position)
            onFavoriteClick(sport)
        }
    }

    override fun getItemCount(): Int = sportList.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(newList: List<Sport>) {
        sportList = newList
        notifyDataSetChanged()
    }
}