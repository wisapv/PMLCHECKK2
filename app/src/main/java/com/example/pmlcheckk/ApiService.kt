package com.example.pmlcheckk

import retrofit2.http.GET

// ในไฟล์ ApiService.kt
interface ApiService {
    @GET(":u:/t/HOTCALLOVERFLOW/IQBSqpVjv-sfTJegqaHXFAtGAQKF1BNfraUbVZYARBuESwQ?download=1")
    suspend fun getInventoryList(): List<InventoryItem>
}

