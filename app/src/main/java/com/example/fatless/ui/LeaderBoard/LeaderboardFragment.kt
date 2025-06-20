package com.example.fatless.ui.LeaderBoard

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.fatless.adapter.LeaderboardAdapter
import com.example.fatless.data.User
import com.example.fatless.databinding.FragmentLeaderboardBinding
import com.example.fatless.utilities.constants
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LeaderboardFragment: Fragment() {

    private var _binding: FragmentLeaderboardBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: LeaderboardAdapter
    private val userList = mutableListOf<User>()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        _binding = FragmentLeaderboardBinding.inflate(inflater, container, false)



        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        loadLeaderboardFromRealtimeDB()
    }

    private fun loadLeaderboardFromRealtimeDB() {
        val databaseRef = FirebaseDatabase.getInstance().getReference("users")
        val todayDate = getTodayDate()

        databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                userList.clear()

                for (userSnapshot in snapshot.children) {
                    val name = userSnapshot.child(constants.DB.nameRef).getValue(String::class.java) ?: continue
                    val age = userSnapshot.child(constants.DB.ageRef).getValue(Int::class.java) ?: continue

                    //  Get today's calories only
                    val calories = userSnapshot.child(constants.DB.caloriesPerDayRef).child(todayDate).getValue(Int::class.java) ?: 0

                    // Only include users with non-zero calories today
                    if (calories > 0) {
                        val user = User(name, age, calories)
                        userList.add(user)
                    }
                }

                // Sort by calories descending and take top 10 (or less)
                userList.sortByDescending { it.caloriesBurned }
                val top10 = userList.take(10)
                userList.clear()
                userList.addAll(top10)

                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun getTodayDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    private fun setupRecyclerView() {
        adapter = LeaderboardAdapter(requireContext(), userList)
        binding.leaderboardRecyclerView.adapter = adapter
        binding.leaderboardRecyclerView.setHasFixedSize(true)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

