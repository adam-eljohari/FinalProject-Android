package com.example.fatless.ui.currentSession

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.fatless.R
import com.example.fatless.data.Sport
import com.example.fatless.data.SportData
import com.example.fatless.databinding.FragmentCurrentsessionBinding
import com.example.fatless.utilities.constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class currentSessionFragment : Fragment()  {

    private var _binding: FragmentCurrentsessionBinding? = null
    private val binding get() = _binding!!

    private val db = FirebaseDatabase.getInstance().reference
    private val uid = FirebaseAuth.getInstance().currentUser?.uid

    private var countDownTimer: CountDownTimer? = null
    private var isTimerRunning = false
    private var totalDurationSeconds: Int = 0
    private var caloriesPerSecond: Float = 0f
    private var totalCalories: Int = 0

    private lateinit var currentSport: Sport
    private var totalTimeInMillis: Long = 0L
    private var timeLeftInMillis: Long = 0L

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCurrentsessionBinding.inflate(inflater, container, false)

        loadCurrentSportData()

        binding.currentBTNStart.setOnClickListener {
            if (!isTimerRunning && timeLeftInMillis > 0) {
                startTimer()
            }
        }

        binding.currentBTNPause.setOnClickListener {
            pauseTimerAndSave()
        }

        binding.currentBTNDelete.setOnClickListener {

            finishSession()
        }

        return binding.root
    }

    private fun loadCurrentSportData() {
        if (uid == null) return

        db.child(constants.DB.usersRef).child(uid).child(constants.DB.currentSportRef)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val sportName = snapshot.getValue(String::class.java)

                    if (sportName.isNullOrEmpty()) {
                        showNoSession()
                        return
                    }

                    val sport = SportData.getLocalSports().find { it.name == sportName }

                    if (sport != null) {


                        currentSport = sport
                        totalTimeInMillis = (sport.duration * 60 * 1000).toLong()
                        timeLeftInMillis = totalTimeInMillis



                        binding.currentLBLSportName.text = sport.name

                        totalDurationSeconds = sport.duration * 60
                        totalCalories = sport.calories
                        caloriesPerSecond = sport.calories.toFloat() / totalDurationSeconds

                        db.child(constants.DB.usersRef).child(uid).child(constants.DB.sessionProgressRef).get()
                            .addOnSuccessListener { snap ->
                                timeLeftInMillis =
                                    snap.child(constants.DB.timeLeftInMillisRef).getValue(Long::class.java)
                                        ?: (totalDurationSeconds * 1000L)

                                val savedCalories = snap.child(constants.DB.burnedCaloriesRef).getValue(Int::class.java) ?: 0

                                updateTimerUI()
                                binding.currentLBLCaloriesValue.text = sport.calories.toString()
                                binding.currentPBProgressCalories.max = sport.calories
                                binding.currentPBProgressCalories.progress = savedCalories
                                binding.currentImgSessionArt.setImageResource(sport.imageResId)
                            }
                    } else {
                        showNoSession()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    showNoSession()
                }
            })
    }

    private fun startTimer() {
        countDownTimer = object : CountDownTimer(timeLeftInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillis = millisUntilFinished
                updateTimerUI()
            }

            override fun onFinish() {
                isTimerRunning = false
                binding.currentLBLTimer.text = buildString { append("00:00") }
                Toast.makeText(requireContext(), "Session Completed!", Toast.LENGTH_SHORT).show()
            }
        }.start()

        isTimerRunning = true
    }

    private fun pauseTimerAndSave() {
        countDownTimer?.cancel()
        isTimerRunning = false

        val elapsedSeconds = totalDurationSeconds - (timeLeftInMillis / 1000).toInt()
        val burnedCalories = (elapsedSeconds * caloriesPerSecond).toInt().coerceAtMost(totalCalories)

        binding.currentPBProgressCalories.progress = burnedCalories

        val sessionData = mapOf(
            constants.DB.timeLeftInMillisRef to timeLeftInMillis,
            constants.DB.burnedCaloriesRef to burnedCalories
        )

        uid?.let {
            db.child(constants.DB.usersRef).child(it).child(constants.DB.sessionProgressRef).setValue(sessionData)
        }

    }

    private fun updateTimerUI() {
        val minutes = (timeLeftInMillis / 1000) / 60
        val seconds = (timeLeftInMillis / 1000) % 60
        binding.currentLBLTimer.text = String.format("%02d:%02d", minutes, seconds)

        val elapsedSeconds = totalDurationSeconds - (timeLeftInMillis / 1000).toInt()
        val caloriesProgress = (caloriesPerSecond * elapsedSeconds).toInt().coerceAtMost(totalCalories)
        binding.currentPBProgressCalories.progress = caloriesProgress
    }

    private fun finishSession() {
        pauseTimerAndSave()
        if (uid == null) return

        val burnedCalories = binding.currentPBProgressCalories.progress.toString().toIntOrNull() ?: 0
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())


        val userRef = db.child(constants.DB.usersRef).child(uid)

        userRef.child(constants.DB.currentSportRef).removeValue()
        userRef.child(constants.DB.sessionProgressRef).removeValue()

        userRef.child(constants.DB.caloriesPerDayRef).child(today)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val current = snapshot.getValue(Int::class.java) ?: 0
                    val newTotal = current + burnedCalories

                    userRef.child(constants.DB.caloriesPerDayRef).child(today).setValue(newTotal)
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(), "Session finished and calories updated!", Toast.LENGTH_SHORT).show()
                            showNoSession()
                        }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(), "Failed to update calories", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun showNoSession() {
        binding.currentLBLSportName.text = buildString {
        append("No active session")
    }
        binding.currentLBLTimer.text = "--:--"
        binding.currentLBLCaloriesValue.text = "0"
        binding.currentPBProgressCalories.progress = 0
        binding.currentImgSessionArt.setImageResource(R.drawable.fatlessicon)
    }

    override fun onPause() {
        super.onPause()
        if (isTimerRunning) {
            pauseTimerAndSave()
        }
    }

    override fun onStop() {
        super.onStop()
        if (isTimerRunning) {
            pauseTimerAndSave()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        countDownTimer?.cancel()
        _binding = null
    }
}
