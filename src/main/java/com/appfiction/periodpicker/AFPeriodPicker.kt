package com.appfiction.periodpicker

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.AppCompatImageView
import androidx.fragment.app.Fragment
import com.appfiction.period_picker.R
import com.appfiction.periodpicker.model.Period
import com.skydoves.powerspinner.PowerSpinnerView
import java.util.Date

class AFPeriodPicker @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val imageView: AppCompatImageView
    private val spinnerView: PowerSpinnerView
    private var spinnerAdapter: SpinnerAdapter? = null

    private var rangePickerLauncher: ActivityResultLauncher<Intent>? = null

    // Callback for selection
    var onDateRangeSelected: ((Period) -> Unit)? = null

    init {
        // Inflate the layout from XML
        val view = LayoutInflater.from(context).inflate(R.layout.view_af_period_picker, this, true)

        // Get references to the child views
        imageView = view.findViewById(R.id.iconView)
        spinnerView = view.findViewById(R.id.dateRangesSpinner)

        // Optional: Initialize custom attributes if needed
        context.theme.obtainStyledAttributes(attrs, R.styleable.AFPeriodPicker, 0, 0).apply {
            try {
                val hint = getString(R.styleable.AFPeriodPicker_spinnerHint)
                val iconVisibility = getBoolean(R.styleable.AFPeriodPicker_iconVisibility, true)

                spinnerView.hint = hint ?: spinnerView.hint
                imageView.visibility = if (iconVisibility) VISIBLE else GONE
            } finally {
                recycle()
            }
        }
    }

    // Constructor for programmatically creating the view
    constructor(
        context: Context,
        dateRanges: List<Period>,
        baseFragment: Fragment,
        onDateRangeSelected: ((Period) -> Unit)? = null
    ) : this(context) {
        this.onDateRangeSelected = onDateRangeSelected
        setup(dateRanges, baseFragment)
    }

    // Function to set up the spinner
    fun setup(
        dateRanges: List<Period>,
        baseFragment: Fragment,
        onDateRangeSelected: ((Period) -> Unit)? = null
    ) {

        this.onDateRangeSelected = onDateRangeSelected
        // Initialize and set up the adapter
        val lifecycleOwner = baseFragment.viewLifecycleOwner
        spinnerAdapter = SpinnerAdapter(spinnerView)
        spinnerView.setSpinnerAdapter(spinnerAdapter!!)
        spinnerView.setItems(dateRanges)
        spinnerView.lifecycleOwner = lifecycleOwner

        // Register the activity result launcher
        rangePickerLauncher = baseFragment.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            handleRangePickerResult(result)
        }

        spinnerView.setOnClickListener {
            spinnerView.setItems(dateRanges)
            spinnerView.showOrDismiss()
        }

        spinnerView.setOnSpinnerItemSelectedListener<Period> { _, _, newIndex, newRange ->
            if (newRange.type == Period.Type.Range) {
                spinnerAdapter!!.setSelectedIndex(newIndex)
                spinnerView.dismiss() // Dismiss dropdown
                spinnerView.text = "${newRange.name}\n${newRange.description}"

                // Trigger callback
                onDateRangeSelected?.invoke(spinnerAdapter!!.getSelectedItem())
            } else if (newRange.type == Period.Type.Custom) {
                openRangeCalendar()
            }
        }

        // Select the first item by default
        spinnerView.selectItemByIndex(0)
    }

    // Function to open the custom range calendar
    private fun openRangeCalendar() {
        val intent = Intent(context, PeriodPickerModal::class.java)
        rangePickerLauncher?.launch(intent)
    }

    // Handle the result from the custom range picker
    private fun handleRangePickerResult(result: ActivityResult) {
        if (result.resultCode == Activity.RESULT_OK) {
            val startDateMillis = result.data?.getLongExtra("startDate", 0L)
            val endDateMillis = result.data?.getLongExtra("endDate", 0L)

            if (startDateMillis != null && endDateMillis != null) {
                val updatedItem = spinnerAdapter?.getSelectedItem()?.apply {
                    type = Period.Type.Range
                    name = "Custom"
                    minTime = Date(startDateMillis)
                    maxTime = Date(endDateMillis)
                    monthChange = null
                    secondsChange = null
                }

                spinnerView.selectItemByIndex(spinnerAdapter!!.getItemIndex(updatedItem!!))
                spinnerView.dismiss()
                spinnerView.text = "${updatedItem.name}\n${updatedItem.description}"

                // Trigger callback
                onDateRangeSelected?.invoke(updatedItem)
            }
        } else if (result.resultCode == Activity.RESULT_CANCELED) {
            spinnerView.dismiss()
        }
    }

    fun getSelectedItem(): Period = spinnerAdapter!!.getSelectedItem()

}
