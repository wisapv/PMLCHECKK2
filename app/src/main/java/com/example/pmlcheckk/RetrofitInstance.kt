package com.example.pmlcheckk

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    // 🚩 เปลี่ยน "https://your-onedrive-base-url.com/" เป็น URL หลักของ OneDrive คุณ
    private const val BASE_URL = "https://toyotaasia.sharepoint.com/"

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}