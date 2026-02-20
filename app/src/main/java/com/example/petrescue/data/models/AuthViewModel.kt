package com.example.petrescue.data.models

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.petrescue.dao.AppDatabase
import com.example.petrescue.data.repository.cloudinary.CloudinaryRepository
import com.example.petrescue.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {

  private val auth = try {
    FirebaseAuth.getInstance()
  } catch (e: Exception) {
    Log.e("Error", "Firebase not initialized.", e)
    null
  }

  private val userDao = AppDatabase.Companion.getDatabase(application).userDao()
  private val prefs = application.getSharedPreferences("pet_rescue_prefs", Context.MODE_PRIVATE)
  private val cloudinaryRepository = CloudinaryRepository()

  private val _userLiveData = MutableLiveData<FirebaseUser?>()
  val userLiveData: LiveData<FirebaseUser?> = _userLiveData

  private val _localUserLiveData = MutableLiveData<User?>()
  val localUserLiveData: LiveData<User?> = _localUserLiveData

  private val _errorLiveData = MutableLiveData<String?>()
  val errorLiveData: LiveData<String?> = _errorLiveData

  private val _loadingLiveData = MutableLiveData<Boolean>()
  val loadingLiveData: LiveData<Boolean> = _loadingLiveData

  init {
    val firebaseUser = auth?.currentUser
    val savedEmail = firebaseUser?.email ?: prefs.getString("current_user_email", null)
    
    if (firebaseUser != null) {
        _userLiveData.value = firebaseUser
    }
    
    if (savedEmail != null) {
        loadUser(savedEmail)
    }
  }

  fun loadUser(email: String) {
    viewModelScope.launch {
      val user = userDao.getUserByEmail(email)
      _localUserLiveData.postValue(user)
    }
  }

  fun clearError() {
    _errorLiveData.value = null
  }

  fun login(email: String, pass: String) {
    clearError()
    _loadingLiveData.value = true

    viewModelScope.launch {
      val localUser = userDao.getUserByEmail(email)

      if (auth != null && auth.app.options.apiKey != "API_KEY") {
        auth.signInWithEmailAndPassword(email, pass)
          .addOnCompleteListener { task ->
            if (task.isSuccessful) {
              saveSession(email)
              _userLiveData.postValue(auth.currentUser)
              // Ensure we have a local user record even if logged in via Firebase
              viewModelScope.launch {
                  if (userDao.getUserByEmail(email) == null) {
                      userDao.insertUser(User(email, email.substringBefore("@"), pass))
                  }
                  loadUser(email)
                  _loadingLiveData.postValue(false)
              }
            } else if (localUser != null && localUser.password == pass) {
              saveSession(email)
              _localUserLiveData.postValue(localUser)
              _loadingLiveData.postValue(false)
            } else {
              _loadingLiveData.postValue(false)
              _errorLiveData.postValue("Invalid email or password")
            }
          }
      } else {
        _loadingLiveData.postValue(false)
        if (localUser != null && localUser.password == pass) {
          saveSession(email)
          _localUserLiveData.postValue(localUser)
        } else {
          _errorLiveData.postValue("Invalid email or password")
        }
      }
    }
  }

  fun signUp(username: String, email: String, pass: String) {
    clearError()
    _loadingLiveData.value = true

    viewModelScope.launch {
      val existingUser = userDao.getUserByEmail(email)
      if (existingUser != null) {
        _loadingLiveData.postValue(false)
        _errorLiveData.postValue("An account with this email already exists.")
        return@launch
      }

      val newUser = User(email, username, pass)

      if (auth != null && auth.app.options.apiKey != "API_KEY") {
        auth.createUserWithEmailAndPassword(email, pass)
          .addOnCompleteListener { task ->
            if (task.isSuccessful) {
              viewModelScope.launch {
                userDao.insertUser(newUser)
                saveSession(email)
                _loadingLiveData.postValue(false)
                _userLiveData.postValue(auth.currentUser)
                _localUserLiveData.postValue(newUser)
              }
            } else {
              _loadingLiveData.postValue(false)
              _errorLiveData.postValue(task.exception?.message ?: "Registration failed")
            }
          }
      } else {
        userDao.insertUser(newUser)
        saveSession(email)
        _loadingLiveData.postValue(false)
        _localUserLiveData.postValue(newUser)
      }
    }
  }

  fun updateProfile(
    email: String,
    username: String,
    phone: String,
    animal: String,
    imageBitmap: Bitmap?
  ) {
    _loadingLiveData.value = true
    viewModelScope.launch {
      try {
        var currentUser = userDao.getUserByEmail(email)
        
        // Fallback: If user is logged in but record is missing in Room, create a stub
        if (currentUser == null) {
            currentUser = User(email, username, "SOCIAL_LOGIN")
        }

        var profileImageUrl = currentUser.profileImage
        
        if (imageBitmap != null) {
          profileImageUrl = cloudinaryRepository.uploadProfileImage(imageBitmap, email)
        }

        val updatedUser = currentUser.copy(
          username = username,
          phoneNumber = phone,
          animal = animal,
          profileImage = profileImageUrl
        )
        userDao.insertUser(updatedUser)
        _localUserLiveData.postValue(updatedUser)
        _loadingLiveData.postValue(false)
      } catch (e: Exception) {
        _errorLiveData.postValue(e.message ?: "Failed to update profile")
        _loadingLiveData.postValue(false)
      }
    }
  }

  fun deleteProfileImage(email: String) {
    viewModelScope.launch {
      val currentUser = userDao.getUserByEmail(email)
      if (currentUser != null) {
        val updatedUser = currentUser.copy(profileImage = null)
        userDao.insertUser(updatedUser)
        _localUserLiveData.postValue(updatedUser)
      }
    }
  }

  private fun saveSession(email: String) {
    prefs.edit().putString("current_user_email", email).apply()
  }

  private fun clearSession() {
    prefs.edit().remove("current_user_email").apply()
  }

  fun logout() {
    auth?.signOut()
    clearSession()
    _userLiveData.value = null
    _localUserLiveData.value = null
    clearError()
  }
}
