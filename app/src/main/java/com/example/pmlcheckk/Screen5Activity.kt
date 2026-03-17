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
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay

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
    private var debounceJob: Job? = null
    private var isProcessingScan: Boolean = false




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

        // 1. ประกาศตัวแปรเพื่อช่วยแยกแยะระหว่างคนพิมพ์ กับ ปืนสแกน
        var lastInputTime = 0L
        var validBoxCountBeforeScan = currentBoxCount

        // ตรวจจับและอัปเดตค่าเมื่อ User พิมพ์ตัวเลขด้วยคีย์บอร์ดมือถือปกติ
        // ------------------------------------------------------------------
        // ระบบดักจับการสแกน และการพิมพ์ด้วยมือ
        // ------------------------------------------------------------------
        edtBox.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (isProcessingScan) return // ป้องกันการวนลูป

                val text = s?.toString()?.trim() ?: ""

                // ยกเลิกเวลานับถอยหลังอันเก่า ทุกครั้งที่ปืนสแกนพิมพ์ตัวอักษรใหม่เข้ามา
                debounceJob?.cancel()

                // เริ่มจับเวลาใหม่ รอจนกว่าปืนสแกนจะหยุดพิมพ์ (หยุดนิ่ง 300ms)
                debounceJob = lifecycleScope.launch {
                    delay(300)

                    isProcessingScan = true // ล็อคระบบกัน UI สับสน

                    // 1. ถ้าข้อความยาวกว่า 5 ตัวอักษร ถือว่ามาจากการ "สแกนบาร์โค้ด" แน่นอน
                    // (เพราะคนคงไม่พิมพ์จำนวนกล่องเกิน 5 หลักด้วยมือ)
                    if (text.length > 5) {
                        val expectedKbn = txtKbn.text.toString().trim()

                        if (text.contains(expectedKbn, ignoreCase = true)) {
                            // MATCH! สแกนถูก
                            currentBoxCount += 1
                            Toast.makeText(this@Screen5Activity, "✅ KBN Match! บวกเพิ่ม 1 กล่อง", Toast.LENGTH_SHORT).show()

                            edtBox.setText(currentBoxCount.toString())
                            edtBox.selectAll()
                        } else {
                            // ไม่ MATCH! สแกนผิด
                            // คืนค่ากลับเป็นเลขที่ถูกต้อง "ทันที" ในครั้งแรก
                            edtBox.setText(currentBoxCount.toString())
                            edtBox.selectAll()

                            // แล้วค่อยเรียกหน้าต่าง Error
                            showWrongPartDialog(text, expectedKbn)
                        }
                    }
                    // 2. ถ้าข้อความสั้นๆ (ไม่เกิน 5 ตัว) ถือว่า User ตั้งใจเอานิ้วจิ้มพิมพ์ตัวเลข
                    else if (text.isNotEmpty()) {
                        val parsed = text.toIntOrNull()
                        if (parsed != null) {
                            currentBoxCount = parsed // บันทึกค่าที่คนพิมพ์มือไว้
                        }
                    }

                    isProcessingScan = false // ปลดล็อคระบบ
                }
            }
        })

        // (เผื่อไว้) ดักการกด Enter จาก Keyboard มือถือ หรือปืนสแกนบางรุ่นที่ชอบส่ง Action Done
        // ไม่ให้มันเด้งไปช่องอื่นหรือขึ้นบรรทัดใหม่
        edtBox.setOnEditorActionListener { _, actionId, event ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE ||
                (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER)) {
                return@setOnEditorActionListener true
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
            validBoxCountBeforeScan = 0 // เคลียร์ค่านี้ด้วยเพื่อความชัวร์
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
        val scannedKbnDisplay = if (rawBarcode.length >= 72) rawBarcode.substring(68, 72).trim() else rawBarcode

        val builder = AlertDialog.Builder(this)
        builder.setTitle("❌ WRONG PART!")
        builder.setMessage("คุณสแกนผิดชิ้นครับ!\n\nข้อมูลที่สแกนได้: KBN $scannedKbnDisplay\nที่ต้องหยิบ: KBN $expectedKbn\n\nกรุณาตรวจสอบของในกล่องอีกครั้ง!")
        builder.setCancelable(false)
        builder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()

            edtBox.post {
                isProcessingScan = true // ล็อก
                edtBox.setText(currentBoxCount.toString())
                edtBox.selectAll()
                edtBox.requestFocus()
                isProcessingScan = false // ปลดล็อก
            }
        }
        builder.create().show()
    }
}