/*
 * Copyright (C) 2018-2020 Vad Nik (http://github.com/vadniks). All rights reserved.
 *
 * This code is a part of proprietary software.
 * Usage, distribution, redistribution, modifying
 * and/or commercial use of this code,
 * without author's written permission, are strongly prohibited.
 */

package .mvp.presenter.commands

import android.app.Activity
import android.content.Intent
import android.view.Menu
import android.view.View
import androidx.annotation.MenuRes
import androidx.appcompat.view.ActionMode
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.FragmentManager
import .mvp.model.individual.ICommands

/**
 * @author Vad Nik
 * @version dated Aug 13, 2019.
 * @link https://github.com/vadniks
 */
interface MainActivityCommands : ICommands {
    
    fun getFragmentManager(): FragmentManager
    
    fun setToolbar(t: Toolbar)
    
    fun inflateMenu(@MenuRes id: Int)
    
    fun deflateMenu()
    
    fun queueForMenuCreation(action: (menu: Menu?) -> Unit)
    
    fun finish()
    
    fun setResult(res: Int, data: Intent?)
    
    fun setTheme(isDark: Boolean)
    
    fun getIntent(): Intent
    
    fun startActionMode(callback: ActionMode.Callback)
    
    fun startActivityForResult(i: Intent, r: Int)
    
    fun getActivity(): Activity
    
    fun setSearchViewEnabled(e: Boolean)

    fun setShowHomeButton(b: Boolean)

    fun performBack()

    fun getRootView(): View
}
