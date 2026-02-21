package com.example.petrescue.data.models

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.example.petrescue.dao.AppDatabase
import com.example.petrescue.data.repository.UserRepository
import com.example.petrescue.model.Post
import com.example.petrescue.model.User
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch

/**
 * ViewModel responsible for managing authentication state and user profile data.
 * It interacts with the [UserRepository] to perform login, signup, and profile updates.
 */
class AuthViewModel(application: Application) : AndroidViewModel(application) {

  private val userRepository = UserRepository(application)
  private val postDao = AppDatabase.getDatabase(application).postDao()

  private val _userLiveData = MutableLiveData<FirebaseUser?>()
  /** Observable Firebase user state. */
  val userLiveData: LiveData<FirebaseUser?> = _userLiveData

  private val _currentUserEmail = MutableLiveData<String?>()

  /** Observable local user profile data, automatically updated based on the current user's email. */
  val localUserLiveData: LiveData<User?> = _currentUserEmail.switchMap { email ->
    if (email != null) userRepository.getLocalUser(email)
    else MutableLiveData(null)
  }

  /** Observable list of posts created by the current user. */
  val userPostsLiveData: LiveData<MutableList<Post>> = _currentUserEmail.switchMap { email ->
    if (email != null) postDao.getPostsByEmail(email)
    else MutableLiveData(mutableListOf())
  }

  private val _errorLiveData = MutableLiveData<String?>()
  /** Observable error messages for UI display. */
  val errorLiveData: LiveData<String?> = _errorLiveData

  private val _loadingLiveData = MutableLiveData<Boolean>()
  /** Observable loading state for UI progress indicators. */
  val loadingLiveData: LiveData<Boolean> = _loadingLiveData

  init {
    val firebaseUser = userRepository.currentUser
    val savedEmail = firebaseUser?.email ?: userRepository.getSavedEmail()

    if (firebaseUser != null) _userLiveData.value = firebaseUser

    if (savedEmail != null) {
      _currentUserEmail.value = savedEmail
    }
  }

  /**
   * Resets the current error state.
   */
  fun clearError() {
    _errorLiveData.value = null
  }

  /**
   * Attempts to log in a user with the provided credentials.
   *
   * @param email The user's email address.
   * @param pass The user's password.
   */
  fun login(email: String, pass: String) {
    clearError()
    _loadingLiveData.value = true

    viewModelScope.launch {
      val result = userRepository.login(email, pass)

      result.onSuccess { firebaseUser ->
        _userLiveData.postValue(firebaseUser)
        _currentUserEmail.postValue(email)
      }.onFailure {
        _errorLiveData.postValue(it.message ?: "Invalid email or password")
      }

      _loadingLiveData.postValue(false)
    }
  }

  /**
   * Attempts to register a new user account.
   *
   * @param username The display name for the new user.
   * @param email The user's email address.
   * @param pass The user's password.
   */
  fun signUp(username: String, email: String, pass: String) {
    clearError()
    _loadingLiveData.value = true

    viewModelScope.launch {
      val result = userRepository.signUp(username, email, pass)

      result.onSuccess { user ->
        _currentUserEmail.postValue(email)
        _userLiveData.postValue(userRepository.currentUser)
      }.onFailure {
        _errorLiveData.postValue(it.message ?: "Registration failed")
      }

      _loadingLiveData.postValue(false)
    }
  }

  /**
   * Updates the current user's profile details.
   *
   * @param email The user's email (used as identifier).
   * @param username The updated username.
   * @param phone The updated phone number.
   * @param animal The updated animal preference/type.
   * @param imageBitmap Optional new profile image bitmap.
   */
  fun updateProfile(
    email: String,
    username: String,
    phone: String,
    animal: String,
    imageBitmap: Bitmap?
  ) {
    _loadingLiveData.value = true

    viewModelScope.launch {
      val result = userRepository.updateProfile(email, username, phone, animal, imageBitmap)

      result.onFailure { _errorLiveData.postValue(it.message ?: "Failed to update profile") }

      _loadingLiveData.postValue(false)
    }
  }

  /**
   * Deletes the current user's profile image.
   *
   * @param email The email address of the user.
   */
  fun deleteProfileImage(email: String) {
    viewModelScope.launch {
      userRepository.deleteProfileImage(email)
    }
  }

  /**
   * Performs logout by clearing both remote and local session states.
   */
  fun logout() {
    userRepository.logout()
    
    _userLiveData.value = null
    _currentUserEmail.value = null

    clearError()
  }
}
