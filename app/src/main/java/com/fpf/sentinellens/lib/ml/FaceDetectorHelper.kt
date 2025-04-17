package com.fpf.sentinellens.lib.ml

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.Log
import android. graphics.Paint
import com.fpf.sentinellens.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.util.Collections
import kotlin.math.max
import kotlin.math.min
import kotlin.system.measureTimeMillis
import androidx.core.graphics.scale
import java.nio.ByteBuffer

class FaceDetectorHelper(resources: Resources) {
    private var ortEnv: OrtEnvironment = OrtEnvironment.getEnvironment()
    private var session: OrtSession? = null

    companion object {
        private const val TAG = "FaceComparisonHelper"
        private const val CONF_THRESHOLD = 0.5f
        private const val NMS_THRESHOLD = 0.3f
    }

    init {
        session = loadModel(resources, R.raw.face_detect)
    }

    private fun loadModel(resources: Resources, resourceId: Int): OrtSession {
        lateinit var session: OrtSession
        val timeTaken = measureTimeMillis {
            val modelBytes = resources.openRawResource(resourceId).readBytes()
            session = ortEnv.createSession(modelBytes)
        }
        Log.i(TAG, "Face detection model loaded in ${timeTaken}ms")
        return session
    }

    suspend fun detectFaces(bitmap: Bitmap): Pair<List<Float>, List<FloatArray>> =
        withContext(Dispatchers.Default) {
            // Ensure the session is loaded.
            val session = session ?: throw IllegalStateException("Model not loaded")
            val startTime = System.currentTimeMillis()
            val inputShape = longArrayOf(1, 3, 240, 320)
            val inputName = session.inputNames.iterator().next()
            val imgData: FloatBuffer = preprocessImg(bitmap)

            OnnxTensor.createTensor(ortEnv, imgData, inputShape).use { inputTensor ->
                session.run(Collections.singletonMap(inputName, inputTensor)).use { outputs ->
                    @Suppress("UNCHECKED_CAST")
                    val scoresRawFull = outputs[0].value as Array<Array<FloatArray>>
                    @Suppress("UNCHECKED_CAST")
                    val boxesRawFull = outputs[1].value as Array<Array<FloatArray>>

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
            }
        }

    fun cropFaces(bitmap: Bitmap, boxes: List<FloatArray>): List<Bitmap> {
        val faces = mutableListOf<Bitmap>()
        for (box in boxes) {
            val x1 = max(0, box[0].toInt())
            val y1 = max(0, box[1].toInt())
            val x2 = min(bitmap.width, box[2].toInt())
            val y2 = min(bitmap.height, box[3].toInt())
            val width = x2 - x1
            val height = y2 - y1
            if (width > 0 && height > 0) {
                val faceBitmap = Bitmap.createBitmap(bitmap, x1, y1, width, height)
                faces.add(faceBitmap)
            }
        }
        return faces
    }

    fun drawBoxes(bitmap: Bitmap, boxes: List<FloatArray>, color: Int, strokeWidth: Float = 2f): Bitmap {
        val mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(mutableBitmap)

        val paint = Paint().apply {
            this.color = color
            this.strokeWidth = strokeWidth
        }

        for (box in boxes) {
            val x1 = max(0, box[0].toInt())
            val y1 = max(0, box[1].toInt())
            val x2 = min(mutableBitmap.width, box[2].toInt())
            val y2 = min(mutableBitmap.height, box[3].toInt())

            canvas.drawRect(x1.toFloat(), y1.toFloat(), x2.toFloat(), y2.toFloat(), paint)
        }

        return mutableBitmap
    }

    fun preprocessImg(bitmap: Bitmap): FloatBuffer {
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


    /**
     * Applies Non-Maximum Suppression (NMS) over the list of boxes.
     * This prevents mutliple similar bounding boxes
     *
     * @param boxes A list of boxes, where each box is represented as FloatArray of four values: [x1, y1, x2, y2].
     * @param scores A list of confidence scores corresponding to each box.
     * @param iouThreshold The threshold for Intersection-over-Union.
     * @return A list of indices corresponding to boxes that are kept after NMS.
     */
    private fun nms(boxes: List<FloatArray>, scores: List<Float>, iouThreshold: Float): List<Int> {
        if (boxes.isEmpty()) return emptyList()

        val indices = scores.indices.sortedByDescending { scores[it] }.toMutableList()
        val keep = mutableListOf<Int>()

        while (indices.isNotEmpty()) {
            val current = indices.removeAt(0)
            keep.add(current)
            val currentBox = boxes[current]

            indices.removeAll { idx ->
                val iou = computeIoU(currentBox, boxes[idx])
                iou > iouThreshold
            }
        }
        return keep
    }

    /**
     * Computes the Intersection over Union (IoU) of two boxes.
     *
     * @param boxA A box in the format [x1, y1, x2, y2].
     * @param boxB A box in the format [x1, y1, x2, y2].
     * @return IoU value.
     */
    private fun computeIoU(boxA: FloatArray, boxB: FloatArray): Float {
        val x1 = max(boxA[0], boxB[0])
        val y1 = max(boxA[1], boxB[1])
        val x2 = min(boxA[2], boxB[2])
        val y2 = min(boxA[3], boxB[3])
        val intersectionArea = max(0f, x2 - x1) * max(0f, y2 - y1)
        val areaA = max(0f, boxA[2] - boxA[0]) * max(0f, boxA[3] - boxA[1])
        val areaB = max(0f, boxB[2] - boxB[0]) * max(0f, boxB[3] - boxB[1])
        val unionArea = areaA + areaB - intersectionArea
        return if (unionArea <= 0f) 0f else intersectionArea / unionArea
    }


    fun closeSession() {
        session?.close()
        session = null
    }

}
