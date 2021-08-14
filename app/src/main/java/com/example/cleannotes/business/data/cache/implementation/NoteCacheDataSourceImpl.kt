package com.example.cleannotes.business.data.cache.implementation

import com.example.cleannotes.business.data.cache.abstraction.NoteCacheDataSource
import com.example.cleannotes.business.domain.model.Note
import javax.inject.Inject
import javax.inject.Singleton

/**
 *      We cannot test Room lib, so we built a fake in between for testing
 * */

@Singleton
class NoteCacheDataSourceImpl
@Inject constructor(
    private val noteDaoService: NoteDaoService
): NoteCacheDataSource {

    override suspend fun insertNote(note: Note)
            = noteDaoService.insertNote(note)

    override suspend fun deleteNote(primaryKey: String)
            = noteDaoService.deleteNote(primaryKey)

    override suspend fun deleteNotes(notes: List<Note>)
            = noteDaoService.deleteNotes(notes)

    override suspend fun updateNote(primaryKey: String, newTitle: String, newBody: String)
            = noteDaoService.updateNote(primaryKey, newTitle, newBody)

    override suspend fun serachNotes(query: String, filterAndOrder: String, page: Int)
            = noteDaoService.serachNotes(query, filterAndOrder, page)

    override suspend fun searchNoteById(primaryKey: String)
            = noteDaoService.searchNoteById(primaryKey)

    override suspend fun getNumNotes()
            = noteDaoService.getNumNotes()

    override suspend fun insertNotes(notes: List<Note>)
            = noteDaoService.insertNotes(notes)

}