package com.example.petrescue.data.repository.location

import android.util.Log
import com.example.petrescue.BuildConfig
import com.example.petrescue.data.networking.NetworkClient
import com.example.petrescue.data.networking.locationAPI.LocationIQResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.await

class RemoteLocationRepository : LocationRepository {
  companion object {
    val shared = RemoteLocationRepository()
  }
  
  override suspend fun getLocations(query: String): List<LocationIQResult> =
    withContext(Dispatchers.IO) {
      try {
        val response = NetworkClient.locationIQAPIClient
          .getAutocomplete(BuildConfig.LOCATION_IQ_KEY, query)
          .await()

        Log.i("TAG", "getLocations: ${response.size} results")

        response
      } catch (e: Exception) {
        Log.e("TAG", "getLocations: failed", e)

        emptyList()
      }
    }

  override suspend fun getAddressFromLatLon(lat: Double, lon: Double): String =
    withContext(Dispatchers.IO) {
      try {
        val response = NetworkClient.locationIQAPIClient
          .reverseGeocode(BuildConfig.LOCATION_IQ_KEY, lat, lon)
          .await()

        response.display_name
      } catch (e: Exception) {
        Log.e("TAG", "getAddressFromLatLon: failed", e)

        ""
      }
    }
}
