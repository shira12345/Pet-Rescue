package com.example.petrescue.data.repository.cloudinary

import android.content.Context
import android.graphics.Bitmap
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.cloudinary.android.policy.GlobalUploadPolicy
import com.cloudinary.android.policy.UploadPolicy
import com.example.petrescue.base.MyApplication
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class CloudinaryRepository {
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

  suspend fun uploadPostImage(imageBitmap: Bitmap, postId: String): String =
    suspendCancellableCoroutine { continuation ->
      val imageFile =
        bitmapToFile(
          imageBitmap,
          MyApplication.appContext
            ?: return@suspendCancellableCoroutine continuation.resumeWithException(
              Exception("App context is null")
            )
        )

      val requestId = MediaManager.get().upload(imageFile.path)
        .option("folder", "posts/${postId}")
        .option("public_id", "pet_image")
        .callback(object : UploadCallback {
          override fun onSuccess(requestId: String, resultData: Map<*, *>) {
            val url = resultData["secure_url"] as? String

            if (url != null) continuation.resume(url)
            else continuation.resumeWithException(Exception("Cloudinary returned null URL"))
          }

          override fun onError(requestId: String?, error: ErrorInfo?) {
            println("Error while uploading an image: ${error?.description}")

            continuation.resumeWithException(Exception(error?.description))
          }

          override fun onStart(requestId: String) {
            println("Starting to upload with request id $requestId")
          }

          override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {
            println("Image upload process: $bytes / $totalBytes bytes")
          }

          override fun onReschedule(requestId: String?, error: ErrorInfo?) {
            println(error)
          }
        })
        .dispatch()

      // Cancel Cloudinary upload if coroutine is cancelled
      continuation.invokeOnCancellation { MediaManager.get().cancelRequest(requestId) }
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