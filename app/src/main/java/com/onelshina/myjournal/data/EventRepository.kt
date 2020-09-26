package com.onelshina.myjournal.data

import androidx.lifecycle.LiveData
import java.time.LocalDate

class EventRepository(private val eventDao: EventDao) {

    // Room executes all queries on a separate thread.
    // Observed LiveData will notify the observer when the data has changed.
    fun getAllEvents() : LiveData<List<Event>> {
        return eventDao.getAllEvents()
    }

    fun getSingleEvent(id : Int) : LiveData<Event> {
        return eventDao.getSingleEvent(id = id)
    }

    suspend fun insertEvent(event: Event) {
        eventDao.insertEvent(event)
    }

    suspend fun deleteEvent(event: Event) {
        eventDao.deleteEvent(event)
    }

    suspend fun deleteAllEntries() {
        eventDao.deleteAllEntries()
    }

    suspend fun deleteEventByDate(date: LocalDate) {
        eventDao.deleteEventByDate(date)
    }
}