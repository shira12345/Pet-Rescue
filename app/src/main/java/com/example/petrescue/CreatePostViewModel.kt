package com.example.petrescue

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.petrescue.dao.AppDatabase
import com.example.petrescue.data.networking.locationAPI.LocationIQResult
import com.example.petrescue.data.repository.cloudinary.CloudinaryRepository
import com.example.petrescue.data.repository.location.RemoteLocationRepository
import com.example.petrescue.data.repository.posts.PostsRepository
import com.example.petrescue.features.create_post.CreatePostState
import com.example.petrescue.model.Post
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

const val MIN_LOCATION_QUERY_LENGTH = 3

class CreatePostViewModel(application: Application) : AndroidViewModel(application) {

  var selectedLat: Double? = null
  var selectedLon: Double? = null

  private val _results = MutableLiveData<List<LocationIQResult>>()
  val results: LiveData<List<LocationIQResult>> = _results

  private val _createPostState = MutableLiveData<CreatePostState>()
  val createPostState: LiveData<CreatePostState> = _createPostState

  private val postsRepository = PostsRepository()
  private val cloudinaryRepository = CloudinaryRepository()
  private val locationRepository = RemoteLocationRepository()
  private val userDao = AppDatabase.getDatabase(application).userDao()
  private val prefs = application.getSharedPreferences("pet_rescue_prefs", Context.MODE_PRIVATE)

  private var searchJob: Job? = null

  fun searchLocation(query: String) {
    if (query.length < MIN_LOCATION_QUERY_LENGTH) return

    searchJob?.cancel()

    searchJob = viewModelScope.launch {
      delay(500) // debounce

      val locations = withContext(Dispatchers.IO) {
        locationRepository.getLocations(query)
      }

      _results.value = locations
    }
  }

  fun createPost(
    petName: String,
    petType: String,
    breed: String?,
    status: String,
    description: String,
    imageBitmap: Bitmap
  ) {
    val latitude = selectedLat
    val longitude = selectedLon

    if (latitude == null || longitude == null) {
      _createPostState.value =
        CreatePostState.Error("Please select a location from the suggestions list")

      return
    }

    _createPostState.value = CreatePostState.Loading

    viewModelScope.launch {
      try {
        // 1. Get current logged-in user details
        val savedEmail = prefs.getString("current_user_email", "") ?: ""
        val currentUser = withContext(Dispatchers.IO) {
            userDao.getUserByEmail(savedEmail)
        }

        // 2. Create the post with real contact info
        val post = Post(
          petName = petName,
          petType = petType,
          breed = breed,
          status = status,
          description = description,
          imageUri = null,
          creatorEmail = currentUser?.email ?: savedEmail,
          creatorPhone = currentUser?.phoneNumber ?: "",
          latitude = latitude,
          longitude = longitude
        )

        val createdPost = postsRepository.createPost(post)

        val uploadedImageUri = cloudinaryRepository.uploadPostImage(imageBitmap, createdPost.id)

        postsRepository.updatePost(createdPost.id, mapOf("imageUri" to uploadedImageUri))

        _createPostState.value = CreatePostState.Success
      } catch (exception: Exception) {
        _createPostState.value =
          CreatePostState.Error(exception.message ?: "Unknown error during creating post")
      }
    }
  }
}