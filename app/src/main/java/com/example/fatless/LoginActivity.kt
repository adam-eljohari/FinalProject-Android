package com.example.fatless

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.fatless.utilities.constants
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        if (FirebaseAuth.getInstance().currentUser == null){
            signIn()
        }
        else{
            checkProfileCreated()
        }
    }

    private val signInLauncher = registerForActivityResult(
        FirebaseAuthUIActivityResultContract(),
    ) { res ->
        this.onSignInResult(res)
    }

    private fun signIn() {

        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.PhoneBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build(),)

        val signInIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .setTheme(R.style.MyFirebaseUITheme)
            .setLogo(R.drawable.fatlessicon)
            .build()
        signInLauncher.launch(signInIntent)
    }

    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {

        if (result.resultCode == RESULT_OK) {
            // Successfully signed in

            checkProfileCreated()

        } else {
            Toast.makeText(this,"Error: Failed logging in.",Toast.LENGTH_LONG).show()
            signIn()
        }
    }

    private fun checkProfileCreated(){

        val uid = FirebaseAuth.getInstance().currentUser?.uid
        val userRef = FirebaseDatabase.getInstance().getReference(constants.DB.usersRef).child(uid!!)

        userRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                Toast.makeText(this, "Welcome back!", Toast.LENGTH_SHORT).show()
                transactToMain()
            } else {
                Toast.makeText(this, "Welcome! Please set up your profile.", Toast.LENGTH_SHORT).show()
                transactToNextScreen()
            }
        }

    }
    @SuppressLint("UnsafeIntentLaunch")
    private fun transactToMain() {
        intent = Intent(this,MainActivity::class.java)
        startActivity(intent)
        finish()
    }
    @SuppressLint("UnsafeIntentLaunch")
    private fun transactToNextScreen() {
        intent = Intent(this,CreateProfileActivity::class.java)
        startActivity(intent)
        finish()
    }

}