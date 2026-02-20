package com.example.petrescue.dao

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.petrescue.model.Post
import com.example.petrescue.model.User

@Database(entities = [User::class, Post::class], version = 9) // Bumped to 9 to fix crashes
abstract class AppDatabase : RoomDatabase() {
  abstract fun userDao(): UserDao
  abstract fun postDao(): PostDao

  companion object {
    @Volatile
    private var INSTANCE: AppDatabase? = null

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