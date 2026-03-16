package com.example.pmlcheckk

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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

    // ตัวแปรจดจำจำนวนกล่องแบบเรียลไทม์
    private var currentBoxCount: Int = 0

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

        loadIntentDataToUI()

        // กำหนดค่าเริ่มต้นให้กับกล่อง
        currentBoxCount = intent.getStringExtra("ITEM_BOX")?.toIntOrNull() ?: 0
        edtBox.setText(currentBoxCount.toString())
        edtBox.requestFocus()

        // 1. ตรวจจับและอัปเดตค่าเมื่อ User พิมพ์ตัวเลขด้วยคีย์บอร์ดมือถือปกติ
        edtBox.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val text = s?.toString()?.trim() ?: ""
                // ถ้าสั้นๆ (ไม่เกิน 10 ตัว) แสดงว่าเป็นคนพิมพ์ ไม่ใช่ปืนสแกน ให้จำค่าไว้
                if (text.length <= 10 && text.isNotEmpty()) {
                    currentBoxCount = text.toIntOrNull() ?: currentBoxCount
                }
            }
        })

        // 2. ดักจับเมื่อปืนสแกนยิงข้อความเข้ามาเสร็จ (ปืนสแกนจะส่งปุ่ม Enter ปิดท้ายเสมอ)
        edtBox.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                // ดึงข้อความดิบทั้งหมดที่กองอยู่ในช่อง Box
                val rawData = edtBox.text.toString().trim()

                // ถ้ายาวเกิน 20 ตัว แปลว่ามาจากการสแกนแน่นอน
                if (rawData.length > 20) {
                    val expectedKbn = txtKbn.text.toString().trim()

                    // ลอจิกคุณ: จับทั้งชุดข้อความมาเช็คว่ามี KBN ไหม
                    if (rawData.contains(expectedKbn, ignoreCase = true)) {
                        // MATCH! บวก 1
                        currentBoxCount += 1
                        Toast.makeText(this@Screen5Activity, "✅ KBN Match! บวกเพิ่ม 1 กล่อง", Toast.LENGTH_SHORT).show()
                    } else {
                        // ไม่ MATCH! แจ้งเตือน Error
                        showWrongPartDialog(rawData, expectedKbn)
                    }

                    // *** ทีเด็ดอยู่ตรงนี้ ***
                    // ไม่ว่าสแกนถูกหรือผิด เราจะเขียนทับข้อมูลยาวๆ ทิ้ง ด้วยตัวเลขที่ถูกต้อง (N หรือ N+1)
                    edtBox.setText(currentBoxCount.toString())
                    edtBox.setSelection(edtBox.text.length)
                }
                return@setOnKeyListener true // กลืนปุ่ม Enter ไปไม่ให้มันขึ้นบรรทัดใหม่
            }
            false
        }

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
            currentBoxCount = 0
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

        edtPcs.setText(intent.getStringExtra("ITEM_PCS") ?: "")
        edtSeq.setText(intent.getStringExtra("ITEM_SEQ") ?: "")
        edtOrder.setText(intent.getStringExtra("ITEM_LASTORDER") ?: "")
    }

    private fun showWrongPartDialog(rawBarcode: String, expectedKbn: String) {
        // หั่นเอาแค่ตรงท้ายๆ มาโชว์ให้ User ดูคร่าวๆ หรือโชว์ทั้งก้อน
        val scannedKbnDisplay = if (rawBarcode.length >= 72) rawBarcode.substring(68, 72).trim() else rawBarcode

        val builder = AlertDialog.Builder(this)
        builder.setTitle("❌ WRONG PART!")
        builder.setMessage("คุณสแกนผิดชิ้นครับ!\n\nข้อมูลที่สแกนได้: KBN $scannedKbnDisplay\nที่ต้องหยิบ: KBN $expectedKbn\n\nกรุณาตรวจสอบของในกล่องอีกครั้ง!")
        builder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
            edtBox.requestFocus()
        }
        builder.create().show()
    }
}