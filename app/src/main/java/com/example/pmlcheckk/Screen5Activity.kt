package com.example.pmlcheckk

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Screen5Activity : AppCompatActivity() {

    private lateinit var db: AppDatabase // เพิ่มตัวแปร Database

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_screen5)

        // สร้าง Instance ของ Database
        db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "pml_db"
        ).build()

        // ผูกตัวแปร Text และ Input
        val txtSupplier = findViewById<TextView>(R.id.txtSupplier)
        val txtPartNo = findViewById<TextView>(R.id.txtPartNo)
        val txtPartName = findViewById<TextView>(R.id.txtPartName)
        val txtKbn = findViewById<TextView>(R.id.txtKbn)
        val txtFullAddr = findViewById<TextView>(R.id.txtFullAddr)
        val txtQty = findViewById<TextView>(R.id.txtQty)

        val edtBox = findViewById<EditText>(R.id.edtBox)
        val edtPcs = findViewById<EditText>(R.id.edtPcs)
        val edtSeq = findViewById<EditText>(R.id.edtSeq)
        val edtOrder = findViewById<EditText>(R.id.edtOrder)

        val btnNotFound = findViewById<Button>(R.id.btnNotFound)
        val btnBack = findViewById<Button>(R.id.btnBack)
        val btnSave = findViewById<Button>(R.id.btnSave)

        // รับข้อมูลจาก Intent (รับ ITEM_ID มาด้วยเพื่อใช้อ้างอิงตอนอัปเดต)
        val itemId = intent.getIntExtra("ITEM_ID", -1)
        val supplier = intent.getStringExtra("SUPPLIER") ?: "-"
        val partNo = intent.getStringExtra("PART_NO") ?: "-"
        val partName = intent.getStringExtra("PART_NAME") ?: "-"
        val kbn = intent.getStringExtra("KBN") ?: "-"
        val fullAddr = intent.getStringExtra("FULL_ADDR") ?: "-"
        val qty = intent.getIntExtra("QTY", 0)
        val savedBox = intent.getStringExtra("BOX") ?: ""
        val savedPcs = intent.getStringExtra("PCS") ?: ""
        val savedSeq = intent.getStringExtra("SEQ") ?: ""
        // โชว์ข้อมูล
        txtSupplier.text = supplier
        txtPartNo.text = partNo
        txtPartName.text = partName
        txtKbn.text = kbn
        txtFullAddr.text = fullAddr
        txtQty.text = qty.toString()

        edtBox.setText(savedBox)
        edtPcs.setText(savedPcs)
        edtSeq.setText(savedSeq)

        btnNotFound.setOnClickListener {
            edtBox.setText("0")
            edtPcs.setText("0")
            edtOrder.setText("0")

        }

        btnBack.setOnClickListener {
            finish()
        }

        // กดปุ่ม Save -> บันทึกลง Room Database
        btnSave.setOnClickListener {
            val box = edtBox.text.toString().trim()
            val pcs = edtPcs.text.toString().trim()
            val seq = edtSeq.text.toString().trim()

            // 1. เช็คว่ากรอกครบทุกช่องไหม
            if (box.isEmpty() || pcs.isEmpty() || seq.isEmpty()) {
                Toast.makeText(this, "กรุณากรอก Box, Pcs และ Seq ให้ครบ", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // ==========================================
            // 2. เพิ่มเงื่อนไขเช็คว่า Seq ต้องมี 3 หลักเท่านั้น
            // ==========================================
            if (seq.length != 3) {
                Toast.makeText(this, "กรุณากรอก Seq ให้ครบ 3 หลัก (เช่น 001)", Toast.LENGTH_SHORT)
                    .show()
                // ให้เด้งไปโฟกัสที่ช่อง Seq เพื่อให้ผู้ใช้พิมพ์แก้ได้เลย
                edtSeq.requestFocus()
                return@setOnClickListener
            }

            // 3. ถ้าผ่านเงื่อนไขทั้งหมด ค่อยบันทึกข้อมูล
            if (itemId != -1) {
                // บันทึกข้อมูลแบบ Asynchronous ด้วย Coroutine
                lifecycleScope.launch(Dispatchers.IO) {
                    db.inventoryDao().updateStockData(itemId, box, pcs, seq)

                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@Screen5Activity, "Saved!", Toast.LENGTH_SHORT).show()

                        // ส่งสัญญาณบอกหน้า 4 ว่าเซฟสำเร็จแล้ว
                        val resultIntent = Intent()
                        setResult(Activity.RESULT_OK, resultIntent)
                        finish()
                    }
                }
            } else {
                Toast.makeText(this, "ไม่พบ ID ของสินค้านี้", Toast.LENGTH_SHORT).show()
            }
        }
    }
}