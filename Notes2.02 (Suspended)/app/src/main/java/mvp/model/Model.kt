/*
 * Copyright (C) 2018-2020 Vad Nik (http://github.com/vadniks). All rights reserved.
 *
 * This code is a part of proprietary software.
 * Usage, distribution, redistribution, modifying 
 * and/or commercial use of this code,
 * without author's written permission, are strongly prohibited. 
 */

package .mvp.model

import android.content.Intent
import androidx.annotation.AnyThread
import androidx.annotation.UiThread
import androidx.annotation.WorkerThread
import com.google.android.gms.ads.AdRequest
import .processing.database.NoteDao
import java.io.File

/**
 * @author Vad Nik
 * @version dated Jul 09, 2019.
 * @link https://github.com/vadniks
 */
interface /* Kernel */ Model :
    CrossPresenterModel.OnActivityStarted,
    CrossPresenterModel.OnUIRendered,
    CrossPresenterModel.OnRequestPermissionsResult {

    val crossPresenterModel: CrossPresenterModel
    var password: String?
    var isActivityShown: Boolean
    
    fun driveOperations()

    fun getKeyForEncryption(): String

    fun rememberPassword()
    
    fun noteDao(): NoteDao
    
    fun onReceivedEventForDecryption(event: Int, intent: Intent?)
    
    @WorkerThread
    fun resetReminders()
    
    @WorkerThread
    fun resetWidgets()

    @UiThread
    fun createAdRequest(): AdRequest?

    @AnyThread
    fun showInterstitial()

    @AnyThread
    fun handleUncoughtException(t: Thread, e: Throwable)

    @AnyThread
    fun getExternalStorageFolder(): File?

    @AnyThread
    fun makeDateTime(): String

    @AnyThread
    fun logToFile(msg: String)

    companion object {
        const val MODE_NOTE_USUAL = 0x0
        const val MODE_NOTE_AUDIO = 0x2
        const val MODE_NOTE_DRAWN = 0x4

        const val LOGS_FOLDER_POSTFIX = "/Logs"
        const val LOG_FILE = "log.txt"
    }
}
