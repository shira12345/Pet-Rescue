package com.example.petrescue

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.petrescue.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    
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
        
        val passedUsername = arguments?.getString("username")
        val passedEmail = arguments?.getString("email")
        
        binding.tvUsername.text = passedUsername ?: "---"
        binding.tvEmail.text = passedEmail ?: "---"

        viewModel.localUserLiveData.observe(viewLifecycleOwner) { user ->
            user?.let {
                binding.tvUsername.text = it.username
                binding.tvEmail.text = it.email
                binding.etPhone.setText(it.phoneNumber)
                binding.etAnimal.setText(it.animal)
            }
        }

        binding.btnSave.setOnClickListener {
            val email = binding.tvEmail.text.toString()
            val username = binding.tvUsername.text.toString()
            val phone = binding.etPhone.text.toString().trim()
            val animal = binding.etAnimal.text.toString().trim()
            
            // Call the update method in AuthViewModel (ensure it's implemented there)
            viewModel.updateProfile(email, username, phone, animal)
            Toast.makeText(requireContext(), "Profile updated!", Toast.LENGTH_SHORT).show()
        }

        binding.btnLogout.setOnClickListener {
            viewModel.logout()
            arguments?.clear()
            findNavController().navigate(R.id.action_profileFragment_to_loginFragment)
        }
        
        binding.fabEditImage.setOnClickListener {
            Toast.makeText(requireContext(), "Image upload coming soon", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}