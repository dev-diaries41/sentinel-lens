package com.fpf.sentinellens.lib.ml

import android.app.Application
import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import com.fpf.sentinellens.lib.centerCrop
import com.fpf.sentinellens.lib.preProcess
import com.fpf.smartscansdk.core.ml.embeddings.ImageEmbeddingProvider
import com.fpf.smartscansdk.core.ml.models.FileOnnxLoader
import com.fpf.smartscansdk.core.ml.models.FilePath
import com.fpf.smartscansdk.core.ml.models.ModelSource
import com.fpf.smartscansdk.core.ml.models.OnnxModel
import com.fpf.smartscansdk.core.ml.models.ResourceId
import com.fpf.smartscansdk.core.ml.models.ResourceOnnxLoader
import com.fpf.smartscansdk.core.ml.models.TensorData
import com.fpf.smartscansdk.core.processors.BatchProcessor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FaceComparisonHelper(
    resources: Resources,
    modelSource: ModelSource,
) : ImageEmbeddingProvider {
    private val model: OnnxModel = when(modelSource){
        is FilePath -> OnnxModel(FileOnnxLoader(modelSource.path))
        is ResourceId -> OnnxModel(ResourceOnnxLoader(resources, modelSource.resId))
    }

    companion object {
        private const val TAG = "FaceComparisonHelper"
    }

    override val embeddingDim: Int = 512
    private var closed = false

    suspend fun initialize() = model.loadModel()

    fun isInitialized() = model.isLoaded()

    override suspend fun embed(bitmap: Bitmap): FloatArray = withContext(Dispatchers.Default) {
        if (!isInitialized()) throw IllegalStateException("Model not initialized")

        val processedBitmap = centerCrop(bitmap, 160)
        val inputShape = longArrayOf(1, 3, 160, 160)
        val imgData = preProcess(processedBitmap)

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

    override fun closeSession() {
        if (closed) return
        closed = true
        (model as? AutoCloseable)?.close()
    }
}
