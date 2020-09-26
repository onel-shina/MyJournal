package com.onelshina.myjournal

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.applandeo.materialcalendarview.CalendarView
import com.applandeo.materialcalendarview.EventDay
import com.applandeo.materialcalendarview.listeners.OnDayClickListener
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.nambimobile.widgets.efab.ExpandableFab
import com.nambimobile.widgets.efab.FabOption
import com.onelshina.myjournal.data.AppDatabase
import com.onelshina.myjournal.data.Event
import com.onelshina.myjournal.data.EventViewModel
import kotlinx.android.synthetic.main.fragment_journal.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*
import kotlin.collections.ArrayList

class JournalFragment : Fragment() {
    lateinit var recyclerView: RecyclerView
    lateinit var database: AppDatabase
    private lateinit var eventViewModel: EventViewModel
    private lateinit var calendarView : CalendarView
    private lateinit var addEventButton: FloatingActionButton
    private lateinit var deleteExpandableFab: ExpandableFab
    private lateinit var deleteAllFabOption: FabOption
    private lateinit var deleteOfDateFabOption: FabOption


    private lateinit var dateTextViewEditEvent : TextView

    var calendars: MutableList<EventDay> = ArrayList()
    lateinit var adapter: EventsAdapter
    lateinit var date: LocalDate


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View?
    {   val journalFragmentView : View = inflater.inflate(R.layout.fragment_journal, container, false)
        recyclerView = journalFragmentView.findViewById(R.id.eventsRecyclerView)
        dateTextViewEditEvent = journalFragmentView.findViewById(R.id.dateTextViewEditEvent)
        calendarView = journalFragmentView.findViewById(R.id.calendarView)
        addEventButton = journalFragmentView.findViewById(R.id.addEventButton)
        deleteExpandableFab = journalFragmentView.findViewById(R.id.deleteExpandableFab)
        deleteAllFabOption = journalFragmentView.findViewById(R.id.deleteAllFabOption)
        deleteOfDateFabOption = journalFragmentView.findViewById(R.id.deleteOfDateFabOption)


        database = AppDatabase.getDatabase(requireContext())
            eventViewModel = ViewModelProvider(
                this,
                ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)
            ).get(
                EventViewModel::class.java
            )

            adapter = EventsAdapter(requireContext())

            recyclerView.adapter = adapter
            recyclerView.layoutManager = LinearLayoutManager(requireContext())


            date = LocalDate.now()
            var dateFormatted: String? = "Today; ${formatDate(date)}"
            dateTextViewEditEvent.text = dateFormatted

            eventViewModel.allEvents.observe(viewLifecycleOwner, Observer { events ->
                adapter.setEvents(events, date)
                updateCalendarEvents(events)
            })




            calendarView.setOnDayClickListener(object : OnDayClickListener {

                override fun onDayClick(eventDay: EventDay) {
                    calendarView.invalidateOutline()
                    calendarView.refreshDrawableState()
                    calendarView.setDate(eventDay.calendar)

                    date = LocalDateTime.ofInstant(eventDay.calendar.time.toInstant(), ZoneOffset.UTC).toLocalDate()
                    adapter.setEventByDate(date)
                    eventsRecyclerView.refreshDrawableState()
                    dateFormatted = if (date == LocalDate.now()) {
                        "Today; ${formatDate(date)}"
                    } else {
                        formatDate(date)
                    }
                    dateTextViewEditEvent.text = dateFormatted
                }
            })


            ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
                0,
                ItemTouchHelper.LEFT
            ) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    return false
                }


                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val builder = AlertDialog.Builder(requireContext())
                    builder.setMessage("Are you sure you want to delete this entry?")
                        .setCancelable(false).setIcon(
                            resources.getDrawable(
                                android.R.drawable.ic_menu_delete, resources.newTheme()
                            )
                        )
                        .setTitle("Deleting Journal Entry")
                        .setPositiveButton("Yes") { dialog, id ->
                            adapter.getEventAt(viewHolder.adapterPosition)?.let {
                                eventViewModel.deleteEvent(it)
                            }
                        }
                        .setNegativeButton("No") { dialog, id ->
                            adapter.notifyItemChanged(viewHolder.adapterPosition);
                            dialog.dismiss()
                        }
                    val alert = builder.create()
                    alert.show()
                }
            }).attachToRecyclerView(recyclerView)

            addEventButton.setOnClickListener {
                val intent = Intent(requireActivity(), EditEventActivity::class.java)
                intent.putExtra("date_formatted", dateFormatted)
                intent.putExtra("date", date.toEpochDay())
                startActivity(intent)

            }



            // Deleting all journal entries
            deleteAllFabOption.setOnClickListener { displayDeletingDialog(null) }
            // Deleting journal entries for the selected date
            deleteOfDateFabOption.setOnClickListener { displayDeletingDialog(date)}

        return journalFragmentView
    }
    fun formatDate(date: LocalDate): String? {
        return date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG))
    }

    // Updating calendar view with icons for dates that a journal entry has been added to
    private fun updateCalendarEvents(events: List<Event>) {
        calendars = ArrayList()
        events.forEach {
            var calendar: Calendar? = Calendar.getInstance()
            calendar?.time = Date.from(it.date.atStartOfDay(ZoneId.systemDefault()).toInstant())
            if (calendar != null) {
                calendars.add(EventDay(calendar, R.drawable.ic_report))
            }
        }
        calendarView.setEvents(calendars)
    }

    private fun displayDeletingDialog(date: LocalDate?){
        var message = ""
        message = if(date == null){
            "Are you sure you want to delete all journal entries?"
        } else {
            "Are you sure you want to delete ${formatDate(date)} journal entries"
        }

        val builder = AlertDialog.Builder(requireContext())
        builder.setMessage(message)
            .setCancelable(false).setIcon(
                resources.getDrawable(
                    android.R.drawable.ic_menu_delete, resources.newTheme()
                )
            )
            .setTitle("Deleting Journal Entries")
            .setNegativeButton("No") { dialog, id ->
                dialog.dismiss()
            }
            .setPositiveButton("Yes"){ dialog, id ->
                if(date == null){
                    eventViewModel.deleteAllEntries()
                }
                else {
                    eventViewModel.deleteEventByDate(date)
                }
            }
        val alert = builder.create()
        alert.show()
    }
}