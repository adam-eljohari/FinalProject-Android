package com.example.fatless.ui.LeaderBoard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fatless.adapter.LeaderboardAdapter
import com.example.fatless.data.User
import com.example.fatless.databinding.FragmentLeaderboardBinding
import com.example.fatless.utilities.constants
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LeaderboardFragment: Fragment() {

    private var _binding: FragmentLeaderboardBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: LeaderboardAdapter
    private val userList = mutableListOf<User>()
    private val db = FirebaseDatabase.getInstance().reference


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        _binding = FragmentLeaderboardBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = LeaderboardAdapter(requireContext())
        binding.leaderboardRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.leaderboardRecyclerView.adapter = adapter

        loadLeaderboardData()
    }

        private fun loadLeaderboardData() {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())


        db.child(constants.DB.usersRef).get().addOnSuccessListener { snap ->
            for (userSnap in snap.children) {
                val name = userSnap.child(constants.DB.nameRef).getValue(String::class.java) ?: continue
                val age = userSnap.child(constants.DB.ageRef).getValue(Int::class.java) ?: continue
                val calories = userSnap.child(constants.DB.caloriesPerDayRef).child(today).getValue(Int::class.java) ?: 0
                userList.add(User(name, age, calories))
            }
            val top10 = userList.sortedByDescending { it.caloriesBurned }.take(10)
            adapter.submitList(top10)
        }.addOnFailureListener {
            Toast.makeText(requireContext(), "Failed to load leaderboard", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}

