/*
 * Copyright (C) 2018-2020 Vad Nik (http://github.com/vadniks). All rights reserved.
 *
 * This code is a part of proprietary software.
 * Usage, distribution, redistribution, modifying
 * and/or commercial use of this code,
 * without author's written permission, are strongly prohibited.
 */

package .mvp.presenter

import .mvp.model.individual.presenter.MainActivityModel
import .mvp.presenter.viewbridge.MainActivityViewBridge
import .mvp.presenter.viewstate.MainActivityView

/**
 * @author Vad Nik
 * @version dated Jul 09, 2019.
 * @link https://github.com/vadniks
 */
abstract class MainActivityPresenter(view: MainActivityView, vararg args: Any) :
    BasePresenter<MainActivityView, MainActivityModel, MainActivityViewBridge>(view, *args)
