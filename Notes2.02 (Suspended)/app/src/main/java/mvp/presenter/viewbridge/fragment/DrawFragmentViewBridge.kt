/*
 * Copyright (C) 2018-2020 Vad Nik (http://github.com/vadniks). All rights reserved.
 *
 * This code is a part of proprietary software.
 * Usage, distribution, redistribution, modifying
 * and/or commercial use of this code,
 * without author's written permission, are strongly prohibited.
 */

package .mvp.presenter.viewbridge.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import .mvp.presenter.viewbridge.IViewBridge

/**
 * @author Vad Nik
 * @version dated Sep 01, 2019.
 * @link https://github.com/vadniks
 */
interface DrawFragmentViewBridge : IViewBridge {
    
    fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    
    fun getArgs(): Bundle?
}
