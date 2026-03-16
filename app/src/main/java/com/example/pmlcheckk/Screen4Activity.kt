package com.example.pmlcheckk

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Button
import android.widget.Toast
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
    private lateinit var edtFastScan: EditText

    private var myPartList = mutableListOf<InventoryItem>()
    private var selectedAddress: String = ""

    private val startScreen5ForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK || result.resultCode == Activity.RESULT_CANCELED) {
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
        edtFastScan = findViewById(R.id.edtFastScan)

        db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "pml_db").build()

        selectedAddress = intent.getStringExtra("SELECTED_ADDRESS") ?: "M01: PART LIST"
        txtHeaderAddress.text = selectedAddress

        recyclerViewPartList.layoutManager = LinearLayoutManager(this)

        adapter = PartListAdapter(myPartList) { item ->
            openScreen5(item)
        }
        recyclerViewPartList.adapter = adapter

        loadDataFromDatabase(selectedAddress)

        // ปิดไม่ให้คีย์บอร์ดบนหน้าจอเด้งขึ้นมา
        edtFastScan.showSoftInputOnFocus = false

        // ดักจับข้อมูลหลังสแกนเสร็จ (กด Enter)
        edtFastScan.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                val rawBarcode = edtFastScan.text.toString().trim()
                if (rawBarcode.isNotEmpty()) {
                    processFastScan(rawBarcode)
                    // เมื่อเช็คเสร็จ เคลียร์ข้อมูลชุดยาวๆ ในช่องทิ้งทันที
                    edtFastScan.text.clear()
                }
                return@setOnKeyListener true
            }
            false
        }

        btnBack.setOnClickListener { finish() }
        btnEdit.setOnClickListener { showEditDialog() }
    }

    private fun processFastScan(rawBarcode: String) {
        // ลอจิกใหม่: ค้นหาว่าในก้อนข้อความที่สแกนมา มี KBN ของตัวไหนใน Address นี้แฝงอยู่บ้าง
        val foundItem = myPartList.find { item ->
            val kbn = item.kbn?.trim() ?: ""
            kbn.isNotEmpty() && rawBarcode.contains(kbn, ignoreCase = true)
        }

        if (foundItem != null) {
            openScreen5(foundItem)
        } else {
            Toast.makeText(this, "❌ ไม่พบ KBN ที่ตรงกันใน Address นี้!", Toast.LENGTH_LONG).show()
        }
    }

    private fun openScreen5(item: InventoryItem) {
        val intent = Intent(this, Screen5Activity::class.java)
        intent.putExtra("ITEM_ID", item.id)
        intent.putExtra("ITEM_KBN", item.kbn)
        intent.putExtra("ITEM_ADDR", item.fullAddr)
        intent.putExtra("ITEM_PARTNAME", item.partName)
        intent.putExtra("ITEM_BOX", item.box)
        intent.putExtra("ITEM_PCS", item.pcs)
        intent.putExtra("ITEM_SEQ", item.seq)
        intent.putExtra("ITEM_LASTORDER", item.lastOrder)
        intent.putExtra("PART_NO", item.partNo)
        val supName = item.sup ?: "-"
        val splantName = item.splant ?: "-"
        intent.putExtra("SUPPLIER", "$supName-$splantName")
        intent.putExtra("QTY", item.qty ?: 0)

        startScreen5ForResult.launch(intent)
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
                openScreen5(foundItem)
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

            val sortedItems = items.sortedBy { !it.box.isNullOrEmpty() }

            myPartList.clear()
            myPartList.addAll(sortedItems)

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