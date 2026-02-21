package com.example.petrescue.data.networking.locationAPI

import com.google.gson.annotations.SerializedName

data class LocationIQResult(
  @SerializedName("display_name") val displayName: String,
  val lat: String,
  val lon: String
)