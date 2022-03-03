/*
 * Copyright (C) 2018-2020 Vad Nik (http://github.com/vadniks). All rights reserved.
 *
 * This code is a part of proprietary software.
 * Usage, distribution, redistribution, modifying
 * and/or commercial use of this code,
 * without author's written permission, are strongly prohibited.
 */

package .mvp.presenter.fragment

import .mvp.model.individual.presenter.fragment.MainFragmentModel
import .mvp.presenter.BasePresenter
import .mvp.presenter.viewbridge.fragment.MainFragmentViewBridge
import .mvp.presenter.viewstate.fragment.MainFragmentView

/**
 * @author Vad Nik
 * @version dated Jul 09, 2019.
 * @link https://github.com/vadniks
 */
abstract class MainFragmentPresenter(view: MainFragmentView, vararg args: Any) :
    BasePresenter<MainFragmentView, MainFragmentModel, MainFragmentViewBridge>(view, *args)
