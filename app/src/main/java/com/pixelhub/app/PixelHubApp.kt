package com.pixelhub.app

import android.app.Application
import android.util.Log
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import dagger.hilt.android.HiltAndroidApp
import okhttp3.OkHttpClient
import timber.log.Timber
import java.io.File
import java.util.concurrent.TimeUnit

@HiltAndroidApp
class PixelHubApp : Application(), ImageLoaderFactory {

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
            Timber.plant(FileLogTree(this))
        }
        Timber.d("PixelHubApp onCreate - version ${BuildConfig.VERSION_NAME}")
    }

    override fun newImageLoader(): ImageLoader {
        Timber.d("Creating custom Coil ImageLoader")
        return ImageLoader.Builder(this)
            .okHttpClient {
                OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .followRedirects(true)
                    .followSslRedirects(true)
                    .addInterceptor { chain ->
                        val request = chain.request()
                        val builder = request.newBuilder()
                            .header("User-Agent", "Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36")
                        if (request.url.host.contains("pixiv")) {
                            builder.header("Referer", "https://www.pixiv.net/")
                        }
                        chain.proceed(builder.build())
                    }
                    .build()
            }
            .memoryCachePolicy(CachePolicy.ENABLED)
            .memoryCache { MemoryCache.Builder(this).maxSizePercent(0.25).build() }
            .diskCachePolicy(CachePolicy.ENABLED)
            .diskCache { DiskCache.Builder().directory(cacheDir.resolve("image_cache")).maxSizePercent(0.02).build() }
            .crossfade(true)
            .respectCacheHeaders(false)
            .build()
    }
}

class FileLogTree(private val app: Application) : Timber.Tree() {
    private val logDir = File(app.filesDir, "logs").apply { mkdirs() }
    private val maxFiles = 5

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (priority < Log.DEBUG) return
        val ts = java.text.SimpleDateFormat("HH:mm:ss.SSS", java.util.Locale.getDefault()).format(java.util.Date())
        val level = when (priority) { Log.DEBUG -> "D"; Log.INFO -> "I"; Log.WARN -> "W"; Log.ERROR -> "E"; else -> "V" }
        val sb = StringBuilder()
        sb.appendLine("$ts $level/${tag ?: "?"}: $message")
        if (t != null) {
            sb.appendLine("  ${t.javaClass.simpleName}: ${t.message}")
            t.stackTrace.take(5).forEach { sb.appendLine("    at $it") }
        }
        try {
            val logFile = File(logDir, "log_${java.text.SimpleDateFormat("yyyyMMdd", java.util.Locale.getDefault()).format(java.util.Date())}.txt")
            logFile.appendText(sb.toString())
            logDir.listFiles()?.sortedByDescending { it.lastModified() }?.drop(maxFiles)?.forEach { it.delete() }
        } catch (_: Exception) {}
    }
}
