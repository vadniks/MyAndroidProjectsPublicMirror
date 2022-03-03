/*
 * Copyright (C) 2018-2020 Vad Nik (http://github.com/vadniks). All rights reserved.
 *
 * This code is a part of proprietary software.
 * Usage, distribution, redistribution, modifying
 * and/or commercial use of this code,
 * without author's written permission, are strongly prohibited.
 */

package .mvp.presenter.viewstate.fragment

import android.content.Context
import android.os.Bundle
import .mvp.presenter.viewstate.IViewState

/**
 * @author Vad Nik
 * @version dated Jul 11, 2019.
 * @link https://github.com/vadniks
 */
interface MainFragmentView : IViewState {
    
    fun _getContext(): Context
    
    fun getArgs(): Bundle?
}
