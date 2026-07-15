# ProGuard rules for PixelHub

# Retrofit
-keepattributes Signature,Exceptions,*Annotation*
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * { @retrofit2.http.* <methods>; }

# Gson
-keep class com.google.gson.** { *; }
-keep class com.pixelhub.app.data.remote.model.** { *; }
-keepclassmembers class * { @com.google.gson.annotations.SerializedName <fields>; }

# OkHttp
-dontwarn okhttp3.**,okio.**

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao class *

# Hilt
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }

# Coil
-dontwarn coil.**

# Compose
-dontwarn androidx.compose.**

# Timber
-dontwarn timber.log.**

# Keep Application class
-keep class com.pixelhub.app.PixelHubApp { *; }

# Keep ViewModel classes
-keep class * extends androidx.lifecycle.ViewModel { *; }

# Keep data classes
-keep class com.pixelhub.app.domain.model.** { *; }
-keep class com.pixelhub.app.data.local.entity.** { *; }
