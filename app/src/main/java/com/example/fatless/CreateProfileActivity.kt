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
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class CreateProfileActivity : AppCompatActivity() {

    private lateinit var profile_LBL_error : TextView
    private lateinit var profile_EDIT_name : TextInputEditText
    private lateinit var profile_EDIT_age : TextInputEditText
    private lateinit var profile_BTN_save : MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_create_profile)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        findView()
        profile_BTN_save.setOnClickListener{
            saveProfileData()
        }
    }

    private fun saveProfileData() {
        val name = profile_EDIT_name.text.toString()
        val age = profile_EDIT_age.text.toString().toIntOrNull() ?: 0

        if (age <= 9 || name.isBlank()){
            profile_LBL_error.visibility = View.VISIBLE
            Toast.makeText(this, "Failed to save profile", Toast.LENGTH_SHORT).show()
            return
        }
        else{
//            save profile data to data base in firebase


            transactToMain()
        }

    }

    private fun findView() {
        profile_LBL_error = findViewById(R.id.profile_LBL_error)
        profile_EDIT_name = findViewById(R.id.profile_EDIT_name)
        profile_EDIT_age = findViewById(R.id.profile_EDIT_age)
        profile_BTN_save = findViewById(R.id.profile_BTN_save)
    }

    private fun transactToMain() {
        intent = Intent(this,MainActivity::class.java)
        startActivity(intent)
        finish()
    }



}