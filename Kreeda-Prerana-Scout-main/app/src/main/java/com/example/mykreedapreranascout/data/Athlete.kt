package com.example.mykreedapreranascout.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "athlete")
data class Athlete(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val age: Int,
    val rollNumber: String,
    val studentClass: String,
    val school: String,
    val sport: String,
    val photoUri: String?,
    val earnedBadge: String? = null,
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)