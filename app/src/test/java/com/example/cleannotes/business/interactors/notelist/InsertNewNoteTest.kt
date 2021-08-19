package com.example.cleannotes.business.interactors.notelist

import com.example.cleannotes.business.data.cache.CacheErrors
import com.example.cleannotes.business.data.cache.FORCE_GENERAL_FAILURE
import com.example.cleannotes.business.data.cache.FORCE_NEW_NOTE_EXCEPTION
import com.example.cleannotes.business.data.cache.abstraction.NoteCacheDataSource
import com.example.cleannotes.business.data.network.abstraction.NoteNetworkDataSource
import com.example.cleannotes.business.domain.model.NoteFactory
import com.example.cleannotes.business.domain.state.DataState
import com.example.cleannotes.business.interactors.notelist.InsertNewNote.Companion.INSERT_NOTE_FAILED
import com.example.cleannotes.business.interactors.notelist.InsertNewNote.Companion.INSERT_NOTE_SUCCESS
import com.example.cleannotes.di.DependencyContainer
import com.example.cleannotes.framework.presentation.notelist.state.NoteListStateEvent
import com.example.cleannotes.framework.presentation.notelist.state.NoteListViewState
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.*


/*
Test cases:
1. insertNote_success_confirmNetworkAndCacheUpdated()
    a) insert a new note
    b) listen for INSERT_NOTE_SUCCESS emission from flow
    c) confirm cache was updated with new note
    d) confirm network was updated with new note
2. insertNote_fail_confirmNetworkAndCacheUnchanged()
    a) insert a new note
    b) force a failure (return -1 from db operation)
    c) listen for INSERT_NOTE_FAILED emission from flow
    e) confirm cache was not updated
    e) confirm network was not updated
3. throwException_checkGenericError_confirmNetworkAndCacheUnchanged()
    a) insert a new note
    b) force an exception
    c) listen for CACHE_ERROR_UNKNOWN emission from flow
    e) confirm cache was not updated
    e) confirm network was not updated
 */

@InternalCoroutinesApi
class InsertNewNoteTest {

    // system in test
    private val insertNewNote: InsertNewNote

    // dependencies
    private val dependencyContainer: DependencyContainer
    private val noteCacheDataSource: NoteCacheDataSource
    private val noteNetworkDataSource: NoteNetworkDataSource
    private val noteFactory: NoteFactory

    init {
        dependencyContainer = DependencyContainer()
        dependencyContainer.build()
        noteCacheDataSource = dependencyContainer.noteCacheDataSource
        noteNetworkDataSource = dependencyContainer.noteNetworkDataSource
        noteFactory = dependencyContainer.noteFactory
        insertNewNote = InsertNewNote(
            noteCacheDataSource = noteCacheDataSource,
            noteNetworkDataSource = noteNetworkDataSource,
            noteFactory = noteFactory
        )
    }



    // gonna call coroutines inside, so use runBlocking
    @Test
    fun insertNote_success_confirmNetworkAndCacheUpdated() = runBlocking {

        val newNote = noteFactory.createSingleNote(
            id= null,
            title = UUID.randomUUID().toString()
        )

        insertNewNote.insertNewNote(
            id= newNote.id,
            title= newNote.title,
            stateEvent = NoteListStateEvent.InsertNewNoteEvent(newNote.title)
        ).collect(object : FlowCollector<DataState<NoteListViewState>?> {  // listening flow emission
            override suspend fun emit(value: DataState<NoteListViewState>?) {
                assertEquals(
                    value?.stateMessage?.response?.message,
                    INSERT_NOTE_SUCCESS
                )
            }
        })


        // confirm cache was updated
        val cacheNoteThatWasInserted = noteCacheDataSource.searchNoteById(newNote.id)
        assertTrue { cacheNoteThatWasInserted == newNote }

        // confirm network was updated
        val networkNoteThatWasInserted = noteNetworkDataSource.searchNote(newNote)
        assertTrue { networkNoteThatWasInserted == newNote}

    }



    @Test
    fun insertNote_fail_confirmNetworkAndCacheUnchanged() = runBlocking {

        val newNote = noteFactory.createSingleNote(
            id= FORCE_GENERAL_FAILURE,
            title = UUID.randomUUID().toString()
        )

        insertNewNote.insertNewNote(
            id= newNote.id,
            title= newNote.title,
            stateEvent = NoteListStateEvent.InsertNewNoteEvent(newNote.title)
        ).collect(object : FlowCollector<DataState<NoteListViewState>?> {  // listening flow emission
            override suspend fun emit(value: DataState<NoteListViewState>?) {
                assertEquals(
                    value?.stateMessage?.response?.message,
                    INSERT_NOTE_FAILED
                )
            }
        })


        // confirm cache was not updated
        val cacheNoteThatWasInserted = noteCacheDataSource.searchNoteById(newNote.id)
        assertTrue { cacheNoteThatWasInserted == null }

        // confirm network was not updated
        val networkNoteThatWasInserted = noteNetworkDataSource.searchNote(newNote)
        assertTrue { networkNoteThatWasInserted == null}

    }

    @Test
    fun throwException_checkGenericError_confirmNetworkAndCacheUnchanged() = runBlocking {

        val newNote = noteFactory.createSingleNote(
            id= FORCE_NEW_NOTE_EXCEPTION,
            title = UUID.randomUUID().toString()
        )

        insertNewNote.insertNewNote(
            id= newNote.id,
            title= newNote.title,
            stateEvent = NoteListStateEvent.InsertNewNoteEvent(newNote.title)
        ).collect(object : FlowCollector<DataState<NoteListViewState>?> {  // listening flow emission
            override suspend fun emit(value: DataState<NoteListViewState>?) {
                assert(
                    value?.stateMessage?.response?.message
                        ?.contains(CacheErrors.CACHE_ERROR_UNKNOWN)?: false
                )
            }
        })


        // confirm cache was not updated
        val cacheNoteThatWasInserted = noteCacheDataSource.searchNoteById(newNote.id)
        assertTrue { cacheNoteThatWasInserted == null }

        // confirm network was not updated
        val networkNoteThatWasInserted = noteNetworkDataSource.searchNote(newNote)
        assertTrue { networkNoteThatWasInserted == null}

    }



}


