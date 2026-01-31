package com.example.petrescue

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.petrescue.databinding.FragmentLoginBinding

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: AuthViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupObservers()
        
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            
            if (email.isNotEmpty() && password.isNotEmpty()) {
                viewModel.login(email, password)
            } else {
                Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }
        
        binding.tvRegister.setOnClickListener {
            viewModel.clearError()
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }
    }
    
    private fun setupObservers() {
        viewModel.userLiveData.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                navigateToProfile(user.displayName ?: "User", user.email ?: "")
            }
        }

        viewModel.localUserLiveData.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                navigateToProfile(user.username, user.email)
            }
        }
        
        viewModel.errorLiveData.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                viewModel.clearError()
            }
        }
        
        viewModel.loadingLiveData.observe(viewLifecycleOwner) { isLoading ->
            binding.btnLogin.isEnabled = !isLoading
        }
    }

    private fun navigateToProfile(username: String, email: String) {
        if (findNavController().currentDestination?.id == R.id.loginFragment) {
            val bundle = bundleOf(
                "username" to username,
                "email" to email
            )
            findNavController().navigate(R.id.action_loginFragment_to_profileFragment, bundle)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}