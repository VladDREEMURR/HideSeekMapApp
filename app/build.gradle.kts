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

    // нужные библиотеки
    implementation(libs.maps.mobile) // яндекс карты

    implementation(libs.voronoikotlin) // полигоны вороного

    implementation(libs.overpasser) // запросы в overpass (JAVA)

    // адаптация некоторых функций turf (JAVA)
    implementation("com.mapbox.mapboxsdk:mapbox-sdk-geojson:7.7.0")
    implementation("com.mapbox.mapboxsdk:mapbox-sdk-services:7.7.0")
    implementation("com.mapbox.mapboxsdk:mapbox-sdk-turf:7.7.0")
    implementation("com.mapbox.mapboxsdk:mapbox-sdk-core:7.7.0")
    implementation("androidx.annotation:annotation:1.0.0")

    implementation("com.menecats:polybool-java:1.0.1") // булевые операции с областями (JAVA)
}