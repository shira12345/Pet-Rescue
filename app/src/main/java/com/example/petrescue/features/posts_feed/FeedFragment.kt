package com.example.petrescue.features.posts_feed

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.petrescue.R
import com.example.petrescue.data.repository.posts.PostsRepository
import com.example.petrescue.databinding.FragmentFeedBinding
import com.example.petrescue.model.Post
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

/**
 * Fragment that displays a feed of pet rescue posts.
 * Provides functionality for searching, filtering by type, and sorting posts.
 */
class FeedFragment : Fragment() {
  private var _binding: FragmentFeedBinding? = null
  private val binding get() = _binding!!

  private val viewModel: PostsFeedViewModel by viewModels()
  private lateinit var adapter: PostsAdapter

  private var fullList: List<Post> = emptyList()
  private var isDescending = true
  private var selectedTypeFilter: String? = null

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
    setupSearchAndSort()
    setupObservers()
  }

  /**
   * Initializes the RecyclerView with its layout manager and custom adapter.
   */
  private fun setupRecyclerView() {
    val layout = LinearLayoutManager(context)
    binding.rvPosts.layoutManager = layout
    binding.rvPosts.setHasFixedSize(true)
    val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email

    adapter = PostsAdapter(
      currentUserEmail = currentUserEmail,
      onPostClick = { post -> navigateToPostDetailsFragment(post) },
      onEditClick = { post ->
        val action = FeedFragmentDirections.actionFeedFragmentToPostFormFragment(post)
        findNavController().navigate(action)
      },
      onDeleteClick = { post ->
        deletePost(post)
      }
    )

    binding.swipeRefresh.setOnRefreshListener {
      refreshData()
    }

    binding.rvPosts.adapter = adapter
  }

  private fun deletePost(post: Post) {
    viewLifecycleOwner.lifecycleScope.launch {
      PostsRepository.shared.deletePost(post)
      Toast.makeText(requireContext(), "Post deleted", Toast.LENGTH_SHORT).show()
    }
  }

  /**
   * Triggers a data refresh in the ViewModel.
   */
  private fun refreshData() {
    viewModel.refreshPosts()
  }

  /**
   * Sets up UI listeners for search queries, sort button, and filter chips.
   */
  private fun setupSearchAndSort() {
    binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
      override fun onQueryTextSubmit(query: String?): Boolean = false
      override fun onQueryTextChange(newText: String?): Boolean {
        filterAndSort(newText)

        return true
      }
    })

    binding.btnSort.setOnClickListener {
      isDescending = !isDescending

      binding.btnSort.rotation = if (isDescending) 0f else 180f

      filterAndSort(binding.searchView.query.toString())
    }

    binding.chipGroupFilters.setOnCheckedChangeListener { group, checkedId ->
      selectedTypeFilter = when (checkedId) {
        R.id.chipDog -> Post.TYPE_DOG
        R.id.chipCat -> Post.TYPE_CAT
        R.id.chipMyPosts -> "Mine"
        else -> null
      }

      filterAndSort(binding.searchView.query.toString())
    }
  }

  /**
   * Observes the ViewModel's data and updates the UI accordingly.
   */
  private fun setupObservers() {
    binding.shimmerLayout.startShimmer()
    binding.shimmerLayout.visibility = View.VISIBLE
    binding.swipeRefresh.visibility = View.GONE
    binding.tvEmptyState.visibility = View.GONE

    viewModel.data.observe(viewLifecycleOwner) { posts ->
      fullList = posts

      binding.shimmerLayout.stopShimmer()
      binding.shimmerLayout.visibility = View.GONE
      binding.swipeRefresh.visibility = View.VISIBLE

      filterAndSort(binding.searchView.query.toString())

      binding.swipeRefresh.isRefreshing = false
    }
  }

  /**
   * Filters the full list of posts based on search query and selected filters,
   * then sorts the result before updating the adapter.
   *
   * @param query The search string to filter by name or description.
   */
  private fun filterAndSort(query: String?) {
    val searchText = query?.lowercase() ?: ""
    val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email

    var filteredList = fullList

    if (searchText.isNotEmpty())
      filteredList = filteredList.filter {
        it.petName.lowercase().contains(searchText) ||
            it.description.lowercase().contains(searchText)
      }

    filteredList = when (selectedTypeFilter) {
      Post.TYPE_DOG -> filteredList.filter { it.petType.equals(Post.TYPE_DOG, ignoreCase = true) }
      Post.TYPE_CAT -> filteredList.filter { it.petType.equals(Post.TYPE_CAT, ignoreCase = true) }
      "Mine" -> filteredList.filter { it.creatorEmail == currentUserEmail }
      else -> filteredList
    }

    filteredList =
      if (isDescending) filteredList.sortedByDescending { it.updatedAt }
      else filteredList.sortedBy { it.updatedAt }

    adapter.submitList(filteredList)

    if (filteredList.isEmpty()) {
      binding.tvEmptyState.visibility = View.VISIBLE
    } else {
      binding.tvEmptyState.visibility = View.GONE
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

  /**
   * Navigates to the Post Details screen.
   *
   * @param post The post object to display.
   */
  private fun navigateToPostDetailsFragment(post: Post) {
    val action = FeedFragmentDirections.actionFeedFragmentToPostDetailsFragment(post)

    findNavController().navigate(action)
  }
}
