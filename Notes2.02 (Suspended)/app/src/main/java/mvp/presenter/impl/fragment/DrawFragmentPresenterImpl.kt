/*
 * Copyright (C) 2018-2020 Vad Nik (http://github.com/vadniks). All rights reserved.
 *
 * This code is a part of proprietary software.
 * Usage, distribution, redistribution, modifying
 * and/or commercial use of this code,
 * without author's written permission, are strongly prohibited.
 */

package .mvp.presenter.impl.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.ads.AdView
import .R
import .common.doInCoroutine
import .common.doInUI
import .common.visibleOrGone
import .mvp.model.individual.IndividualModelFactory
import .mvp.model.individual.presenter.fragment.DrawFragmentModel
import .mvp.presenter.commands.fragment.DrawFragmentCommands
import .mvp.presenter.fragment.DrawFragmentPresenter
import .mvp.presenter.viewbridge.fragment.DrawFragmentViewBridge
import .mvp.presenter.viewstate.fragment.DrawFragmentView

/**
 * @author Vad Nik
 * @version dated Sep 01, 2019.
 * @link https://github.com/vadniks
 */

fun getDrawFragmentPresenter(view: DrawFragmentView, vararg args: Any): DrawFragmentPresenter =
    DrawFragmentPresenterImpl(view, *args)

private class DrawFragmentPresenterImpl(view: DrawFragmentView, vararg args: Any) :
    DrawFragmentPresenter(view, *args), DrawFragmentCommands, DrawFragmentViewBridge {
    
    override val model: DrawFragmentModel =
        IndividualModelFactory.imf.getPresenterModel(IndividualModelFactory.ID_DF, this) as DrawFragmentModel
    
    init {
        model.init()
    }
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val v = model.onCreateView(inflater, container, savedInstanceState)

        model.crossPresenterModel.setToolbar(v.findViewById(R.id.fragment_draw_toolbar))
        model.crossPresenterModel.inflateMenu(R.menu.menu_stub)

        model.crossPresenterModel.setShowHomeButton(true)

        v.findViewById<AdView>(R.id.fragment_draw_ad_view).apply {
            doInCoroutine {
                val r = model.createAdRequest()

                if (r == null) {
                    this.visibility = visibleOrGone(false)
                    return@doInCoroutine
                }

                doInUI { loadAd(r) }
            }
        }

        return v
    }
    
    override fun getArgs(): Bundle? = view.getArgs()
    
    override fun getContext(): Context = view._getContext()
    
    override fun onDestroy() {
        super.onDestroy()
        model.onDestroy()
    }
}
