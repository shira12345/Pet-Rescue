package com.example.petrescue.data.repository.location

import com.example.petrescue.data.networking.locationAPI.LocationIQResult

interface LocationRepository {
  suspend fun getLocations(query: String): List<LocationIQResult>

  suspend fun getAddressFromLatLon(lat: Double, lon: Double): String
}