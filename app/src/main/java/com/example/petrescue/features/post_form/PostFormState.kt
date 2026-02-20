package com.example.petrescue.features.post_form

sealed class PostFormState {
  object Idle : PostFormState()
  object Loading : PostFormState()
  object Success : PostFormState()
  data class Error(val message: String) : PostFormState()
}