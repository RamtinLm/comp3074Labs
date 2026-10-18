package ca.gbc.comp3074.labex4.data

import androidx.room.Entity
import androidx.room.PrimaryKey

// one Customer object = one row in the customers table
@Entity(tableName = "customers")
data class Customer(
    // 0 means "no id yet", SQLite hands out the next one on insert
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val age: Int,
    // Room saves this as 0/1 for me, no manual converting like the SQLite version
    val isActive: Boolean
)
