package com.example.cleannotes.di

import com.example.cleannotes.business.data.NoteDataFactory
import com.example.cleannotes.business.data.cache.FakeNoteCacheDataSourceImpl
import com.example.cleannotes.business.data.cache.abstraction.NoteCacheDataSource
import com.example.cleannotes.business.data.network.FakeNoteNetworkDataSourceImpl
import com.example.cleannotes.business.data.network.abstraction.NoteNetworkDataSource
import com.example.cleannotes.business.domain.model.Note
import com.example.cleannotes.business.domain.model.NoteFactory
import com.example.cleannotes.business.domain.util.DateUtil
import com.example.cleannotes.util.isUnitTest
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class DependencyContainer {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd hh:mm:ss a", Locale.ENGLISH)
    val dateUtil = DateUtil(dateFormat)
    lateinit var noteNetworkDataSource: NoteNetworkDataSource
    lateinit var noteCacheDataSource: NoteCacheDataSource
    lateinit var noteFactory: NoteFactory
    lateinit var noteDataFactory: NoteDataFactory

    private var notesData: HashMap<String, Note> = HashMap()

    init {
        isUnitTest = true // for Logger.kt
    }

    fun build() {

        this.javaClass.classLoader?.let { classLoader ->
            noteDataFactory = NoteDataFactory(classLoader)

            // fake data set
            notesData = noteDataFactory.produceHashMapOfNotes(
                noteDataFactory.produceListOfNotes()
            )
        }


        noteFactory = NoteFactory(dateUtil)
        noteNetworkDataSource = FakeNoteNetworkDataSourceImpl(
            notesData = notesData,
            deletedNotesData = HashMap()
        )
        noteCacheDataSource = FakeNoteCacheDataSourceImpl(
            notesData = notesData,
            dateUtil = dateUtil
        )
    }

}