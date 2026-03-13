package com.example.pmlcheckk

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface InventoryDao {
    @Query("DELETE FROM inventory_table")
    suspend fun clearAll(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<InventoryItem>): List<Long>

    @Query("SELECT DISTINCT addrGroup FROM inventory_table WHERE addrGroup != '' ORDER BY addrGroup ASC")
    suspend fun getAllAddrGroups(): List<String>

    @Query("SELECT * FROM inventory_table WHERE addrGroup = :groupName")
    suspend fun getItemsByGroup(groupName: String): List<InventoryItem>

    // --- เพิ่มคำสั่งนี้เข้าไปใหม่เพื่อใช้บันทึก (อัปเดต) ข้อมูล Box, Pcs, Seq ---
    @Query("UPDATE inventory_table SET Box = :box, Pcs = :pcs, Seq = :seq WHERE id = :id")
    suspend fun updateStockData(id: Int, box: String, pcs: String, seq: String)

    // เพิ่มคำสั่งนี้นับจำนวนรายการที่กรอก Box แล้ว
    @Query("SELECT COUNT(*) FROM inventory_table WHERE Box IS NOT NULL AND Box != ''")
    suspend fun getCompletedItemsCount(): Int
}