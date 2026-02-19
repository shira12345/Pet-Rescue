package com.example.petrescue.features.posts_feed

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.petrescue.databinding.FragmentFeedBinding

class FeedFragment : Fragment() {

  private var binding: FragmentFeedBinding? = null

  private val viewModel: PostsFeedViewModel by viewModels()

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    binding = FragmentFeedBinding.inflate(inflater, container, false)

    return binding?.root
  }

  override fun onResume() {
    super.onResume()

    refreshData()
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    binding?.btnFeedAction?.setOnClickListener {
      refreshData()
    }

    viewModel.data.observe(viewLifecycleOwner) { posts ->
      println("Posts updated (${posts.size}): $posts")
    }
  }

  private fun refreshData() {
    viewModel.refreshPosts()
  }

  override fun onDestroyView() {
    super.onDestroyView()

    binding = null
  }
}