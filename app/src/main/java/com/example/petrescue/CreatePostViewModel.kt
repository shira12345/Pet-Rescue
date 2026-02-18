package com.example.petrescue

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petrescue.LocationAPI.LocationIQAPI
import com.example.petrescue.LocationAPI.LocationIQResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class CreatePostViewModel : ViewModel() {
  private val _results = MutableLiveData<List<LocationIQResult>>()
  val results: LiveData<List<LocationIQResult>> = _results

  var selectedLat: Double? = null
  var selectedLon: Double? = null

  private var searchJob: Job? = null

  private val locationIQAPI = Retrofit.Builder()
    .baseUrl("https://eu1.locationiq.com/")
    .addConverterFactory(GsonConverterFactory.create())
    .build()
    .create(LocationIQAPI::class.java)

  fun searchLocation(query: String, apiKey: String) {
    if (query.length < 3) return

    searchJob?.cancel()
    searchJob = viewModelScope.launch {
      delay(500)
      try {
        val response = locationIQAPI.getAutocomplete(apiKey, query)
        _results.postValue(response)
      } catch (e: Exception) {
        // TODO: Handle errors!

        _results.postValue(emptyList())
      }
    }
  }
}