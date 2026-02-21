package com.example.petrescue

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.example.petrescue.databinding.FragmentPostDetailsBinding
import com.squareup.picasso.Picasso

class PostDetailsFragment : Fragment() {

    private var _binding: FragmentPostDetailsBinding? = null
    private val binding get() = _binding!!
    
    // Use Safe Args to retrieve the post object
    private val args: PostDetailsFragmentArgs by navArgs()

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

        val post = args.post

        // Display the dynamic data from the post object
        binding.tvPostTitle.text = "${post.status} ${post.petName}"
        binding.tvPostDetailsLine.text = "${post.petType} • ${post.breed ?: "Unknown"}"
        binding.tvPostContent.text = post.description

        if (!post.imageUri.isNullOrEmpty()) {
            Picasso.get().load(post.imageUri).placeholder(R.drawable.logo).into(binding.ivPostImage)
        } else {
            binding.ivPostImage.setImageResource(R.drawable.logo)
        }

        binding.btnPostAction.setOnClickListener {
            when {
                !post.creatorPhone.isNullOrBlank() -> {
                    binding.btnPostAction.text = "Call: ${post.creatorPhone}"
                }
                !post.creatorEmail.isNullOrBlank() -> {
                    binding.btnPostAction.text = "Email: ${post.creatorEmail}"
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