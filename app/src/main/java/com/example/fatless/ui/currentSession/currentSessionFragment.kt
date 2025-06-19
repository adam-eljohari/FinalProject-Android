package com.example.fatless.ui.currentSession

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.fatless.databinding.FragmentCurrentsessionBinding
import com.example.fatless.ui.favorite.favoriteViewModel

class currentSessionFragment : Fragment()  {

    private var _binding: FragmentCurrentsessionBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        val currentSessionViewModel = ViewModelProvider(this).get(favoriteViewModel::class.java)

        _binding = FragmentCurrentsessionBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textCurrentsession
        currentSessionViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}