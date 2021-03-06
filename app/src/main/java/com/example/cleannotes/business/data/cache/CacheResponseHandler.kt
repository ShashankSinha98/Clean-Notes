package com.example.cleannotes.business.data.cache

import com.example.cleannotes.business.data.cache.CacheErrors.CACHE_DATA_NULL
import com.example.cleannotes.business.domain.state.*

/**
 *      Result of cache DB is wrapped in CacheResult
 *      ViewModel need DataState acc to MVI
 *      So, we need a class to convert CacheResult to DataState
 *
 * */

abstract class CacheResponseHandler<ViewState, Data>(
    private val response: CacheResult<Data?>,
    private val stateEvent: StateEvent?
) {

    suspend fun getResult(): DataState<ViewState>? {

        return when(response) {

            is CacheResult.GenericError ->
                DataState.error(
                    response = Response(
                        message = "${stateEvent?.errorInfo()}\n\n" +
                                "Reason: ${response.errorMessage}",
                        uiComponentType =  UIComponentType.Dialog(),
                        messageType = MessageType.Error()
                    ),
                    stateEvent = stateEvent
                )

            is CacheResult.Success -> {
                if(response.value==null) {
                    DataState.error(
                        response = Response(
                            message = "${stateEvent?.errorInfo()}\n\n" +
                                    "Reason: ${CACHE_DATA_NULL}",
                            uiComponentType =  UIComponentType.Dialog(),
                            messageType = MessageType.Error()
                        ),
                        stateEvent = stateEvent
                    )
                } else {
                    handleSuccess(resultObj = response.value)
                }
            }
        }
    }

    abstract suspend fun handleSuccess(resultObj: Data): DataState<ViewState>?
}