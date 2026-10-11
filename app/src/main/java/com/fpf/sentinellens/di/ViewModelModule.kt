package com.fpf.sentinellens.di

import com.fpf.sentinellens.MainViewModel
import com.fpf.sentinellens.ui.screens.log.DetectionLogViewModel
import com.fpf.sentinellens.ui.screens.settings.SettingsViewModel
import com.fpf.sentinellens.ui.screens.surveillance.SurveillanceViewModel
import com.fpf.sentinellens.ui.screens.test.TestFaceIdViewModel
import com.fpf.sentinellens.ui.screens.watchlist.WatchlistViewModel
import com.fpf.sentinellens.ui.screens.watchlist.person.AddPersonViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel {
        MainViewModel(
            application = get(),
            sharedPrefs = get()
        )
    }
    viewModel {
        AddPersonViewModel(
            application = get(),
            repository = get(),
            faceComparer = get(),
            faceDetector = get()
        )
    }
    viewModel {
        SurveillanceViewModel(
            application = get(),
            repository = get()
        )
    }
    viewModel {
        WatchlistViewModel(
            application = get(),
            repository = get(),
        )
    }
    viewModel {
        DetectionLogViewModel(
            application = get(),
            detectionLogRepository = get(),
        )
    }
    viewModel {
        TestFaceIdViewModel(
            application = get(),
            repository = get(),
            faceComparer = get(),
            faceDetector = get()
        )
    }
    viewModel {
        SettingsViewModel(
            application = get(),
            sharedPrefs = get()
        )
    }
}