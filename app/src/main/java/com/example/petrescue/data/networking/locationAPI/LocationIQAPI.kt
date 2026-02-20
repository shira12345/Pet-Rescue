package com.example.petrescue.data.networking.locationAPI

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface LocationIQAPI {
  @GET("v1/autocomplete.php")
  fun getAutocomplete(
    @Query("key") apiKey: String,
    @Query("q") query: String,
    @Query("format") format: String = "json"
  ): Call<List<LocationIQResult>>

  @GET("v1/reverse.php")
  fun reverseGeocode(
    @Query("key") apiKey: String,
    @Query("lat") lat: Double,
    @Query("lon") lon: Double,
    @Query("format") format: String = "json"
  ): Call<LocationIQReverseResult>
}