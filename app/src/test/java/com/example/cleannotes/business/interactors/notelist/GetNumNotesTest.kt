package com.example.cleannotes.business.interactors.notelist

import com.example.cleannotes.business.data.cache.abstraction.NoteCacheDataSource
import com.example.cleannotes.business.domain.model.NoteFactory
import com.example.cleannotes.business.domain.state.DataState
import com.example.cleannotes.di.DependencyContainer
import com.example.cleannotes.framework.presentation.notelist.state.NoteListStateEvent
import com.example.cleannotes.framework.presentation.notelist.state.NoteListViewState
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/*
Test cases:
1. getNumNotes_success_confirmCorrect()
    a) get the number of notes in cache
    b) listen for GET_NUM_NOTES_SUCCESS from flow emission
    c) compare with the number of notes in the fake data set
*/

class GetNumNotesTest {

    // system in test
    private val getNumNotes: GetNumNotes

    // dependencies
    private val dependencyContainer: DependencyContainer
    private val noteCacheDataSource: NoteCacheDataSource
    private val noteFactory: NoteFactory

    init {
        dependencyContainer = DependencyContainer()
        dependencyContainer.build()
        noteCacheDataSource = dependencyContainer.noteCacheDataSource
        noteFactory = dependencyContainer.noteFactory

        getNumNotes = GetNumNotes(
            noteCacheDataSource = noteCacheDataSource
        )
    }



    @InternalCoroutinesApi
    @Test
    fun getNumNotes_success_confirmCorrect() = runBlocking {

        var numNotes=0

        getNumNotes.getNumNotes(
            stateEvent = NoteListStateEvent.GetNumNotesInCacheEvent()
        ).collect(object: FlowCollector<DataState<NoteListViewState>?> {


            override suspend fun emit(value: DataState<NoteListViewState>?) {

                assertEquals ( value?.stateMessage?.response?.message,
                    GetNumNotes.GET_NUM_NOTES_SUCCESS
                )

                numNotes = value?.data?.numNotesInCache?: 0
            }
        })

        val actualNumberInCache = noteCacheDataSource.getNumNotes()
        assertTrue { numNotes == actualNumberInCache}

    }

}