package com.example.fatless.ui.profile


import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.fatless.LoginActivity
import com.example.fatless.databinding.FragmentProfileBinding
import com.example.fatless.utilities.constants
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase


class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)

        currentUserProfile()

        binding.profileBTNEdit.setOnClickListener {

            editProfile(true)
        }

        binding.profileBTNSave.setOnClickListener {
            checkSaveProfile()
        }

        binding.profileBTNSignOut.setOnClickListener {
            userSignOut()
        }

        return binding.root
    }

    private fun userSignOut() {

        AuthUI.getInstance().signOut(requireContext()).addOnCompleteListener {
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

    }

    private fun checkSaveProfile() {

        val name = binding.profileEDITName.text.toString().trim()
        val age = binding.profileEDITAge.text.toString().toIntOrNull() ?: 0



        if (name.isBlank() || age < 10 || age > 99 ) {
            binding.profileLBLError.visibility = View.VISIBLE
            Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        val updatedData = mapOf(constants.DB.nameRef to name, constants.DB.ageRef to age)

        val userRef = FirebaseDatabase.getInstance().getReference(constants.DB.usersRef).child(uid)

        userRef.updateChildren(updatedData)
            .addOnSuccessListener {
                binding.profileLBLError.visibility = View.INVISIBLE
                Toast.makeText(requireContext(), "Profile updated!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Update failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        editProfile(false)
    }

    private fun currentUserProfile() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        val database = FirebaseDatabase.getInstance()
        val userRef = database.getReference(constants.DB.usersRef).child(uid!!)

        userRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val name = snapshot.child(constants.DB.nameRef).value.toString()
                val age = snapshot.child(constants.DB.ageRef).value.toString()

                binding.profileEDITName.setText(name)
                binding.profileEDITAge.setText(age)

            }
        }.addOnFailureListener {
            Toast.makeText(requireContext(), "Failed to load profile", Toast.LENGTH_SHORT).show()
        }
        editProfile(false)
    }

    private fun editProfile(enabled: Boolean) {
        binding.profileEDITName.isEnabled = enabled
        binding.profileEDITName.isFocusableInTouchMode = enabled
        binding.profileEDITAge.isEnabled = enabled
        binding.profileEDITAge.isFocusableInTouchMode = enabled

        binding.profileBTNSave.visibility = if (enabled) {
            View.VISIBLE
        }
        else{
            View.GONE
        }

        binding.profileBTNEdit.visibility = if (enabled){
            View.GONE
        }
        else{
            View.VISIBLE
        }

        binding.profileBTNSignOut.visibility = if (enabled){
            View.GONE
        }
        else{
            View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }





}