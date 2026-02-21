package com.example.petrescue.features.post_details

import android.os.Bundle
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import com.example.petrescue.R
import com.example.petrescue.databinding.FragmentPostDetailsBinding
import com.example.petrescue.model.Post
import com.squareup.picasso.Picasso
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

class PostDetailsFragment : Fragment() {
  private var _binding: FragmentPostDetailsBinding? = null
  private val binding get() = _binding!!

  private val args: PostDetailsFragmentArgs by navArgs()

  private val viewModel: PostDetailsViewModel by viewModels()

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

    lifecycleScope.launch { bindPostData(post) }
  }

  private suspend fun bindPostData(post: Post) {
    binding.tvPostTitle.text = "${post.status}: ${post.petName}"
    binding.tvPostDetailsLine.text = "${post.petType} • ${post.breed ?: "Unknown Breed"}"
    binding.tvPostContent.text = post.description

    binding.tvLocation.text = viewModel.getAddressFromPostLocation(post.latitude, post.longitude)

    val createdAt = formatTimestamp(post.createdAt)
    val updatedAt = formatTimestamp(post.updatedAt)
    binding.tvTimestamps.text = "Posted on: $createdAt\nLast updated: $updatedAt"

    if (!post.imageUri.isNullOrEmpty())
      Picasso.get()
        .load(post.imageUri)
        .placeholder(R.drawable.logo)
        .into(binding.ivPostImage)
    else
      binding.ivPostImage.setImageResource(R.drawable.logo)


    binding.btnPostAction.setOnClickListener {
      binding.btnPostAction.text =
        if (post.creatorPhone.isNotBlank()) "Call: ${post.creatorPhone}"
        else if (post.creatorEmail.isNotBlank()) "Email: ${post.creatorEmail}"
        else "No contact info"
    }
  }

  private fun formatTimestamp(timestamp: Long): String {
    val deviceLocale = Locale.getDefault()
    val calendar = Calendar.getInstance(deviceLocale)
    calendar.timeInMillis = timestamp

    val formattedTimestamp = DateFormat.format(
      DateFormat.getBestDateTimePattern(deviceLocale, "ddMMyyyyHHmm"),
      calendar
    ).toString()

    return formattedTimestamp
  }

  override fun onDestroyView() {
    super.onDestroyView()

    _binding = null
  }
}