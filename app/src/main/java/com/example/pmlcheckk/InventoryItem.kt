package com.example.pmlcheckk

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "inventory_table")
data class InventoryItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val Dock: String,
    val Sup: String,
    val Splant: String,
    val Sdock: String, // จากคอลัมน์ Sup ใน Excel
    val PartNo: String,       // จากคอลัมน์ PART NO
    val PartName: String,     // จากคอลัมน์ PartName
    val Kbn: String,          // จากคอลัมน์ Kbn
    val Qty: Int,             // จากคอลัมน์ Q'ty
    val fullAddr: String,     // จากคอลัมน์ Addr
    val addrGroup: String = "", // ตัวนี้แอปจะสร้างให้เองจาก 3 ตัวแรกของ Addr
    val Box: Int,
    val Pcs: Int,
    val Seq: Int,
    val LastOrder: Int,
)