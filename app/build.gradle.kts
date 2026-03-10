plugins {
    alias(libs.plugins.android.application)
    // ❌ ลบบรรทัด alias(libs.plugins.kotlin.android) ออกไปเลยครับ
    alias(libs.plugins.ksp) // ✅ เก็บอันนี้ไว้สำหรับ Room Database
}

android {
    namespace = "com.example.pmlcheckk"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.pmlcheckk"
        minSdk = 24
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
}

dependencies {
    val room_version = "2.6.1"
    implementation("androidx.room:room-runtime:$room_version")
    implementation("androidx.room:room-ktx:$room_version")
    ksp("androidx.room:room-compiler:$room_version")
    // Retrofit สำหรับดึงข้อมูลจาก URL
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    // GSON สำหรับแปลง JSON เป็น Object ในแอปอัตโนมัติ
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
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