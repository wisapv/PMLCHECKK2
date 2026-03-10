package com.example.pmlcheckk // <--- บรรทัดนี้ใช้ชื่อ package ของคุณเองนะครับ

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // บรรทัดนี้คือการดึงหน้าตาจากไฟล์ XML (activity_main) มาแสดง
        setContentView(R.layout.activity_main)

        // 1. เชื่อมต่อตัวแปรในโค้ด เข้ากับปุ่ม (LinearLayout) ที่เราสร้างไว้ใน XML
        val btnRunOut = findViewById<LinearLayout>(R.id.btnRunOut)
        val btnCheckStock = findViewById<LinearLayout>(R.id.btnCheckStock)

        // 2. ใส่คำสั่งดักจับการกดปุ่ม Run Out
        btnRunOut.setOnClickListener {
            // Intent คือคำสั่งสำหรับย้ายหน้าจอ (จากหน้าปัจจุบัน ไป Screen2Activity)
            val intent = Intent(this, Screen2Activity::class.java)

            // ส่งข้อมูลแนบไปด้วยว่า User เลือกโหมดไหน เผื่อเอาไปใช้แสดงผลในหน้า 2
            intent.putExtra("SELECTED_MODE", "RUN_OUT")

            // เริ่มต้นเปิดหน้าใหม่
            startActivity(intent)
        }

        // 3. ใส่คำสั่งดักจับการกดปุ่ม Check Stock
        btnCheckStock.setOnClickListener {
            val intent = Intent(this, Screen2Activity::class.java)
            intent.putExtra("SELECTED_MODE", "CHECK_STOCK")
            startActivity(intent)
        }
    }
}