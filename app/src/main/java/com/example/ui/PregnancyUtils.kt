package com.example.ui

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object PregnancyUtils {
    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    fun formatDate(timestamp: Long): String {
        return dateFormat.format(Date(timestamp))
    }

    fun calculateEdd(lmpTimestamp: Long): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = lmpTimestamp
        cal.add(Calendar.DAY_OF_YEAR, 280)
        return cal.timeInMillis
    }

    fun calculateGestationalAge(lmpTimestamp: Long, targetTimestamp: Long = System.currentTimeMillis()): Pair<Int, Int> {
        val diffMs = targetTimestamp - lmpTimestamp
        val diffDays = (diffMs / (1000 * 60 * 60 * 24)).toInt()
        if (diffDays < 0) return Pair(0, 0)
        val weeks = diffDays / 7
        val days = diffDays % 7
        return Pair(weeks, days)
    }

    fun getGestationalAgeString(lmpTimestamp: Long, targetTimestamp: Long = System.currentTimeMillis()): String {
        val (weeks, days) = calculateGestationalAge(lmpTimestamp, targetTimestamp)
        return "$weeks weeks, $days days"
    }

    fun getDaysUntilDue(eddTimestamp: Long): Int {
        val diffMs = eddTimestamp - System.currentTimeMillis()
        val days = (diffMs / (1000 * 60 * 60 * 24)).toInt()
        return if (days < 0) 0 else days
    }
}
