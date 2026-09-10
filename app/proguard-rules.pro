# Neo GPT ProGuard Rules

# Compose
-keep class androidx.compose.** { *; }

# Kotlin Coroutines
-keepclassmembernames class kotlinx.** { volatile <fields>; }

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Moshi
-keepclassmembers class * {
    @com.squareup.moshi.Json <fields>;
}
-keep @com.squareup.moshi.JsonClass class *

# Retrofit
-keepattributes Signature, Exceptions
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# Security
-keep class androidx.security.crypto.** { *; }

# Hilt
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
