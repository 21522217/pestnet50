package com.vn.uit.viewmodel

import android.app.Application
import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ImageDecoder
import android.graphics.Paint
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.vn.uit.ml.PestClassifier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PestClassificationViewModel(application: Application) : AndroidViewModel(application) {

    private val _classificationState = MutableLiveData<ClassificationState>()
    val classificationState: LiveData<ClassificationState> = _classificationState

    private val _capturedImageUri = MutableLiveData<Uri?>()
    val capturedImageUri: LiveData<Uri?> = _capturedImageUri

    private val _processedImage = MutableLiveData<Bitmap?>()
    val processedImage: LiveData<Bitmap?> = _processedImage

    private val _detectedPestName = MutableLiveData<String>()
    val detectedPestName: LiveData<String> = _detectedPestName

    private val context = application.applicationContext

    private val pestClassifier: PestClassifier by lazy {
        PestClassifier(context)
    }

    private val TAG = "PestClassifyImage"

    private val pestLabels = listOf(
        "Cnaphalocrocis medinalis",
        "Scirpophaga incertulas",
        "Chrysodeixis chalcites",
        "Spodoptera frugiperda",
        "Helicoverpa armigera",
        "Nezara viridula",
        "Henosepilachna vigintioctopunctata",
        "Zeuzera coffeae",
        "Maiestas dorsalis",
        "Pieris rapae",
        "Plutella xylostella",
        "Leptoglossus gonagra",
        "Cydia pomonella",
        "Riptortus linearis",
        "Spilosoma lubricipeda",
        "Spodoptera exigua",
        "Chilo sacchariphagus",
        "Ostrinia furnacalis",
        "Maruca vitrata",
        "Lobesia botrana",
        "Mamestra brassicae",
        "Anarsia lineatella",
        "Cydia pyrivora",
        "Papilio demoleus",
        "Aspila molesta",
        "Etiella zinckenella",
        "Aulacophora indica",
        "Leucinodes orbonalis",
        "Bemisia tabaci",
        "Tetranychus urticae",
        "Myzus persicae",
        "Aphis gossypii",
        "Phyllocnistis citrella",
        "Leptocorisa oratoria",
        "Liriomyza sativae",
        "Operophtera brumata",
        "Sogatella furcifera",
        "Pseudococcus longispinus",
        "Diaphorina citri",
        "Chilo partellus",
        "Eriogaster lanestris",
        "Gastropacha populifolia",
        "Cydia splendana",
        "Euthrix potatoria",
        "Anomala corpulenta",
        "Lygus lineolaris",
        "Danaus genutia",
        "Spodoptera litura",
        "Helicoverpa zea",
        "Bactrocera dorsalis"
    )

    private fun convertToSoftwareBitmap(bitmap: Bitmap): Bitmap {
        if (bitmap.config == Bitmap.Config.HARDWARE) {
            Log.d(TAG, "Converting HARDWARE bitmap to ARGB_8888")
            return bitmap.copy(Bitmap.Config.ARGB_8888, true)
        }
        return bitmap
    }

    private fun softmax(logits: FloatArray): FloatArray {
        val maxLogit = logits.maxOrNull() ?: 0f
        val expScores = logits.map { Math.exp((it - maxLogit).toDouble()) }
        val sumExpScores = expScores.sum()
        return expScores.map { (it / sumExpScores).toFloat() }.toFloatArray()
    }

    fun classifyImage(bitmap: Bitmap) {

        _classificationState.value = ClassificationState.Loading

        val softwareBitmap = convertToSoftwareBitmap(bitmap)

        viewModelScope.launch {
            try {
                val outputLogits = withContext(Dispatchers.Default) {
                    pestClassifier.classify(softwareBitmap)
                }

                val outputProbabilities = softmax(outputLogits)

                outputProbabilities.forEachIndexed { index, probability ->
                    Log.d(TAG, "Class ${pestLabels[index]} (class #${index + 1}): $probability")
                }

                val maxIndex = outputProbabilities.indices.maxByOrNull { outputProbabilities[it] } ?: -1

                if (maxIndex != -1 && maxIndex < pestLabels.size) {
                    val confidenceThreshold = 0.5f
                    val maxConfidence = outputProbabilities[maxIndex]

                    if (maxConfidence >= confidenceThreshold) {
                        val pestName = pestLabels[maxIndex]
                        _detectedPestName.value = pestName
                        _classificationState.value = ClassificationState.Success(pestName)
                    } else {
                        _classificationState.value = ClassificationState.Error("Low confidence classification.")
                    }
                } else {
                    _classificationState.value = ClassificationState.Error("Unable to classify pest.")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Classification failed", e)
                Log.v(TAG, "Exception details: ${e.message}", e)
                _classificationState.value = ClassificationState.Error("Classification failed: ${e.message}")
            }
        }

    }

    fun setImageUri(uri: Uri) {
        try {
            _capturedImageUri.value = uri

            val bitmap = if (Build.VERSION.SDK_INT < 28) {
                @Suppress("DEPRECATION")
                android.provider.MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            } else {
                val source = ImageDecoder.createSource(context.contentResolver, uri)
                ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                    decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                }
            }

            setProcessedImage(bitmap)

            classifyImage(bitmap)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to decode image", e)
            _classificationState.value = ClassificationState.Error("Failed to decode image: ${e.message}")
        }
    }

    fun resetClassification() {
        _classificationState.value = ClassificationState.Idle
        _capturedImageUri.value = null
        _processedImage.value = null
        _detectedPestName.value = null
    }

    fun setProcessedImage(bitmap: Bitmap) {
        val softwareBitmap = convertToSoftwareBitmap(bitmap)
        _processedImage.value = softwareBitmap
    }

    fun saveResult(): Uri? {
        val bitmap = _processedImage.value ?: return null
        val pestName = _detectedPestName.value ?: "Unknown"

        try {
            val resultBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, bitmap.config)
            val canvas = Canvas(resultBitmap)
            canvas.drawBitmap(bitmap, 0f, 0f, null)

            val paint = Paint().apply {
                color = android.graphics.Color.WHITE
                textSize = 50f
                style = Paint.Style.FILL
                isFakeBoldText = true
                setShadowLayer(10f, 0f, 0f, android.graphics.Color.BLACK)
            }
            canvas.drawText(pestName, 20f, bitmap.height - 20f, paint)

            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val filename = "PestDetection_${timestamp}.jpg"

            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/PestDetector")
                }
            }

            val uri = context.contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues
            ) ?: return null

            context.contentResolver.openOutputStream(uri)?.use { outputStream: OutputStream ->
                resultBitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            }

            return uri
        } catch (e: Exception) {
            return null
        }
    }

    sealed class ClassificationState {
        object Idle : ClassificationState()
        object Loading : ClassificationState()
        data class Success(val pestName: String) : ClassificationState()
        data class Error(val message: String) : ClassificationState()
    }
}