package com.fpf.sentinellens.lib.ml

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import android.content.res.Resources
import android.graphics.Bitmap
import android.util.Log
import com.fpf.sentinellens.R
import com.fpf.sentinellens.lib.centerCrop
import com.fpf.sentinellens.lib.preProcess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Collections
import kotlin.system.measureTimeMillis

class FaceComparisonHelper(resources: Resources) {
    private var ortEnv: OrtEnvironment = OrtEnvironment.getEnvironment()
    private var session: OrtSession? = null

    init {
        session = loadModel(resources, R.raw.inception_resnet_v1_quant)
    }

    private fun loadModel(resources: Resources, resourceId: Int): OrtSession {
        lateinit var session: OrtSession
        val timeTaken = measureTimeMillis {
            val modelBytes = resources.openRawResource(resourceId).readBytes()
            session = ortEnv.createSession(modelBytes)
        }
        Log.i(TAG, "Face comparison model loaded in ${timeTaken}ms")
        return session
    }

    suspend fun generateFaceEmbedding(bitmap: Bitmap): FloatArray =
        withContext(Dispatchers.Default) {
            val faceSession = session ?: throw IllegalStateException("Image model not loaded")
            val processedBitmap = centerCrop(bitmap, 160)
            val inputShape = longArrayOf(1, 3, 160, 160)
            val inputName = faceSession.inputNames.iterator().next()
            val imgData = preProcess(processedBitmap)
            val startTime = System.currentTimeMillis()

            OnnxTensor.createTensor(ortEnv, imgData, inputShape).use { inputTensor ->
                faceSession.run(Collections.singletonMap(inputName, inputTensor)).use { output ->
                    @Suppress("UNCHECKED_CAST")
                    val inferenceTime = System.currentTimeMillis() - startTime
                    Log.d(TAG, "Comparison Inference Time: $inferenceTime ms")
                    val rawOutput = (output[0].value as Array<FloatArray>)[0]
                    rawOutput // already l2 normalized
                }
            }
        }

    fun closeSession() {
        session?.close()
        session = null
    }

    companion object {
        private const val TAG = "FaceComparisonHelper"
    }
}
