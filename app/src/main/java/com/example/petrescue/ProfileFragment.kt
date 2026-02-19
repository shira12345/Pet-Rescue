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
import com.example.petrescue.data.models.AuthViewModel
import com.example.petrescue.databinding.FragmentProfileBinding
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.Picasso
import java.io.File
import java.io.FileOutputStream

class ProfileFragment : Fragment() {

  private var _binding: FragmentProfileBinding? = null
  private val binding get() = _binding!!
  private val viewModel: AuthViewModel by activityViewModels()
  private var internalImageUri: Uri? = null
  private var userEmail: String? = null

  private val imagePickerLauncher =
    registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
      uri?.let {
        val emailToUse = userEmail ?: arguments?.getString("email") ?: ""
        if (emailToUse.isNotEmpty()) {
          val savedFile = saveImageToInternalStorage(it, emailToUse)
          if (savedFile != null) {
            internalImageUri = Uri.fromFile(savedFile)
            // Invalidate Picasso cache for this file to ensure the NEW image shows
            Picasso.get().invalidate(savedFile)
            Picasso.get()
              .load(savedFile)
              .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
              .into(binding.ivProfileImage)
          }
        } else {
          Toast.makeText(requireContext(), "Error: User not identified", Toast.LENGTH_SHORT).show()
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

    viewModel.localUserLiveData.observe(viewLifecycleOwner) { user ->
      user?.let {
        userEmail = it.email
        binding.tvUsername.text = it.username
        binding.tvEmail.text = it.email
        binding.etPhone.setText(it.phoneNumber)
        binding.etAnimal.setText(it.animal)

        if (internalImageUri == null && !it.profileImage.isNullOrEmpty()) {
          val file = File(it.profileImage!!)
          if (file.exists()) {
            Picasso.get().load(file).into(binding.ivProfileImage)
          } else {
            binding.ivProfileImage.setImageResource(R.drawable.logo)
          }
        }
      }
    }

    binding.btnSave.setOnClickListener {
      val email = userEmail ?: binding.tvEmail.text.toString()
      val username = binding.tvUsername.text.toString()
      val phone = binding.etPhone.text.toString().trim()
      val animal = binding.etAnimal.text.toString().trim()
      val imagePath = internalImageUri?.path

      viewModel.updateProfile(email, username, phone, animal, imagePath)
      Toast.makeText(requireContext(), "Profile Saved!", Toast.LENGTH_SHORT).show()
    }

    binding.btnLogout.setOnClickListener {
      viewModel.logout()
      internalImageUri = null
      findNavController().navigate(R.id.action_profileFragment_to_loginFragment)
    }

    binding.fabEditImage.setOnClickListener {
      imagePickerLauncher.launch("image/*")
    }

    binding.fabDeleteImage.setOnClickListener {
      internalImageUri = null
      binding.ivProfileImage.setImageResource(R.drawable.logo)

      val emailToDeleteFor = userEmail ?: arguments?.getString("email")
      emailToDeleteFor?.let { email ->
        val fileName = "profile_${email.hashCode()}.jpg"
        val file = File(requireContext().filesDir, fileName)
        if (file.exists()) {
          Picasso.get().invalidate(file)
          file.delete()
        }
      }

      Toast.makeText(requireContext(), "Click Save to confirm deletion", Toast.LENGTH_SHORT).show()
    }
  }

  private fun saveImageToInternalStorage(uri: Uri, email: String): File? {
    return try {
      val inputStream = requireContext().contentResolver.openInputStream(uri)
      val fileName = "profile_${email.hashCode()}.jpg"
      val file = File(requireContext().filesDir, fileName)
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