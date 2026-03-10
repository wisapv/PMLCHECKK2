plugins {
    alias(libs.plugins.android.application) apply false
    // ลบ alias(libs.plugins.kotlin.android) ออกแล้ว
    alias(libs.plugins.ksp) apply false
}