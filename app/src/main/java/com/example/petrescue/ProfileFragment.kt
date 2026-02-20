package com.example.petrescue

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.petrescue.data.models.AuthViewModel
import com.example.petrescue.databinding.FragmentProfileBinding
import com.example.petrescue.features.posts_feed.PostsAdapter
import com.example.petrescue.model.Post
import com.example.petrescue.utilis.extensions.bitmap
import com.squareup.picasso.Picasso
import java.io.File

class ProfileFragment : Fragment() {

  private var _binding: FragmentProfileBinding? = null
  private val binding get() = _binding!!
  private val viewModel: AuthViewModel by activityViewModels()
  private var userEmail: String? = null
  private lateinit var postsAdapter: PostsAdapter

  private val imagePickerLauncher =
    registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
      uri?.let {
        binding.ivProfileImage.setImageURI(it)
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

    setupRecyclerView()
    setupObservers()
    setupListeners()
  }

  private fun setupRecyclerView() {
    postsAdapter = PostsAdapter { post ->
      val bundle = bundleOf(
        Post.PET_NAME_KEY to post.petName,
        Post.PET_TYPE_KEY to post.petType,
        Post.BREED_KEY to post.breed,
        Post.STATUS_KEY to post.status,
        Post.DESCRIPTION_KEY to post.description,
        Post.IMAGE_URI_KEY to post.imageUri,
        Post.CREATOR_EMAIL_KEY to post.creatorEmail,
        Post.CREATOR_PHONE_KEY to post.creatorPhone
      )
      // Corrected navigation action for ProfileFragment
      findNavController().navigate(R.id.action_profileFragment_to_postDetailsFragment, bundle)
    }
    binding.rvMyReports.adapter = postsAdapter
  }

  private fun setupObservers() {
    // 1. Observe profile data
    viewModel.localUserLiveData.observe(viewLifecycleOwner) { user ->
      user?.let {
        userEmail = it.email
        binding.tvUsername.text = it.username
        binding.tvEmail.text = it.email
        binding.etPhone.setText(it.phoneNumber)
        binding.etAnimal.setText(it.animal)

        if (!it.profileImage.isNullOrEmpty()) {
          Picasso.get().load(it.profileImage).placeholder(R.drawable.logo).into(binding.ivProfileImage)
        } else {
          binding.ivProfileImage.setImageResource(R.drawable.logo)
        }
      }
    }

    // 2. Observe user's specific posts
    viewModel.userPostsLiveData.observe(viewLifecycleOwner) { posts ->
      postsAdapter.submitList(posts)
      binding.tvNoReports.isVisible = posts.isEmpty()
      binding.rvMyReports.isVisible = posts.isNotEmpty()
    }

    viewModel.loadingLiveData.observe(viewLifecycleOwner) { isLoading ->
      binding.progressBar.isVisible = isLoading
      binding.btnSave.isEnabled = !isLoading
    }

    viewModel.errorLiveData.observe(viewLifecycleOwner) { error ->
      error?.let {
        Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
        viewModel.clearError()
      }
    }
  }

  private fun setupListeners() {
    binding.btnSave.setOnClickListener {
      val email = userEmail ?: binding.tvEmail.text.toString()
      val username = binding.tvUsername.text.toString()
      val phone = binding.etPhone.text.toString().trim()
      val animal = binding.etAnimal.text.toString().trim()
      val imageBitmap = binding.ivProfileImage.bitmap

      viewModel.updateProfile(email, username, phone, animal, imageBitmap)
      Toast.makeText(requireContext(), "Updating profile...", Toast.LENGTH_SHORT).show()
    }

    binding.btnLogout.setOnClickListener {
      viewModel.logout()
      findNavController().navigate(R.id.action_profileFragment_to_loginFragment)
    }

    binding.fabEditImage.setOnClickListener {
      imagePickerLauncher.launch("image/*")
    }

    binding.fabDeleteImage.setOnClickListener {
      binding.ivProfileImage.setImageResource(R.drawable.logo)
      userEmail?.let { email ->
        viewModel.deleteProfileImage(email)
      }
      Toast.makeText(requireContext(), "Image cleared. Click Save to confirm.", Toast.LENGTH_SHORT).show()
    }
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }
}
