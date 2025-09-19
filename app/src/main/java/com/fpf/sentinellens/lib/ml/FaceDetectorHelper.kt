package com.fpf.sentinellens.lib.ml

import android.content.res.Resources
import android.graphics.Bitmap
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.ByteOrder
import java.nio.FloatBuffer
import androidx.core.graphics.scale
import com.fpf.smartscansdk.core.ml.models.FileOnnxLoader
import com.fpf.smartscansdk.core.ml.models.FilePath
import com.fpf.smartscansdk.core.ml.models.ModelSource
import com.fpf.smartscansdk.core.ml.models.OnnxModel
import com.fpf.smartscansdk.core.ml.models.ResourceId
import com.fpf.smartscansdk.core.ml.models.ResourceOnnxLoader
import com.fpf.smartscansdk.core.ml.models.TensorData
import java.nio.ByteBuffer

interface IDetectionProvider<T> {
    fun closeSession() = Unit
    suspend fun detect(data: T): Pair<List<Float>, List<FloatArray>>
}

typealias FaceDetectorProvider = IDetectionProvider<Bitmap>

class FaceDetectorHelper(
    resources: Resources,
    modelSource: ModelSource,
) : FaceDetectorProvider {
    private val model: OnnxModel = when(modelSource){
        is FilePath -> OnnxModel(FileOnnxLoader(modelSource.path))
        is ResourceId -> OnnxModel(ResourceOnnxLoader(resources, modelSource.resId))
    }

    companion object {
        private const val TAG = "FaceDetectorHelper"
        private const val CONF_THRESHOLD = 0.5f
        private const val NMS_THRESHOLD = 0.3f
    }

    suspend fun initialize() = model.loadModel()

    fun isInitialized() = model.isLoaded()

    private var closed = false

    override suspend fun detect(bitmap: Bitmap): Pair<List<Float>, List<FloatArray>> = withContext(Dispatchers.Default) {
        val startTime = System.currentTimeMillis()
        val inputShape = longArrayOf(1, 3, 240, 320)
        val imgData: FloatBuffer = preprocessImg(bitmap)
        val inputName = model.getInputNames()?.firstOrNull() ?: throw IllegalStateException("Model inputs not available")
        val outputs = model.run(mapOf(inputName to TensorData.FloatBufferTensor(imgData, inputShape)))

        val outputList = outputs.values.toList()
        @Suppress("UNCHECKED_CAST")
        val scoresRawFull  = outputList[0] as Array<Array<FloatArray>>
        @Suppress("UNCHECKED_CAST")
        val boxesRawFull = outputList[1] as Array<Array<FloatArray>>

        // Extract the first element (batch dimension)
        val scoresRaw = scoresRawFull[0]  // shape: [num_boxes, 2]
        val boxesRaw = boxesRawFull[0]    // shape: [num_boxes, 4]

        val imgWidth = bitmap.width
        val imgHeight = bitmap.height

        val boxesList = mutableListOf<FloatArray>()
        val scoresList = mutableListOf<Float>()
        for (i in scoresRaw.indices) {
            val faceScore = scoresRaw[i][1]
            if (faceScore > CONF_THRESHOLD) {
                val box = boxesRaw[i]
                // Box values are normalized; convert to absolute pixel coordinates.
                val x1 = box[0] * imgWidth
                val y1 = box[1] * imgHeight
                val x2 = box[2] * imgWidth
                val y2 = box[3] * imgHeight
                boxesList.add(floatArrayOf(x1, y1, x2, y2))
                scoresList.add(faceScore)
            }
        }

        val inferenceTime = System.currentTimeMillis() - startTime
        Log.d(TAG, "Detection Inference Time: $inferenceTime ms")

        // Apply NMS if any detection exists.
        if (boxesList.isNotEmpty()) {
            val keepIndices = nms(boxesList, scoresList, NMS_THRESHOLD)
            val filteredBoxes = keepIndices.map { boxesList[it] }
            val filteredScores = keepIndices.map { scoresList[it] }
            return@withContext Pair(filteredScores, filteredBoxes)
        } else {
            return@withContext Pair(emptyList<Float>(), emptyList<FloatArray>())
        }
    }

    private fun preprocessImg(bitmap: Bitmap): FloatBuffer {
        val targetWidth = 320
        val targetHeight = 240
        val resizedBitmap = bitmap.scale(targetWidth, targetHeight)

        val width = resizedBitmap.width
        val height = resizedBitmap.height
        val intValues = IntArray(width * height)
        resizedBitmap.getPixels(intValues, 0, width, 0, 0, width, height)

        val floatArray = FloatArray(3 * height * width)

        // Process each pixel and store them in channel-first order.
        // Channel 0: indices 0 .. height*width-1, etc.
        for (i in 0 until height) {
            for (j in 0 until width) {
                val pixel = intValues[i * width + j]
                val r = ((pixel shr 16) and 0xFF).toFloat()
                val g = ((pixel shr 8) and 0xFF).toFloat()
                val b = (pixel and 0xFF).toFloat()

                // Normalize channels
                val normalizedR = (r - 127f) / 128f
                val normalizedG = (g - 127f) / 128f
                val normalizedB = (b - 127f) / 128f

                val index = i * width + j
                floatArray[index] = normalizedR                               // Channel 0
                floatArray[height * width + index] = normalizedG                // Channel 1
                floatArray[2 * height * width + index] = normalizedB            // Channel 2
            }
        }

        // Allocate a direct ByteBuffer, set the native byte order,
        // then convert it to a FloatBuffer.
        val byteBuffer = ByteBuffer.allocateDirect(floatArray.size * 4)
            .order(ByteOrder.nativeOrder())
        val floatBuffer = byteBuffer.asFloatBuffer()
        floatBuffer.put(floatArray)
        // Reset the position to the beginning of the buffer.
        floatBuffer.position(0)
        return floatBuffer
    }

    override fun closeSession() {
        if (closed) return
        closed = true
        (model as? AutoCloseable)?.close()
    }
}
