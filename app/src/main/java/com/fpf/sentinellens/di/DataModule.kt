package com.fpf.sentinellens.di

import com.fpf.sentinellens.data.faces.FaceDatabase
import com.fpf.sentinellens.data.faces.FacesRepository
import com.fpf.sentinellens.data.logs.DetectionLogDatabase
import com.fpf.sentinellens.data.logs.DetectionLogRepository
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

val dataModule = module {
    single { FaceDatabase.getDatabase(androidApplication()) }
    single { DetectionLogDatabase.getDatabase(androidApplication()) }

    single { get<FaceDatabase>().faceDao() }
    single { get<DetectionLogDatabase>().detectionLogsDao() }

    single { FacesRepository(get()) }
    single { DetectionLogRepository(get()) }
}