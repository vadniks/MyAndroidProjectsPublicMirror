/*
 * Copyright (C) 2018-2020 Vad Nik (http://github.com/vadniks). All rights reserved.
 *
 * This code is a part of proprietary software.
 * Usage, distribution, redistribution, modifying
 * and/or commercial use of this code,
 * without author's written permission, are strongly prohibited.
 */

package .processing.database

import android.content.Context
import androidx.annotation.WorkerThread
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.commonsware.cwac.saferoom.SafeHelperFactory
import .common.toEditable
import .processing.common.DB_NAME
import .processing.common.DB_VERSION
import .processing.common.Note

/**
 * @author Vad Nik
 * @version dated Jul 12, 2019.
 * @link https://github.com/vadniks
 */
@WorkerThread
@Database(entities = [Note::class], exportSchema = false, version = DB_VERSION)
abstract class NotesDatabase : RoomDatabase() {

    abstract fun noteDao(): NoteDao

    companion object {
        private var instance: NotesDatabase? = null

        @Synchronized
        fun getInstance(context: Context, password: String?): NotesDatabase = instance ?:
                Room.databaseBuilder(context, NotesDatabase::class.java, DB_NAME)
                    .apply {
                        if (password != null)
                            openHelperFactory(SafeHelperFactory.fromUser(password.toEditable()))
                    }
                    .fallbackToDestructiveMigration()
                    .build().apply { instance = this }
    }
}
