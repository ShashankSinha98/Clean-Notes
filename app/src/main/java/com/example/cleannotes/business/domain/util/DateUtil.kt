package com.example.cleannotes.business.domain.util

import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DateUtil @Inject constructor(
    private val dateFormat: SimpleDateFormat
) {

    fun removeTimeFromDateString(sd: String): String{
        return sd.substring(0, sd.indexOf(" "))
    }

    fun convertFirebaseTimestampToStringData(timestamp: Timestamp): String{
        return dateFormat.format(timestamp.toDate())
    }

    // Date format: "2019-07-23 HH:mm:ss"
    fun convertStringDateToFirebaseTimestamp(date: String): Timestamp? {
        dateFormat.parse(date)?.let { date: Date ->
            return Timestamp(date)
        }
        return null
    }

    // dates format looks like this: "2019-07-23 HH:mm:ss"
    fun getCurrentTimestamp(): String {
        return dateFormat.format(Date())
    }
}