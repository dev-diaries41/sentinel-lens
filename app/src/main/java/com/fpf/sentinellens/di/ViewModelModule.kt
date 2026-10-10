package com.fpf.sentinellens.di

import com.fpf.sentinellens.ui.screens.surveillance.SurveillanceViewModel
import com.fpf.sentinellens.ui.screens.watchlist.WatchlistViewModel
import com.fpf.sentinellens.ui.screens.watchlist.person.AddPersonViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
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
}