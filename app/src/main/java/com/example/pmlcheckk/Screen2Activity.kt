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

    // 1. ประกาศตัวแปร View ไว้ด้านบน เพื่อให้เรียกใช้งานได้ในทุกฟังก์ชัน
    private lateinit var db: AppDatabase
    private lateinit var btnLoadList: Button
    private lateinit var btnSelectAddress: LinearLayout
    private lateinit var txtUpdated: TextView
    private lateinit var btnFreeZone: LinearLayout
    private lateinit var txtSelectAction: TextView
    private lateinit var btnExport: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_screen2)

        // ผูกตัวแปรกับหน้า UI
        btnLoadList = findViewById(R.id.btnLoadList)
        btnSelectAddress = findViewById(R.id.btnSelectAddress)
        txtUpdated = findViewById(R.id.txtUpdated)
        btnFreeZone = findViewById(R.id.btnFreeZone)
        txtSelectAction = findViewById(R.id.txtSelectAction)
        btnExport = findViewById(R.id.btnExport)

        db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "pml_db"
        ).build()

        btnLoadList.setOnClickListener {
            Toast.makeText(this, "Updating Data...", Toast.LENGTH_SHORT).show()

            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val cloudData = RetrofitInstance.api.getInventoryList()

                    db.inventoryDao().clearAll()
                    val processedData = cloudData.map { item ->
                        val group = if (item.fullAddr.length >= 3) item.fullAddr.take(3) else item.fullAddr
                        item.copy(addrGroup = group)
                    }
                    db.inventoryDao().insertAll(processedData)

                    withContext(Dispatchers.Main) {
                        // เปลี่ยนข้อความปุ่มเพื่อเตือนว่า ถ้ากดอีกคือการ "โหลดข้อมูลใหม่ทับของเดิม"
                        btnLoadList.text = "Reload List from Cloud"
                        txtUpdated.visibility = View.VISIBLE

                        txtSelectAction.visibility = View.VISIBLE
                        btnSelectAddress.visibility = View.VISIBLE
                        btnFreeZone.visibility = View.VISIBLE

                        // เมื่อเพิ่งโหลดรายการใหม่ ข้อมูลเซฟยังเป็น 0 จึงให้ซ่อนปุ่ม Export ไว้ก่อน
                        btnExport.visibility = View.GONE

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

        btnFreeZone.setOnClickListener {
            Toast.makeText(this, "Free Zone Selected", Toast.LENGTH_SHORT).show()
            // val intent = Intent(this, ScreenFreeZoneActivity::class.java)
            // startActivity(intent)
        }

        btnExport.setOnClickListener {
            Toast.makeText(this, "เตรียมส่งออกข้อมูล...", Toast.LENGTH_SHORT).show()
        }
    }

    // =======================================================
    // เช็คสถานะข้อมูลทุกครั้งที่หน้าจอ Screen 2 แสดงขึ้นมา
    // =======================================================
    override fun onResume() {
        super.onResume()
        checkDatabaseState()
    }

    private fun checkDatabaseState() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // 1. เช็คว่ามีรายการข้อมูลใดๆ ในระบบอยู่แล้วหรือไม่ (เพื่อดูว่าเคย Load List มาหรือยัง)
                val hasData = db.inventoryDao().getAllAddrGroups().isNotEmpty()

                // 2. นับจำนวน KBN ที่กรอก Box แล้ว (เพื่อดูว่าควรโชว์ปุ่ม Export ไหม)
                val completedCount = db.inventoryDao().getCompletedItemsCount()

                withContext(Dispatchers.Main) {
                    if (hasData) {
                        // **กรณีมีข้อมูลเก่าอยู่ในเครื่อง** ให้โชว์เมนูต่างๆ ขึ้นมามารอเลย
                        btnLoadList.text = "Reload List from Cloud"
                        txtUpdated.visibility = View.VISIBLE
                        txtSelectAction.visibility = View.VISIBLE
                        btnSelectAddress.visibility = View.VISIBLE
                        btnFreeZone.visibility = View.VISIBLE

                        // โชว์ปุ่ม Export เฉพาะตอนที่ข้อมูลกรอกไปแล้ว 1 ตัวขึ้นไป
                        if (completedCount > 0) {
                            btnExport.visibility = View.VISIBLE
                        } else {
                            btnExport.visibility = View.GONE
                        }

                    } else {
                        // **กรณียังไม่มีข้อมูลเลย (แอปเพิ่งลงใหม่ หรือกดล้างข้อมูล)**
                        btnLoadList.text = "Load List from Cloud"
                        txtUpdated.visibility = View.GONE
                        txtSelectAction.visibility = View.GONE
                        btnSelectAddress.visibility = View.GONE
                        btnFreeZone.visibility = View.GONE
                        btnExport.visibility = View.GONE
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}