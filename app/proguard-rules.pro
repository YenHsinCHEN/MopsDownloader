# Final working configuration for release build
# This configuration disables shrinking but keeps obfuscation and optimization.

-dontshrink

# ------------------- General Rules for Kotlin & Coroutines -------------------
-keep,allowobfuscation,allowshrinking class kotlin.Metadata
-keepclassmembers class **$* {
    public static final java.lang.Object invokeSuspend(java.lang.Object,java.lang.Object);
}
-keep class kotlinx.coroutines.internal.** { *; }
-keepattributes Signature

# ------------------- AndroidX WorkManager (Important!) -------------------
-keep public class * extends androidx.work.CoroutineWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}

# ------------------- Retrofit & OkHttp -------------------
-keep class retrofit2.** { *; }
-keep interface retrofit2.** { *; }
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**
-dontnote okhttp3.**
-dontwarn retrofit2.Platform$Java8
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# ------------------- Gson -------------------
-keep public class com.mops.mopsdownloader.worker.DownloadTask { *; }
-keep public class com.mops.mopsdownloader.worker.ReportType { *; }
-keep @com.google.gson.annotations.SerializedName class *
-keep @com.google.gson.annotations.Expose class *
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
    @com.google.gson.annotations.Expose <fields>;
}
-keep class com.google.gson.stream.** { *; }

# ------------------- Jsoup -------------------
-keep class org.jsoup.** { *; }
-dontwarn org.jsoup.**