package com.example.pmlcheckk

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.content.res.ColorStateList

class Screen4Activity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private lateinit var adapter: PartListAdapter

    private lateinit var txtHeaderAddress: TextView
    private lateinit var txtRemainCount: TextView
    private lateinit var recyclerViewPartList: RecyclerView
    private lateinit var btnBack: Button
    private lateinit var btnEdit: Button
    private lateinit var btnNext: Button

    private var myPartList = mutableListOf<InventoryItem>()
    private var selectedAddress: String = ""

    // สร้างตัวรับ Result ที่เด้งกลับมาจากหน้า 5
    private val startScreen5ForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK || result.resultCode == Activity.RESULT_CANCELED) {
            // หากหน้า 5 ปิดลงมา ให้โหลดข้อมูลจาก Database ใหม่เพื่ออัปเดตตัวเลข Remain และสลับสี
            loadDataFromDatabase(selectedAddress)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_screen4)

        txtHeaderAddress = findViewById(R.id.txtHeaderAddress)
        txtRemainCount = findViewById(R.id.txtRemainCount)
        recyclerViewPartList = findViewById(R.id.recyclerViewPartList)
        btnBack = findViewById(R.id.btnBack)
        btnEdit = findViewById(R.id.btnEdit)
        btnNext = findViewById(R.id.btnNext)

        db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "pml_db"
        ).build()

        selectedAddress = intent.getStringExtra("SELECTED_ADDRESS") ?: "M01: PART LIST"
        txtHeaderAddress.text = selectedAddress

        recyclerViewPartList.layoutManager = LinearLayoutManager(this)

        // ส่ง Callback เมื่อมีการคลิก Item เข้าไปที่ Adapter
        adapter = PartListAdapter(myPartList) { item ->
            val intent = Intent(this, Screen5Activity::class.java)

            // 🚨 ปรับชื่อ Key ให้ตรงกับที่ Screen 5 รอรับ และเพิ่ม ITEM_LASTORDER เข้าไป
            intent.putExtra("ITEM_ID", item.id)
            intent.putExtra("ITEM_KBN", item.kbn)
            intent.putExtra("ITEM_ADDR", item.fullAddr)
            intent.putExtra("ITEM_PARTNAME", item.partName)
            intent.putExtra("ITEM_BOX", item.box)
            intent.putExtra("ITEM_PCS", item.pcs)
            intent.putExtra("ITEM_SEQ", item.seq)
            intent.putExtra("ITEM_LASTORDER", item.lastOrder)

            // ส่งข้อมูลเพิ่มเติมเผื่อต้องใช้
            intent.putExtra("PART_NO", item.partNo)
            intent.putExtra("SUPPLIER", item.sup)
            intent.putExtra("QTY", item.qty ?: 0)

            // เปิดหน้า 5 แบบรอรับผลลัพธ์
            startScreen5ForResult.launch(intent)
        }

        recyclerViewPartList.adapter = adapter

        loadDataFromDatabase(selectedAddress)

        btnBack.setOnClickListener {
            finish()
        }
        btnNext.setOnClickListener {
            // TODO: ใส่โค้ดไปหน้าถัดไปในอนาคต
        }
        btnEdit.setOnClickListener {
            showEditDialog()
        }
    }

    private fun showEditDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_kbn, null)

        val builder = android.app.AlertDialog.Builder(this)
        builder.setView(dialogView)
        val dialog = builder.create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val edtKbn = dialogView.findViewById<android.widget.AutoCompleteTextView>(R.id.edtDialogKbn)
        val btnOk = dialogView.findViewById<android.widget.Button>(R.id.btnDialogOk)
        val btnCancel = dialogView.findViewById<android.widget.Button>(R.id.btnDialogCancel)

        // ดึงชื่อ KBN ที่เคยกรอกข้อมูลแล้วมาใส่ Dropdown
        val savedKbnList = myPartList
            .filter { !it.box.isNullOrEmpty() }
            .mapNotNull { it.kbn }
            .distinct()

        val dropdownAdapter = android.widget.ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            savedKbnList
        )
        edtKbn.setAdapter(dropdownAdapter)

        btnOk.setOnClickListener {
            val searchKbn = edtKbn.text.toString().trim()

            if (searchKbn.isEmpty()) {
                android.widget.Toast.makeText(this, "กรุณากรอก KBN", android.widget.Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val foundItem = myPartList.find {
                it.kbn.equals(searchKbn, ignoreCase = true) && !it.box.isNullOrEmpty()
            }

            if (foundItem != null) {
                val intent = Intent(this, Screen5Activity::class.java)

                // 🚨 ปรับชื่อ Key และส่ง ITEM_LASTORDER เช่นเดียวกัน
                intent.putExtra("ITEM_ID", foundItem.id)
                intent.putExtra("ITEM_KBN", foundItem.kbn)
                intent.putExtra("ITEM_ADDR", foundItem.fullAddr)
                intent.putExtra("ITEM_PARTNAME", foundItem.partName)
                intent.putExtra("ITEM_BOX", foundItem.box)
                intent.putExtra("ITEM_PCS", foundItem.pcs)
                intent.putExtra("ITEM_SEQ", foundItem.seq)
                intent.putExtra("ITEM_LASTORDER", foundItem.lastOrder)

                intent.putExtra("PART_NO", foundItem.partNo)
                intent.putExtra("SUPPLIER", foundItem.sup)
                intent.putExtra("QTY", foundItem.qty ?: 0)

                startScreen5ForResult.launch(intent)
                dialog.dismiss()
            } else {
                android.widget.Toast.makeText(this, "ไม่พบ KBN นี้ หรือข้อมูลนี้ยังไม่ได้บันทึก", android.widget.Toast.LENGTH_SHORT).show()
            }
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun loadDataFromDatabase(groupName: String) {
        lifecycleScope.launch {
            val items = withContext(Dispatchers.IO) {
                db.inventoryDao().getItemsByGroup(groupName)
            }

            // จัดเรียงข้อมูล: เอา item ที่ box ยังเป็นค่าว่างไว้ด้านบนสุด
            val sortedItems = items.sortedBy { !it.box.isNullOrEmpty() }

            myPartList.clear()
            myPartList.addAll(sortedItems)

            // อัปเดต Remain Count
            val remainCount = sortedItems.count { it.box.isNullOrEmpty() }
            txtRemainCount.text = remainCount.toString()

            adapter.notifyDataSetChanged()
        }
    }
}

// ------------------- ADAPTER -------------------
class PartListAdapter(
    private val dataList: List<InventoryItem>,
    private val onItemClick: (InventoryItem) -> Unit
) : RecyclerView.Adapter<PartListAdapter.MyViewHolder>() {

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

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = dataList[position]

        holder.txtNo.text = (position + 1).toString()
        holder.txtSupplier.text = item.sup ?: "-"
        holder.txtKbn.text = item.kbn ?: "-"
        holder.txtAddress.text = item.fullAddr

        // ตรวจสอบสถานะการตรวจเช็ค เพื่อเปลี่ยนสีปุ่ม KBN
        if (!item.box.isNullOrEmpty()) {
            holder.txtKbn.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#5C5470"))
            holder.txtKbn.setTextColor(Color.WHITE)
        } else {
            holder.txtKbn.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#FF65A3"))
            holder.txtKbn.setTextColor(Color.BLACK)
        }

        holder.itemView.setOnClickListener {
            onItemClick(item)
        }
    }

    override fun getItemCount(): Int {
        return dataList.size
    }
}