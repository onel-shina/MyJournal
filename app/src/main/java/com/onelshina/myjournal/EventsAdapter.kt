package com.onelshina.myjournal

import android.content.Context
import android.content.Intent
import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.onelshina.myjournal.data.Event
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*


class EventsAdapter internal constructor(
    context: Context
) : RecyclerView.Adapter<EventsAdapter.ViewHolder>() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private var events = emptyList<Event>() // Cached copy of words
    private var allEvents = emptyList<Event>()

    // getting user device time format
    private val timeFormat = DateFormat.is24HourFormat(context)
    private val mainActivityContext = context

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val emotionsTextView: TextView = itemView.findViewById(R.id.emotionsTextView)
        val ratingTextView: TextView = itemView.findViewById(R.id.ratingTextView)
        val ratingImageView: ImageView = itemView.findViewById(R.id.ratingImageView)

        val timeTextView: TextView = itemView.findViewById(R.id.timeTextView)
        val editEventRowButton: FloatingActionButton =
            itemView.findViewById(R.id.editEventRowButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = inflater.inflate(R.layout.event_row, parent, false)
        return ViewHolder(itemView)

    }

    fun getEventAt(position: Int): Event? {
        return events[position]
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val current = events[position]
        val emotions = current.emotions.toMutableList()
        emotions.removeIf(String::isEmpty)

        // assigning emotions to emotionsTextView
        if (emotions.isEmpty()) {
            holder.emotionsTextView.text = "No Emotions Specified"
        } else {
            holder.emotionsTextView.text = emotionsListToString(emotions)
        }

        // if satisfaction was not entered, set ratingTextView as invisible
        if (current.satisfaction == 0) {
            holder.ratingTextView.visibility = View.INVISIBLE
            holder.ratingImageView.visibility = View.INVISIBLE
        } else {
            holder.ratingTextView.text = current.satisfaction.toString()
        }
        val hour = current.entryTime.get(Calendar.HOUR_OF_DAY)
        val minute = current.entryTime.get(Calendar.MINUTE)
        val timeString = "$hour:$minute"
        if (timeFormat) {
            holder.timeTextView.text = timeString
        } else {
            // formatting time to AM/PM if user's device format is not 24 hr
            val twelveHourSDF = SimpleDateFormat("hh:mm a")
            val twentyFourHourSDF = SimpleDateFormat("HH:mm")
            val twentyFourHourDt: Date = twentyFourHourSDF.parse(timeString)
            holder.timeTextView.text = twelveHourSDF.format(twentyFourHourDt)
        }

        // editing journal button after it has been created
        holder.editEventRowButton.setOnClickListener { v ->
            val intent = Intent(mainActivityContext, EditEventActivity::class.java)
            val date = current.date
            val dateFormatted: String
            dateFormatted = if (date == LocalDate.now()) {
                "Today; " + date.format(
                    DateTimeFormatter.ofLocalizedDate(
                        FormatStyle.LONG
                    )
                )
            } else {
                date.format(
                    DateTimeFormatter.ofLocalizedDate(
                        FormatStyle.LONG
                    )
                )
            }
            intent.putExtra("id", current.id)
            intent.putExtra("date", date.toEpochDay())
            intent.putExtra("date_formatted", dateFormatted)

            v?.context?.startActivity(intent)
        }
    }
    fun setEvents(events: List<Event>, date: LocalDate) {
        this.allEvents = events

        // to filter out events based on a specific date
        setEventByDate(date)
    }

    fun setEventByDate(date: LocalDate) {
        this.events = allEvents.filter { it.date == date }
        notifyDataSetChanged()
    }

    override fun getItemCount() = events.size


    // converting emotions list to string to be shown on recycler view rows
    private fun emotionsListToString(list: List<String>): String {

        var resultString = ""
        val listLength = list.size

            // example: Sad
            if(listLength == 1){
                resultString = list[0].capitalize(Locale.ROOT)
            }
            // example: Sad and Angry
            else if (listLength == 2){
                resultString = "${list[0].capitalize(Locale.ROOT)} and ${list[1].capitalize(Locale.ROOT)}"
            }
            // example: Sad, Nervous, and Angry

            else {
                for ((index, emotion) in list.withIndex()) {
                    resultString += if (index == listLength - 1) {
                        "and ${emotion.capitalize(Locale.ROOT)}"
                    } else {
                        "${emotion.capitalize(Locale.ROOT)}, "
                    }
                }
            }
            return resultString

    }

}