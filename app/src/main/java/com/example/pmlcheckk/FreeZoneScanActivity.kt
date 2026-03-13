package com.example.pmlcheckk

import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class ScannedItem(val kbn: String, var boxCount: Int = 1)

class FreeZoneScanActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private lateinit var recyclerView: RecyclerView
    private lateinit var txtTotalScan: TextView
    private lateinit var adapter: ScanAdapter
    private lateinit var edtBarcodeScanner: EditText

    private var targetZone = ""
    private val scannedList = mutableListOf<ScannedItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_free_zone_scan)

        targetZone = intent.getStringExtra("ZONE_NAME") ?: "FreeZone"
        findViewById<EditText>(R.id.edtScanLocation).setText(targetZone)

        db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "pml_db").build()

        txtTotalScan = findViewById(R.id.txtTotalScan)
        val btnBack = findViewById<Button>(R.id.btnBack)
        val btnSaveAll = findViewById<Button>(R.id.btnSaveAll)
        edtBarcodeScanner = findViewById(R.id.edtBarcodeScanner)

        recyclerView = findViewById(R.id.recyclerViewScannedItems)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = ScanAdapter(scannedList) { position ->
            scannedList.removeAt(position)
            updateUI()
        }
        recyclerView.adapter = adapter

        // ========================================================
        // 1. ระบบรับค่าสแกนรัวๆ (ดักจับปุ่ม Enter จากเครื่อง Handheld)
        // ========================================================
        edtBarcodeScanner.requestFocus()
        edtBarcodeScanner.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                // 1. รับข้อความดิบยาวๆ จากเครื่องสแกน
                val rawBarcode = edtBarcodeScanner.text.toString()

                if (rawBarcode.isNotEmpty()) {
                    var finalKbn = rawBarcode.trim()

                    try {
                        // 2. กระบวนการ "หั่น" เอาเฉพาะ KBN
                        // เช็คก่อนว่าบาร์โค้ดยาวพอไหม (ป้องกัน error ถ้ายิง QR ผิดประเภท)
                        if (rawBarcode.length >= 71) {
                            // ใช้คำสั่ง substring ดึงตัวอักษรตั้งแต่ตำแหน่งที่ 67 ถึง 71 (ไม่รวม 71)
                            finalKbn = rawBarcode.substring(67, 71).trim()
                        }
                        // 💡 ทริคเสริม: ถ้าบาร์โค้ดมีการขยับหน้าหลังนิดหน่อย
                        // เราสามารถใช้ Regex จับรูปแบบ Date+Time แทนได้ (ถ้าสนใจบอกผมได้ครับ)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    // 3. เอา KBN ที่ตัดได้ (เช่น A277) ยัดเข้า List บนหน้าจอ
                    scannedList.add(0, ScannedItem(finalKbn, 1))
                    updateUI()

                    // เคลียร์ช่องให้ว่าง รอรับสแกนตัวต่อไปทันที
                    edtBarcodeScanner.text.clear()
                }

                edtBarcodeScanner.requestFocus()
                return@setOnKeyListener true
            }
            false

        }

        // ========================================================
        // 2. ปุ่ม Back (กลับไปหน้า Summary)
        // ========================================================
        btnBack.setOnClickListener {
            // เช็คหน่อยว่าถ้ามีของสแกนค้างอยู่แล้วเผลอกด Back จะเตือนไหม
            if (scannedList.isNotEmpty()) {
                val builder = android.app.AlertDialog.Builder(this)
                builder.setTitle("Warning")
                builder.setMessage("You have unsaved items. Are you sure you want to go back?")
                builder.setPositiveButton("Yes") { _, _ -> finish() }
                builder.setNegativeButton("No", null)
                builder.show()
            } else {
                finish()
            }
        }

        // ========================================================
        // 3. ปุ่ม SAVE ALL
        // ========================================================
        btnSaveAll.setOnClickListener {
            if (scannedList.isEmpty()) {
                Toast.makeText(this, "No items to save!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch(Dispatchers.IO) {
                for (scanned in scannedList) {
                    val dbItem = db.inventoryDao().getItemByKbn(scanned.kbn)
                    if (dbItem != null) {
                        val currentBox = dbItem.box?.toIntOrNull() ?: 0
                        val newBoxTotal = currentBox + scanned.boxCount
                        db.inventoryDao().updateFreeZoneData(scanned.kbn, newBoxTotal.toString(), targetZone)
                    }
                }
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@FreeZoneScanActivity, "Saved ${scannedList.size} Items!", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }

    private fun updateUI() {
        adapter.notifyDataSetChanged()
        txtTotalScan.text = " ${scannedList.size}"
        recyclerView.scrollToPosition(0)
    }
}

class ScanAdapter(private val list: List<ScannedItem>, private val onDelete: (Int) -> Unit) : RecyclerView.Adapter<ScanAdapter.ViewHolder>() {
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtItemNo: TextView = view.findViewById(R.id.txtItemNo)
        val txtScanKbn: TextView = view.findViewById(R.id.txtScanKbn)
        val txtScanBoxCount: TextView = view.findViewById(R.id.txtScanBoxCount)
        val btnDeleteScan: TextView = view.findViewById(R.id.btnDeleteScan)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_free_zone_scan, parent, false))
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.txtItemNo.text = (list.size - position).toString()
        holder.txtScanKbn.text = item.kbn
        holder.txtScanBoxCount.text = item.boxCount.toString()
        holder.btnDeleteScan.setOnClickListener { onDelete(position) }
    }
    override fun getItemCount() = list.size
}