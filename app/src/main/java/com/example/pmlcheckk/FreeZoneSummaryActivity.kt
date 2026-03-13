package com.example.pmlcheckk

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.PopupMenu
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class SummaryData(val sup: String, val kbn: String, val totalBox: Int)

class FreeZoneSummaryActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private lateinit var txtSelectedZone: TextView
    private lateinit var recyclerView: RecyclerView
    private var currentZone = "S-lane"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_free_zone_summary)

        db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "pml_db").build()

        txtSelectedZone = findViewById(R.id.txtSelectedZone)
        val btnSelectZone = findViewById<RelativeLayout>(R.id.btnSelectZone)
        val btnBack = findViewById<Button>(R.id.btnBack)
        val btnStartScan = findViewById<Button>(R.id.btnStartScan)

        recyclerView = findViewById(R.id.recyclerViewFreeZoneSummary)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // ระบบเลือก Zone Dropdown
        // ระบบเลือก Zone Dropdown (อัปเกรดเป็น ListPopupWindow เพื่อแต่งสีได้)
        btnSelectZone.setOnClickListener {
            // สร้าง Dropdown แบบกำหนดเอง
            val listPopupWindow = androidx.appcompat.widget.ListPopupWindow(this, null, androidx.appcompat.R.attr.listPopupWindowStyle)
            listPopupWindow.anchorView = btnSelectZone
            listPopupWindow.width = btnSelectZone.width // บังคับให้ Dropdown กว้างเท่ากับปุ่มพอดีเป๊ะ

            val zones = listOf("S-lane", "Overflow")

            // นำไฟล์ดีไซน์สีม่วงที่เราเพิ่งสร้าง มาครอบข้อมูล
            val adapter = android.widget.ArrayAdapter(this, R.layout.item_dropdown_zone, zones)
            listPopupWindow.setAdapter(adapter)

            // ลบขอบเงาสีขาวๆ กวนใจทิ้งไป และย้อมพื้นหลังเป็นสีม่วง
            listPopupWindow.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.parseColor("#673AB7")))

            // เมื่อกดเลือกรายการ
            listPopupWindow.setOnItemClickListener { _, _, position, _ ->
                currentZone = zones[position]
                txtSelectedZone.text = currentZone // อัปเดตข้อความบนปุ่ม
                loadSummaryData() // โหลดตารางใหม่
                listPopupWindow.dismiss() // เลือกเสร็จให้พับเก็บ
            }

            // โชว์ Dropdown ขึ้นมา
            listPopupWindow.show()
        }

        btnBack.setOnClickListener { finish() }

        // ส่งชื่อ Zone ข้ามไปหน้า Scan
        btnStartScan.setOnClickListener {
            val intent = Intent(this, FreeZoneScanActivity::class.java)
            intent.putExtra("ZONE_NAME", currentZone)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        loadSummaryData()
    }

    private fun loadSummaryData() {
        lifecycleScope.launch(Dispatchers.IO) {
            val items = db.inventoryDao().getItemsByGroup(currentZone)
            val summaryList = items.filter { !it.box.isNullOrEmpty() }
                .groupBy { it.kbn }
                .map { (kbnKey, list) ->
                    SummaryData(
                        sup = list.first().sup ?: "-",
                        kbn = kbnKey ?: "-",
                        totalBox = list.sumOf { it.box?.toIntOrNull() ?: 0 }
                    )
                }.sortedByDescending { it.totalBox }

            withContext(Dispatchers.Main) {
                recyclerView.adapter = SummaryAdapter(summaryList)
            }
        }
    }
}

class SummaryAdapter(private val list: List<SummaryData>) : RecyclerView.Adapter<SummaryAdapter.ViewHolder>() {
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtNo: TextView = view.findViewById(R.id.txtNo)
        val txtSup: TextView = view.findViewById(R.id.txtSup)
        val txtKbn: TextView = view.findViewById(R.id.txtKbn)
        val txtTotalBox: TextView = view.findViewById(R.id.txtTotalBox)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_free_zone_summary, parent, false))
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.txtNo.text = (position + 1).toString()
        holder.txtSup.text = item.sup
        holder.txtKbn.text = item.kbn
        holder.txtTotalBox.text = item.totalBox.toString()
    }
    override fun getItemCount() = list.size
}