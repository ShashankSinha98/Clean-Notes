package com.example.cleannotes.business.domain.util

import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * There are 3 places were time is used:
 *
 * 1. Recycler view - String : date (YYYY-MM-DD)
 * 2. ROOM DB - String: date-time (YYYY-MM-DD hh:mm:ss)
 * 3. Firestore - Timestamp (class of Firebase for date-time)
 * */

@Singleton
class DateUtil
@Inject constructor(
    private val dateFormat: SimpleDateFormat
){

    // date format: 2021-08-12 HH:MM:SS

    fun removeTimeFromDateString(sd: String): String {
        return sd.substring(0, sd.indexOf(" "))
    }


    fun convertFirebaseTimestampToStringDate(timestamp: Timestamp): String {
        return dateFormat.format(timestamp.toDate())
    }

    fun convertStringDateToFirebaseTimestamp(date: String): Timestamp {
        return Timestamp(dateFormat.parse(date))
    }

    fun getCurrentTimestamp(): String {
        return dateFormat.format(Date())
    }
}