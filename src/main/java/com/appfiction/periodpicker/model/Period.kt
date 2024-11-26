package com.appfiction.periodpicker.model

import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Represents a customizable date range with support for various configurations,
 * such as a range defined by months, seconds, or explicit minimum and maximum times.
 *
 * This class is designed to be flexible and caters to multiple use cases for date range management.
 * It can calculate dynamic ranges like "last 30 days" or "last 15 minutes,"
 * as well as handle fixed date ranges using explicitly provided start and end times.
 *
 * @property name The name or label of the date range (e.g., "Last Month", "Custom Range").
 * @property monthChange The number of months to adjust for calculating the date range. Positive for future ranges, negative for past ranges.
 * @property secondsChange The number of seconds to adjust for calculating the date range. Positive for future ranges, negative for past ranges.
 * @property type The type of the date range, used to distinguish between dummy, custom, and predefined ranges.
 * @property minTime The explicit start time of the range (Firestore Timestamp). Overrides dynamic calculations when set.
 * @property maxTime The explicit end time of the range (Firestore Timestamp). Overrides dynamic calculations when set.
 *
 * @constructor Creates a dynamic date range using months or seconds to adjust the start and end times.
 * @constructor Creates a fixed date range using explicit minimum and maximum times.
 *
 * @see [description] Provides a human-readable string representation of the range.
 * @see [calculatedMinTime] Dynamically calculates the minimum time if not explicitly provided.
 * @see [calculatedMaxTime] Dynamically calculates the maximum time if not explicitly provided.
 */
data class Period(
    var name: String?,
    var monthChange: Int? = null,
    var secondsChange: Long? = null, // Added secondsChange for adjusting time by seconds
    var type: Type? = null,  // Default value for type is Dummy
    var minTime: Date? = null,
    var maxTime: Date? = null
) {

    enum class Type {
        Dummy,
        Custom,
        Range
    }

    /**
     * Get minTime based on monthChange or secondsChange
     */
    val calculatedMinTime: Date?
        get() {
            // Use provided minTime if available, else calculate based on monthChange or secondsChange
            return minTime ?: calculateMinTimeBasedOnChange()
        }

    /**
     * Get maxTime based on provided value or default to current time
     */
    val calculatedMaxTime: Date
        get() {
            return maxTime ?: Date()
        }

    /**
     * Helper method to calculate minTime based on monthChange or secondsChange
     */
    private fun calculateMinTimeBasedOnChange(): Date {
        val c = Calendar.getInstance()
        if (monthChange != null) {
            c.add(Calendar.MONTH, monthChange!!) // Adjust by months
        } else if (secondsChange != null) {
            c.add(Calendar.SECOND, secondsChange!!.toInt()) // Adjust by seconds
        }
        return c.time // Return calculated time as Firestore Timestamp
    }

    /**
     * Show year if needed (based on the format)
     */
    val description: String?
        get() {
            return when {
                secondsChange != null -> {
                    val c = Calendar.getInstance()
                    val maxDate = c.time
                    val maxCal = Calendar.getInstance()
                    maxCal.time = maxDate
                    c.add(Calendar.SECOND, secondsChange!!.toInt())
                    val minDate = c.time
                    val minCal = Calendar.getInstance()
                    minCal.time = minDate
                    var formatStr = DATE_FORMAT2
                    if (maxCal[Calendar.YEAR] != minCal[Calendar.YEAR]) {
                        formatStr = DATE_FORMAT1
                    }
                    formatTime(formatStr, minDate) + " - " + formatTime(formatStr, maxDate)
                }

                minTime != null && maxTime != null -> {
                    var formatStr = DATE_FORMAT2
                    val minCal = Calendar.getInstance().apply { time = minTime!! }
                    val maxCal = Calendar.getInstance().apply { time = maxTime!!}
                    if (maxCal[Calendar.YEAR] != minCal[Calendar.YEAR]) {
                        formatStr = DATE_FORMAT1
                    }
                    formatTime(formatStr, minTime!!) + " - " + formatTime(formatStr, maxTime!!)
                }

                monthChange != null -> {
                    val c = Calendar.getInstance()
                    val maxDate = c.time
                    val maxCal = Calendar.getInstance()
                    maxCal.time = maxDate
                    c.add(Calendar.MONTH, monthChange!!)
                    val minDate = c.time
                    val minCal = Calendar.getInstance()
                    minCal.time = minDate
                    var formatStr = DATE_FORMAT2
                    if (maxCal[Calendar.YEAR] != minCal[Calendar.YEAR]) {
                        formatStr = DATE_FORMAT1
                    }
                    formatTime(formatStr, minDate) + " - " + formatTime(formatStr, maxDate)
                }

                else -> name
            }
        }

    /**
     * Utility method to format time (if needed)
     */
    private fun formatTime(format: String, date: Date): String {
        val formatter = java.text.SimpleDateFormat(format, Locale.getDefault())
        return formatter.format(date)
    }


    companion object {
        const val DATE_FORMAT2 = "MMM d"
        const val DATE_FORMAT1 = "MMM d yyyy"
    }
}
