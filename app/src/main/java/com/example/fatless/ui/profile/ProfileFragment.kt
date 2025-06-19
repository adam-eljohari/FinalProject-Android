package com.example.fatless.ui.profile


import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.fatless.LoginActivity
import com.example.fatless.dataModels.Profile
import com.example.fatless.databinding.FragmentProfileBinding
import com.firebase.ui.auth.AuthUI


class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private var currentProfile: Profile? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
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

        val name = binding.profileEDITName.text.toString()
        val age = binding.profileEDITAge.text.toString().toIntOrNull() ?: 0

        val saveUpdatedProfileData = mutableMapOf<String, Any>()

        currentProfile?.let { profile ->
            if (profile.name != name) {
                saveUpdatedProfileData["name"] = name
            }
            if (profile.age != age){
                saveUpdatedProfileData["age"] = age
            }
        }

        if (saveUpdatedProfileData.isNotEmpty()){

        }

        editProfile(false)
    }

    private fun currentUserProfile() {



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