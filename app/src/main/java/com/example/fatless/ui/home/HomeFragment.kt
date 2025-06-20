package com.example.fatless.ui.home

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fatless.R
import com.example.fatless.adapter.HomeAdapter
import com.example.fatless.data.Sport
import com.example.fatless.databinding.FragmentHomeBinding
import com.example.fatless.utilities.constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeFragment : Fragment() {

private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var homeAdapter: HomeAdapter


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
      _binding = FragmentHomeBinding.inflate(inflater, container, false)

      return binding.root
  }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadUserName()
        binding.homeBTNCalculateBMI.setOnClickListener {
            calculateAndShowBMI()
        }
        setupRecyclerView()
    }

    private fun loadUserName() {

        val uid = FirebaseAuth.getInstance().currentUser?.uid
        val database = FirebaseDatabase.getInstance()
        val userRef = database.getReference("users").child(uid!!)
        val todayDate = getTodayDate()

        userRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val name = snapshot.child(constants.DB.nameRef).value.toString()
                val caloriesPerDay = snapshot.child(constants.DB.caloriesPerDayRef).child(todayDate).getValue(Int::class.java) ?: 0

                binding.homeETUserName.text = name

                binding.homeLBLCaloriesValue.text = caloriesPerDay.toString()
            }
        }.addOnFailureListener {
            Toast.makeText(requireContext(), "Failed to load profile", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getTodayDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    @SuppressLint("DefaultLocale")
    private fun calculateAndShowBMI() {
        val heightText = binding.homeETHeight.text.toString()
        val weightText = binding.homeETWeight.text.toString()

        if (heightText.isBlank() || weightText.isBlank()) {
            binding.homeLBLBMIValue.text = "-"
            binding.homeLBLError.visibility = View.VISIBLE
            Toast.makeText(requireContext(), "Please enter height and weight", Toast.LENGTH_SHORT).show()
            return
        }

        val heightCm = heightText.toFloatOrNull()
        val weightKg = weightText.toFloatOrNull()

        if (heightCm == null || weightKg == null || heightCm == 0f ) {
            binding.homeLBLBMIValue.text = "-"
            binding.homeLBLError.visibility = View.VISIBLE
            Toast.makeText(requireContext(), "Invalid input", Toast.LENGTH_SHORT).show()
            return
        }
        if (heightCm !in 50.0..272.0) {
            binding.homeLBLBMIValue.text = "-"
            Toast.makeText(context, "Please enter a valid height between 50 cm and 272 cm", Toast.LENGTH_SHORT).show()
            binding.homeLBLError.visibility = View.VISIBLE
            return
        }

        if (weightKg !in 2.0..635.0) {
            binding.homeLBLBMIValue.text = "-"
            Toast.makeText(context, "Please enter a valid weight between 2 kg and 635 kg", Toast.LENGTH_SHORT).show()
            binding.homeLBLError.visibility = View.VISIBLE
            return
        }

        binding.homeLBLError.visibility = View.INVISIBLE

        val heightM = heightCm / 100
        val bmi = weightKg / (heightM * heightM)

        val category = when {
            bmi < 18.5 -> "Underweight"
            bmi < 25 -> "Normal weight"
            bmi < 30 -> "Overweight"
            bmi < 40 -> "Obesity"
            else -> "Morbidly Obesity"
        }
        binding.homeLBLBMIValue.text = category
    }

    private fun setupRecyclerView() {
        val sports = listOf(
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

        homeAdapter = HomeAdapter(
            sportList = sports,
            onPlayClick = { selectedSport ->
                val updatedList = sports.map {
                    it.copy(isInCurrent = it.name == selectedSport.name)
                }
                homeAdapter.updateList(updatedList)
            },
            onFavoriteClick = { sport ->
                saveFavoriteSportToFirebase(sport)
                Toast.makeText(requireContext(), "${sport.name} favorited: ${sport.isFavorite}", Toast.LENGTH_SHORT).show()
            }
        )

        binding.homeRVSportList.adapter = homeAdapter
        binding.homeRVSportList.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun saveFavoriteSportToFirebase(sport: Sport) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val favRef = FirebaseDatabase.getInstance()
            .getReference("users")
            .child(uid)
            .child("favoriteSports")

        favRef.get().addOnSuccessListener { snapshot ->
            val currentFavorites = snapshot.getValue(object : GenericTypeIndicator<List<String>>() {}) ?: emptyList()

            val updatedFavorites = if (sport.isFavorite) {
                currentFavorites + sport.name
            } else {
                currentFavorites - sport.name
            }

            favRef.setValue(updatedFavorites.distinct())
        }
    }

override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}