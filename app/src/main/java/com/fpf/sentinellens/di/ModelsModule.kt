package com.fpf.sentinellens.di

import android.app.Application
import com.fpf.sentinellens.R
import com.fpf.smartscansdk.ml.detectors.FaceDetector
import com.fpf.smartscansdk.ml.embeddings.inception.InceptionResnetFaceEmbedder
import com.fpf.smartscansdk.ml.models.ModelAssetSource
import org.koin.dsl.module

val modelsModule = module {
    single {
        val app = get<Application>()
        FaceDetector(modelSource = ModelAssetSource.Resource(app.resources, R.raw.face_detect))
    }
    single {
        val app = get<Application>()
        InceptionResnetFaceEmbedder(modelSource = ModelAssetSource.Resource(app.resources, R.raw.inception_resnet_v1_quant))
    }
}
