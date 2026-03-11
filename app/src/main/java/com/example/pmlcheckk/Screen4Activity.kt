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
    private lateinit var btnEdit: Button // เพิ่มบรรทัดนี้
    private lateinit var btnNext: Button

    private var myPartList = mutableListOf<InventoryItem>()
    private var selectedAddress: String = ""

    // สร้างตัวรับ Result ที่เด้งกลับมาจากหน้า 5
    private val startScreen5ForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // หากหน้า 5 บันทึกสำเร็จ ให้โหลดข้อมูลจาก Database ใหม่ (มันจะทำการจัดเรียงและนับ Remain ให้ใหม่ทันที)
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
        btnEdit = findViewById(R.id.btnEdit) // ผูกตัวแปร
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
            intent.putExtra("ITEM_ID", item.id) // ส่ง ID เข้าไปด้วยเพื่อใช้สำหรับอัปเดต Database
            intent.putExtra("PART_NO", item.partNo)
            intent.putExtra("PART_NAME", item.partName)
            intent.putExtra("SUPPLIER", item.sup)
            intent.putExtra("KBN", item.kbn)
            intent.putExtra("FULL_ADDR", item.fullAddr)
            intent.putExtra("QTY", item.qty ?: 0)

            intent.putExtra("BOX", item.box)
            intent.putExtra("PCS", item.pcs)
            intent.putExtra("SEQ", item.seq)

            // เปิดหน้า 5 แบบรอรับผลลัพธ์
            startScreen5ForResult.launch(intent)
        }

        recyclerViewPartList.adapter = adapter

        loadDataFromDatabase(selectedAddress)

        btnBack.setOnClickListener {
            finish() // คำสั่ง finish() จะปิด Screen 4 และเด้งกลับไป Screen 3 ที่เปิดค้างไว้ก่อนหน้านี้
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

        // 1. ผูกตัวแปร (สังเกตว่าช่องพิมพ์เปลี่ยนไปใช้ AutoCompleteTextView)
        val edtKbn = dialogView.findViewById<android.widget.AutoCompleteTextView>(R.id.edtDialogKbn)
        val btnOk = dialogView.findViewById<android.widget.Button>(R.id.btnDialogOk)
        val btnCancel = dialogView.findViewById<android.widget.Button>(R.id.btnDialogCancel)

        // ==========================================
        // 2. ดึงชื่อ KBN ที่เคยกรอกข้อมูลแล้วมาใส่ Dropdown
        // ==========================================
        // กรองเอาเฉพาะตัวที่ box ไม่ว่าง -> ดึงเอาแค่ชื่อ kbn -> และตัดชื่อที่ซ้ำกันออก (distinct)
        val savedKbnList = myPartList
            .filter { !it.box.isNullOrEmpty() }
            .mapNotNull { it.kbn }
            .distinct()

        // นำ List ที่ได้ไปผูกกับ Adapter เพื่อแสดงผลใน Dropdown (ใช้รูปแบบลิสต์มาตรฐานของ Android)
        val dropdownAdapter = android.widget.ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            savedKbnList
        )
        edtKbn.setAdapter(dropdownAdapter)

        // ------------------------------------------

        // 3. สั่งให้ปุ่ม OK ทำงาน (โค้ดส่วนนี้ใช้ของเดิมได้เลยครับ)
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
                intent.putExtra("ITEM_ID", foundItem.id)
                intent.putExtra("PART_NO", foundItem.partNo)
                intent.putExtra("PART_NAME", foundItem.partName)
                intent.putExtra("SUPPLIER", foundItem.sup)
                intent.putExtra("KBN", foundItem.kbn)
                intent.putExtra("FULL_ADDR", foundItem.fullAddr)
                intent.putExtra("QTY", foundItem.qty ?: 0)
                intent.putExtra("BOX", foundItem.box)
                intent.putExtra("PCS", foundItem.pcs)
                intent.putExtra("SEQ", foundItem.seq)

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

            // จัดเรียงข้อมูล (Sort): เอา item ที่ box ยังเป็นค่าว่าง (หรือ null) ไว้ด้านบนสุด ตัวที่มีค่าแล้วจะตกไปอยู่ล่างสุด
            val sortedItems = items.sortedBy { !it.box.isNullOrEmpty() }

            myPartList.clear()
            myPartList.addAll(sortedItems)

            // อัปเดต Remain Count (นับเฉพาะตัวที่ยังไม่ได้กรอก Box)
            val remainCount = sortedItems.count { it.box.isNullOrEmpty() }
            txtRemainCount.text = remainCount.toString()

            adapter.notifyDataSetChanged()
        }
    }
}

// ปรับปรุง Adapter ให้รับ Click Listener มาจาก Activity แทนการ startActivity ตรงๆ
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

        // ==========================================
        // ตรวจสอบสถานะการตรวจเช็ค เพื่อเปลี่ยนสีปุ่ม KBN และสีตัวหนังสือ
        // ==========================================
        if (!item.box.isNullOrEmpty()) {
            // 1. ถ้ามีข้อมูลแล้ว: เปลี่ยนเป็น "สีม่วงเทาเข้ม" และตัวหนังสือ "สีอ่อน (สีขาว)"
            holder.txtKbn.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#5C5470"))
            holder.txtKbn.setTextColor(Color.WHITE)
        } else {
            // 2. ถ้ายังไม่มีข้อมูล: เป็นสีชมพู (accent_pink) ตามที่คุณต้องการ และตัวหนังสือสีดำ (หรือสีเดิม)
            holder.txtKbn.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#FF65A3"))
            holder.txtKbn.setTextColor(Color.BLACK) // หากต้องการให้ตัวหนังสือตอนเป็นสีชมพูเป็นสีขาวด้วย ให้เปลี่ยนเป็น Color.WHITE ครับ
        }

        holder.itemView.setOnClickListener {
            onItemClick(item)
        }

    }

    override fun getItemCount(): Int {
        return dataList.size
    }
}