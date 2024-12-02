package com.appfiction.periodpicker

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.appfiction.periodpicker.databinding.PickerModalBinding
import com.savvi.rangedatepicker.CalendarPickerView
import java.util.Calendar
import java.util.Date

class PeriodPickerModal : AppCompatActivity() {

    // View binding property
    private lateinit var binding: PickerModalBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize binding
        binding = PickerModalBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Set up Toolbar and Up button
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true) // Show the Up button (close button)
            setHomeAsUpIndicator(R.drawable.ic_close) // Set your close button icon here
        }

        // Set up the CalendarPickerView
        val today = Date()
        val nextYear = Calendar.getInstance().apply {
            add(Calendar.YEAR, 1)
        }.time

        binding.calendarPickerView.init(today, nextYear)
            .inMode(CalendarPickerView.SelectionMode.RANGE)

        // Save button click listener
        binding.btnSave.setOnClickListener {
            val selectedDates = binding.calendarPickerView.selectedDates
            if (selectedDates.isNotEmpty()) {
                val startDate = selectedDates.first()
                val endDate = selectedDates.last()

                val resultIntent = Intent().apply {
                    putExtra("startDate", startDate.time)
                    putExtra("endDate", endDate.time)
                }
                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            } else {
                Toast.makeText(this, "Please select a date range", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Handle Up button click
    override fun onSupportNavigateUp(): Boolean {
        setResult(Activity.RESULT_CANCELED) // Optional: Notify that no result is returned
        finish() // Close the activity
        return true
    }
}