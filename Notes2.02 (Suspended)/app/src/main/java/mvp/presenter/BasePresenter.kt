/*
 * Copyright (C) 2018-2020 Vad Nik (http://github.com/vadniks). All rights reserved.
 *
 * This code is a part of proprietary software.
 * Usage, distribution, redistribution, modifying
 * and/or commercial use of this code,
 * without author's written permission, are strongly prohibited.
 */

package .mvp.presenter

import androidx.lifecycle.ViewModel
import .mvp.model.individual.ICommands
import .mvp.model.individual.IIndividualModel
import .mvp.presenter.viewbridge.IViewBridge
import .mvp.presenter.viewstate.IViewState

/**
 * @author Vad Nik
 * @version dated Jul 11, 2019.
 * @link https://github.com/vadniks
 */
abstract class BasePresenter
    <View : IViewState, Model : IIndividualModel, ViewBridge : IViewBridge>
    constructor(protected val view: View, @Suppress("UNUSED") vararg args: Any) :
    ViewModel(), IPresenter, ICommands, IViewBridge {
    
    protected abstract val model: Model
    
    fun getCommands(): ICommands = this
    
    @Suppress("UNCHECKED_CAST")
    fun getViewBridge(): ViewBridge = this as ViewBridge
    
    open fun onCreate() = Unit

    open fun onStart() = Unit

    open fun onResume() = Unit

    open fun onPause() = Unit

    open fun onStop() = Unit

    open fun onDestroy() = Unit
}
