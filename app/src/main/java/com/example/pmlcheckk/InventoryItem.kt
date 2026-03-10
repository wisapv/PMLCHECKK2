package com.example.pmlcheckk

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "inventory_table")
data class InventoryItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,

    @SerializedName("No") val no: Int? = 0,
    @SerializedName("Dock") val dock: String? = "",
    @SerializedName("Sup") val sup: String? = "",
    @SerializedName("Splant") val splant: String? = "",
    @SerializedName("Sdock") val sdock: String? = "",
    @SerializedName("PartNo") val partNo: String? = "",
    @SerializedName("PartName") val partName: String? = "",
    @SerializedName("Kbn") val kbn: String? = "",
    @SerializedName("Qty") val qty: Int? = 0,
    @SerializedName("fullAddr") val fullAddr: String = "",
    @SerializedName("Box") val box: String? = "",
    @SerializedName("Pcs") val pcs: String? = "",
    @SerializedName("Seq") val seq: String? = "",
    @SerializedName("LastOrder") val lastOrder: String? = "",

    val addrGroup: String = "" // เอาไว้เก็บชื่อ 3 ตัวหน้าที่แอปเราหั่นเอง
)