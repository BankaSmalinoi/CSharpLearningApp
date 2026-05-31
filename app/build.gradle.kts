plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.c"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.c"
        minSdk = 24
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.drawerlayout)
    implementation(libs.fragment)
    implementation(libs.recyclerview)

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation(libs.gson)

    implementation("androidx.work:work-runtime:2.9.1")

    // Room / SQLite
    implementation("androidx.room:room-runtime:2.6.1")
    annotationProcessor("androidx.room:room-compiler:2.6.1")

    // Lifecycle / MVVM
    implementation("androidx.lifecycle:lifecycle-livedata:2.6.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel:2.6.2")
}