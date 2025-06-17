package com.vn.uit.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.cloudinary.Cloudinary
import com.vn.uit.BuildConfig
import com.vn.uit.data.remote.ApiClient
import com.vn.uit.ml.PestClassifier
import com.vn.uit.model.ClassificationRequest
import com.vn.uit.model.ClassificationResponse
import com.vn.uit.repository.PestRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
class PestClassificationViewModel(application: Application) : AndroidViewModel(application) {

    private val _classificationState = MutableLiveData<ClassificationState>()
    val classificationState: LiveData<ClassificationState> = _classificationState

    private val _capturedImageUri = MutableLiveData<Uri?>()
    val capturedImageUri: LiveData<Uri?> = _capturedImageUri

    private val _processedImage = MutableLiveData<Bitmap?>()
    val processedImage: LiveData<Bitmap?> = _processedImage

    private val _detectedPestName = MutableLiveData<String>()
    val detectedPestName: LiveData<String> = _detectedPestName

    private val _confidenceScore = MutableLiveData<Float?>()
    val confidenceScore: LiveData<Float?> = _confidenceScore

    private val _classificationResult = MutableLiveData<ClassificationResponse?>()
    val classificationResult: LiveData<ClassificationResponse?> = _classificationResult

    @SuppressLint("StaticFieldLeak")
    private val context = application.applicationContext

    private val pestClassifier: PestClassifier by lazy { PestClassifier(context) }

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
                    val confidenceThreshold = 0.8f
                    val maxConfidence = outputProbabilities[maxIndex]

                    _confidenceScore.value = maxConfidence
                    val pestName = pestLabels[maxIndex]
                    _detectedPestName.value = pestName

                    Log.d(TAG, "Detected Pest Name: $pestName")

                    if (maxConfidence < confidenceThreshold) {
                        Log.w(TAG, "Low confidence classification: $maxConfidence for $pestName")
                    }

                    val modelName = "SWIN Transformer"
                    val confidenceStr = String.format("%.2f", maxConfidence)
                    val originalName = "${modelName}_${pestName}_${confidenceStr}.jpg".replace(" ", "_")

                    val imageUrl = uploadToCloudinary(bitmap)

                    val result = sendClassificationToBackend(
                        imageUrl = imageUrl,
                        originalName = originalName,
                        pestScientificName = pestName,
                        modelName = modelName,
                        confidence = maxConfidence
                    )

                    viewModelScope.launch {
                        val pestRepo = PestRepository(ApiClient.pestApi)
                        when (val result = pestRepo.increasePestOccurrence(pestName)) {
                            else -> Log.d(
                                TAG,
                                "Occurrence updated: ${result.getOrNull()?.occurrenceCount}"
                            )
                        }
                    }

                    _classificationState.value = ClassificationState.Success(result)

                } else {
                    _confidenceScore.value = 0f
                    _classificationState.value = ClassificationState.Error("Unable to classify pest.")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Classification failed", e)
                _confidenceScore.value = null
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
        _confidenceScore.value = null
    }

    fun setProcessedImage(bitmap: Bitmap) {
        _processedImage.value = convertToSoftwareBitmap(bitmap)
    }


    sealed class ClassificationState {
        object Idle : ClassificationState()
        object Loading : ClassificationState()
        data class Success(val result: ClassificationResponse) : ClassificationState()
        data class Error(val message: String) : ClassificationState()
    }

    private suspend fun sendClassificationToBackend(
        imageUrl: String,
        originalName: String,
        pestScientificName: String,
        modelName: String,
        confidence: Float
    ): ClassificationResponse {
        val request = ClassificationRequest(
            imageUrl = imageUrl,
            originalName = originalName,
            scientificName = pestScientificName,
            modelName = modelName,
            confidence = confidence
        )

        val response = ApiClient.classificationApi.createClassification(request)

        if (response.status == 201 && response.data != null) {
            Log.d(TAG, "Classification sent successfully: ${response.data}")
            return response.data
        } else {
            val message = response.message ?: "Unknown backend error"
            Log.w(TAG, "Classification failed: $message")
            throw Exception(message)
        }
    }

    private suspend fun uploadToCloudinary(bitmap: Bitmap): String {
        return withContext(Dispatchers.IO) {
            val file = File.createTempFile("upload", ".jpg", context.cacheDir)
            val out = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            out.flush()
            out.close()

            val cloudinary = Cloudinary(
                mapOf(
                    "cloud_name" to BuildConfig.CLOUDINARY_CLOUD_NAME,
                    "api_key" to BuildConfig.CLOUDINARY_API_KEY,
                    "api_secret" to BuildConfig.CLOUDINARY_API_SECRET
                )
            )

            val uploadResult = cloudinary.uploader().upload(file, mapOf(
                "upload_preset" to BuildConfig.CLOUDINARY_UPLOAD_PRESET
            ))

            file.delete()

            return@withContext uploadResult["secure_url"] as String
        }
    }
}
