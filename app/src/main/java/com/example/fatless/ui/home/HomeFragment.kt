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
import com.example.fatless.data.SportData
import com.example.fatless.databinding.FragmentHomeBinding
import com.example.fatless.utilities.constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeFragment : Fragment() {

private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var homeAdapter: HomeAdapter
    private var sportList = listOf<Sport>()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
      _binding = FragmentHomeBinding.inflate(inflater, container, false)

      return binding.root
  }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        loadUserData()
        binding.homeBTNCalculateBMI.setOnClickListener {
            calculateAndShowBMI()
        }
    }


    private fun setupRecyclerView() {
        homeAdapter = HomeAdapter(
            sportList = sportList,
            onPlayClick = { sport ->
                setCurrentSport(sport.name)
            },
            onFavoriteClick = { sport ->
                toggleFavorite(sport)
            }
        )

        binding.homeRVSportList.adapter = homeAdapter
        binding.homeRVSportList.layoutManager = LinearLayoutManager(requireContext())
    }




    private fun loadUserData() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val userRef = FirebaseDatabase.getInstance()
            .getReference(constants.DB.usersRef)
            .child(uid)

        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())


        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userName = snapshot.child(constants.DB.nameRef).getValue(String::class.java)
                val caloriesToday = snapshot.child(constants.DB.caloriesPerDayRef).child(today).getValue(Int::class.java) ?: 0
                val favoriteNames = snapshot.child(constants.DB.favoriteSportsRef)
                    .getValue(object : GenericTypeIndicator<List<String>>() {}) ?: emptyList()
                val currentSport = snapshot.child(constants.DB.currentSportRef).getValue(String::class.java)

                binding.homeETUserName.text = userName
                binding.homeLBLCaloriesValue.text = caloriesToday.toString()

                sportList = SportData.getLocalSports().map { sport ->
                    sport.copy(
                        isFavorite = sport.name in favoriteNames,
                        isInCurrent = currentSport != null && sport.name == currentSport
                    )
                }

                homeAdapter.updateList(sportList)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Failed to load user data", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun toggleFavorite(sport: Sport) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val favRef = FirebaseDatabase.getInstance()
            .getReference(constants.DB.usersRef)
            .child(uid)
            .child(constants.DB.favoriteSportsRef)

        favRef.get().addOnSuccessListener { snapshot ->
            val currentFavorites = snapshot.getValue(object : GenericTypeIndicator<List<String>>() {}) ?: emptyList()

            val updatedFavorites = if (!sport.isFavorite) {
                (currentFavorites + sport.name).distinct()
            } else {
                currentFavorites - sport.name
            }

            favRef.setValue(updatedFavorites).addOnSuccessListener {
                sportList = sportList.map {
                    if (it.name == sport.name) it.copy(isFavorite = !sport.isFavorite) else it
                }
                homeAdapter.updateList(sportList)
            }
        }
    }

    private fun setCurrentSport(sportName: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val userRef = FirebaseDatabase.getInstance()
            .getReference(constants.DB.usersRef)
            .child(uid)

        userRef.child(constants.DB.currentSportRef).setValue(sportName)
            .addOnSuccessListener {
                sportList = sportList.map {
                    it.copy(isInCurrent = it.name == sportName)
                }
                homeAdapter.updateList(sportList)
                Toast.makeText(requireContext(), "Now playing: $sportName", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to set current sport", Toast.LENGTH_SHORT).show()
            }
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


override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}