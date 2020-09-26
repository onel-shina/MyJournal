package com.onelshina.myjournal.data

import androidx.lifecycle.LiveData
import androidx.room.*
import java.time.LocalDate

@Dao
interface EventDao {
    @Query("SELECT * FROM event_table")
    fun getAllEvents(): LiveData<List<Event>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: Event) : Long

    @Delete
    suspend fun deleteEvent(event: Event)

    @Query("DELETE FROM event_table")
    suspend fun deleteAllEntries()

    @Query("DELETE FROM event_table where date=:date")
    suspend fun deleteEventByDate(date: LocalDate)

    @Query("SELECT * FROM event_table WHERE id=:id ")
    fun getSingleEvent(id: Int): LiveData<Event>
}