/*
 * Copyright (C) 2018-2020 Vad Nik (http://github.com/vadniks). All rights reserved.
 *
 * This code is a part of proprietary software.
 * Usage, distribution, redistribution, modifying
 * and/or commercial use of this code,
 * without author's written permission, are strongly prohibited.
 */

package .mvp.view.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.NavHostFragment
import .mvp.presenter.PresenterFactory
import .mvp.presenter.fragment.MainFragmentPresenter
import .mvp.presenter.viewstate.fragment.MainFragmentView

/**
 * @author Vad Nik
 * @version dated Jul 09, 2019.
 * @link https://github.com/vadniks
 */
class MainFragment : NavHostFragment(), MainFragmentView {
    private val presenter = PresenterFactory.get(PresenterFactory.ID_MF, this) as MainFragmentPresenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return presenter.getViewBridge().onCreateView(inflater, container, savedInstanceState)
    }
    
    override fun _getContext(): Context = activity!!
    
    override fun getArgs(): Bundle? = arguments
    
    override fun onDestroyView() {
        super.onDestroyView()
        presenter.onDestroy()
    }
    
    companion object {
        private var instance: MainFragment? = null

        fun getInstance() = instance ?: MainFragment().apply { instance = this }
    }
}
