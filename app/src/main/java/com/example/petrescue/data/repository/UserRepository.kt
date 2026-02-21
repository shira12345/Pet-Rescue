package com.example.petrescue.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.core.content.edit
import androidx.lifecycle.LiveData
import com.example.petrescue.dao.AppDatabase
import com.example.petrescue.data.repository.cloudinary.CloudinaryRepository
import com.example.petrescue.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await

/**
 * Repository class that handles user-related data operations, including authentication
 * and profile management. It provides a unified interface for both Firebase Authentication
 * and local Room database storage.
 *
 * @param context The application context used to initialize the local database and preferences.
 */
class UserRepository(context: Context) {

  companion object {
    const val PREF_CURRENT_USER_EMAIL = "current_user_email"
    const val PREFS_NAME = "pet_rescue_prefs"
  }

  private val auth = try {
    FirebaseAuth.getInstance()
  } catch (exception: Exception) {
    Log.e("UserRepository", "Firebase not initialized.", exception)

    null
  }

  private val userDao = AppDatabase.getDatabase(context).userDao()
  private val cloudinaryRepository = CloudinaryRepository()
  private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

  /**
   * Returns the currently authenticated Firebase user, if any.
   */
  val currentUser: FirebaseUser? get() = auth?.currentUser

  /**
   * Returns an observable LiveData for a user stored in the local database.
   *
   * @param email The email address of the user to retrieve.
   * @return A LiveData object containing the user profile or null if not found.
   */
  fun getLocalUser(email: String): LiveData<User?> {
    return userDao.getUserLiveDataByEmail(email)
  }

  /**
   * Authenticates a user using email and password.
   *
   * This method first attempts to sign in via Firebase. If Firebase is unavailable or the
   * sign-in fails, it falls back to local database authentication.
   *
   * @param email The user's email address.
   * @param pass The user's password.
   * @return A Result containing the FirebaseUser on success, or an Exception on failure.
   *         Success with a null value indicates successful local-only authentication.
   */
  suspend fun login(email: String, pass: String): Result<FirebaseUser?> {
    return try {
      if (auth != null && auth.app.options.apiKey != "API_KEY") {
        auth.signInWithEmailAndPassword(email, pass).await()
        saveSession(email)

        Result.success(auth.currentUser)
      } else {
        if (authenticateLocally(email, pass)) Result.success(null)
        else Result.failure(Exception("Invalid email or password"))
      }
    } catch (exception: Exception) {
      if (authenticateLocally(email, pass)) Result.success(null)
      else Result.failure(exception)
    }
  }

  /**
   * Helper method to validate user credentials against the local database.
   *
   * @param email The user's email address.
   * @param pass The user's password.
   * @return True if the credentials are valid and the session was saved, false otherwise.
   */
  private suspend fun authenticateLocally(email: String, pass: String): Boolean {
    val localUser = userDao.getUserByEmail(email)

    return if (localUser != null && localUser.password == pass) {
      saveSession(email)

      true
    } else {
      false
    }
  }

  /**
   * Registers a new user account.
   *
   * Creates an account in Firebase (if configured) and persists the user profile
   * in the local database.
   *
   * @param username The desired display name.
   * @param email The user's email address.
   * @param pass The user's password.
   * @return A Result containing the newly created User object or an Exception on failure.
   */
  suspend fun signUp(username: String, email: String, pass: String): Result<User> {
    return try {
      val existingUser = userDao.getUserByEmail(email)
      if (existingUser != null) return Result.failure(Exception("Account already exists"))

      val newUser = User(email, username, pass)
      if (auth != null && auth.app.options.apiKey != "API_KEY") {
        auth.createUserWithEmailAndPassword(email, pass).await()
      }
      userDao.insertUser(newUser)
      saveSession(email)
      Result.success(newUser)
    } catch (exception: Exception) {
      Result.failure(exception)
    }
  }

  /**
   * Updates an existing user's profile information.
   *
   * Optionally uploads a new profile image to Cloudinary and saves the updated
   * information to the local database.
   *
   * @param email The user's email address (used as a key).
   * @param username The updated username.
   * @param phone The updated phone number.
   * @param animal The user's favorite or rescued animal type.
   * @param imageBitmap Optional bitmap of the new profile image.
   * @return A Result containing the updated User object or an Exception on failure.
   */
  suspend fun updateProfile(
    email: String,
    username: String,
    phone: String,
    animal: String,
    imageBitmap: Bitmap?
  ): Result<User> {
    return try {
      val currentUser = userDao.getUserByEmail(email) ?: User(email, username, "SOCIAL_LOGIN")

      var profileImageUrl = currentUser.profileImage

      if (imageBitmap != null)
        profileImageUrl = cloudinaryRepository.uploadProfileImage(imageBitmap, email)

      val updatedUser = currentUser.copy(
        username = username,
        phoneNumber = phone,
        animal = animal,
        profileImage = profileImageUrl
      )
      userDao.insertUser(updatedUser)
      Result.success(updatedUser)
    } catch (exception: Exception) {
      Result.failure(exception)
    }
  }

  /**
   * Removes the profile image URL for a given user in the local database.
   *
   * @param email The email address of the user.
   */
  suspend fun deleteProfileImage(email: String) {
    userDao.getUserByEmail(email)?.let {
      userDao.insertUser(it.copy(profileImage = null))
    }
  }

  /**
   * Signs the user out of Firebase and clears the local session data.
   */
  fun logout() {
    auth?.signOut()
    clearSession()
  }

  /**
   * Persists the current user's email in SharedPreferences.
   */
  private fun saveSession(email: String) {
    prefs.edit { putString(PREF_CURRENT_USER_EMAIL, email) }
  }

  /**
   * Removes the stored user email from SharedPreferences.
   */
  private fun clearSession() {
    prefs.edit { remove(PREF_CURRENT_USER_EMAIL) }
  }

  /**
   * Retrieves the currently saved user email from the previous session.
   *
   * @return The stored email address or null if no session exists.
   */
  fun getSavedEmail(): String? = prefs.getString(PREF_CURRENT_USER_EMAIL, null)
}
