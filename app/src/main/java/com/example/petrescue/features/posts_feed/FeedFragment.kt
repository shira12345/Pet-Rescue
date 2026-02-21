package com.example.petrescue.features.posts_feed

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.petrescue.databinding.FragmentFeedBinding
import com.example.petrescue.model.Post

class FeedFragment : Fragment() {

    private var _binding: FragmentFeedBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PostsFeedViewModel by viewModels()
    private lateinit var adapter: PostsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFeedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupObservers()
        setupListeners()
    }

    private fun setupRecyclerView() {
        adapter = PostsAdapter { post ->
            // Use Safe Args to navigate and pass the entire Post object
            val action = FeedFragmentDirections.actionFeedFragmentToPostDetailsFragment(post)
            findNavController().navigate(action)
        }
        binding.rvPosts.adapter = adapter
    }

    private fun setupObservers() {
        viewModel.data.observe(viewLifecycleOwner) { posts ->
            adapter.submitList(posts)
        }
    }

    private fun setupListeners() {
        binding.btnFeedAction.setOnClickListener {
            viewModel.refreshPosts()
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshPosts()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}