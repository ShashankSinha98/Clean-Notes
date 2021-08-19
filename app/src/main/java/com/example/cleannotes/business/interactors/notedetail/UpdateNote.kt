package com.example.cleannotes.business.interactors.notedetail

import com.example.cleannotes.business.data.cache.CacheResponseHandler
import com.example.cleannotes.business.data.cache.CacheResult
import com.example.cleannotes.business.data.cache.abstraction.NoteCacheDataSource
import com.example.cleannotes.business.data.network.abstraction.NoteNetworkDataSource
import com.example.cleannotes.business.data.util.safeApiCall
import com.example.cleannotes.business.data.util.safeCacheCall
import com.example.cleannotes.business.domain.model.Note
import com.example.cleannotes.business.domain.state.*
import com.example.cleannotes.framework.presentation.notedetail.state.NoteDetailViewState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class UpdateNote(
    private val noteCacheDataSource: NoteCacheDataSource,
    private val noteNetworkDataSource: NoteNetworkDataSource
) {

    fun updateNote(
        note: Note,
        stateEvent: StateEvent
    ): Flow<DataState<NoteDetailViewState>?> = flow {

        val cacheResult: CacheResult<Int?> = safeCacheCall(IO) {
            noteCacheDataSource.updateNote(
                primaryKey = note.id,
                newTitle = note.title,
                newBody = note.body
            )
        }


        val cacheResponse: DataState<NoteDetailViewState>?
            = object : CacheResponseHandler<NoteDetailViewState, Int>(
                response = cacheResult,
                stateEvent = stateEvent
            ) {

            override suspend fun handleSuccess(resultObj: Int): DataState<NoteDetailViewState>? {

                return if(resultObj > 0) { // success
                    DataState.data(
                        response = Response(
                            message = UPDATE_NOTE_SUCCESS,
                            uiComponentType = UIComponentType.Toast(),
                            messageType = MessageType.Success()
                        ),
                        data = null,
                        stateEvent = stateEvent
                    )
                } else { // failure
                    DataState.data(
                        response = Response(
                            message = UPDATE_NOTE_FAILED,
                            uiComponentType = UIComponentType.Toast(),
                            messageType = MessageType.Error()
                        ),
                        data = null,
                        stateEvent = stateEvent
                    )
                }
            }

        }.getResult()

        emit(cacheResponse)

        updateNetwork(
            response = cacheResponse?.stateMessage?.response?.message,
            note = note
        )

    }


    private suspend fun updateNetwork(response: String?, note: Note) {
        if(response.equals(UPDATE_NOTE_SUCCESS)){

            safeApiCall(IO){
                noteNetworkDataSource.insertOrUpdateNote(note)
            }
        }
    }

    companion object{
        val UPDATE_NOTE_SUCCESS = "Successfully updated note."
        val UPDATE_NOTE_FAILED = "Failed to update note."
        val UPDATE_NOTE_FAILED_PK = "Update failed. Note is missing primary key."

    }
}