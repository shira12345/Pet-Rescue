package com.example.petrescue

import android.app.Application
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.petrescue.base.StringCompletion
import com.example.petrescue.data.networking.locationAPI.LocationIQResult
import com.example.petrescue.data.repository.location.RemoteLocationRepository
import com.example.petrescue.data.repository.posts.PostsRepository
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

  private var searchJob: Job? = null

  fun searchLocation(query: String) {
    if (query.length < MIN_LOCATION_QUERY_LENGTH) return

    searchJob?.cancel()

    searchJob = viewModelScope.launch(Dispatchers.IO) {
      delay(500)

      val locations = RemoteLocationRepository.shared.getLocations(query)
      Log.i("TAG", "Locations: ${locations.size}")

      withContext(Dispatchers.Main) { _results.value = locations }
    }
  }

  fun createPost(
    petName: String,
    petType: String,
    breed: String?,
    status: String,
    description: String,
    image: Bitmap,
    completion: StringCompletion
  ) {
    val latitude = selectedLat
    val longitude = selectedLon

    if (latitude == null || longitude == null)
      return completion("Please select a location from the suggestions list")

    val newPost = Post(
      petName = petName,
      petType = petType,
      breed = breed,
      status = status,
      description = description,
      imageUri = null, // Will be set in repository
      latitude = latitude,
      longitude = longitude
    )

    PostsRepository.shared.createPost(image, newPost, completion)
  }
}