/*
 * Copyright (C) 2018-2020 Vad Nik (http://github.com/vadniks). All rights reserved.
 *
 * This code is a part of proprietary software.
 * Usage, distribution, redistribution, modifying
 * and/or commercial use of this code,
 * without author's written permission, are strongly prohibited.
 */

package .mvp.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.NavHostFragment
import .mvp.presenter.PresenterFactory
import .mvp.presenter.fragment.EditFragmentPresenter
import .mvp.presenter.viewstate.fragment.EditFragmentView

/**
 * @author Vad Nik
 * @version dated Jul 09, 2019.
 * @link https://github.com/vadniks
 */
class EditFragment : NavHostFragment(), EditFragmentView {
    private val presenter = PresenterFactory.get(PresenterFactory.ID_EF, this) as EditFragmentPresenter
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return presenter.getViewBridge().onCreateView(inflater, container, savedInstanceState)
    }
    
    override fun onResume() {
        super.onResume()
        presenter.onResume()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        presenter.onDestroy()
    }
    
    override fun getArgs(): Bundle? = arguments
    
    companion object {
        private var instance: EditFragment? = null

        fun getInstance() = instance ?: EditFragment().apply { instance = this }
    }
}
