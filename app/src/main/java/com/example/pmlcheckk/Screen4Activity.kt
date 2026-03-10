package com.example.pmlcheckk

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Screen4Activity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private lateinit var adapter: PartListAdapter

    // ประกาศตัวแปรสำหรับ View ต่างๆ
    private lateinit var txtHeaderAddress: TextView
    private lateinit var txtRemainCount: TextView
    private lateinit var recyclerViewPartList: RecyclerView
    private lateinit var btnEdit: Button
    private lateinit var btnNext: Button

    private var myPartList = mutableListOf<InventoryItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_screen4)

        // --- ขั้นตอนสำคัญ: เชื่อม ID จาก XML เข้ากับตัวแปร Kotlin ---
        txtHeaderAddress = findViewById(R.id.txtHeaderAddress)
        txtRemainCount = findViewById(R.id.txtRemainCount)
        recyclerViewPartList = findViewById(R.id.recyclerViewPartList)
        btnEdit = findViewById(R.id.btnEdit)
        btnNext = findViewById(R.id.btnNext)

        // เริ่มต้น Database (ใช้ Singleton pattern ตามไฟล์ AppDatabase.kt ของคุณ)
        db = AppDatabase.getDatabase(this)

        // รับค่า Address ที่เลือกมาจากหน้า Screen 3
        val selectedAddress = intent.getStringExtra("SELECTED_ADDRESS") ?: "M01: PART LIST"
        txtHeaderAddress.text = selectedAddress

        // ตั้งค่า RecyclerView
        recyclerViewPartList.layoutManager = LinearLayoutManager(this)
        adapter = PartListAdapter(myPartList)
        recyclerViewPartList.adapter = adapter

        // โหลดข้อมูลจริงจาก Database
        loadDataFromDatabase(selectedAddress)

        // ตั้งค่าปุ่ม (ถ้ามีฟังก์ชันเพิ่มในอนาคต)
        btnEdit.setOnClickListener {
            // โค้ดสำหรับปุ่ม Edit
        }

        btnNext.setOnClickListener {
            // โค้ดสำหรับปุ่ม Next
        }
    }

    private fun loadDataFromDatabase(groupName: String) {
        lifecycleScope.launch {
            val items = withContext(Dispatchers.IO) {
                // ดึงข้อมูลตามกลุ่ม Address ที่เลือก
                db.inventoryDao().getItemsByGroup(groupName)
            }

            myPartList.clear()
            myPartList.addAll(items)

            // อัปเดตตัวเลข REMAIN ตามจำนวนที่นับได้จาก Database จริง
            txtRemainCount.text = myPartList.size.toString()

            adapter.notifyDataSetChanged()
        }
    }
}

// --- Adapter สำหรับจัดการรายการในตาราง ---
class PartListAdapter(private val dataList: List<InventoryItem>) :
    RecyclerView.Adapter<PartListAdapter.MyViewHolder>() {

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtNo: TextView = itemView.findViewById(R.id.txtNo)
        val txtSupplier: TextView = itemView.findViewById(R.id.txtSupplier)
        val txtKbn: TextView = itemView.findViewById(R.id.txtKbn)
        val txtAddress: TextView = itemView.findViewById(R.id.txtAddress)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_part_list, parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int = dataList.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = dataList[position]

        holder.txtNo.text = (position + 1).toString()
        holder.txtSupplier.text = item.sup ?: "-"     // ใช้ฟิลด์ 'sup' จาก InventoryItem.kt
        holder.txtKbn.text = item.kbn ?: "-"         // ใช้ฟิลด์ 'kbn' จาก InventoryItem.kt
        holder.txtAddress.text = item.fullAddr       // ใช้ฟิลด์ 'fullAddr' จาก InventoryItem.kt
    }
}