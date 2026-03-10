package com.example.pmlcheckk

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

// 1. สร้างกล่องเก็บข้อมูลสำหรับ 1 แถวในตาราง
data class PartData(val no: String, val supplier: String, val kbn: String, val address: String)

class Screen4Activity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_screen4)

        // 2. รับชื่อ Address ที่เราจิ้มมาจากหน้า 3 (เช่น A01: Zone A) มาแสดงเป็นหัวข้อ
        val selectedAddress = intent.getStringExtra("SELECTED_ADDRESS") ?: "M01: PART LIST"
        val txtHeaderAddress = findViewById<TextView>(R.id.txtHeaderAddress)
        txtHeaderAddress.text = selectedAddress

        // 3. จำลองข้อมูล Part List ที่อยู่ในโซนนี้
        val myPartList = arrayListOf(
            PartData("1", "SAB1A", "A001", "A01"),
            PartData("2", "SAB1A", "A001", "A01"),
            PartData("3", "SAB1A", "A001", "A01"),
            PartData("4", "SAB1A", "A001", "A01"),
            PartData("5", "SAB1A", "A001", "A01")
        )

        // 4. เอาจำนวน Part ไปแสดงที่เลข REMAIN ตัวใหญ่ๆ
        val txtRemainCount = findViewById<TextView>(R.id.txtRemainCount)
        txtRemainCount.text = myPartList.size.toString()

        // 5. ตั้งค่า RecyclerView ให้แสดงผลเป็นตาราง
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewPartList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = PartListAdapter(myPartList)
    }
}

// ------------------------------------------------------------------------
// 6. ส่วนของ Adapter (โรงงานปั๊มแถวตาราง)
// ------------------------------------------------------------------------
class PartListAdapter(private val dataList: ArrayList<PartData>) :
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

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = dataList[position]
        // เอาข้อมูลไปหยอดลงแต่ละคอลัมน์
        holder.txtNo.text = item.no
        holder.txtSupplier.text = item.supplier
        holder.txtKbn.text = item.kbn
        holder.txtAddress.text = item.address
    }
}