package com.fpf.sentinellens.lib.ml

import android.app.Application
import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import com.fpf.smartscansdk.core.ml.embeddings.ImageEmbeddingProvider
import com.fpf.smartscansdk.core.ml.models.FileOnnxLoader
import com.fpf.smartscansdk.core.ml.models.FilePath
import com.fpf.smartscansdk.core.ml.models.ModelSource
import com.fpf.smartscansdk.core.ml.models.OnnxModel
import com.fpf.smartscansdk.core.ml.models.ResourceId
import com.fpf.smartscansdk.core.ml.models.ResourceOnnxLoader
import com.fpf.smartscansdk.core.ml.models.TensorData
import com.fpf.smartscansdk.core.processors.BatchProcessor
import com.fpf.smartscansdk.core.utils.centerCrop
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.FloatBuffer


class FaceEmbedder(
    resources: Resources,
    modelSource: ModelSource,
) : ImageEmbeddingProvider {
    private val model: OnnxModel = when(modelSource){
        is FilePath -> OnnxModel(FileOnnxLoader(modelSource.path))
        is ResourceId -> OnnxModel(ResourceOnnxLoader(resources, modelSource.resId))
    }

    companion object {
        private const val TAG = "FaceEmbedder"
        const val DIM_BATCH_SIZE = 1
        const val DIM_PIXEL_SIZE = 3
        const val INCEPTION_IMAGE_SIZE_X = 160
        const val INCEPTION_IMAGE_SIZE_Y = 160
    }

    override val embeddingDim: Int = 512
    private var closed = false

    suspend fun initialize() = model.loadModel()

    fun isInitialized() = model.isLoaded()

    override suspend fun embed(data: Bitmap): FloatArray = withContext(Dispatchers.Default) {
        if (!isInitialized()) throw IllegalStateException("Model not initialized")

        val imgData = preProcess(data)
        val inputShape = longArrayOf(DIM_BATCH_SIZE.toLong(), DIM_PIXEL_SIZE.toLong(), INCEPTION_IMAGE_SIZE_X.toLong(), INCEPTION_IMAGE_SIZE_Y.toLong())
        val inputName = model.getInputNames()?.firstOrNull() ?: throw IllegalStateException("Model inputs not available")
        val output = model.run(mapOf(inputName to TensorData.FloatBufferTensor(imgData, inputShape)))
        (output.values.first() as Array<FloatArray>)[0]
    }

    suspend fun embedBatch(context: Context, bitmaps: List<Bitmap>): List<FloatArray> {
        val allEmbeddings = mutableListOf<FloatArray>()

        val processor = object : BatchProcessor<Bitmap, FloatArray>(application = context.applicationContext as Application) {
            override suspend fun onProcess(context: Context, item: Bitmap): FloatArray {
                return embed(item)
            }
            override suspend fun onBatchComplete(context: Context, batch: List<FloatArray>) {
                allEmbeddings.addAll(batch)
            }
        }

        processor.run(bitmaps)
        return allEmbeddings
    }

    private fun preProcess(bitmap: Bitmap): FloatBuffer {
        val centredBitmap = centerCrop(bitmap, INCEPTION_IMAGE_SIZE_X)

        val imgData = FloatBuffer.allocate(
            DIM_BATCH_SIZE
                    * DIM_PIXEL_SIZE
                    * INCEPTION_IMAGE_SIZE_X
                    * INCEPTION_IMAGE_SIZE_Y
        )
        imgData.rewind()
        val stride = INCEPTION_IMAGE_SIZE_X * INCEPTION_IMAGE_SIZE_Y
        val bmpData = IntArray(stride)
        centredBitmap.getPixels(bmpData, 0, centredBitmap.width, 0, 0, centredBitmap.width, centredBitmap.height)
        for (i in 0..INCEPTION_IMAGE_SIZE_X - 1) {
            for (j in 0..INCEPTION_IMAGE_SIZE_Y - 1) {
                val idx = INCEPTION_IMAGE_SIZE_Y * i + j
                val pixelValue = bmpData[idx]
                imgData.put(idx, (((pixelValue shr 16 and 0xFF) / 255f - 0.485f) / 0.229f))
                imgData.put(idx + stride, (((pixelValue shr 8 and 0xFF) / 255f - 0.456f) / 0.224f))
                imgData.put(idx + stride * 2, (((pixelValue and 0xFF) / 255f - 0.406f) / 0.225f))
            }
        }

        imgData.rewind()
        return imgData
    }

    override fun closeSession() {
        if (closed) return
        closed = true
        (model as? AutoCloseable)?.close()
    }
}
