/*
 * Copyright (C) 2018-2020 Vad Nik (http://github.com/vadniks). All rights reserved.
 *
 * This code is a part of proprietary software.
 * Usage, distribution, redistribution, modifying 
 * and/or commercial use of this code,
 * without author's written permission, are strongly prohibited. 
 */

package .mvp.model.individual.presenter.fragment

import android.app.Activity
import android.content.Context
import androidx.annotation.UiThread
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.paging.PagedList
import .processing.common.Note

/**
 * @author Vad Nik
 * @version dated Aug 13, 2019.
 * @link https://github.com/vadniks
 */
interface MainFragmentModel : IFragmentIndividualModel{
    
    @WorkerThread
    fun getNotesPagingLimited(context: Context): LiveData<PagedList<Note>>
    
    @WorkerThread
    fun getNotesPagingLimited(query: String, context: Context): LiveData<PagedList<Note>>
    
    fun isFromWidget(): Boolean
    
    fun onListItemClicked(n: Note, context: Context)
    
    @WorkerThread
    fun getSearchedNote(query: String, context: Context): Note?
    
    @WorkerThread
    fun forceDismissAttached(context: Context)
    
    @WorkerThread
    fun forceDismissScheduled(context: Context)
    
    @WorkerThread
    fun resetReminders(context: Context)
    
    @WorkerThread
    fun resetWidgets(context: Context)
    
    @UiThread
    fun encryptDB(context: Context)
    
    @WorkerThread
    fun forceRemoveAudios(context: Context)

    @WorkerThread
    fun forceRemoveDrawns(context: Context)

    fun send(n: Note, context: Context)

    fun buy(activity: Activity)

    fun onCreateView()

    fun onDestroyView()

    @WorkerThread
    fun restoreAudios(context: Context)

    @WorkerThread
    fun restoreDrawns(context: Context)

    @WorkerThread
    fun removeExtData()

    @UiThread
    fun exportDB(context: Context)
}
