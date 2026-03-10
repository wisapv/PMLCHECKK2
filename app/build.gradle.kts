plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
}

// โค้ดส่วน android { ... } และ dependencies { ... } ด้านล่าง ปล่อยไว้เหมือนเดิม

// โค้ดส่วน android { ... } และ dependencies { ... } ด้านล่าง ปล่อยไว้เหมือนเดิมได้เลยครับ

android {
    namespace = "com.example.pmlcheckk"
    compileSdk = 35 // Keep at 35 for stability

    defaultConfig {
        applicationId = "com.example.pmlcheckk"
        minSdk = 24
        targetSdk = 35 // targetSdk should match compileSdk
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
    // Note: The extra tasks.withType block was removed as it is redundant
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    // Recommendation: Move these versions to libs.versions.toml
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")

    val retrofitVersion = "2.11.0"
    implementation("com.squareup.retrofit2:retrofit:$retrofitVersion")
    implementation("com.squareup.retrofit2:converter-gson:$retrofitVersion")

    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}