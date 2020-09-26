package com.onelshina.myjournal.data

import androidx.room.Entity
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.util.*

@Entity(tableName = "event_table")
data class Event(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val date: LocalDate,
    val journalText: String,
    val emotions: List<String>,
    val satisfaction: Int,
    val entryTime: Calendar
)
