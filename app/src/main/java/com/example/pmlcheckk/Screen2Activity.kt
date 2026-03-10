package com.example.pmlcheckk

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Screen2Activity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_screen2)

        // 🚩 จุดที่แก้: ใช้ LinearLayout แทน Button เพื่อให้ไม่แครช
        val btnLoadList = findViewById<LinearLayout>(R.id.btnLoadList)
        val btnSelectAddress = findViewById<LinearLayout>(R.id.btnSelectAddress)
        val txtUpdated = findViewById<TextView>(R.id.txtUpdated)

        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "pml_db"
        ).build()

        btnLoadList.setOnClickListener {
            Toast.makeText(this, "Updating Data...", Toast.LENGTH_SHORT).show()

            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    // 1. โหลดข้อมูลจริงจาก SharePoint
                    val cloudData = RetrofitInstance.api.getInventoryList()

                    // 2. จัดการ Database
                    db.inventoryDao().clearAll()
                    val processedData = cloudData.map { item ->
                        val group = if (item.fullAddr.length >= 3) item.fullAddr.take(3) else item.fullAddr
                        item.copy(addrGroup = group)
                    }
                    db.inventoryDao().insertAll(processedData)

                    // 🚩 3. Logic UI เดิมที่พี่ต้องการ:
                    withContext(Dispatchers.Main) {
                        btnLoadList.visibility = View.GONE     // ปุ่มโหลดหายไป
                        txtUpdated.visibility = View.VISIBLE   // คำว่า Updated โผล่มา
                        btnSelectAddress.visibility = View.VISIBLE // ปุ่มไปต่อโผล่มา
                        Toast.makeText(this@Screen2Activity, "Updated Successfully!", Toast.LENGTH_SHORT).show()
                    }

                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@Screen2Activity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        btnSelectAddress.setOnClickListener {
            val intent = Intent(this, Screen3Activity::class.java)
            startActivity(intent)
        }
    }
}