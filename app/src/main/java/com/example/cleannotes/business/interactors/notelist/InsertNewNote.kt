package com.example.cleannotes.business.interactors.notelist

import com.example.cleannotes.business.data.cache.abstraction.NoteCacheDataSource
import com.example.cleannotes.business.data.network.abstraction.NoteNetworkDataSource
import com.example.cleannotes.business.domain.model.Note
import com.example.cleannotes.business.domain.model.NoteFactory
import com.example.cleannotes.business.domain.state.DataState
import com.example.cleannotes.business.domain.state.MessageType
import com.example.cleannotes.business.domain.state.Response
import com.example.cleannotes.business.domain.state.StateEvent
import com.example.cleannotes.business.domain.state.UIComponentType
import com.example.cleannotes.framework.presentation.notelist.state.NoteListViewState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.UUID

class InsertNewNote(
    private val noteCacheDataSource: NoteCacheDataSource,
    private val noteNetworkDataSource: NoteNetworkDataSource,
    private val noteFactory: NoteFactory
) {

    // stateEvent will uniquely identify which event triggered this fn, will follow thru
    fun insertNewNote(
        id: String? = null,
        title: String,
        stateEvent: StateEvent
    ): Flow<DataState<NoteListViewState>> = flow {

        val newNote = noteFactory.createSingleNote(
            id = id ?: UUID.randomUUID().toString(),
            title = title,
            body = ""
        )

        val cacheResult = noteCacheDataSource.insertNote(newNote)

        var cacheResponse: DataState<NoteListViewState>?= null

        if(cacheResult > 0) { // new note successfully inserted in cache
            val viewState = NoteListViewState(
                newNote = newNote
            )

            cacheResponse = DataState.data(
                response = Response(
                    message = INSERT_NOTE_SUCCESS,
                    uiComponentType = UIComponentType.Toast(),
                    messageType = MessageType.Success()
                ),
                data = viewState,
                stateEvent= stateEvent
            )
        } else { // failed to insert new note
            cacheResponse = DataState.error(
                response = Response(
                    message = INSERT_NOTE_FAILED,
                    uiComponentType = UIComponentType.Toast(),
                    messageType = MessageType.Error()
                ),
                stateEvent= stateEvent
            )
        }

        emit(cacheResponse)

        updateNetwork(cacheResponse.stateMessage?.response?.message, newNote)
    }

    private suspend fun updateNetwork(cacheResponse: String?, newNote: Note) {
        // insert note in network only if it's success
        if(cacheResponse.equals(INSERT_NOTE_SUCCESS)) {
            noteNetworkDataSource.insertOrUpdateNote(newNote)
        }
    }

    companion object {
        private const val INSERT_NOTE_SUCCESS = "Successfully inserted new note"
        private const val INSERT_NOTE_FAILED = "Failed to insert new note"
    }
}