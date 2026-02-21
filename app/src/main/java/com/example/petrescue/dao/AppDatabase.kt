package com.example.petrescue.dao

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.petrescue.model.Post
import com.example.petrescue.model.User

/**
 * The Room database for the application.
 * Defines the schema and provides access to the Data Access Objects (DAOs).
 */
@Database(entities = [User::class, Post::class], version = 9)
abstract class AppDatabase : RoomDatabase() {
  /** Provides access to the [UserDao]. */
  abstract fun userDao(): UserDao
  /** Provides access to the [PostDao]. */
  abstract fun postDao(): PostDao

  companion object {
    @Volatile
    private var INSTANCE: AppDatabase? = null

    /**
     * Gets the singleton instance of the [AppDatabase].
     *
     * @param context The context used to build the database.
     * @return The singleton database instance.
     */
    fun getDatabase(context: Context): AppDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          AppDatabase::class.java,
          "petrescue_db"
        )
          .fallbackToDestructiveMigration(dropAllTables = true)
          .build()
        INSTANCE = instance
        instance
      }
    }
  }
}
