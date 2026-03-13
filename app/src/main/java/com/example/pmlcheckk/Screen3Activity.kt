package com.example.pmlcheckk

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// กล่องเก็บข้อมูล
data class AddressData(val addressName: String, var isCompleted: Boolean)

class Screen3Activity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_screen3)

        recyclerView = findViewById(R.id.recyclerViewAddress)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // 1. เชื่อมต่อ Database
        db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "pml_db"
        ).build()

        val btnBack = findViewById<android.widget.Button>(R.id.btnBack)
        btnBack.setOnClickListener {
            finish() // ปิดหน้า 3 เพื่อกลับไปหน้า 2
        }
    }

    // ========================================================
    // ย้ายการโหลดข้อมูลมาไว้ใน onResume
    // เพื่อให้มันอัปเดตสีปุ่มใหม่ "ทุกครั้ง" ที่กลับมาจากหน้า 4
    // ========================================================
    override fun onResume() {
        super.onResume()
        loadAddressGroups()
    }

    private fun loadAddressGroups() {
        lifecycleScope.launch(Dispatchers.IO) {
            // ไปดึงชื่อกลุ่ม (เช่น BP1, CH1)
            val groups = db.inventoryDao().getAllAddrGroups()

            val myAddressList = ArrayList<AddressData>()

            for (groupName in groups) {
                // ดึงรายการทั้งหมดที่อยู่ในกลุ่ม Address นีัมา
                val itemsInGroup = db.inventoryDao().getItemsByGroup(groupName)

                // เช็คว่ารายการทั้งหมดนี้มีข้อมูล Box ครบทุกตัวแล้วใช่หรือไม่ (Remain = 0)
                // itemsInGroup.all { ... } หมายถึง ทุกตัวต้องตรงตามเงื่อนไข
                val isCompleted = itemsInGroup.isNotEmpty() && itemsInGroup.all { !it.box.isNullOrEmpty() }

                // นำข้อมูลแพ็คใส่กล่อง ถ้าครบแล้ว isCompleted จะเป็น true
                myAddressList.add(AddressData(groupName, isCompleted))
            }

            withContext(Dispatchers.Main) {
                // สั่งให้โรงงานประกอบปุ่มออกมาโชว์บนหน้าจอ
                val adapter = AddressAdapter(myAddressList)
                recyclerView.adapter = adapter
            }
        }
    }
}

// ------------------------------------------------------------------------
// ส่วนของ Adapter (โรงงานปั๊มปุ่มหน้าตาเดิมที่พี่ออกแบบไว้)
// ------------------------------------------------------------------------
class AddressAdapter(private val dataList: ArrayList<AddressData>) :
    RecyclerView.Adapter<AddressAdapter.MyViewHolder>() {

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtAddressName: TextView = itemView.findViewById(R.id.txtAddressName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_address, parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentItem = dataList[position]

        // ใส่ชื่อกลุ่ม (เช่น BP1) ลงในปุ่ม
        holder.txtAddressName.text = currentItem.addressName

        // เปลี่ยนสีถ้า Address นี้ทำเสร็จแล้ว
        if (currentItem.isCompleted) {
            holder.txtAddressName.backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(holder.itemView.context, R.color.status_green)
            )
            holder.txtAddressName.setTextColor(Color.BLACK)
        } else {
            holder.txtAddressName.backgroundTintList = null
            holder.txtAddressName.setTextColor(
                ContextCompat.getColor(holder.itemView.context, R.color.accent_pink)
            )
        }

        // เมื่อกดที่ตัวปุ่มนี้ ให้ส่งชื่อกลุ่มข้ามไปหน้า 4
        holder.txtAddressName.setOnClickListener {
            val intent = Intent(holder.itemView.context, Screen4Activity::class.java)
            intent.putExtra("SELECTED_ADDRESS", currentItem.addressName)
            holder.itemView.context.startActivity(intent)
        }
    }
}