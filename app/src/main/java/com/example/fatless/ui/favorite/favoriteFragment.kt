package com.example.fatless.ui.favorite

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fatless.adapter.HomeAdapter
import com.example.fatless.data.Sport
import com.example.fatless.data.SportData
import com.example.fatless.databinding.FragmentFavoriteBinding
import com.example.fatless.utilities.constants
import com.example.fatless.utilities.constants.DB.burnedCaloriesRef
import com.example.fatless.utilities.constants.DB.caloriesPerDayRef
import com.example.fatless.utilities.constants.DB.currentSportRef
import com.example.fatless.utilities.constants.DB.isThereIsSportInCurrentRef
import com.example.fatless.utilities.constants.DB.sessionProgressRef
import com.example.fatless.utilities.constants.DB.usersRef
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class favoriteFragment : Fragment() {

    private var _binding: FragmentFavoriteBinding? = null
    private val binding get() = _binding!!

    private lateinit var homeAdapter: HomeAdapter
    private var favoriteSports = listOf<Sport>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentFavoriteBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        loadFavoriteSports()
    }


    private fun setupRecyclerView() {
        homeAdapter = HomeAdapter(
            sportList = favoriteSports,
            onPlayClick = { sport ->
                setCurrentSport(sport.name)
            },
            onFavoriteClick = { sport ->
                sport.isFavorite = !sport.isFavorite
                updateFavoriteInFirebase(sport)
            }
        )

        binding.recyclerViewFavorites.adapter = homeAdapter
        binding.recyclerViewFavorites.layoutManager = LinearLayoutManager(requireContext())
    }



    private fun loadFavoriteSports() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val userRef = FirebaseDatabase.getInstance().getReference(usersRef).child(uid)

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val favoriteNames = snapshot.child(constants.DB.favoriteSportsRef)
                    .getValue(object : GenericTypeIndicator<List<String>>() {}) ?: emptyList()

                val currentSport = snapshot.child(sessionProgressRef).child(currentSportRef)
                    .getValue(String::class.java)

                val allSports = SportData.getLocalSports()

                favoriteSports = allSports.filter { it.name in favoriteNames }.map { sport ->
                    sport.copy(
                        isFavorite = true,
                        isInCurrent = currentSport != null && sport.name == currentSport
                    )
                }

                homeAdapter.updateList(favoriteSports)

                binding.favoriteLBLTitleEmptyFavorites.visibility = if (favoriteSports.isEmpty()) View.VISIBLE else View.GONE
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Failed to load favorites", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateFavoriteInFirebase(sport: Sport) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val favRef = FirebaseDatabase.getInstance().getReference(usersRef).child(uid).child(constants.DB.favoriteSportsRef)

        favRef.get().addOnSuccessListener { snapshot ->
            val currentFavorites = snapshot.getValue(object : GenericTypeIndicator<List<String>>() {}) ?: emptyList()

            val updatedFavorites = currentFavorites - sport.name

            favRef.setValue(updatedFavorites).addOnSuccessListener {
                // 🧠 Remove from list and update UI
                favoriteSports = favoriteSports.filter { it.name != sport.name }
                homeAdapter.updateList(favoriteSports)

                // Show empty state if needed
                binding.favoriteLBLTitleEmptyFavorites.visibility = if (favoriteSports.isEmpty()) View.VISIBLE else View.GONE
            }
        }
    }

    private fun checkCurrentSportAndSave(sportName: String){

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val userRef = FirebaseDatabase.getInstance().getReference(usersRef).child(uid)

        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val sport = SportData.getLocalSports().find { it.name == sportName }

        if (sport != null){

            userRef.get().addOnSuccessListener { snap ->
                val current = snap.child(caloriesPerDayRef).child(today).getValue(Int::class.java) ?: 0
                val burnedCalories = snap.child(sessionProgressRef).child(burnedCaloriesRef).getValue(Int::class.java) ?: 0
                val newTotal = current + burnedCalories
                userRef.child(caloriesPerDayRef).child(today).setValue(newTotal)

            }
            val sessionData = mapOf(
                isThereIsSportInCurrentRef to false,
                currentSportRef to null,
                constants.DB.timeLeftInMillisRef to 0,
                burnedCaloriesRef to 0
            )
            userRef.child(sessionProgressRef).setValue(sessionData)
        }

    }

    private fun setCurrentSport(sportName: String) {

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val userRef = FirebaseDatabase.getInstance().getReference(usersRef).child(uid)

        userRef.child(sessionProgressRef).get().addOnSuccessListener { snap ->
            val currentSportName = snap.child(currentSportRef).getValue(String::class.java)
            val isThereIsCurrentSport = snap.child(isThereIsSportInCurrentRef).getValue(Boolean::class.java)
            if (currentSportName != null) {
                if (isThereIsCurrentSport == true && currentSportName != sportName) {
                    checkCurrentSportAndSave(currentSportName)
                }
            }
            val sport = SportData.getLocalSports().find { it.name == sportName }
            if (isThereIsCurrentSport == false && currentSportName != sportName && sport != null) {

                val timeLeftInMillis = (sport.duration * 60 * 1000).toLong()

                val sessionData = mapOf(
                    isThereIsSportInCurrentRef to false,
                    currentSportRef to sportName,
                    constants.DB.timeLeftInMillisRef to timeLeftInMillis,
                    burnedCaloriesRef to 0
                )

                userRef.child(sessionProgressRef).setValue(sessionData)
                    .addOnSuccessListener {

                        Toast.makeText(
                            requireContext(),
                            "Now playing: $sportName",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                        loadFavoriteSports()
                    }
                    .addOnFailureListener {
                        Toast.makeText(
                            requireContext(),
                            "Failed to set current sport",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

            }
        }
    }




    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
