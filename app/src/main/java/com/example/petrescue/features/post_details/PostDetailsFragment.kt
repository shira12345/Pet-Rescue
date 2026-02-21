package com.example.petrescue.features.post_details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import com.example.petrescue.R
import com.example.petrescue.databinding.FragmentPostDetailsBinding
import com.squareup.picasso.Picasso

class PostDetailsFragment : Fragment() {

  private var _binding: FragmentPostDetailsBinding? = null
  private val binding get() = _binding!!

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

    val post = args.post ?: run {
      view.findNavController().popBackStack()

      throw Exception("Post not found")
    }

    binding.tvPostTitle.text = "${post.status} ${post.petName}"
    binding.tvPostDetailsLine.text = "${post.petType} • ${post.breed}"
    binding.tvPostContent.text = post.description

    if (!post.imageUri.isNullOrEmpty()) {
      Picasso.get().load(post.imageUri).into(binding.ivPostImage)
    } else {
      binding.ivPostImage.setImageResource(R.drawable.logo)
    }

    binding.btnPostAction.setOnClickListener {
      when {
        post.creatorPhone.isNotBlank() -> {
          binding.btnPostAction.setText("Call: ${post.creatorPhone}")
        }

        post.creatorEmail.isNotBlank() -> {
          binding.btnPostAction.setText("Email: ${post.creatorEmail}")
        }

        else -> {
          binding.btnPostAction.setText("Contact creator via app")
        }
      }
    }
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }
}