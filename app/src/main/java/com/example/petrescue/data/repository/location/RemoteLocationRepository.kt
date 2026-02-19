package com.example.petrescue.data.repository.location

import android.util.Log
import com.example.petrescue.BuildConfig
import com.example.petrescue.data.networking.NetworkClient
import com.example.petrescue.data.networking.locationAPI.LocationIQResult
import com.example.petrescue.data.repository.movies.LocationRepository

class RemoteLocationRepository : LocationRepository {
  companion object {
    val shared = RemoteLocationRepository()
  }

  override fun getLocations(query: String): List<LocationIQResult> {
    val request =
      NetworkClient.locationIQAPIClient.getAutocomplete(BuildConfig.LOCATION_IQ_KEY, query)

    Log.i("TAG", "getLocations: request: $request")

    val response = request.execute()

    val locations = when (response.isSuccessful) {
      true -> {
        response.body() ?: mutableListOf()
      }

      false -> {
        Log.i(
          "TAG",
          "getLocations: failed ${response.message()}, code: ${response.code()}, errorBody: ${
            response.errorBody()?.string()
          }"
        )

        return mutableListOf<LocationIQResult>()
      }
    }

    return locations
  }
}