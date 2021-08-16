package com.example.cleannotes.business.interactors.notelist

import com.example.cleannotes.business.data.cache.CacheResponseHandler
import com.example.cleannotes.business.data.cache.abstraction.NoteCacheDataSource
import com.example.cleannotes.business.data.network.abstraction.NoteNetworkDataSource
import com.example.cleannotes.business.data.util.safeApiCall
import com.example.cleannotes.business.data.util.safeCacheCall
import com.example.cleannotes.business.domain.model.Note
import com.example.cleannotes.business.domain.model.NoteFactory
import com.example.cleannotes.business.domain.state.*
import com.example.cleannotes.framework.presentation.notelist.state.NoteListViewState
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.net.CacheResponse

class InsertNewNote (
    private val noteCacheDataSource: NoteCacheDataSource,
    private val noteNetworkDataSource: NoteNetworkDataSource,
    private val noteFactory: NoteFactory
) {

    fun insertNewNote(
        id: String?= null,
        title: String,
        stateEvent: StateEvent // NoteListStateEvent.InsertNewNoteEvent
    ): Flow<DataState<NoteListViewState>?> = flow {

        val newNote = noteFactory.creatingSingleNote(id, title)

        // return line no if successfully inserted, else -1
        val cacheResult = safeCacheCall(IO) {
            noteCacheDataSource.insertNote(newNote)
        }


        // Long- return type of insertNote
        val cacheResponse: DataState<NoteListViewState>? = object : CacheResponseHandler<NoteListViewState, Long>(
            response = cacheResult,
            stateEvent= stateEvent
        ) {
            override suspend fun handleSuccess(resultObj: Long): DataState<NoteListViewState>? {

                return if(resultObj>0) {  // Success
                    val viewState = NoteListViewState(newNote = newNote)

                    DataState.data(
                        response = Response(message = INSERT_NOTE_SUCCESS,
                            uiComponentType = UIComponentType.Toast(),
                            messageType = MessageType.Success()),
                        data = viewState,
                        stateEvent = stateEvent
                    )

                } else { // Failure
                    DataState.error(
                        response = Response(message = INSERT_NOTE_FAILED,
                            uiComponentType = UIComponentType.Toast(),
                            messageType = MessageType.Error()),
                        stateEvent = stateEvent
                    )
                }

            }
        }.getResult()


        emit(cacheResponse)

        updateNetwork(cacheResponse?.stateMessage?.response?.message, newNote)

    }

    private suspend fun updateNetwork(cacheResponse: String?, newNote: Note) {
        if(cacheResponse.equals(INSERT_NOTE_SUCCESS)) {
            safeApiCall(IO) {
                noteNetworkDataSource.insertOrUpdateNote(newNote)
            }
        }
    }

    companion object {
        const val INSERT_NOTE_SUCCESS = "Successfully Inserted New Note"
        const val INSERT_NOTE_FAILED = "Failed To Insert New Note"
    }


}