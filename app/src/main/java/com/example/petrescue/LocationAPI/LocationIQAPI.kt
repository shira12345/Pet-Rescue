package com.example.petrescue.LocationAPI

import retrofit2.http.GET
import retrofit2.http.Query

interface LocationIQAPI {
  @GET("v1/autocomplete.php")
  suspend fun getAutocomplete(
    @Query("key") apiKey: String,
    @Query("q") query: String,
    @Query("format") format: String = "json"
  ): List<LocationIQResult>
}