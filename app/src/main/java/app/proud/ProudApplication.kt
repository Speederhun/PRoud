package app.proud

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import java.io.File

class ProudApplication : Application(), ImageLoaderFactory {
    lateinit var store: Store
        private set

    override fun onCreate() {
        super.onCreate()
        ExerciseRepo.load(this)
        store = Store(File(filesDir, "proud.json"))
    }

    override fun newImageLoader(): ImageLoader =
        ImageLoader.Builder(this)
            .respectCacheHeaders(false)
            .crossfade(true)
            .diskCache {
                DiskCache.Builder()
                    .directory(File(cacheDir, "exercise_images"))
                    .maxSizeBytes(300L * 1024L * 1024L)
                    .build()
            }
            .build()
}
