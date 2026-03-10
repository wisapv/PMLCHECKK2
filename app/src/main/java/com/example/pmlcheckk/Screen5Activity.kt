package com.example.pmlcheckk

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class Screen5Activity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_screen5)

        // ผูกตัวแปรหน้าจอ
        val txtPartNo = findViewById<TextView>(R.id.txtPartNo)
        val txtPartName = findViewById<TextView>(R.id.txtPartName)
        val txtSupplier = findViewById<TextView>(R.id.txtSupplier)

        val edtBox = findViewById<EditText>(R.id.edtBox)
        val edtPcs = findViewById<EditText>(R.id.edtPcs)
        val edtSeq = findViewById<EditText>(R.id.edtSeq)
        val edtOrder = findViewById<EditText>(R.id.edtOrder)
        val btnSave = findViewById<Button>(R.id.btnSave)

        // รับข้อมูลที่ส่งมาจากหน้า Screen 4 โชว์อัตโนมัติ
        val partNo = intent.getStringExtra("PART_NO") ?: "-"
        val partName = intent.getStringExtra("PART_NAME") ?: "-"
        val supplier = intent.getStringExtra("SUPPLIER") ?: "-"

        txtPartNo.text = "Part No: $partNo"
        txtPartName.text = "Name: $partName"
        txtSupplier.text = "Supplier: $supplier"

        // กดปุ่ม Save
        btnSave.setOnClickListener {
            val box = edtBox.text.toString()
            val pcs = edtPcs.text.toString()
            val seq = edtSeq.text.toString()
            val order = edtOrder.text.toString() // อันนี้ไม่ต้องบังคับกรอก

            // บังคับกรอก 3 ช่องนี้
            if (box.isEmpty() || pcs.isEmpty() || seq.isEmpty()) {
                Toast.makeText(this, "Please fill in Box, Pcs and Seq", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // (TODO ในอนาคต: เขียนโค้ดบันทึกค่าลง Database)

            Toast.makeText(this, "Saved Successfully!", Toast.LENGTH_SHORT).show()
            finish() // ปิดหน้าต่างนี้ เด้งกลับไปหน้า Screen 4 ให้ลุยไอเท็มต่อไป
        }
    }
}