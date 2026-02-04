package com.example.petrescue

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.petrescue.databinding.FragmentProfileBinding
import com.squareup.picasso.Picasso

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: AuthViewModel by activityViewModels()
    private var selectedImageUri: Uri? = null

    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            Picasso.get().load(it).into(binding.ivProfileImage)
        }
    }

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

        // Load data from database
        viewModel.localUserLiveData.observe(viewLifecycleOwner) { user ->
            user?.let {
                binding.tvUsername.text = it.username
                binding.tvEmail.text = it.email
                binding.etPhone.setText(it.phoneNumber)
                binding.etAnimal.setText(it.animal)
                
                // Load saved image if exists and no new image is selected
                if (selectedImageUri == null && !it.profileImage.isNullOrEmpty()) {
                    Picasso.get().load(Uri.parse(it.profileImage)).into(binding.ivProfileImage)
                }
            }
        }

        binding.btnSave.setOnClickListener {
            val email = binding.tvEmail.text.toString()
            val username = binding.tvUsername.text.toString()
            val phone = binding.etPhone.text.toString().trim()
            val animal = binding.etAnimal.text.toString().trim()
            val imagePath = selectedImageUri?.toString()
            
            viewModel.updateProfile(email, username, phone, animal, imagePath)
            Toast.makeText(requireContext(), "Profile updated!", Toast.LENGTH_SHORT).show()
        }

        binding.btnLogout.setOnClickListener {
            viewModel.logout()
            arguments?.clear()
            findNavController().navigate(R.id.action_profileFragment_to_loginFragment)
        }
        
        binding.fabEditImage.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}