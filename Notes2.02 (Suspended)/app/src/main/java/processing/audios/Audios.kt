/*
 * Copyright (C) 2018-2020 Vad Nik (http://github.com/vadniks). All rights reserved.
 *
 * This code is a part of proprietary software.
 * Usage, distribution, redistribution, modifying
 * and/or commercial use of this code,
 * without author's written permission, are strongly prohibited.
 */

package .processing.audios

import android.content.Context
import androidx.annotation.UiThread
import androidx.annotation.WorkerThread
import .mvp.model.individual.IIndividualModel
import .mvp.model.individual.IndividualModelWrapper
import .processing.audios.AudiosGetter.getAudios
import .processing.common.Note
import java.io.File

/**
 * @author Vad Nik
 * @version dated Aug 24, 2019.
 * @link https://github.com/vadniks
 */
abstract class Audios(
    protected val context: Context,
    protected val note: Note?,
    im: IIndividualModel)
: IndividualModelWrapper(im) {
    protected val isView = note != null

    @UiThread
    abstract fun showAudioDialog()
    
    @WorkerThread
    abstract fun deleteAllAudios()
    
    @WorkerThread
    abstract fun deleteAudio(name: String)
    
    @WorkerThread
    abstract fun getAudiosNames(): Array<String>?
    
    @WorkerThread
    abstract fun restoreAudios()

    abstract fun getFileFromTitle(t: String): File
    
    companion object {
        
        fun get(context: Context, note: Note?) = getAudios(context, note)
    }
}
