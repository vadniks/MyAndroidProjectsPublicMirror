/*
 * Copyright (C) 2018-2020 Vad Nik (http://github.com/vadniks). All rights reserved.
 *
 * This code is a part of proprietary software.
 * Usage, distribution, redistribution, modifying
 * and/or commercial use of this code,
 * without author's written permission, are strongly prohibited.
 */

package .processing.database

import androidx.annotation.WorkerThread
import androidx.paging.DataSource
import androidx.room.*
import .common.NUM_UNDEF
import .processing.common.*

/**
 * @author Vad Nik
 * @version dated Jul 12, 2019.
 * @link https://github.com/vadniks
 */
@WorkerThread
@Dao
interface NoteDao {

    @Query("SELECT * FROM $DB_NAME")
    fun getAll(): DataSource.Factory<Int, Note>

    @Query("SELECT * FROM $DB_NAME WHERE instr(LOWER($TITLE), LOWER(:query)) > 0 OR instr(LOWER($TEXT), LOWER(:query)) > 0 COLLATE NOCASE")
    fun getAllForSearch(query: String): DataSource.Factory<Int, Note>

    @Query("SELECT * FROM $DB_NAME WHERE $ID = :$ID LIMIT 1")
    fun getNoteById(id: Int): Note?

    @Query("SELECT * FROM $DB_NAME WHERE $TITLE = :$TITLE LIMIT 1")
    fun getNoteByTitle(title: String): Note?

    @Query("SELECT * FROM $DB_NAME WHERE $TITLE = :query LIMIT 1")
    fun getSearchedNote(query: String): Note?

    @Query("SELECT * FROM $DB_NAME WHERE $NID = :$ID LIMIT 1")
    fun getNoteByNid(id: Long): Note?

    @Query("SELECT * FROM $DB_NAME WHERE $RID = :$ID LIMIT 1")
    fun getNoteByRid(id: Long): Note?

    @Query("SELECT * FROM $DB_NAME WHERE $SID = :$ID LIMIT 1")
    fun getNoteBySid(id: Long): Note?

    @Query("SELECT * FROM $DB_NAME WHERE $NID != $NUM_UNDEF")
    fun getAllConstedNotes(): List<Note>?

    @Query("SELECT * FROM $DB_NAME WHERE $RID != $NUM_UNDEF")
    fun getAllReminderedNotes(): List<Note>?

    @Query("SELECT * FROM $DB_NAME WHERE $SID != $NUM_UNDEF")
    fun getAllScheduledNotes(): List<Note>?

    @Query("SELECT * FROM $DB_NAME WHERE $WID = :$ID LIMIT 1")
    fun getNoteByWidgetId(id: Long): Note?

    @Query("SELECT * FROM $DB_NAME WHERE $WID != $NUM_UNDEF")
    fun getAllWidgetedNotes(): List<Note>?

    @Query("SELECT * FROM $DB_NAME WHERE $NID != $NUM_UNDEF OR $RID != $NUM_UNDEF OR ($SID != $NUM_UNDEF AND $SID_2 != $NUM_UNDEF)")
    fun getAllNonPureNonWidgetedNotes(): List<Note>?

    @Query("SELECT COUNT($ID) FROM $DB_NAME")
    fun getSize(): Int

    @Query("SELECT $ID FROM $DB_NAME WHERE $TITLE = :$TITLE")
    fun getIdByTitle(title: String): Int?

    @Query("SELECT $SID_2 FROM $DB_NAME WHERE $SID = :$SID")
    fun getSid2BySid(sid: Long): Long?

    @Query("DELETE FROM $DB_NAME WHERE $ID = :$ID")
    fun deleteById(id: Int)

    @Insert
    fun insert(n: Note)

    @Update
    fun update(n: Note)

    @Delete
    fun delete(n: Note)
}
