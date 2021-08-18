package com.example.cleannotes.business.interactors.common

import com.example.cleannotes.business.data.cache.CacheResponseHandler
import com.example.cleannotes.business.data.cache.CacheResult
import com.example.cleannotes.business.data.cache.abstraction.NoteCacheDataSource
import com.example.cleannotes.business.data.network.abstraction.NoteNetworkDataSource
import com.example.cleannotes.business.data.util.safeApiCall
import com.example.cleannotes.business.data.util.safeCacheCall
import com.example.cleannotes.business.domain.model.Note
import com.example.cleannotes.business.domain.state.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.net.CacheResponse

/**
 *  Generic ViewState as delete note operation can be performed from 2 places:
 *  1. Note List Fragment
 *  2. Note Detail Fragment
 *
 *  both has 2 separate ViewState
 * */

class DeleteNote<ViewState>(
    private val noteCacheDataSource: NoteCacheDataSource,
    private val noteNetworkDataSource: NoteNetworkDataSource
) {

    fun deleteNote(
        note: Note,
        stateEvent: StateEvent
    ): Flow<DataState<ViewState>?> = flow {

        val cacheResult: CacheResult<Int?> = safeCacheCall(IO) {
            noteCacheDataSource.deleteNote(note.id)
        }

        val cacheResponse: DataState<ViewState>?
            = object: CacheResponseHandler<ViewState, Int>(
                response = cacheResult,
                stateEvent = stateEvent
            ) {

            override suspend fun handleSuccess(resultObj: Int): DataState<ViewState>? {

                return if(resultObj > 0) { // success delete
                    DataState.data(
                        response = Response(
                            message = DELETE_NOTE_SUCCESS,
                            uiComponentType = UIComponentType.None(),
                            messageType = MessageType.Success()
                        ),
                        data= null,
                        stateEvent = stateEvent
                    )

                } else { // success delete
                    DataState.data(
                        response = Response(
                            message = DELETE_NOTE_FAILED,
                            uiComponentType = UIComponentType.Toast(),
                            messageType = MessageType.Error()
                        ),
                        data= null,
                        stateEvent = stateEvent
                    )
                }
            }

        } .getResult()


        emit(cacheResponse)

        updateNetwork(
            message = cacheResponse?.stateMessage?.response?.message,
            note = note
        )

    }

    private suspend fun updateNetwork(message: String?, note: Note) {
        if(message == DELETE_NOTE_SUCCESS) {

            // delete from 'notes' node
            safeApiCall(IO) {
                noteNetworkDataSource.deleteNote(note.id)
            }

            // insert into 'delete' node
            safeApiCall(IO) {
                noteNetworkDataSource.insertDeletedNote(note)
            }
        }
    }


    companion object {
        val DELETE_NOTE_SUCCESS = "Successfully deleted note."
        val DELETE_NOTE_PENDING = "Delete pending..."
        val DELETE_NOTE_FAILED = "Failed to delete note."
        val DELETE_ARE_YOU_SURE = "Are you sure you want to delete this?"
    }
}