package com.example.petrescue.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
  @PrimaryKey val email: String,
  val username: String,
  val password: String,
  val phoneNumber: String? = null,
  val animal: String? = null,
  val profileImage: String? = null
)