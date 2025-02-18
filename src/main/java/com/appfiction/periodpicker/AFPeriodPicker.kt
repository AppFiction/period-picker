package com.appfiction.periodpicker

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import com.appfiction.periodpicker.model.Period
import com.skydoves.powerspinner.PowerSpinnerView
import skedgo.datetimerangepicker.DateTimeRangePickerActivity
import java.util.Date
import java.util.TimeZone

class AFPeriodPicker @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val imageView: AppCompatImageView
    private val spinnerView: PowerSpinnerView
    private var spinnerAdapter: SpinnerAdapter? = null

    private lateinit var rangePickerLauncher: ActivityResultLauncher<Intent>
    private lateinit var lifecycleOwner: LifecycleOwner
    private var dateRanges: MutableList<Period> = mutableListOf()

    // Callback for selection
    var onDateRangeSelected: ((Period) -> Unit)? = null

    init {
        // Inflate the layout from XML
        val view = LayoutInflater.from(context).inflate(R.layout.view_af_period_picker, this, true)

        // Get references to the child views
        imageView = view.findViewById(R.id.iconView)
        spinnerView = view.findViewById(R.id.dateRangesSpinner)

        context.theme.obtainStyledAttributes(attrs, R.styleable.AFPeriodPicker, 0, 0).apply {
            try {
                val hint = getString(R.styleable.AFPeriodPicker_spinnerHint)
                val iconVisibility = getBoolean(R.styleable.AFPeriodPicker_iconVisibility, true)
                val textSize =
                    getDimension(R.styleable.AFPeriodPicker_spinnerTextSize, spinnerView.textSize)
                val hintColor =
                    getColor(R.styleable.AFPeriodPicker_spinnerTextColorHint, Color.GRAY)

                spinnerView.hint = hint ?: spinnerView.hint
                imageView.visibility = if (iconVisibility) VISIBLE else GONE
                spinnerView.setHintTextColor(hintColor)
                spinnerView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
                imageView.setColorFilter(spinnerView.currentTextColor)

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
        setup(dateRanges, baseFragment, onDateRangeSelected)
    }

    fun setup(
        dateRanges: List<Period>,
        activity: AppCompatActivity,
        onDateRangeSelected: ((Period) -> Unit)? = null
    ) {
        this.onDateRangeSelected = onDateRangeSelected
        lifecycleOwner = activity
        rangePickerLauncher = activity.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            handleRangePickerResult(result)
        }
        create(dateRanges)
    }

    // Function to set up the spinner
    fun setup(
        dateRanges: List<Period>,
        baseFragment: Fragment,
        onDateRangeSelected: ((Period) -> Unit)? = null
    ) {
        this.onDateRangeSelected = onDateRangeSelected
        lifecycleOwner = baseFragment.viewLifecycleOwner
        rangePickerLauncher = baseFragment.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            handleRangePickerResult(result)
        }
        create(dateRanges)
    }

    // Function to open the custom range calendar
    private fun openRangeCalendar() {
        val intent = DateTimeRangePickerActivity.newIntent(
            context,
            TimeZone.getDefault(),
            null, null
        )
        rangePickerLauncher.launch(intent)
    }

    private fun create(newDateRanges: List<Period>) {
        refreshItems(newDateRanges)
        spinnerAdapter = SpinnerAdapter(spinnerView)
        spinnerView.setSpinnerAdapter(spinnerAdapter!!)
        spinnerView.setItems(this.dateRanges)
        spinnerView.lifecycleOwner = lifecycleOwner

        spinnerView.setOnClickListener {
            refreshItems(newDateRanges)
            spinnerView.setItems(this.dateRanges)
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

    /**
     * Refreshes the items in the `dateRanges` list.
     *
     * This function clears the existing `dateRanges` list and repopulates it with
     * copies of the current items in the list. Using `copy()` ensures that the new
     * items are independent of the originals, preventing unintended side effects
     * caused by modifying the original objects.
     *
     * Note: Ensure that the `copy()` function is properly implemented for the
     * class of objects stored in the `dateRanges` list.
     */
    private fun refreshItems(newDateRanges: List<Period>) {
        this.dateRanges.clear()
        this.dateRanges.addAll(newDateRanges.map { it.copy() })
    }

    // Handle the result from the custom range picker
    private fun handleRangePickerResult(result: ActivityResult) {
        if (result.resultCode == Activity.RESULT_OK) {
            val startDateMillis = result.data?.getLongExtra("startTimeInMillis", 0L)
            val endDateMillis = result.data?.getLongExtra("endTimeInMillis", 0L)
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

