package com.example.fatless

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.fatless.utilities.constants
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class CreateProfileActivity : AppCompatActivity() {

    private lateinit var crprofile_LBL_error : TextView
    private lateinit var crprofile_EDIT_name : TextInputEditText
    private lateinit var crprofile_EDIT_age : TextInputEditText
    private lateinit var crprofile_BTN_save : MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_create_profile)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        checkProfileCreated()
        findView()
        crprofile_BTN_save.setOnClickListener{
            saveProfileData()
        }
    }

    private fun checkProfileCreated(){

        val user = FirebaseAuth.getInstance().currentUser
        val uid = user?.uid

        val database = FirebaseDatabase.getInstance()
        val userRef = database.getReference(constants.DB.usersRef)

        if (uid != null) {
            val profComp = userRef.child(uid).database.getReference(constants.DB.profileCompleteRef)

            if (profComp.equals(true)) {
                transactToMain()
            }
        }

    }

    private fun saveProfileData() {
        val name = crprofile_EDIT_name.text.toString()
        val age = crprofile_EDIT_age.text.toString().toIntOrNull() ?: 0



        if (age < 10 || name.isBlank() || age > 99){
            crprofile_LBL_error.visibility = View.VISIBLE
            Toast.makeText(this, "Failed to save profile", Toast.LENGTH_SHORT).show()
            return
        }
//            save profile data to data base in firebase
        val user = FirebaseAuth.getInstance().currentUser
        val uid = user?.uid

        val database = FirebaseDatabase.getInstance()
        val userRef = database.getReference(constants.DB.usersRef)

        val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val dailyCaloriesMap = mapOf(todayDate to 0)

        val userData = mapOf(
            constants.DB.nameRef to name,
            constants.DB.ageRef to age,
            constants.DB.profileCompleteRef to true,
            constants.DB.caloriesPerDayRef to dailyCaloriesMap,
            constants.DB.favoriteSportsRef to emptyList<String>()
        )

        if (uid != null) {
            userRef.child(uid).setValue(userData)
                .addOnSuccessListener {
                    Toast.makeText(this, "User profile saved!", Toast.LENGTH_SHORT).show()
                    transactToMain()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to save", Toast.LENGTH_SHORT).show()
                }
        }

    }

    private fun findView() {
        crprofile_LBL_error = findViewById(R.id.crprofile_LBL_error)
        crprofile_EDIT_name = findViewById(R.id.crprofile_EDIT_name)
        crprofile_EDIT_age = findViewById(R.id.crprofile_EDIT_age)
        crprofile_BTN_save = findViewById(R.id.crprofile_BTN_save)
    }

    private fun transactToMain() {
        intent = Intent(this,MainActivity::class.java)
        startActivity(intent)
        finish()
    }






}