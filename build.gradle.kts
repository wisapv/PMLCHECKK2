plugins {
    alias(libs.plugins.android.application) apply false
    // ❌ ลบบรรทัด alias(libs.plugins.kotlin.android) apply false ออกครับ
    alias(libs.plugins.ksp) apply false // ✅ เก็บอันนี้ไว้
}