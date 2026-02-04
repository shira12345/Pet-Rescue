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
import java.io.File
import java.io.FileOutputStream

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: AuthViewModel by activityViewModels()
    private var internalImageUri: Uri? = null

    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val savedFile = saveImageToInternalStorage(it)
            if (savedFile != null) {
                internalImageUri = Uri.fromFile(savedFile)
                Picasso.get().load(savedFile).into(binding.ivProfileImage)
            }
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
        
        val passedEmail = arguments?.getString("email") ?: ""
        
        // Load the latest data from SQLite
        viewModel.localUserLiveData.observe(viewLifecycleOwner) { user ->
            user?.let {
                binding.tvUsername.text = it.username
                binding.tvEmail.text = it.email
                binding.etPhone.setText(it.phoneNumber)
                binding.etAnimal.setText(it.animal)
                
                // 3. Load the permanent path from SQLite
                if (internalImageUri == null && !it.profileImage.isNullOrEmpty()) {
                    val file = File(it.profileImage!!)
                    if (file.exists()) {
                        Picasso.get().load(file).into(binding.ivProfileImage)
                    }
                }
            }
        }

        binding.btnSave.setOnClickListener {
            val email = binding.tvEmail.text.toString()
            val username = binding.tvUsername.text.toString()
            val phone = binding.etPhone.text.toString().trim()
            val animal = binding.etAnimal.text.toString().trim()
            
            // 4. Save the permanent file path into Rooms
            val imagePath = internalImageUri?.path
            
            viewModel.updateProfile(email, username, phone, animal, imagePath)
            Toast.makeText(requireContext(), "Profile Saved to SQLite!", Toast.LENGTH_SHORT).show()
        }

        binding.btnLogout.setOnClickListener {
            viewModel.logout()
            findNavController().navigate(R.id.action_profileFragment_to_loginFragment)
        }
        
        binding.fabEditImage.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }
    }

    // This helper ensures the image is stored inside the app's folder forever
    private fun saveImageToInternalStorage(uri: Uri): File? {
        return try {
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val file = File(requireContext().filesDir, "profile_image.jpg")
            val outputStream = FileOutputStream(file)
            inputStream?.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            file
        } catch (e: Exception) {
            null
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}