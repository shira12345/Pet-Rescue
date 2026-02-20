package com.example.petrescue.features.posts_feed

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.petrescue.R
import com.example.petrescue.databinding.FragmentFeedBinding
import com.example.petrescue.model.Post
import com.google.firebase.auth.FirebaseAuth

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

  private fun setupRecyclerView() {
    val layout = LinearLayoutManager(context)
    binding.rvPosts.layoutManager = layout
    binding.rvPosts.setHasFixedSize(true)

    adapter = PostsAdapter(
      onPostClick = { post -> navigateToPostDetailsFragment(post) },
      onEditClick = { post ->
        val action = FeedFragmentDirections.actionFeedFragmentToPostFormFragment(post)

        findNavController().navigate(action)
      })

    binding.swipeRefresh.setOnRefreshListener {
      binding.swipeRefresh.isRefreshing = true

      refreshData()
    }

    binding.rvPosts.adapter = adapter
  }

  private fun refreshData() {
    viewModel.refreshPosts()
  }

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
        R.id.chipDog -> "Dog"
        R.id.chipCat -> "Cat"
        R.id.chipMyPosts -> "Mine"
        else -> null
      }

      filterAndSort(binding.searchView.query.toString())
    }
  }

  private fun setupObservers() {
    binding.shimmerLayout.startShimmer()
    binding.shimmerLayout.visibility = View.VISIBLE
    binding.swipeRefresh.visibility = View.GONE
    binding.tvEmptyState.visibility = View.GONE

    viewModel.data.observe(viewLifecycleOwner) { posts ->
      fullList = posts

      binding.shimmerLayout.stopShimmer()
      binding.shimmerLayout.visibility = View.GONE

      filterAndSort(binding.searchView.query.toString())

      binding.swipeRefresh.isRefreshing = false
    }
  }

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
      "Dog" -> filteredList.filter { it.petType.equals("Dog", ignoreCase = true) }
      "Cat" -> filteredList.filter { it.petType.equals("Cat", ignoreCase = true) }
      "Mine" -> filteredList.filter { it.creatorEmail == currentUserEmail }
      else -> filteredList
    }

    filteredList =
      if (isDescending) filteredList.sortedByDescending { it.updatedAt }
      else filteredList.sortedBy { it.updatedAt }

    adapter.submitList(filteredList)

    if (filteredList.isEmpty()) {
      binding.tvEmptyState.visibility = View.VISIBLE
      binding.swipeRefresh.visibility = View.GONE
    } else {
      binding.tvEmptyState.visibility = View.GONE
      binding.swipeRefresh.visibility = View.VISIBLE
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

  private fun navigateToPostDetailsFragment(post: Post) {
    val action = FeedFragmentDirections.actionFeedFragmentToPostDetailsFragment(post)

    findNavController().navigate(action)
  }
}