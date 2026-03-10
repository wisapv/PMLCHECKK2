package com.example.pmlcheckk

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    // 🚩 เปลี่ยนเป็นโดเมนหลัก (ต้องมีเครื่องหมาย / ปิดท้ายเสมอ)
    private const val BASE_URL = "https://api.npoint.io/"

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}