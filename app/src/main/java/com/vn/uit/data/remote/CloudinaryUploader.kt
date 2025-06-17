package com.vn.uit.data.remote

import android.util.Log
import com.vn.uit.BuildConfig
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

object CloudinaryUploader {
    suspend fun upload(file: File): String? {
        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
        val filePart = MultipartBody.Part.createFormData("file",    file.name, requestFile)
        val preset = BuildConfig.CLOUDINARY_UPLOAD_PRESET.toRequestBody("text/plain".toMediaTypeOrNull())

        return try {
            val response = CloudinaryClient.instance.uploadImage(
                BuildConfig.CLOUDINARY_CLOUD_NAME,
                preset,
                filePart
            )
            if (response.isSuccessful) {
                response.body()?.secure_url
            } else {
                Log.e("Cloudinary", "Upload failed: ${response.errorBody()?.string()}")
                null
            }
        } catch (e: Exception) {
            Log.e("Cloudinary", "Exception: ${e.localizedMessage}")
            null
        }
    }
}

