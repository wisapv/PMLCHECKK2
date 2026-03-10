package com.example.pmlcheckk

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface InventoryDao {
    // 1. เพิ่ม : Int เข้าไปด้านหลัง เพื่อให้มันส่งตัวเลขจำนวนแถวที่ลบกลับมา (หลอกไม่ให้เป็นค่า Void)
    @Query("DELETE FROM inventory_table")
    suspend fun clearAll(): Int

    // 2. เพิ่ม : List<Long> เข้าไป เพื่อให้มันส่งตัวเลข ID ของแถวที่เพิ่มเสร็จกลับมา
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<InventoryItem>): List<Long>

    // 3. 2 ตัวล่างนี้ดึงข้อมูลกลับมาเป็น List อยู่แล้ว ไม่ติดปัญหาอะไรครับ เก็บไว้เหมือนเดิม
    @Query("SELECT DISTINCT addrGroup FROM inventory_table WHERE addrGroup != '' ORDER BY addrGroup ASC")
    suspend fun getAllAddrGroups(): List<String>

    @Query("SELECT * FROM inventory_table WHERE addrGroup = :groupName")
    suspend fun getItemsByGroup(groupName: String): List<InventoryItem>
}