package com.onelshina.myjournal

import android.app.Application
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.android.material.button.MaterialButtonToggleGroup
import com.jaredrummler.materialspinner.MaterialSpinner
import com.onelshina.myjournal.R
import com.onelshina.myjournal.data.Event
import com.onelshina.myjournal.data.EventViewModel
import kotlinx.android.synthetic.main.fragment_visualize_data.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle


class VisualizeDataFragment : Fragment() {
    var buttonActivatedId: Int? = null
    private lateinit var timeFrameToggleGroup: MaterialButtonToggleGroup
    private lateinit var spinner: MaterialSpinner
    private lateinit var pieChart: PieChart
    private lateinit var barChart: BarChart
    private lateinit var timeFrameTextView : TextView

    // List of different charts to choose from
    private val spinnerItems = listOf(
        "Emotions Pie Chart",
        "Satisfaction Pie Chart",
        "Weekday Satisfaction Bar Chart"
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val visualizeDataFragmentView =
            inflater.inflate(R.layout.fragment_visualize_data, container, false)

        timeFrameToggleGroup = visualizeDataFragmentView.findViewById(R.id.timeFrameToggleGroup)
        spinner = visualizeDataFragmentView.findViewById(R.id.spinner)
        pieChart = visualizeDataFragmentView.findViewById(R.id.pieChart)
        barChart = visualizeDataFragmentView.findViewById(R.id.barChart)
        timeFrameTextView = visualizeDataFragmentView.findViewById(R.id.timeFrameTextView)

        val eventViewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)
        ).get(
            EventViewModel::class.java
        )

        // list that will hold all events
        var eventsList = emptyList<Event>()


        // getting the id of activated button in the button toggle group
        buttonActivatedId = timeFrameToggleGroup.checkedButtonId

        timeFrameToggleGroup.addOnButtonCheckedListener { _: MaterialButtonToggleGroup, i: Int, _: Boolean ->
            buttonActivatedId = i
            // updating the pieChart or the barChart whenever the user changes time frame
            updateChart(spinner.selectedIndex, eventsList, buttonActivatedId!!)
        }

        eventViewModel.allEvents.observe(viewLifecycleOwner, Observer { events ->
            eventsList = events
        })


        spinner.setItems(spinnerItems)
        spinner.textSize = 16f
        spinner.setOnItemSelectedListener { parent, position, id, item ->
            noDataTextView.visibility = View.INVISIBLE
            // updating the pieChart or barChart whenever user switches between different charts
            updateChart(position, eventsList, buttonActivatedId!!)
        }

        // Inflate the layout for this fragment
        return visualizeDataFragmentView
    }

    private fun showEmotionsPieChart(list: List<Event>) {
        if (list.isNotEmpty()) {
            var emotions = mutableMapOf<String, Int>(
                "Happy" to 0,
                "Sad" to 0,
                "Angry" to 0,
                "Nervous" to 0,
                "Sick" to 0
            )
            for (event in list) {
                for (emotion in event.emotions) {
                    emotions[emotion] = emotions.getOrDefault(emotion, 0) + 1
                }
            }
            val entries: ArrayList<PieEntry> = ArrayList()
            emotions.forEach { (k, v) ->
                if (v > 0 && k.isNotEmpty()) {
                    entries.add(PieEntry(v.toFloat(), k))
                }
            }

            val pieDataSet = PieDataSet(entries, "")

            pieDataSet.valueTextSize = 20f
            pieDataSet.yValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
            pieDataSet.xValuePosition = PieDataSet.ValuePosition.INSIDE_SLICE


            pieDataSet.setColors(*ColorTemplate.COLORFUL_COLORS)
            val pieData = PieData(pieDataSet)

            pieChart.data = pieData

            pieChart.description.isEnabled = false
            pieChart.legend.isEnabled = false
            pieChart.setEntryLabelTextSize(14f)

            pieChart.setExtraOffsets(15f, 15f, 15f, 15f)


            pieChart.animateXY(350, 350)
            pieChart.visibility = View.VISIBLE
            resetPieChart()

        }
    }

    private fun showSatisfactionPieChart(list: List<Event>) {
        if (list.isNotEmpty()) {
            var satisfactions = mutableMapOf<Int, Int>(
                1 to 0,
                2 to 0,
                3 to 0,
                4 to 0,
                5 to 0,
            )
            for (event in list) {
                satisfactions[event.satisfaction] = satisfactions.getOrDefault(
                    event.satisfaction,
                    0
                ) + 1
            }
            val entries: ArrayList<PieEntry> = ArrayList()
            satisfactions.forEach { (k, v) ->
                if (v > 0)
                    entries.add(PieEntry(v.toFloat(), "$k★"))
            }

            val pieDataSet = PieDataSet(entries, "")

            pieDataSet.valueTextSize = 20f
            pieDataSet.yValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
            pieDataSet.xValuePosition = PieDataSet.ValuePosition.INSIDE_SLICE


            pieDataSet.setColors(*ColorTemplate.COLORFUL_COLORS)
            val pieData = PieData(pieDataSet)

            pieChart.setEntryLabelTextSize(20f)
            pieChart.data = pieData
            pieChart.description.isEnabled = false
            pieChart.legend.isEnabled = false

            pieChart.setExtraOffsets(15f, 15f, 15f, 15f)
            pieChart.animateXY(350, 350)
            pieChart.visibility = View.VISIBLE
            resetPieChart()

        }
    }

    private fun showWeekDaySatisfactionBarChart(list: List<Event>) {
        val weekDays : Array<ArrayList<Int>> = arrayOf(
            ArrayList(), // mondays
            ArrayList(), // tuesdays
            ArrayList(), // wednesdays
            ArrayList(), // thursdays
            ArrayList(), // fridays
            ArrayList(), // saturdays
            ArrayList(), // sundays
            )

        for (event in list) {
            when (event.date.dayOfWeek.name) {
                "MONDAY" -> weekDays[0].add(event.satisfaction)
                "TUESDAY" -> weekDays[1].add(event.satisfaction)
                "WEDNESDAY" -> weekDays[2].add(event.satisfaction)
                "THURSDAY" -> weekDays[3].add(event.satisfaction)
                "FRIDAY" -> weekDays[4].add(event.satisfaction)
                "SATURDAY" -> weekDays[5].add(event.satisfaction)
                "SUNDAY" -> weekDays[6].add(event.satisfaction)
            }
        }
        val entries: ArrayList<BarEntry> = ArrayList()
        for((index, value) in weekDays.withIndex()){
            entries.add(BarEntry(index.toFloat(), value.average().toFloat()))
        }
        val barDataSet = BarDataSet(entries, "")
        barDataSet.setDrawValues(false)
        barDataSet.color =
            activity?.applicationContext?.let { ContextCompat.getColor(it, R.color.colorPrimary) }!!


        val barData = BarData(barDataSet)

        barChart.data = barData
        barChart.description.isEnabled = false
        barChart.legend.isEnabled = false

        val xAxisLabels = listOf(
            "Monday",
            "Tuesday",
            "Wednesday",
            "Thursday",
            "Friday",
            "Saturday",
            "Sunday"
        )
        barChart.xAxis.valueFormatter = IndexAxisValueFormatter(xAxisLabels)
        barChart.xAxis.labelRotationAngle = 45f
        barChart.setVisibleXRange(0f, 7f)
        barChart.xAxis.granularity = 1f
        barChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        barChart.xAxis.textSize = 16f
        barChart.axisLeft.textSize = 12f
        barChart.axisLeft.setDrawGridLines(false)
        barChart.axisLeft.setDrawGridLinesBehindData(false)
        barChart.axisRight.isEnabled = false
        barChart.axisLeft.setLabelCount(6, true);
        barChart.axisLeft.axisMaximum = 5f
        barChart.axisLeft.axisMinimum = 0f

        barChart.setExtraOffsets(10f, 10f, 10f, 10f)

        barChart.visibility = View.VISIBLE
        barChart.notifyDataSetChanged()
        barChart.invalidate()
        barChart.refreshDrawableState()
    }


    private fun resetPieChart() {
        pieChart.notifyDataSetChanged()
        pieChart.invalidate()
        pieChart.refreshDrawableState()
    }

    private fun updateChart(spinnerChoice: Int, eventsList: List<Event>, buttonActivatedId: Int) {
        var updatedEventsList = emptyList<Event>().toMutableList()
        val localDateToday = LocalDate.now()
        // Checking which time frame button is selected
        when (buttonActivatedId) {
            // if max is selected show all events
            maxButton.id -> {
                updatedEventsList = eventsList as MutableList<Event>
                timeFrameTextView.text = "Showing All Data"
            }

            // if one year time frame is selected
            oneYearButton.id -> {
                val localDateStart = localDateToday.minusYears(1)
                for (event in eventsList) {
                    if (((event.date == localDateToday) || (event.date.isBefore(localDateToday)))
                        && (event.date.isAfter(localDateStart))
                    ) {
                        updatedEventsList.add(event)
                    }
                    timeFrameTextView.text = "Showing Data From ${formatDate(localDateStart)} to ${formatDate(localDateToday)}"
                }
            }

            // if one month time frame is selected
            oneMonthButton.id -> {
                val localDateStart = localDateToday.minusMonths(1)
                for (event in eventsList) {
                    if (((event.date == localDateToday) || (event.date.isBefore(localDateToday)))
                        && (event.date.isAfter(localDateStart))
                    ) {
                        updatedEventsList.add(event)
                    }
                }
                timeFrameTextView.text = "Showing Data From ${formatDate(localDateStart)} to ${formatDate(localDateToday)}"
            }

            // if one week time frame is selected
            oneWeekButton.id -> {
                val localDateStart = localDateToday.minusWeeks(1)
                for (event in eventsList) {
                    if (((event.date == localDateToday) || (event.date.isBefore(localDateToday)))
                        && (event.date.isAfter(localDateStart))
                    ) {
                        updatedEventsList.add(event)
                    }
                }
                timeFrameTextView.text = "Showing Data From ${formatDate(localDateStart)} to ${formatDate(localDateToday)}"

            }
        }


        // showing different charts based on what the user has selected in the spinner
        when (spinnerItems[spinnerChoice]) {
            "Emotions Pie Chart" -> {
                chartDescriptionTextView.text = "Number Of Times Each Emotions\n Was Logged"
                chartConstraintLayout.visibility = View.VISIBLE
                barChart.visibility = View.INVISIBLE
                showEmotionsPieChart(updatedEventsList)
            }
            "Satisfaction Pie Chart" -> {
                chartDescriptionTextView.text =
                    "Number Of Times Each Satisfaction Rate\n Was Logged"
                chartConstraintLayout.visibility = View.VISIBLE
                barChart.visibility = View.INVISIBLE
                showSatisfactionPieChart(updatedEventsList)
            }
            "Weekday Satisfaction Bar Chart" -> {
                chartDescriptionTextView.text = "Average Satisfaction Rate By Weekday"
                chartConstraintLayout.visibility = View.VISIBLE
                pieChart.visibility = View.INVISIBLE
                showWeekDaySatisfactionBarChart(updatedEventsList)
            }
        }
    }
    fun formatDate(date: LocalDate): String? {
        return date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG))
    }
}