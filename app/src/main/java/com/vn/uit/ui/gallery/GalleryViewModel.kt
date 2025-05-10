package com.vn.uit.ui.gallery

import android.app.Application
import android.content.ContentUris
import android.net.Uri
import android.provider.MediaStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date

class GalleryViewModel(application: Application) : AndroidViewModel(application) {

    private val _galleryImages = MutableLiveData<List<GalleryImage>>()
    val galleryImages: LiveData<List<GalleryImage>> = _galleryImages

    fun loadGalleryImages() {
        viewModelScope.launch(Dispatchers.IO) {
            val images = queryImagesFromDevice()
            _galleryImages.postValue(images)
        }
    }

    private fun queryImagesFromDevice(): List<GalleryImage> {
        val imageList = mutableListOf<GalleryImage>()

        // Columns to fetch
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATE_ADDED
        )

        // Sort by date, newest first
        val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

        // Query the media store
        getApplication<Application>().contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            val dateColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val name = cursor.getString(nameColumn)
                val dateAdded = Date(cursor.getLong(dateColumn) * 1000) // Convert to milliseconds

                val contentUri = ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    id
                )

                imageList.add(GalleryImage(contentUri, name, dateAdded))
            }
        }

        return imageList
    }
}

// Data class to hold image information
data class GalleryImage(
    val uri: Uri,
    val name: String,
    val dateAdded: Date
)