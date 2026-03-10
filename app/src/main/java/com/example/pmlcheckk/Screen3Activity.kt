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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

// กล่องเก็บข้อมูล
data class AddressData(val addressName: String, var isCompleted: Boolean)

class Screen3Activity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_screen3)

        // ข้อมูลจำลอง (A01 ทำเสร็จแล้ว = true)
        val myAddressList = arrayListOf(
            AddressData("A01: Zone A", true),
            AddressData("A02: Zone B", false),
            AddressData("A03: Zone C", false),
            AddressData("A04: Zone D", false),
            AddressData("A05: Zone E", false)
        )

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewAddress)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val adapter = AddressAdapter(myAddressList)
        recyclerView.adapter = adapter
    }
}

// ------------------------------------------------------------------------
// ส่วนของ Adapter (โรงงานปั๊มปุ่ม)
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

        // --- แก้ไขตรงนี้: ดักจับการกดปุ่มที่ตัว txtAddressName โดยตรง ---
        holder.txtAddressName.setOnClickListener {
            // สร้างคำสั่งย้ายหน้า
            val intent = Intent(holder.itemView.context, Screen4Activity::class.java)

            // แนบชื่อ Address (เช่น A01) ไปให้หน้า 4 ด้วย
            intent.putExtra("SELECTED_ADDRESS", currentItem.addressName)

            // สั่งเปิดหน้า 4
            holder.itemView.context.startActivity(intent)
        }
    }
}