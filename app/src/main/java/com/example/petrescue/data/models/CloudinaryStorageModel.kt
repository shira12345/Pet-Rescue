package com.example.petrescue.data.models

import android.content.Context
import android.graphics.Bitmap
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.cloudinary.android.policy.GlobalUploadPolicy
import com.cloudinary.android.policy.UploadPolicy
import com.example.petrescue.base.MyApplication
import com.example.petrescue.base.StringCompletion
import com.example.petrescue.model.Post
import java.io.File

class CloudinaryStorageModel {
  init {
    // TODO: MOVE TO ENV VARIABLES
    val config = mapOf(
      "cloud_name" to "deitzns1y",
      "api_key" to "678992518458133",
      "api_secret" to "VIyl8lWYsiVRx4Nfveo4BnqmIQw"
    )

    MyApplication.appContext?.let {
      MediaManager.init(it, config)

      MediaManager.get().globalUploadPolicy = GlobalUploadPolicy.Builder()
        .maxConcurrentRequests(3)
        .networkPolicy(UploadPolicy.NetworkType.UNMETERED)
        .build()
    }
  }

  fun uploadPostImage(image: Bitmap, post: Post, completion: StringCompletion) {
    val context = MyApplication.appContext ?: return
    val file = bitmapToFile(image, context)

    MediaManager.get().upload(file.path)
      .option("images", "posts/${post.id}/pet_image")
      .callback(object : UploadCallback {
        override fun onStart(requestId: String) {
          // Upload started
        }

        override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {
          // Upload progress
        }

        override fun onSuccess(requestId: String, resultData: Map<*, *>) {
          val url = resultData["secure_url"] as? String

          completion(url)
        }

        override fun onError(requestId: String?, error: ErrorInfo?) {
          completion(null)
        }

        override fun onReschedule(requestId: String?, error: ErrorInfo?) {
          TODO("Not yet implemented")
        }
      })
      .dispatch()
  }

  private fun bitmapToFile(image: Bitmap, context: Context): File {
    val file = File(context.cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")

    file.outputStream().use {
      image.compress(Bitmap.CompressFormat.JPEG, 100, it)
      it.flush()
    }

    return file
  }
}