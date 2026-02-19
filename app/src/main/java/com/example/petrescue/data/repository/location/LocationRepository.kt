package com.example.petrescue.data.repository.movies

import com.example.petrescue.data.networking.locationAPI.LocationIQResult

interface LocationRepository {
  fun getLocations(query: String): List<LocationIQResult>
}