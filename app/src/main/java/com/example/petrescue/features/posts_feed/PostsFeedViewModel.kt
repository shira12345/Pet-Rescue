package com.example.petrescue.features.posts_feed

import androidx.lifecycle.ViewModel
import com.example.petrescue.data.repository.posts.PostsRepository

class PostsFeedViewModel : ViewModel() {
  private val postsRepository = PostsRepository()

  val data = postsRepository.getAllPosts()

  fun refreshPosts() {
    postsRepository.refreshPosts()
  }
}