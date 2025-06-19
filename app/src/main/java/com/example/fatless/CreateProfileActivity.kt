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
        val userRef = database.getReference("users")

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
        val userRef = database.getReference("users")

        val userData = mapOf(
            "name" to name,
            "age" to age,
            "profile_complete" to true,
            "calories_burned" to 0,
            "favorite_workouts" to emptyList<String>()
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