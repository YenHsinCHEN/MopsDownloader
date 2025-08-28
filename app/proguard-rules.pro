# Add project specific ProGuard/R8 rules here.
# For more details, see http://developer.android.com/guide/developing/tools/proguard.html

# ------------------- General Rules for Kotlin -------------------
# Keep metadata for reflection, which some libraries might use.
-keep,allowobfuscation,allowshrinking class kotlin.Metadata

# Keep suspend functions for Coroutines to work correctly after obfuscation.
-keepclassmembers class **$* {
    public static final java.lang.Object invokeSuspend(java.lang.Object,java.lang.Object);
}


# ------------------- AndroidX WorkManager (Important!) -------------------
# Keep the default constructor for Workers, otherwise WorkManager can't instantiate it at runtime.
-keepclassmembers public class * extends androidx.work.Worker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}
-keep public class * extends androidx.work.CoroutineWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}


# ------------------- Retrofit & OkHttp (Comprehensive Rules) -------------------
# Keep annotations used by Retrofit for API definitions.
-keep @retrofit2.http.* class * {*;}
-keep class retrofit2.Call
-keep class retrofit2.Callback
-keep class retrofit2.Response

# Keep all classes in the retrofit2 package and their members.
-keep class retrofit2.** { *; }

# Keep all classes in the okhttp3 package and their members.
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

# Prevent warnings from Retrofit/OkHttp about optional dependencies that we don't use.
-dontwarn com.fasterxml.jackson.databind.**
-dontwarn javax.annotation.**
-dontwarn org.conscrypt.**
-dontwarn okio.**
-dontwarn retrofit2.Platform$Java8


# ------------------- Gson (Comprehensive Rules) -------------------
# Keep our specific data classes and their members that are used for JSON serialization.
# This is the most direct and safest way to protect them.
-keep public class com.mops.mopsdownloader.worker.DownloadTask { *; }
-keep public class com.mops.mopsdownloader.worker.ReportType { *; }

# Keep GSON specific annotations.
-keep @com.google.gson.annotations.SerializedName class *
-keep @com.google.gson.annotations.Expose class *

# For generic types, Gson needs to keep the signatures. This is a good general rule.
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
    @com.google.gson.annotations.Expose <fields>;
}

# Keep Gson's TypeAdapterFactory to prevent issues.
-keep class * extends com.google.gson.TypeAdapter


# ------------------- Jsoup (Recommended Rule) -------------------
# Jsoup uses reflection internally, so we need to prevent obfuscation of its core classes.
-keep class org.jsoup.** { *; }
-dontwarn org.jsoup.**