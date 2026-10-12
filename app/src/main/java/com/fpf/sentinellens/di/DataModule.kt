package com.fpf.sentinellens.di

import android.content.Context
import com.fpf.sentinellens.constants.PrefsNames
import com.fpf.sentinellens.data.FaceDatabase
import com.fpf.sentinellens.data.faces.FacesRepository
import com.fpf.sentinellens.data.logs.LogRepository
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val dataModule = module {
    single { FaceDatabase.getDatabase(androidApplication()) }
    single { get<FaceDatabase>().faceDao() }
    single { get<FaceDatabase>().logDao() }

    single { FacesRepository(get()) }
    single { LogRepository(get()) }
    single { androidContext().getSharedPreferences(PrefsNames.APP_PREFS, Context.MODE_PRIVATE) }

}