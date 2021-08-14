package com.example.cleannotes.business.data.cache.abstraction

import com.example.cleannotes.business.domain.model.Note


/**
 *  Abstraction layer which lists all Database / Cache functions
 *
 *  functions are suspend - DB operation, will be done on background thread by Coroutines
 *
 * */
interface NoteCacheDataSource {

    suspend fun insertNote(note: Note): Long

    suspend fun deleteNote(primaryKey: String): Int

    suspend fun deleteNotes(notes: List<Note>): Int

    suspend fun updateNote(primaryKey: String, newTitle: String, newBody: String): Int

    suspend fun serachNotes(
        query: String,
        filterAndOrder: String,
        page: Int
    ): List<Note>

    suspend fun searchNoteById(primaryKey: String): Note?

    suspend fun getNumNotes(): Int

    // Testing
    suspend fun insertNotes(notes: List<Note>): LongArray

}