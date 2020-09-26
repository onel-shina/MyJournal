package com.onelshina.myjournal

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.onelshina.myjournal.data.AppDatabase
import com.onelshina.myjournal.data.Event
import com.onelshina.myjournal.data.EventViewModel
import kotlinx.android.synthetic.main.activity_edit_event.*
import java.time.LocalDate
import java.util.*


class EditEventActivity : AppCompatActivity() {
    lateinit var database: AppDatabase
    private lateinit var eventViewModel: EventViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_event)

        database = AppDatabase.getDatabase(this)
        eventViewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(this.application)
        ).get<EventViewModel>(
            EventViewModel::class.java
        )

        val intent = intent
        val dateFormatted = intent.getStringExtra("date_formatted")
        val date = LocalDate.ofEpochDay(intent.getLongExtra("date", 0))
        val id = intent.getIntExtra("id", -1)

        dateTextViewEditEvent.text = dateFormatted
        val emotionsList = mutableListOf<String>()
        val emotionsLinearLayout : List<LinearLayout> = listOf(happy, sad, angry, nervous, sick)
        val emotionsImageView : List<ImageView> = listOf(
            happyImageView,
            sadImageView,
            angryImageView,
            nervousImageView,
            sickImageView
        )

        if(id != -1) {
            addEditEventButton.setImageResource(R.drawable.ic_baseline_check_24)
            deleteThisActivityButton.visibility = View.VISIBLE
            eventViewModel.getEventById(id).observe(this, Observer { event ->
                if(event != null){
                satisfactionRatingBar.rating = event.satisfaction.toFloat()
                journalEditText.setText(event.journalText)
                for(emotion in event.emotions){
                    emotionsList.add(emotion)
                }
                for ((index, linearLayout) in emotionsLinearLayout.withIndex()) {
                    val color: Int?
                    val linearLayoutId = idToStringEmotions(linearLayout.id)
                    color = if (linearLayoutId !in emotionsList) {
                        ContextCompat.getColor(applicationContext, R.color.black)
                    } else {
                        ContextCompat.getColor(applicationContext, R.color.emotions)
                    }
                    emotionsImageView[index].setColorFilter(color, PorterDuff.Mode.SRC_IN)
                }}
            })

        }

        addEditEventButton.setOnClickListener {
            if((TextUtils.isEmpty(journalEditText.text.toString())) and (satisfactionRatingBar.rating.toInt()==0)){
                Toast.makeText(this, "Empty Entries Are Not Allowed", Toast.LENGTH_SHORT).show()
            }
            else {
                val calendar: Calendar = Calendar.getInstance(Locale.getDefault())
                val event : Event
                if(id == -1){
                    event = Event(
                        date = date,
                        journalText = journalEditText.text.toString(),
                        emotions = emotionsList,
                        satisfaction = satisfactionRatingBar.rating.toInt(),
                        entryTime = calendar
                    )
                }
                else {
                    event = Event(
                        id = id,
                        date = date,
                        journalText = journalEditText.text.toString(),
                        emotions = emotionsList,
                        satisfaction = satisfactionRatingBar.rating.toInt(),
                        entryTime = calendar
                    )
                }
                eventViewModel.insertEvent(event = event)
                this.finish()
            }

        }

        deleteThisActivityButton.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setMessage("Are you sure you want to delete this entry?")
                .setCancelable(false).setIcon(
                    resources.getDrawable(android.R.drawable.ic_menu_delete, theme)
                )
                .setTitle("Deleting Journal Entry")
                .setPositiveButton("Yes") { _, _ ->

                    val calendar: Calendar = Calendar.getInstance(Locale.getDefault())
                    val event = Event(
                        id = id,
                        date = date,
                        journalText = journalEditText.text.toString(),
                        emotions = emotionsList,
                        satisfaction = satisfactionRatingBar.rating.toInt(),
                        entryTime = calendar
                    )
                    eventViewModel.deleteEvent(event)
                    this.finish()
                }
                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
            val alert = builder.create()
            alert.show()
        }




        for((index, linearLayout) in emotionsLinearLayout.withIndex()){
            linearLayout.setOnClickListener {
                val color : Int?
                val linearLayoutId = idToStringEmotions(linearLayout.id)
                if(linearLayoutId in emotionsList) {
                    color = ContextCompat.getColor(applicationContext, R.color.black)
                    emotionsList.remove(linearLayoutId)
                }
                else {
                    color = ContextCompat.getColor(applicationContext, R.color.emotions)
                    if (linearLayoutId != null) {
                        emotionsList.add(linearLayoutId)
                    }
                }
                emotionsImageView[index].setColorFilter(color, PorterDuff.Mode.SRC_IN)
            }
        }

    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (ev != null) {
            if (ev.action == MotionEvent.ACTION_DOWN) {
                val v: View? = currentFocus;
                if (v is EditText) {
                    val outRect = Rect()
                    v.getGlobalVisibleRect(outRect)
                    if (!outRect.contains(ev.rawX.toInt(), ev.rawY.toInt())) {
                        v.clearFocus()
                        hideKeyboard(v)
                        scrollView.fullScroll(ScrollView.FOCUS_UP);
                    }
                }
            }
        }
        return super.dispatchTouchEvent(ev)
    }


    private fun hideKeyboard(view: View) {
       val inputMethodManager: InputMethodManager =
            getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)

    }
    private fun idToStringEmotions(int: Int): String? {
        return resources.getResourceEntryName(int)
    }



}