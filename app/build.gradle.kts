plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.hideseekmapapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.hideseekmapapp"
        minSdk = 26
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
    configurations {
        implementation.get().exclude(mapOf("group" to "org.jetbrains", "module" to "annotations"))
        implementation.get().exclude(mapOf("group" to "xmlpull", "module" to "xmlpull"))
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

    // яндекс карты
    implementation(libs.maps.mobile)

    // полигоны вороного
    implementation(libs.voronoikotlin)

    // запросы в overpass
    implementation("de.westnordost:osmapi-overpass:3.0")

    // булевы операции с мультиполигонами
    implementation("org.locationtech.jts:jts-core:1.20.0")

    // адаптация некоторых функций turf (JAVA)
    implementation("com.mapbox.mapboxsdk:mapbox-sdk-geojson:7.7.0")
    implementation("com.mapbox.mapboxsdk:mapbox-sdk-services:7.7.0")
    implementation("com.mapbox.mapboxsdk:mapbox-sdk-turf:7.7.0")
    implementation("com.mapbox.mapboxsdk:mapbox-sdk-core:7.7.0")
}