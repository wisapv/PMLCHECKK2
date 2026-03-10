package com.example.pmlcheckk

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
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

        val btnLoadList = findViewById<Button>(R.id.btnLoadList)
        val btnSelectAddress = findViewById<LinearLayout>(R.id.btnSelectAddress)
        val txtUpdated = findViewById<TextView>(R.id.txtUpdated)

        // ✅ 1. เพิ่มการผูกตัวแปรสำหรับปุ่ม Free Zone และข้อความ Select Action จาก XML
        val btnFreeZone = findViewById<LinearLayout>(R.id.btnFreeZone)
        val txtSelectAction = findViewById<TextView>(R.id.txtSelectAction)

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

                    // 3. Logic UI
                    withContext(Dispatchers.Main) {
                        btnLoadList.visibility = View.GONE     // ซ่อนปุ่มโหลด
                        txtUpdated.visibility = View.VISIBLE   // แสดงคำว่า Updated Successfully

                        // ✅ 2. แสดงข้อความหัวข้อและปุ่มทั้ง 2 ปุ่มขึ้นมาพร้อมกัน
                        txtSelectAction.visibility = View.VISIBLE
                        btnSelectAddress.visibility = View.VISIBLE
                        btnFreeZone.visibility = View.VISIBLE

                        Toast.makeText(this@Screen2Activity, "Updated Successfully!", Toast.LENGTH_SHORT).show()
                    }

                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@Screen2Activity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        // เมื่อกดปุ่ม Select Address ให้ไปหน้า Screen3
        btnSelectAddress.setOnClickListener {
            val intent = Intent(this, Screen3Activity::class.java)
            startActivity(intent)
        }

        // ✅ 3. เตรียม Listener สำหรับปุ่ม Free Zone (เปลี่ยนหน้าหรือทำคำสั่งอื่นๆ ใส่เพิ่มได้เลย)
        btnFreeZone.setOnClickListener {
            Toast.makeText(this, "Free Zone Selected", Toast.LENGTH_SHORT).show()
            // ตัวอย่าง: ถ้ามีหน้าสำหรับ Free Zone สามารถสั่ง intent ไปได้เลยเหมือนข้างบน
            // val intent = Intent(this, ScreenFreeZoneActivity::class.java)
            // startActivity(intent)
        }
    }
}