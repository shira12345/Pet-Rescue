package com.example.petrescue.features.post_details

import androidx.lifecycle.ViewModel
import com.example.petrescue.data.repository.location.RemoteLocationRepository

class PostDetailsViewModel : ViewModel() {
  private val locationRepository = RemoteLocationRepository.shared

  suspend fun getAddressFromPostLocation(lat: Double, lon: Double): String {
    val address = locationRepository.getAddressFromLatLon(lat, lon)

    return address
  }
}
