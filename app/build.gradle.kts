plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
}

// โค้ดส่วน android { ... } และ dependencies { ... } ด้านล่าง ปล่อยไว้เหมือนเดิม

// โค้ดส่วน android { ... } และ dependencies { ... } ด้านล่าง ปล่อยไว้เหมือนเดิมได้เลยครับ

android {
    namespace = "com.example.pmlcheckk"
    compileSdk = 36 // ✅ เปลี่ยนตรงนี้จาก 35 เป็น 36

    defaultConfig {
        applicationId = "com.example.pmlcheckk"
        minSdk = 24
        targetSdk = 35 // targetSdk ปล่อยเป็น 35 ไว้ก่อนได้ครับ ไม่มีปัญหา
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // ... โค้ดส่วนอื่นๆ คงไว้เหมือนเดิม ...

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
}

dependencies {
    val room_version = "2.6.1"
    implementation("androidx.room:room-runtime:$room_version")
    implementation("androidx.room:room-ktx:$room_version")
    ksp("androidx.room:room-compiler:$room_version")

    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")

    // Lifecycle Scope
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

ksp {
    arg("room.generateKotlin", "true")
}