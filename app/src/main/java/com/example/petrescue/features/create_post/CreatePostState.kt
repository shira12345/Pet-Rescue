package com.example.petrescue.features.create_post

sealed class CreatePostState {
  object Idle : CreatePostState()
  object Loading : CreatePostState()
  object Success : CreatePostState()
  data class Error(val message: String) : CreatePostState()
}