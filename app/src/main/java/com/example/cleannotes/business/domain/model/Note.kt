package com.example.cleannotes.business.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Note(
    val id: String,
    val title: String,
    val body: String,
    val updated_at: String,
    val created_at: String,
): Parcelable