/*
 * Copyright (C) 2018-2020 Vad Nik (http://github.com/vadniks). All rights reserved.
 *
 * This code is a part of proprietary software.
 * Usage, distribution, redistribution, modifying
 * and/or commercial use of this code,
 * without author's written permission, are strongly prohibited.
 */

package .processing.draw.processing

import android.content.Context
import android.view.View
import androidx.annotation.WorkerThread
import .mvp.model.CrossPresenterModel
import .mvp.model.individual.IIndividualModel
import .processing.common.Note
import .processing.draw.processing.DrawnGetter.getDrawn
import java.io.File

/**
 * @author Vad Nik
 * @version dated Sep 01, 2019.
 * @link https://github.com/vadniks
 */
interface Drawn : IIndividualModel, CrossPresenterModel.OnMenuButtonClicked {
    
    fun initializeView(): View
    
    fun onDestroy()
    
    @WorkerThread
    fun deleteDrawn(title: String)
    
    @WorkerThread
    fun deleteAllDrawns()
    
    @WorkerThread
    fun restoreAllDrawns()

    fun getFileFromTitle(t: String): File

    companion object {
        
        fun get(note: Note?, context: Context): Drawn = getDrawn(note, context)
    }
}
