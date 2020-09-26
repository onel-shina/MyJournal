package com.onelshina.myjournal.data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.time.LocalDate

class EventViewModel (application: Application) : AndroidViewModel(application) {

    private val repository: EventRepository
    // Using LiveData and caching what getAlphabetizedWords returns has several benefits:
    // - We can put an observer on the data (instead of polling for changes) and only update the
    //   the UI when the data actually changes.
    // - Repository is completely separated from the UI through the ViewModel.
    val allEvents : LiveData<List<Event>>

    init {
        val eventDao = AppDatabase.getDatabase(application).eventDao()
        repository = EventRepository(eventDao)
        allEvents = repository.getAllEvents()
    }

    fun insertEvent(event : Event) = viewModelScope.launch(Dispatchers.IO) {
        repository.insertEvent(event)
    }

    fun deleteEvent(event : Event) = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteEvent(event)
    }

    fun deleteEventByDate(date: LocalDate) = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteEventByDate(date)
    }

    fun deleteAllEntries() = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteAllEntries()
    }
    fun getEventById(id: Int): LiveData<Event> {
       return repository.getSingleEvent(id)
    }


}