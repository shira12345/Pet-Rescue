package com.example.petrescue.features.post_form

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petrescue.base.MyApplication
import com.example.petrescue.dao.AppDatabase
import com.example.petrescue.data.networking.locationAPI.LocationIQResult
import com.example.petrescue.data.repository.cloudinary.CloudinaryRepository
import com.example.petrescue.data.repository.location.RemoteLocationRepository
import com.example.petrescue.data.repository.posts.PostsRepository
import com.example.petrescue.model.Post
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

const val MIN_LOCATION_QUERY_LENGTH = 3

class PostFormViewModel : ViewModel() {

  var selectedLat: Double? = null
  var selectedLon: Double? = null

  private val _results = MutableLiveData<List<LocationIQResult>>()
  val results: LiveData<List<LocationIQResult>> = _results

  private val _postFormState = MutableLiveData<PostFormState>(PostFormState.Idle)
  val postFormState: LiveData<PostFormState> = _postFormState

  private val postsRepository = PostsRepository.shared
  private val cloudinaryRepository = CloudinaryRepository.shared
  private val locationRepository = RemoteLocationRepository.shared
  private val userDao =
    AppDatabase.getDatabase(MyApplication.appContext ?: throw Exception("App context is null"))
      .userDao()
  private val prefs =
    MyApplication.appContext?.getSharedPreferences("pet_rescue_prefs", Context.MODE_PRIVATE)

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

  suspend fun getAddressFromPostLocation(lat: Double, lon: Double): String {
    val address = locationRepository.getAddressFromLatLon(lat, lon)

    return address
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
      _postFormState.value =
        PostFormState.Error("Please select a location from the suggestions list")

      return
    }

    _postFormState.value = PostFormState.Loading

    viewModelScope.launch {
      try {
        val savedEmail = prefs?.getString("current_user_email", "") ?: ""
        val currentUser = withContext(Dispatchers.IO) {
          userDao.getUserByEmail(savedEmail)
        }
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

        _postFormState.value = PostFormState.Success
      } catch (exception: Exception) {
        _postFormState.value =
          PostFormState.Error(exception.message ?: "Unknown error during creating post")
      }
    }
  }

  fun updatePost(
    postId: String,
    petName: String,
    petType: String,
    breed: String?,
    status: String,
    description: String,
    imageBitmap: Bitmap?,
    existingImageUrl: String?
  ) {
    val latitude = selectedLat
    val longitude = selectedLon

    if (latitude == null || longitude == null) {
      _postFormState.value =
        PostFormState.Error("Please select a location from the suggestions list")

      return
    }

    _postFormState.value = PostFormState.Loading

    viewModelScope.launch {
      try {

        // Decide which image to use
        val finalImageUrl = if (imageBitmap != null) cloudinaryRepository.uploadPostImage(
          imageBitmap,
          postId
        ) else existingImageUrl

        val updates = mutableMapOf<String, Any>(
          "petName" to petName,
          "petType" to petType,
          "status" to status,
          "description" to description,
          "latitude" to latitude,
          "longitude" to longitude
        )

        breed?.let { updates["breed"] = it }
        finalImageUrl?.let { updates["imageUri"] = it }

        postsRepository.updatePost(postId, updates)

        _postFormState.value = PostFormState.Success
      } catch (exception: Exception) {
        _postFormState.value =
          PostFormState.Error(exception.message ?: "Unknown error during updating post")
      }
    }
  }
}
