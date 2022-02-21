/*
 * Copyright (C) 2018, 2019 Vad Nik (http://github.com/vadniks). All rights reserved.
 *
 * This code is a part of proprietary software.
 * Usage, distribution, redistribution, modifying 
 * and/or commercial use of this code,
 * without author's written permission, are strongly prohibited. 
 */

package 

import android.support.annotation.StringRes

/**
 * @author Vad Nik.
 * @version dated Dec 13, 2018.
 * @link http://github.com/vadniks
 */
interface EditorView {
    fun showToast(msg: String)
    fun _getString(@StringRes id: Int): String
}
