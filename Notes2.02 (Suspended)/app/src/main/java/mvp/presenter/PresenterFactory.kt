/*
 * Copyright (C) 2018-2020 Vad Nik (http://github.com/vadniks). All rights reserved.
 *
 * This code is a part of proprietary software.
 * Usage, distribution, redistribution, modifying
 * and/or commercial use of this code,
 * without author's written permission, are strongly prohibited.
 */

package .mvp.presenter

import .mvp.presenter.impl.fragment.getDrawFragmentPresenter
import .mvp.presenter.impl.fragment.getEditFragmentPresenter
import .mvp.presenter.impl.fragment.getMainFragmentPresenter
import .mvp.presenter.impl.getMainActivityPresenter
import .mvp.presenter.viewstate.IViewState
import .mvp.presenter.viewstate.MainActivityView
import .mvp.presenter.viewstate.fragment.DrawFragmentView
import .mvp.presenter.viewstate.fragment.EditFragmentView
import .mvp.presenter.viewstate.fragment.MainFragmentView

/**
 * @author Vad Nik
 * @version dated Jul 09, 2019.
 * @link https://github.com/vadniks
 */
object PresenterFactory {
    const val ID_MA = 0x2
    const val ID_MF = 0x4
    const val ID_EF = 0x6
    const val ID_DF = 0x8

    fun get(id: Int, view: IViewState, vararg args: Any): IPresenter =
        when (id) {
            ID_MA -> getMainActivityPresenter(view as MainActivityView, *args)
            ID_MF -> getMainFragmentPresenter(view as MainFragmentView, *args)
            ID_EF -> getEditFragmentPresenter(view as EditFragmentView, *args)
            ID_DF -> getDrawFragmentPresenter(view as DrawFragmentView, *args)
            else -> throw IllegalArgumentException()
        }
}
