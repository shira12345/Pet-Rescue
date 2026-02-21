package com.example.petrescue.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.petrescue.model.User

/**
 * Data Access Object for the users table.
 * Manages local database operations for user profiles.
 */
@Dao
interface UserDao {
  /**
   * Retrieves a user by their email address as a one-shot fetch.
   *
   * @param email The user's email address.
   * @return The User object or null if not found.
   */
  @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
  suspend fun getUserByEmail(email: String): User?

  /**
   * Retrieves a user by their email address as observable LiveData.
   *
   * @param email The user's email address.
   * @return Observable LiveData containing the User object or null.
   */
  @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
  fun getUserLiveDataByEmail(email: String): LiveData<User?>

  /**
   * Inserts or updates a user profile in the local database.
   *
   * @param user The user profile to save.
   */
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertUser(user: User)
}
