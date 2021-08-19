package com.example.cleannotes.business.interactors.notelist

import com.example.cleannotes.business.data.cache.CacheResponseHandler
import com.example.cleannotes.business.data.cache.CacheResult
import com.example.cleannotes.business.data.cache.abstraction.NoteCacheDataSource
import com.example.cleannotes.business.data.network.abstraction.NoteNetworkDataSource
import com.example.cleannotes.business.data.util.safeApiCall
import com.example.cleannotes.business.data.util.safeCacheCall
import com.example.cleannotes.business.domain.model.Note
import com.example.cleannotes.business.domain.state.*
import com.example.cleannotes.framework.presentation.notelist.state.NoteListViewState
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class RestoreDeletedNote(
    private val noteCacheDataSource: NoteCacheDataSource,
    private val noteNetworkDataSource: NoteNetworkDataSource
) {

    fun restoreDeletedNote(
        note: Note,
        stateEvent: StateEvent
    ): Flow<DataState<NoteListViewState>?> = flow {

        val cacheResult: CacheResult<Long?> = safeCacheCall(IO) {
            noteCacheDataSource.insertNote(note) // re-inserting deleted note
        }

        val cacheResponse: DataState<NoteListViewState>?
        = object : CacheResponseHandler<NoteListViewState, Long>(
            response = cacheResult,
            stateEvent = stateEvent
        ) {

            override suspend fun handleSuccess(resultObj: Long): DataState<NoteListViewState>? {

                return if(resultObj > 0) { // Insertion success
                    val viewState = NoteListViewState(
                        notePendingDelete = NoteListViewState.NotePendingDelete(
                            note = note // note pos not passed here, will be available in viewModel
                        )
                    )

                    DataState.data(
                        response = Response(
                            message = RESTORE_NOTE_SUCCESS,
                            uiComponentType = UIComponentType.Toast(),
                            messageType = MessageType.Success()
                        ),
                        data = viewState,
                        stateEvent = stateEvent
                    )
                } else { // Insertion Failed

                    DataState.data(
                        response = Response(
                            message = RESTORE_NOTE_FAILED,
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

        if(response == RESTORE_NOTE_SUCCESS) {
            // insert into 'notes' node
            safeApiCall(IO) {
                noteNetworkDataSource.insertOrUpdateNote(note)
            }


            // remove from 'deletes' node
            safeApiCall(IO) {
                noteNetworkDataSource.deleteDeletedNote(note)
            }
        }
    }

    companion object{
        val RESTORE_NOTE_SUCCESS = "Successfully restored the deleted note."
        val RESTORE_NOTE_FAILED = "Failed to restore the deleted note."
    }
}