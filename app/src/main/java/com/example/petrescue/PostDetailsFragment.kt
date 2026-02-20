package com.example.petrescue

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.petrescue.databinding.FragmentPostDetailsBinding
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

        // Retrieve arguments passed from the Feed
        val petName = arguments?.getString("petName") ?: "Unknown"
        val petType = arguments?.getString("petType") ?: "Unknown"
        val breed = arguments?.getString("breed") ?: ""
        val status = arguments?.getString("status") ?: "Unknown"
        val description = arguments?.getString("description") ?: "No description provided."
        val imageUri = arguments?.getString("imageUri")
        val creatorEmail = arguments?.getString("creatorEmail") ?: ""
        val creatorPhone = arguments?.getString("creatorPhone") ?: ""

        // Display the data
        binding.tvPostTitle.text = "$status $petName"
        binding.tvPostDetailsLine.text = "$petType • $breed"
        binding.tvPostContent.text = description

        if (!imageUri.isNullOrEmpty()) {
            Picasso.get().load(imageUri).into(binding.ivPostImage)
        } else {
            binding.ivPostImage.setImageResource(R.drawable.logo)
        }

        binding.btnPostAction.setOnClickListener {
            // Show real contact info
            if (creatorPhone.isNotEmpty()) {
                binding.btnPostAction.text = "Call: $creatorPhone"
            } else if (creatorEmail.isNotEmpty()) {
                binding.btnPostAction.text = "Email: $creatorEmail"
            } else {
                binding.btnPostAction.text = "No contact info"
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}