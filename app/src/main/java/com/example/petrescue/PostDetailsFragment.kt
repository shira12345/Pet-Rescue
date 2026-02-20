package com.example.petrescue

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.petrescue.databinding.FragmentPostDetailsBinding
import com.example.petrescue.model.Post
import com.squareup.picasso.Picasso

class PostDetailsFragment : Fragment() {

    private var _binding: FragmentPostDetailsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPostDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val petName = arguments?.getString(Post.PET_NAME_KEY) ?: "Unknown"
        val petType = arguments?.getString(Post.PET_TYPE_KEY) ?: "Unknown"
        val breed = arguments?.getString(Post.BREED_KEY) ?: ""
        val status = arguments?.getString(Post.STATUS_KEY) ?: "Unknown"
        val description = arguments?.getString(Post.DESCRIPTION_KEY) ?: "No description provided."
        val imageUri = arguments?.getString(Post.IMAGE_URI_KEY)
        
        val creatorEmail = arguments?.getString(Post.CREATOR_EMAIL_KEY) ?: ""
        val creatorPhone = arguments?.getString(Post.CREATOR_PHONE_KEY) ?: ""

        binding.tvPostTitle.text = "$status $petName"
        binding.tvPostDetailsLine.text = "$petType • $breed"
        binding.tvPostContent.text = description

        if (!imageUri.isNullOrEmpty()) {
            Picasso.get().load(imageUri).into(binding.ivPostImage)
        } else {
            binding.ivPostImage.setImageResource(R.drawable.logo)
        }

        binding.btnPostAction.setOnClickListener {
            when {
                !creatorPhone.isNullOrBlank() -> {
                    binding.btnPostAction.text = "Call: $creatorPhone"
                }
                !creatorEmail.isNullOrBlank() -> {
                    binding.btnPostAction.text = "Email: $creatorEmail"
                }
                else -> {
                    binding.btnPostAction.text = "Contact creator via app"
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}