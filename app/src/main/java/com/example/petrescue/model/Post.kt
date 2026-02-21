package com.example.petrescue.model

import android.content.Context
import android.os.Parcelable
import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.example.petrescue.base.MyApplication
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import kotlinx.parcelize.Parcelize
import java.util.Date

/**
 * Represents a rescue post for a pet.
 * This class is used for local database persistence (Room), remote storage (Firestore),
 * and is Parcelable for passing between Android components.
 */
@Parcelize
@Keep
@Entity(tableName = "posts")
data class Post(
  /** Unique identifier for the post. */
  @PrimaryKey val id: String = "",
  /** Name of the pet in the post. */
  val petName: String = "",
  /** Type of the pet (e.g., Dog, Cat). */
  val petType: String = "",
  /** Breed of the pet, if known. */
  val breed: String? = null,
  /** Current status of the rescue (e.g., Lost, Found). */
  val status: String = "Lost",
  /** Detailed description of the situation or pet. */
  val description: String = "",
  /** URI or URL string for the post's image. */
  val imageUri: String? = "",
  /** Email address of the user who created the post. */
  val creatorEmail: String = "",
  /** Contact phone number of the creator. */
  val creatorPhone: String = "",
  /** Latitude coordinate of where the pet was seen or found. */
  val latitude: Double = 0.0,
  /** Longitude coordinate of where the pet was seen or found. */
  val longitude: Double = 0.0,
  /** Timestamp when the post was first created. */
  val createdAt: Long = System.currentTimeMillis(),
  /** Timestamp of the last time the post was updated. */
  val updatedAt: Long = System.currentTimeMillis()
) : Parcelable {
  companion object {
    /** Constants for pet types to ensure consistency across the app. */
    const val TYPE_DOG = "Dog"
    const val TYPE_CAT = "Cat"
    const val TYPE_BIRD = "Bird"
    const val TYPE_OTHER = "Other"

    /**
     * Globally tracks the last time any post was updated, used for delta-sync with remote DB.
     * Persisted in SharedPreferences.
     */
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

    /** Key for the ID field in JSON/Firestore. */
    const val ID_KEY = "id"
    /** Key for the pet name field in JSON/Firestore. */
    const val PET_NAME_KEY = "petName"
    /** Key for the pet type field in JSON/Firestore. */
    const val PET_TYPE_KEY = "petType"
    /** Key for the breed field in JSON/Firestore. */
    const val BREED_KEY = "breed"
    /** Key for the status field in JSON/Firestore. */
    const val STATUS_KEY = "status"
    /** Key for the description field in JSON/Firestore. */
    const val DESCRIPTION_KEY = "description"
    /** Key for the image URI field in JSON/Firestore. */
    const val IMAGE_URI_KEY = "imageUri"
    /** Key for the creator email field in JSON/Firestore. */
    const val CREATOR_EMAIL_KEY = "creatorEmail"
    /** Key for the creator phone field in JSON/Firestore. */
    const val CREATOR_PHONE_KEY = "creatorPhone"
    /** Key for the latitude field in JSON/Firestore. */
    const val LATITUDE_KEY = "latitude"
    /** Key for the longitude field in JSON/Firestore. */
    const val LONGITUDE_KEY = "longitude"
    /** Key for the created timestamp field in JSON/Firestore. */
    const val CREATED_AT_KEY = "createdAt"
    /** Key for the updated timestamp field in JSON/Firestore. */
    const val UPDATED_AT_KEY = "updatedAt"

    /**
     * Factory method to create a [Post] object from a Firestore data map.
     *
     * @param json Map of field names to values from Firestore.
     * @return A populated Post object.
     */
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
      val updatedLong = updatedTimestamp?.toDate()?.time ?: createdLong

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

  /**
   * Converts the [Post] object into a map suitable for Firestore storage.
   */
  @get:Ignore
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
