package com.example.pmlcheckk

import android.os.Bundle
import android.view.KeyEvent
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Screen5Activity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private var itemId: Int = -1

    // ตัวแปรเก็บข้อความจากเครื่องสแกนเนอร์
    private var barcodeBuffer = StringBuilder()

    private lateinit var txtKbn: TextView
    private lateinit var txtFullAddress: TextView
    private lateinit var edtBox: EditText
    private lateinit var edtOrder: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_screen5)

        db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "pml_db").build()

        // รับค่าข้อมูลที่ส่งมาจากหน้า 4
        itemId = intent.getIntExtra("ITEM_ID", -1)
        val kbn = intent.getStringExtra("ITEM_KBN") ?: ""
        val fullAddr = intent.getStringExtra("ITEM_ADDR") ?: ""
        val partName = intent.getStringExtra("ITEM_PARTNAME") ?: ""
        val box = intent.getStringExtra("ITEM_BOX") ?: ""
        val pcs = intent.getStringExtra("ITEM_PCS") ?: ""
        val seq = intent.getStringExtra("ITEM_SEQ") ?: ""
        val lastOrder = intent.getStringExtra("ITEM_LASTORDER") ?: "" // รับค่า lastOrder เดิมมาแสดง

        // ผูกตัวแปร View ให้ตรงกับ ID ในไฟล์ XML
        txtKbn = findViewById<TextView>(R.id.txtKbn)
        txtFullAddress = findViewById<TextView>(R.id.txtFullAddr)
        val txtPartName = findViewById<TextView>(R.id.txtPartName)

        edtBox = findViewById<EditText>(R.id.edtBox)
        val edtPcs = findViewById<EditText>(R.id.edtPcs)
        val edtSeq = findViewById<EditText>(R.id.edtSeq)
        edtOrder = findViewById<EditText>(R.id.edtOrder) // ช่องสำหรับกรอก Order ของคุณ

        val btnSave = findViewById<Button>(R.id.btnSave)
        val btnBack = findViewById<Button>(R.id.btnBack)
        val btnNotFound = findViewById<Button>(R.id.btnNotFound)

        // แสดงข้อมูลลงบนหน้าจอ
        txtKbn.text = kbn
        txtFullAddress.text = fullAddr
        txtPartName.text = partName
        edtBox.setText(box)
        edtPcs.setText(pcs)
        edtSeq.setText(seq)
        edtOrder.setText(lastOrder) // แสดงข้อมูล lastOrder เดิมในช่อง

        // โฟกัสไปที่ช่อง Box อัตโนมัติรอให้สแกนหรือพิมพ์
        edtBox.requestFocus()

        // ปุ่ม Save (บันทึกข้อมูล)
        btnSave.setOnClickListener {
            val newBox = edtBox.text.toString()
            val newPcs = edtPcs.text.toString()
            val newSeq = edtSeq.text.toString()
            val newLastOrder = edtOrder.text.toString() // ดึงค่าจากช่อง Order เพื่อเตรียมเซฟ

            lifecycleScope.launch(Dispatchers.IO) {
                // ส่งค่า newLastOrder ไปอัปเดตลง Database
                db.inventoryDao().updateStockData(itemId, newBox, newPcs, newSeq, newLastOrder)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@Screen5Activity, "Saved Successfully", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }

        btnBack.setOnClickListener { finish() }

        btnNotFound.setOnClickListener {
            edtBox.setText("0")
            edtPcs.setText("0")
        }
    }

    // ==========================================================
    // ดักจับสัญญาณจากปืนสแกนเนอร์
    // ==========================================================
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.deviceId > 0 && event.action == KeyEvent.ACTION_DOWN) {

            if (event.keyCode == KeyEvent.KEYCODE_ENTER) {
                val scannedText = barcodeBuffer.toString().trim()
                if (scannedText.isNotEmpty()) {
                    verifyScannedBarcode(scannedText)
                    barcodeBuffer.clear()
                }
                return true
            } else {
                val char = event.unicodeChar.toChar()
                if (char.code > 0) {
                    barcodeBuffer.append(char)
                    return true
                }
            }
        }
        return super.dispatchKeyEvent(event)
    }

    // ==========================================================
    // หั่นบาร์โค้ดเช็คความถูกต้อง + บวกเลข Box อัตโนมัติ (1 สแกน = 1 Box)
    // ==========================================================
    private fun verifyScannedBarcode(rawBarcode: String) {
        try {
            if (rawBarcode.length >= 71) {
                // ดึง KBN ออกมาจากบาร์โค้ดยาวๆ (ตำแหน่งที่ 67-70)
                val scannedKbn = rawBarcode.substring(67, 71).trim()
                val expectedKbn = txtKbn.text.toString().trim()

                // ตรวจสอบความถูกต้อง
                if (scannedKbn == expectedKbn) {

                    val currentBox = edtBox.text.toString().toIntOrNull() ?: 0
                    val newBoxCount = currentBox + 1

                    edtBox.setText(newBoxCount.toString())
                    edtBox.setSelection(edtBox.text.length)

                    Toast.makeText(this, "✅ KBN Match! นับเป็น $newBoxCount กล่องแล้ว", Toast.LENGTH_SHORT).show()

                } else {
                    val builder = AlertDialog.Builder(this)
                    builder.setTitle("❌ WRONG PART!")
                    builder.setMessage("คุณสแกนผิดชิ้นครับ!\n\nที่สแกนได้: KBN $scannedKbn\nที่ต้องหยิบ: KBN $expectedKbn\n\nกรุณาตรวจสอบของในกล่องอีกครั้ง!")
                    builder.setPositiveButton("OK") { dialog, _ ->
                        dialog.dismiss()
                        edtBox.requestFocus()
                    }
                    builder.create().show()
                }
            } else {
                Toast.makeText(this, "❌ บาร์โค้ดผิดรูปแบบ", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "❌ เกิดข้อผิดพลาดในการอ่านบาร์โค้ด", Toast.LENGTH_SHORT).show()
        }
    }
}