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
import com.example.petrescue.data.repository.UserRepository
import com.example.petrescue.data.repository.cloudinary.CloudinaryRepository
import com.example.petrescue.data.repository.location.RemoteLocationRepository
import com.example.petrescue.data.repository.posts.PostsRepository
import com.example.petrescue.model.Post
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Minimum query length to trigger a location search. */
const val MIN_LOCATION_QUERY_LENGTH = 3

/**
 * ViewModel for the Post Form screen.
 * Manages the state and logic for creating and editing rescue posts,
 * including location searching and image uploading.
 */
class PostFormViewModel : ViewModel() {

  /** The currently selected latitude for the post. */
  var selectedLat: Double? = null

  /** The currently selected longitude for the post. */
  var selectedLon: Double? = null

  private val _results = MutableLiveData<List<LocationIQResult>>()

  /** Observable list of location search results. */
  val results: LiveData<List<LocationIQResult>> = _results

  private val _postFormState = MutableLiveData<PostFormState>(PostFormState.Idle)

  /** Observable state of the post submission process (Idle, Loading, Success, Error). */
  val postFormState: LiveData<PostFormState> = _postFormState

  private val postsRepository = PostsRepository.shared
  private val cloudinaryRepository = CloudinaryRepository.shared
  private val locationRepository = RemoteLocationRepository.shared
  private val userDao =
    AppDatabase.getDatabase(MyApplication.appContext ?: throw Exception("App context is null"))
      .userDao()
  private val prefs =
    MyApplication.appContext?.getSharedPreferences(UserRepository.PREFS_NAME, Context.MODE_PRIVATE)

  private var searchJob: Job? = null

  /**
   * Searches for locations matching the given query with a debounce delay.
   *
   * @param query The search string entered by the user.
   */
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

  /**
   * Translates geographic coordinates into a human-readable address.
   *
   * @param lat Latitude to lookup.
   * @param lon Longitude to lookup.
   * @return Display name of the location.
   */
  suspend fun getAddressFromPostLocation(lat: Double, lon: Double): String {
    val address = locationRepository.getAddressFromLatLon(lat, lon)

    return address
  }

  /**
   * Creates a new rescue post.
   * Performs image upload to Cloudinary and saves the post to remote and local repositories.
   *
   * @param petName Name of the pet.
   * @param petType Type of animal (e.g., Dog, Cat).
   * @param breed Optional breed information.
   * @param status Current status (e.g., Lost, Found).
   * @param description Additional details about the pet or situation.
   * @param imageBitmap Bitmap of the pet image to be uploaded.
   */
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
        val savedEmail = prefs?.getString(UserRepository.PREF_CURRENT_USER_EMAIL, "") ?: ""
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

  /**
   * Updates an existing rescue post.
   *
   * @param postId ID of the post to update.
   * @param petName Updated name.
   * @param petType Updated type.
   * @param breed Updated breed.
   * @param status Updated status.
   * @param description Updated description.
   * @param imageBitmap Optional new image bitmap; if null, the [existingImageUrl] is kept.
   * @param existingImageUrl URL of the current image if no new image is provided.
   */
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
