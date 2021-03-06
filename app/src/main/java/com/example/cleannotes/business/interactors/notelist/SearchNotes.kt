package com.example.cleannotes.business.interactors.notelist

import com.example.cleannotes.business.data.cache.CacheResponseHandler
import com.example.cleannotes.business.data.cache.CacheResult
import com.example.cleannotes.business.data.cache.abstraction.NoteCacheDataSource
import com.example.cleannotes.business.data.util.safeCacheCall
import com.example.cleannotes.business.domain.model.Note
import com.example.cleannotes.business.domain.state.*
import com.example.cleannotes.framework.presentation.notelist.state.NoteListStateEvent
import com.example.cleannotes.framework.presentation.notelist.state.NoteListViewState
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow


class SearchNotes(
    private val noteCacheDataSource: NoteCacheDataSource
) {

    fun searchNotes(
        query: String,
        filterAndOrder: String,
        page: Int,
        stateEvent: StateEvent
    ): Flow<DataState<NoteListViewState>?> = flow {

        var updatedPage = page
        if(updatedPage<=0) updatedPage = 1

        val cacheResult: CacheResult<List<Note>?> = safeCacheCall(IO) {
            noteCacheDataSource.searchNotes(
                query = query,
                filterAndOrder= filterAndOrder,
                page =  updatedPage
            )
        }

        val response: DataState<NoteListViewState>? = object: CacheResponseHandler<NoteListViewState, List<Note>>(
            response = cacheResult,
            stateEvent= stateEvent
        ) {
            override suspend fun handleSuccess(resultObj: List<Note>): DataState<NoteListViewState>? {

                var message: String?= SEARCH_NOTES_SUCCESS
                var uiComponentType: UIComponentType = UIComponentType.None()
                if(resultObj.isEmpty()) {
                   message = SEARCH_NOTES_NO_MATCHING_RESULTS
                   uiComponentType = UIComponentType.Toast()
                }

                return DataState.data(
                    response = Response(
                        message = message,
                        uiComponentType = uiComponentType,
                        messageType = MessageType.Success()
                    ),
                    data = NoteListViewState(noteList = ArrayList(resultObj)),
                    stateEvent = stateEvent
                )
            }

        }.getResult()


        emit(response)

    }

    companion object{
        val SEARCH_NOTES_SUCCESS = "Successfully retrieved list of notes."
        val SEARCH_NOTES_NO_MATCHING_RESULTS = "There are no notes that match that query."
        val SEARCH_NOTES_FAILED = "Failed to retrieve the list of notes."
    }

}