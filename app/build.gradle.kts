plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-kapt")
    id("dagger.hilt.android.plugin")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
}

android {
    compileSdk = 31

    defaultConfig {
        applicationId = "kg.kloop.android.gigaturnip"
        minSdk = 23
        targetSdk = 31
        versionCode = 6
        versionName = "1.0.3"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
//            isMinifyEnabled = true
//            isShrinkResources = true
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
    kotlinOptions {
        jvmTarget = "1.8"
//        useIR = true
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.1.0-beta03"
    }
}

dependencies {
    implementation("com.google.firebase:firebase-crashlytics-ktx:18.2.4")
    implementation("com.google.firebase:firebase-analytics-ktx:20.0.0")

    implementation("androidx.core:core-ktx:1.7.0")
    implementation("androidx.appcompat:appcompat:1.4.0")
    implementation("com.google.android.material:material:1.4.0")
    implementation("androidx.compose.ui:ui:1.1.0-beta03")
    implementation("androidx.compose.material:material:1.1.0-beta03")
    implementation("androidx.compose.ui:ui-tooling:1.1.0-beta03")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.4.0")
    implementation("androidx.activity:activity-compose:1.4.0")
    implementation("androidx.navigation:navigation-compose:2.4.0-beta02")
    implementation("androidx.webkit:webkit:1.4.0")
    testImplementation("junit:junit:4.+")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.1.0-beta03")

    implementation("androidx.compose.material:material-icons-core:1.1.0-beta03")
    implementation("androidx.compose.material:material-icons-extended:1.1.0-beta03")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.4.0")
    implementation("androidx.navigation:navigation-compose:2.4.0-beta02")
    implementation("androidx.constraintlayout:constraintlayout-compose:1.0.0-rc02")

    //Accompanist
    implementation("com.google.accompanist:accompanist-swiperefresh:0.21.2-beta")
    implementation("com.google.accompanist:accompanist-placeholder-material:0.21.2-beta")
    implementation("com.google.accompanist:accompanist-insets-ui:0.21.2-beta")

    //Firebase
    implementation("com.firebaseui:firebase-ui-auth:7.2.0")
    implementation("com.google.firebase:firebase-auth-ktx:21.0.1")

    // Storage
    implementation(platform("com.google.firebase:firebase-bom:28.2.1"))
    implementation("com.google.firebase:firebase-storage-ktx")

    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:5.0.0-alpha.2")
    implementation("com.squareup.okhttp3:logging-interceptor:5.0.0-alpha.2")

    // Timber
    implementation("com.jakewharton.timber:timber:4.7.1")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.5.2")

    // Coroutine Lifecycle Scopes
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.4.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.4.0")

    // LiveData
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.4.0")
    implementation("androidx.compose.runtime:runtime-livedata:1.1.0-beta03")

    //Dagger - Hilt
    implementation("com.google.dagger:hilt-android:2.38.1")
    kapt("com.google.dagger:hilt-android-compiler:2.38.1")
    implementation("androidx.hilt:hilt-lifecycle-viewmodel:1.0.0-alpha03")
    kapt("androidx.hilt:hilt-compiler:1.0.0")
    implementation("androidx.hilt:hilt-navigation-compose:1.0.0-beta01")

    //File compression
    implementation("com.github.AbedElazizShe:LightCompressor:0.9.3")
    implementation("com.googlecode.mp4parser:isoparser:1.1.22")

    //WorkManager
    implementation("androidx.work:work-runtime-ktx:2.7.1")

    //Custom tabs
    implementation("androidx.browser:browser:1.4.0")
}