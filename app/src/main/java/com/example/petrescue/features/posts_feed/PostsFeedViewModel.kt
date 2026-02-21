package com.example.petrescue.features.posts_feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petrescue.data.repository.posts.PostsRepository
import kotlinx.coroutines.launch

/**
 * ViewModel for the Posts Feed screen.
 * Handles loading and refreshing the list of rescue posts.
 */
class PostsFeedViewModel : ViewModel() {
  private val postsRepository = PostsRepository.shared

  /** Observable list of all posts from the local database. */
  val data = postsRepository.getAllPosts()

  /**
   * Triggers a refresh of the posts data by syncing with the remote repository.
   */
  fun refreshPosts() {
    viewModelScope.launch {
      postsRepository.refreshPosts()
    }
  }
}
