package com.example.petrescue

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.petrescue.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    
    // Use activityViewModels to share the same instance across fragments
    private val viewModel: AuthViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Observe changes to the user state
        viewModel.userLiveData.observe(viewLifecycleOwner) { firebaseUser ->
            if (firebaseUser != null) {
                binding.tvUserEmail.text = firebaseUser.email
                binding.tvUsername.text = firebaseUser.displayName ?: "Firebase User"
            }
        }

        viewModel.localUserLiveData.observe(viewLifecycleOwner) { localUser ->
            if (localUser != null) {
                binding.tvUserEmail.text = localUser.email
                binding.tvUsername.text = localUser.username
            }
        }

        // Handle Logout
        binding.btnLogout.setOnClickListener {
            viewModel.logout()
            findNavController().navigate(R.id.loginFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}