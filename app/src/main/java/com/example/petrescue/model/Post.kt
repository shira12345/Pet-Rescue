package com.example.petrescue.model

import android.content.Context
import android.os.Parcelable
import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.petrescue.base.MyApplication
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
@Keep
@Entity(tableName = "posts")
data class Post(
  @PrimaryKey val id: String = "",
  val petName: String = "",
  val petType: String = "",
  val breed: String? = null,
  val status: String = "Lost",
  val description: String = "",
  val imageUri: String? = "",
  val creatorEmail: String = "",
  val creatorPhone: String = "",
  val latitude: Double = 0.0,
  val longitude: Double = 0.0,
  val createdAt: Long = System.currentTimeMillis(),
  val updatedAt: Long = System.currentTimeMillis()
) : Parcelable {
  companion object {
    var lastUpdated: Long
      get() {
        return MyApplication.appContext
          ?.getSharedPreferences("TAG", Context.MODE_PRIVATE)
          ?.getLong(UPDATED_AT_KEY, 0) ?: 0
      }
      set(value) {
        MyApplication.appContext
          ?.getSharedPreferences("", Context.MODE_PRIVATE)
          ?.edit()
          ?.putLong(UPDATED_AT_KEY, value)
          ?.apply()
      }

    const val ID_KEY = "id"
    const val PET_NAME_KEY = "petName"
    const val PET_TYPE_KEY = "petType"
    const val BREED_KEY = "breed"
    const val STATUS_KEY = "status"
    const val DESCRIPTION_KEY = "description"
    const val IMAGE_URI_KEY = "imageUri"
    const val CREATOR_EMAIL_KEY = "creatorEmail"
    const val CREATOR_PHONE_KEY = "creatorPhone"
    const val LATITUDE_KEY = "latitude"
    const val LONGITUDE_KEY = "longitude"
    const val CREATED_AT_KEY = "createdAt"
    const val UPDATED_AT_KEY = "updatedAt"

    fun fromJson(json: Map<String, Any?>): Post {
      val id = json[ID_KEY] as? String ?: ""
      val petName = json[PET_NAME_KEY] as? String ?: ""
      val petType = json[PET_TYPE_KEY] as? String ?: ""
      val breed = json[BREED_KEY] as? String
      val status = json[STATUS_KEY] as? String ?: "Lost"
      val description = json[DESCRIPTION_KEY] as? String ?: ""
      val imageUri = json[IMAGE_URI_KEY] as? String ?: ""
      val creatorEmail = json[CREATOR_EMAIL_KEY] as? String ?: ""
      val creatorPhone = json[CREATOR_PHONE_KEY] as? String ?: ""
      val latitude = (json[LATITUDE_KEY] as? Number)?.toDouble() ?: 0.0
      val longitude = (json[LONGITUDE_KEY] as? Number)?.toDouble() ?: 0.0

      val createdTimestamp = json[CREATED_AT_KEY] as? Timestamp
      val createdLong = createdTimestamp?.toDate()?.time ?: System.currentTimeMillis()

      val updatedTimestamp = json[UPDATED_AT_KEY] as? Timestamp
      val updatedLong =
        updatedTimestamp?.toDate()?.time ?: createdLong

      return Post(
        id = id,
        petName = petName,
        petType = petType,
        breed = breed,
        status = status,
        description = description,
        imageUri = imageUri,
        creatorEmail = creatorEmail,
        creatorPhone = creatorPhone,
        latitude = latitude,
        longitude = longitude,
        createdAt = createdLong,
        updatedAt = updatedLong
      )
    }
  }

  val toJson: Map<String, Any?>
    get() = hashMapOf(
      ID_KEY to id,
      PET_NAME_KEY to petName,
      PET_TYPE_KEY to petType,
      BREED_KEY to breed,
      STATUS_KEY to status,
      DESCRIPTION_KEY to description,
      IMAGE_URI_KEY to imageUri,
      CREATOR_EMAIL_KEY to creatorEmail,
      CREATOR_PHONE_KEY to creatorPhone,
      LATITUDE_KEY to latitude,
      LONGITUDE_KEY to longitude,
      CREATED_AT_KEY to if (createdAt == 0L) FieldValue.serverTimestamp() else Timestamp(
        Date(
          createdAt
        )
      ),
      UPDATED_AT_KEY to FieldValue.serverTimestamp()
    )
}