package com.example.cleannotes.business.data.cache

import com.example.cleannotes.business.data.cache.CacheErrors.CACHE_DATA_NULL
import com.example.cleannotes.business.domain.state.DataState
import com.example.cleannotes.business.domain.state.MessageType
import com.example.cleannotes.business.domain.state.Response
import com.example.cleannotes.business.domain.state.StateEvent
import com.example.cleannotes.business.domain.state.UIComponentType

abstract class CacheResponseHandler <ViewState, Data>(
    private val response: CacheResult<Data?>,
    private val stateEvent: StateEvent?
){
    suspend fun getResult(): DataState<ViewState>? {

        return when(response){

            is CacheResult.GenericError -> {
                DataState.error(
                    response = Response(
                        message = "${stateEvent?.errorInfo()}\n\nReason: ${response.errorMessage}",
                        uiComponentType = UIComponentType.Dialog(),
                        messageType = MessageType.Error()
                    ),
                    stateEvent = stateEvent
                )
            }

            is CacheResult.Success -> {
                if(response.value == null){
                    DataState.error(
                        response = Response(
                            message = "${stateEvent?.errorInfo()}\n\nReason: ${CACHE_DATA_NULL}.",
                            uiComponentType = UIComponentType.Dialog(),
                            messageType = MessageType.Error()
                        ),
                        stateEvent = stateEvent
                    )
                }
                else{
                    handleSuccess(resultObj = response.value)
                }
            }

        }
    }

    abstract suspend fun handleSuccess(resultObj: Data): DataState<ViewState>?

}