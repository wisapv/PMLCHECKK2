package com.example.pmlcheckk

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Screen2Activity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private lateinit var btnLoadList: Button
    private lateinit var btnSelectAddress: LinearLayout
    private lateinit var txtUpdated: TextView
    private lateinit var btnFreeZone: LinearLayout
    private lateinit var txtSelectAction: TextView
    private lateinit var btnExport: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_screen2)

        btnLoadList = findViewById(R.id.btnLoadList)
        btnSelectAddress = findViewById(R.id.btnSelectAddress)
        txtUpdated = findViewById(R.id.txtUpdated)
        btnFreeZone = findViewById(R.id.btnFreeZone)
        txtSelectAction = findViewById(R.id.txtSelectAction)
        btnExport = findViewById(R.id.btnExport)

        db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "pml_db").build()

        btnLoadList.setOnClickListener {
            Toast.makeText(this, "Updating Data...", Toast.LENGTH_SHORT).show()
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val cloudData = RetrofitInstance.api.getInventoryList()
                    db.inventoryDao().clearAll()
                    val processedData = cloudData.map { item ->
                        val group = if (item.fullAddr.length >= 3) item.fullAddr.take(3) else item.fullAddr
                        item.copy(addrGroup = group)
                    }
                    db.inventoryDao().insertAll(processedData)

                    withContext(Dispatchers.Main) {
                        btnLoadList.text = "Reload List from Cloud"
                        txtUpdated.visibility = View.VISIBLE
                        txtSelectAction.visibility = View.VISIBLE
                        btnSelectAddress.visibility = View.VISIBLE
                        btnFreeZone.visibility = View.VISIBLE
                        btnExport.visibility = View.GONE
                        Toast.makeText(this@Screen2Activity, "Updated Successfully!", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) { Toast.makeText(this@Screen2Activity, "Error: ${e.message}", Toast.LENGTH_LONG).show() }
                }
            }
        }

        btnSelectAddress.setOnClickListener {
            startActivity(Intent(this, Screen3Activity::class.java))
        }

        btnFreeZone.setOnClickListener {
            startActivity(Intent(this, FreeZoneSummaryActivity::class.java))
        }

        btnExport.setOnClickListener {
            Toast.makeText(this, "เตรียมส่งออกข้อมูล...", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        checkDatabaseState()
    }

    private fun checkDatabaseState() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val hasData = db.inventoryDao().getAllAddrGroups().isNotEmpty()
                val completedCount = db.inventoryDao().getCompletedItemsCount()

                withContext(Dispatchers.Main) {
                    if (hasData) {
                        btnLoadList.text = "Reload List from Cloud"
                        txtUpdated.visibility = View.VISIBLE
                        txtSelectAction.visibility = View.VISIBLE
                        btnSelectAddress.visibility = View.VISIBLE
                        btnFreeZone.visibility = View.VISIBLE
                        btnExport.visibility = if (completedCount > 0) View.VISIBLE else View.GONE
                    } else {
                        btnLoadList.text = "Load List from Cloud"
                        txtUpdated.visibility = View.GONE
                        txtSelectAction.visibility = View.GONE
                        btnSelectAddress.visibility = View.GONE
                        btnFreeZone.visibility = View.GONE
                        btnExport.visibility = View.GONE
                    }
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }
}