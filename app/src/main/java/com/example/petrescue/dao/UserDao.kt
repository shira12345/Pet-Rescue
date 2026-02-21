package com.example.petrescue.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.petrescue.model.User

@Dao
interface UserDao {
  @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
  suspend fun getUserByEmail(email: String): User?

  @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
  suspend fun insertUser(user: User)
}