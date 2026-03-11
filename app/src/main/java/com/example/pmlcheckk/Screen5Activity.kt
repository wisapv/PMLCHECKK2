package com.example.pmlcheckk

import android.app.Activity
import android.content.Intent
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

        // ผูกตัวแปร Text
        val txtSupplier = findViewById<TextView>(R.id.txtSupplier)
        val txtPartNo = findViewById<TextView>(R.id.txtPartNo)
        val txtPartName = findViewById<TextView>(R.id.txtPartName)
        val txtKbn = findViewById<TextView>(R.id.txtKbn)
        val txtFullAddr = findViewById<TextView>(R.id.txtFullAddr)
        val txtQty = findViewById<TextView>(R.id.txtQty)

        // ผูกตัวแปร Input และ Button
        val edtBox = findViewById<EditText>(R.id.edtBox)
        val edtPcs = findViewById<EditText>(R.id.edtPcs)
        val edtSeq = findViewById<EditText>(R.id.edtSeq)
        val edtOrder = findViewById<EditText>(R.id.edtOrder)

        val btnNotFound = findViewById<Button>(R.id.btnNotFound)
        val btnBack = findViewById<Button>(R.id.btnBack)
        val btnSave = findViewById<Button>(R.id.btnSave)

        // รับข้อมูลจาก Intent
        val supplier = intent.getStringExtra("SUPPLIER") ?: "-"
        val partNo = intent.getStringExtra("PART_NO") ?: "-"
        val partName = intent.getStringExtra("PART_NAME") ?: "-"
        val kbn = intent.getStringExtra("KBN") ?: "-"
        val fullAddr = intent.getStringExtra("FULL_ADDR") ?: "-"
        val qty = intent.getIntExtra("QTY", 0)

        // โชว์ข้อมูล
        txtSupplier.text = supplier
        txtPartNo.text = partNo
        txtPartName.text = partName
        txtKbn.text = kbn
        txtFullAddr.text = fullAddr
        txtQty.text = qty.toString()

        // 1. กดปุ่ม Not Found -> เติม 0 อัตโนมัติ
        btnNotFound.setOnClickListener {
            edtBox.setText("0")
            edtPcs.setText("0")
            edtSeq.setText("0")
            edtOrder.setText("0")
        }

        // 2. กดปุ่ม Back -> กลับไปหน้า 4 แบบไม่มีอะไรเกิดขึ้น
        btnBack.setOnClickListener {
            finish()
        }

        // 3. กดปุ่ม Save -> เซฟแล้วส่งค่ายืนยันกลับไปหน้า 4
        btnSave.setOnClickListener {
            val box = edtBox.text.toString().trim()
            val pcs = edtPcs.text.toString().trim()
            val seq = edtSeq.text.toString().trim()

            if (box.isEmpty() || pcs.isEmpty() || seq.isEmpty()) {
                Toast.makeText(this, "กรุณากรอก Box, Pcs และ Seq ให้ครบ", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // TODO: โค้ดบันทึกลง Database (Room) จะใส่ตรงนี้ในอนาคต

            // สร้าง Intent ส่ง Part No ที่เพิ่งเซฟเสร็จกลับไปบอก Screen 4
            val resultIntent = Intent()
            resultIntent.putExtra("SAVED_PART_NO", partNo)
            setResult(Activity.RESULT_OK, resultIntent)

            Toast.makeText(this, "Saved!", Toast.LENGTH_SHORT).show()
            finish() // เด้งกลับไปหน้า 4
        }
    }
}