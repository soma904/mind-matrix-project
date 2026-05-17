package com.example.mykreedapreranascout.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "trial",
    foreignKeys = [ForeignKey(
        entity = Athlete::class,
        parentColumns = ["id"],
        childColumns = ["athleteId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class Trial(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val athleteId: Int,
    val eventType: String,
    val resultValue: Double,
    val unit: String,
    val trialDate: Long = System.currentTimeMillis()
)