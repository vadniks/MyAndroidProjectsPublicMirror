/*
 * Copyright (C) 2018-2020 Vad Nik (http://github.com/vadniks). All rights reserved.
 *
 * This code is a part of proprietary software.
 * Usage, distribution, redistribution, modifying
 * and/or commercial use of this code,
 * without author's written permission, are strongly prohibited.
 */

package .mvp.presenter.fragment

import .mvp.model.individual.presenter.fragment.DrawFragmentModel
import .mvp.presenter.BasePresenter
import .mvp.presenter.viewbridge.fragment.DrawFragmentViewBridge
import .mvp.presenter.viewstate.fragment.DrawFragmentView

/**
 * @author Vad Nik
 * @version dated Sep 01, 2019.
 * @link https://github.com/vadniks
 */
abstract class DrawFragmentPresenter(view: DrawFragmentView, vararg args: Any) :
    BasePresenter<DrawFragmentView, DrawFragmentModel, DrawFragmentViewBridge>(view, *args)
