package com.example.pmlcheckk

import retrofit2.http.GET

interface ApiService {
    // 🚩 เอารหัส 7c6a22758c17b1961b97 มาใส่ตรงนี้ครับ
    @GET("7c6a22758c17b1961b97")
    suspend fun getInventoryList(): List<InventoryItem>
}