/*
 * Copyright (C) 2018-2020 Vad Nik (http://github.com/vadniks). All rights reserved.
 *
 * This code is a part of proprietary software.
 * Usage, distribution, redistribution, modifying 
 * and/or commercial use of this code,
 * without author's written permission, are strongly prohibited. 
 */

package .mvp.model.individual

import android.content.Context
import android.os.Bundle
import androidx.annotation.AnyThread
import androidx.annotation.UiThread
import androidx.annotation.WorkerThread
import .mvp.model.Model
import .processing.common.Note

/**
 * @author Vad Nik
 * @version dated Aug 13, 2019.
 * @link https://github.com/vadniks
 */
interface IIndividualModel {
    val commands: ICommands
    val model: Model
    
    @WorkerThread
    fun save(n: Note, context: Context)
    
    @WorkerThread
    fun update(n: Note)
    
    @WorkerThread
    fun delete(n: Note, context: Context)
    
    @WorkerThread
    fun getNote(id: Int): Note?
    
    @WorkerThread
    fun isNotePureUsual(id: Int): Boolean

    @WorkerThread
    fun isNoteNotified(id: Int): Boolean

    @WorkerThread
    fun isNoteWidgeted(id: Int): Boolean

    @AnyThread
    fun notifyUserNoteIsNotPureUsual(context: Context)
    
    @WorkerThread
    fun doesNoteAlreadyExist(title: String): Boolean
    
    @AnyThread
    fun notifyUserNoteAlreadyExists(context: Context)
    
    @UiThread
    fun restart(context: Context)
    
    fun getBoolean(key: String, context: Context): Boolean
    
    fun setBoolean(key: String, context: Context, value: Boolean)
    
    fun isDBEncrypted(context: Context): Boolean
    
    @WorkerThread
    fun isDBEmpty(context: Context): Boolean
    
    fun initiateOpening(context: Context, extras: Bundle)
    
    fun updateWidget(
        context: Context,
        id: Long,
        title: String,
        text: String,
        color: Int)

    fun updateNotif(
        context: Context,
        mode: Int,
        id: Long,
        title: String,
        text: String,
        oldTitle: String,
        oldText: String)

    fun chooseOfNotifIds(item: Note): Long

    fun determineNotifMode(item: Note): Int

    fun checkPassword(pass: String, context: Context): Boolean
    
    fun isDark(context: Context): Boolean
    
    fun setDark(context: Context, isDark: Boolean)
    
    @AnyThread
    fun notifyUserNoteDoesntExist(context: Context)

    fun checkSelfPermission(context: Context, p: String): Int

    fun checkSelfPermission2(context: Context, p: String): Boolean

    @AnyThread
    fun notifyUserPermissionsNeeded(context: Context)

    fun decodeHardcoded(e: String): String

    fun decodeHardcodedWithoutEquals(e: String): String

    fun decodeHardcodedWithoutEqual(e: String): String

    fun isAllAvailable(context: Context): Boolean

    fun areAdsDisabled(context: Context): Boolean

    @AnyThread
    fun notifyUserThisIsPaid(context: Context)

    fun checkIsAllAvailable(context: Context): Boolean

    companion object {
        const val EVENT_BOOT_COMPLETED = 0x00000010
        const val EVENT_NOTIFICATIONS = 0x00000012
        const val EVENT_DELETE_WIDGET = 0x00000014
    }
}
