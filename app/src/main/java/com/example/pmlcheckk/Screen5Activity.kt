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
    private var barcodeBuffer = StringBuilder()

    private lateinit var txtKbn: TextView
    private lateinit var txtFullAddress: TextView
    private lateinit var txtPartName: TextView
    private lateinit var txtSupplier: TextView
    private lateinit var txtPartNo: TextView
    private lateinit var txtQty: TextView

    private lateinit var edtBox: EditText
    private lateinit var edtPcs: EditText
    private lateinit var edtSeq: EditText
    private lateinit var edtOrder: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_screen5)

        db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "pml_db").build()

        txtKbn = findViewById(R.id.txtKbn)
        txtFullAddress = findViewById(R.id.txtFullAddr)
        txtPartName = findViewById(R.id.txtPartName)
        txtSupplier = findViewById(R.id.txtSupplier)
        txtPartNo = findViewById(R.id.txtPartNo)
        txtQty = findViewById(R.id.txtQty)

        edtBox = findViewById(R.id.edtBox)
        edtPcs = findViewById(R.id.edtPcs)
        edtSeq = findViewById(R.id.edtSeq)
        edtOrder = findViewById(R.id.edtOrder)

        val btnSave = findViewById<Button>(R.id.btnSave)
        val btnBack = findViewById<Button>(R.id.btnBack)
        val btnNotFound = findViewById<Button>(R.id.btnNotFound)

        // โหลดข้อมูลที่ถูกส่งมาจากหน้า 4
        loadIntentDataToUI()

        edtBox.requestFocus()

        // ==========================================================
        // ปุ่ม Save (บันทึกข้อมูล)
        // ==========================================================
        btnSave.setOnClickListener {
            val newBox = edtBox.text.toString().trim()
            val newPcs = edtPcs.text.toString().trim()
            val newSeq = edtSeq.text.toString().trim()
            val newLastOrder = edtOrder.text.toString().trim()

            if (newBox.isEmpty() || newPcs.isEmpty() || newSeq.isEmpty()) {
                Toast.makeText(this, "⚠️ กรุณากรอก Box, Pcs และ Seq ให้ครบถ้วน", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (newSeq.length != 3) {
                Toast.makeText(this, "⚠️ กรุณากรอก Seq ให้ครบ 3 หลัก", Toast.LENGTH_SHORT).show()
                edtSeq.requestFocus()
                return@setOnClickListener
            }

            lifecycleScope.launch(Dispatchers.IO) {
                db.inventoryDao().updateStockData(itemId, newBox, newPcs, newSeq, newLastOrder)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@Screen5Activity, "✅ Saved Successfully", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }

        btnBack.setOnClickListener { finish() }

        btnNotFound.setOnClickListener {
            edtBox.setText("0")
            edtPcs.setText("0")
            edtSeq.setText("000")
        }
    }

    private fun loadIntentDataToUI() {
        itemId = intent.getIntExtra("ITEM_ID", -1)
        txtKbn.text = intent.getStringExtra("ITEM_KBN") ?: ""
        txtFullAddress.text = intent.getStringExtra("ITEM_ADDR") ?: ""
        txtPartName.text = intent.getStringExtra("ITEM_PARTNAME") ?: ""
        txtSupplier.text = intent.getStringExtra("SUPPLIER") ?: "-"
        txtPartNo.text = intent.getStringExtra("PART_NO") ?: "-"
        txtQty.text = intent.getIntExtra("QTY", 0).toString()

        edtBox.setText(intent.getStringExtra("ITEM_BOX") ?: "")
        edtPcs.setText(intent.getStringExtra("ITEM_PCS") ?: "")
        edtSeq.setText(intent.getStringExtra("ITEM_SEQ") ?: "")
        edtOrder.setText(intent.getStringExtra("ITEM_LASTORDER") ?: "")
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
    // ตรวจสอบ KBN: ถ้าตรงบวก 1 ถ้าไม่ตรงเด้ง Error
    // ==========================================================
    private fun verifyScannedBarcode(rawBarcode: String) {
        val scannedKbn = if (rawBarcode.length >= 71) {
            rawBarcode.substring(67, 71).trim()
        } else if (rawBarcode.isNotEmpty() && rawBarcode.length <= 10) {
            rawBarcode.trim()
        } else {
            Toast.makeText(this, "❌ รูปแบบบาร์โค้ดไม่ถูกต้อง", Toast.LENGTH_SHORT).show()
            return
        }

        val expectedKbn = txtKbn.text.toString().trim()

        if (scannedKbn.equals(expectedKbn, ignoreCase = true)) {
            // KBN ตรงกัน -> บวก Box เพิ่ม 1
            val currentBox = edtBox.text.toString().toIntOrNull() ?: 0
            edtBox.setText((currentBox + 1).toString())
            edtBox.setSelection(edtBox.text.length)
            Toast.makeText(this, "✅ KBN Match! บวกเพิ่ม 1 กล่อง", Toast.LENGTH_SHORT).show()
        } else {
            // KBN ไม่ตรง -> เด้ง Error
            val builder = AlertDialog.Builder(this)
            builder.setTitle("❌ WRONG PART!")
            builder.setMessage("คุณสแกนผิดชิ้นครับ!\n\nที่สแกนได้: KBN $scannedKbn\nที่ต้องหยิบ: KBN $expectedKbn\n\nกรุณาตรวจสอบของในกล่องอีกครั้ง!")
            builder.setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
                edtBox.requestFocus()
            }
            builder.create().show()
        }
    }
}