package com.example.pmlcheckk

import android.content.Intent
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
import androidx.room.Room
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Screen4Activity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private lateinit var adapter: PartListAdapter

    private lateinit var txtHeaderAddress: TextView
    private lateinit var txtRemainCount: TextView
    private lateinit var recyclerViewPartList: RecyclerView
    private lateinit var btnEdit: Button
    private lateinit var btnNext: Button

    private var myPartList = mutableListOf<InventoryItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_screen4)

        txtHeaderAddress = findViewById(R.id.txtHeaderAddress)
        txtRemainCount = findViewById(R.id.txtRemainCount)
        recyclerViewPartList = findViewById(R.id.recyclerViewPartList)
        btnEdit = findViewById(R.id.btnEdit)
        btnNext = findViewById(R.id.btnNext)

        db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "pml_db"
        ).build()

        val selectedAddress = intent.getStringExtra("SELECTED_ADDRESS") ?: "M01: PART LIST"
        txtHeaderAddress.text = selectedAddress

        recyclerViewPartList.layoutManager = LinearLayoutManager(this)
        adapter = PartListAdapter(myPartList)
        recyclerViewPartList.adapter = adapter

        loadDataFromDatabase(selectedAddress)

        btnEdit.setOnClickListener { }
        btnNext.setOnClickListener { }
    }

    private fun loadDataFromDatabase(groupName: String) {
        lifecycleScope.launch {
            val items = withContext(Dispatchers.IO) {
                db.inventoryDao().getItemsByGroup(groupName)
            }
            myPartList.clear()
            myPartList.addAll(items)
            txtRemainCount.text = myPartList.size.toString()
            adapter.notifyDataSetChanged()
        }
    }
}

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

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = dataList[position]

        holder.txtNo.text = (position + 1).toString()
        holder.txtSupplier.text = item.sup ?: "-"
        holder.txtKbn.text = item.kbn ?: "-"
        holder.txtAddress.text = item.fullAddr

        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, Screen5Activity::class.java)
            // ส่งข้อมูลเดิม
            intent.putExtra("PART_NO", item.partNo)
            intent.putExtra("PART_NAME", item.partName)
            intent.putExtra("SUPPLIER", item.sup)

            // --- เพิ่ม 3 บรรทัดนี้เข้าไปใหม่ ---
            intent.putExtra("KBN", item.kbn)
            intent.putExtra("FULL_ADDR", item.fullAddr)
            intent.putExtra("QTY", item.qty ?: 0)

            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return dataList.size
    }
}
