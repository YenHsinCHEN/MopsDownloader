# Add project specific ProGuard/R8 rules here.

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


# 【【【【【 以下是本次最核心的、釜底抽薪的修改 】】】】】
# ------------------- Complete Exemption for Networking Libraries -------------------
# Completely exclude these libraries from any shrinking, optimization, or obfuscation.
# This is a last resort to solve deep, environment-specific issues where R8's
# processing interacts negatively with the system's networking or security layers.

-keep class retrofit2.** { *; }
-keep interface retrofit2.** { *; }
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-keep class okio.** { *; }
-keep interface okio.** { *; }
-keep class com.google.gson.** { *; }
-keep class org.jsoup.** { *; }

# Also, prevent any warnings from these libraries.
-dontwarn retrofit2.**
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn com.google.gson.**
-dontwarn org.jsoup.**
# ------------------- 【【【 修改結束 】】】 -------------------


# We still keep the specific rules for our own data classes as a good practice.
-keep public class com.mops.mopsdownloader.worker.DownloadTask { *; }
-keep public class com.mops.mopsdownloader.worker.ReportType { *; }


# ------------------- AndroidX Core & Lifecycle (Good practice) -------------------
-keep class androidx.lifecycle.SavedStateHandle {*;}
-keep class androidx.lifecycle.ViewModel {*;}