package com.example.cleannotes.business.data.cache


/**
 *      Wrapper class for results returned by DB
 *
 *      T - result
 *      Either success - then, value = actual thing returned by DB
 *      or, Error - then, error message
 *
 * */

sealed class CacheResult <out T> {

    data class Success<out T>(val value: T): CacheResult<T>()

    data class GenericError(
        val errorMessage: String?= null
    ): CacheResult<Nothing>()
}