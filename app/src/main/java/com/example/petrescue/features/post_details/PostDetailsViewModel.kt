package com.example.petrescue.features.post_details

import androidx.lifecycle.ViewModel
import com.example.petrescue.data.repository.location.RemoteLocationRepository

/**
 * ViewModel for the Post Details screen.
 * Handles UI logic related to displaying specific post information.
 */
class PostDetailsViewModel : ViewModel() {
  private val locationRepository = RemoteLocationRepository.shared

  /**
   * Converts geographic coordinates into a human-readable address.
   *
   * @param lat The latitude of the location.
   * @param lon The longitude of the location.
   * @return A string representing the display name of the location.
   */
  suspend fun getAddressFromPostLocation(lat: Double, lon: Double): String {
    val address = locationRepository.getAddressFromLatLon(lat, lon)

    return address
  }
}
