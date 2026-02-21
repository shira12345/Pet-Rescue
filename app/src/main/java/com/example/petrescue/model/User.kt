package com.example.petrescue.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a user profile in the system.
 * This class is used for local database persistence (Room).
 */
@Entity(tableName = "users")
data class User(
  /** Unique identifier and login credential for the user. */
  @PrimaryKey val email: String,
  /** Display name of the user. */
  val username: String,
  /** Hashed or plain text password (depending on auth method). */
  val password: String,
  /** Optional contact phone number. */
  val phoneNumber: String? = null,
  /** Optional favorite or rescued animal type preference. */
  val animal: String? = null,
  /** URL or path to the user's profile image. */
  val profileImage: String? = null
)
