plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.mops.mopsdownloader"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.mops.mopsdownloader"
        minSdk = 35
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // ViewModel for Jetpack Compose
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.1")

    // Kotlin Coroutines for asynchronous tasks
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // Retrofit for networking
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    // Retrofit scalar converter to handle plain text/html responses
    implementation("com.squareup.retrofit2:converter-scalars:2.9.0")

    // Jsoup for HTML parsing
    implementation("org.jsoup:jsoup:1.17.2")
    // WorkManager for background tasks
    implementation("androidx.work:work-runtime-ktx:2.9.0")
    // Gson for serializing data
    implementation("com.google.code.gson:gson:2.10.1")
    // For observing LiveData as State in Compose
    implementation("androidx.compose.runtime:runtime-livedata:1.6.8") // 請使用最新的版本
    // For DocumentFile API to handle storage URIs
    implementation("androidx.documentfile:documentfile:1.0.1")
    // DataStore for saving preferences
    implementation("androidx.datastore:datastore-preferences:1.1.1")
    // Navigation for Jetpack Compose
    // For Transformations.switchMap
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.1") // 請使用最新的版本
    implementation("androidx.navigation:navigation-compose:2.7.7")
}