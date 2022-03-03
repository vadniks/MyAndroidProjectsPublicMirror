/*
 * Copyright (C) 2018-2020 Vad Nik (http://github.com/vadniks). All rights reserved.
 *
 * This code is a part of proprietary software.
 * Usage, distribution, redistribution, modifying 
 * and/or commercial use of this code,
 * without author's written permission, are strongly prohibited. 
 */

package .mvp.model.individual.presenter.fragment

import android.content.Context
import androidx.annotation.WorkerThread
import .processing.common.Note

/**
 * @author Vad Nik
 * @version dated Aug 13, 2019.
 * @link https://github.com/vadniks
 */
interface EditFragmentModel : IFragmentIndividualModel {
    
    fun reminderChoose(n: Note, isView: Boolean, context: Context)
    
    fun getNote(): Note?

    @WorkerThread
    fun dismissNotifs(n: Note, context: Context)

    fun onCreateView()
}
