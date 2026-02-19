package com.example.petrescue.data.networking

import com.example.petrescue.data.networking.locationAPI.LocationIQAPI
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NetworkClient {
  val locationIQAPIClient: LocationIQAPI by lazy {
    val retrofit = Retrofit.Builder()
      .baseUrl("https://eu1.locationiq.com/")
      .addConverterFactory(GsonConverterFactory.create())
      .build()

    retrofit.create(LocationIQAPI::class.java)
  }
}