package com.fpf.sentinellens

import android.app.Application
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.disk.DiskCache
import coil3.disk.directory
import coil3.memory.MemoryCache
import coil3.request.crossfade
import coil3.video.VideoFrameDecoder
import com.fpf.sentinellens.di.dataModule
import com.fpf.sentinellens.di.modelsModule
import com.fpf.sentinellens.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin

class App : Application() {

    companion object {
        private const val TAG = "App"

        fun resetKoin(app: Application){
            stopKoin()

            startKoin {
                androidContext(app)
                modules(
                    dataModule,
                    viewModelModule,
                    modelsModule,
                )
            }
        }
    }
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@App)
            modules(
                dataModule,
                viewModelModule,
                modelsModule,
            )
        }

        SingletonImageLoader.setSafe {
            ImageLoader.Builder(this).components {
                add(VideoFrameDecoder.Factory())
            }
                .crossfade(true)
                .memoryCache { MemoryCache.Builder().maxSizePercent(this, 0.25).build() }
                .diskCache { DiskCache.Builder().directory(cacheDir.resolve("image_cache")).maxSizePercent(0.05).build() }
                .build()
        }

    }


}