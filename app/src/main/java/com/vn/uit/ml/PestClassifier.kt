package com.vn.uit.ml

import android.content.Context
import android.content.res.AssetManager
import android.graphics.Bitmap
import android.util.Log
import org.pytorch.IValue
import org.pytorch.Module
import org.pytorch.torchvision.TensorImageUtils
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class PestClassifier(private val context: Context) {

    private var module: Module? = null
    private val TAG = "PestClassifier"
    private val INPUT_WIDTH = 224
    private val INPUT_HEIGHT = 224

    init {
        try {
            val assetManager = context.assets
            val modelPath = assetFilePath(context, assetManager, "best_swin_pretrained_model.pt")
            module = Module.load(modelPath)
            Log.d(TAG, "Model loaded successfully")
        } catch (e: IOException) {
            Log.e(TAG, "Error loading model", e)
        }
    }

    fun classify(bitmap: Bitmap): FloatArray {
        val localModule = module ?: throw IllegalStateException("Model not loaded")

        try {
            val resizedBitmap = Bitmap.createScaledBitmap(bitmap, INPUT_WIDTH, INPUT_HEIGHT, true)

            val inputTensor = TensorImageUtils.bitmapToFloat32Tensor(
                resizedBitmap,
                TensorImageUtils.TORCHVISION_NORM_MEAN_RGB,
                TensorImageUtils.TORCHVISION_NORM_STD_RGB
            )

            val outputTensor = localModule.forward(IValue.from(inputTensor)).toTensor()

            return outputTensor.dataAsFloatArray
        } catch (e: Exception) {
            Log.e(TAG, "Error during classification", e)
            throw e
        }
    }



    private fun assetFilePath(context: Context, assetManager: AssetManager, assetName: String): String {
        val file = File(context.cacheDir, assetName)
        if (file.exists() && file.length() > 0) {
            Log.d(TAG, "Using cached model file")
            return file.absolutePath
        }

        Log.d(TAG, "Copying model file from assets to cache")
        try {
            assetManager.open(assetName).use { inputStream ->
                FileOutputStream(file).use { outputStream ->
                    val buffer = ByteArray(4 * 1024)
                    var read: Int
                    while (inputStream.read(buffer).also { read = it } != -1) {
                        outputStream.write(buffer, 0, read)
                    }
                    outputStream.flush()
                }
            }
            return file.absolutePath
        } catch (e: IOException) {
            Log.e(TAG, "Error copying asset file: $assetName", e)
            throw e
        }
    }

    fun close() {
        module = null
    }
}