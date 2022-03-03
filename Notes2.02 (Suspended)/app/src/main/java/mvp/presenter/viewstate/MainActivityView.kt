/*
 * Copyright (C) 2018-2020 Vad Nik (http://github.com/vadniks). All rights reserved.
 *
 * This code is a part of proprietary software.
 * Usage, distribution, redistribution, modifying
 * and/or commercial use of this code,
 * without author's written permission, are strongly prohibited.
 */

package .mvp.presenter.viewstate

import android.content.Context
import android.content.Intent
import android.view.MenuInflater
import android.view.View
import androidx.appcompat.view.ActionMode
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.FragmentManager

/**
 * @author Vad Nik
 * @version dated Jul 09, 2019.
 * @link https://github.com/vadniks
 */
interface MainActivityView : IViewState {

    fun _getFragmentManager(): FragmentManager

    fun setToolbar(t: Toolbar)

    fun _getMenuInflater(): MenuInflater

    fun _getIntent(): Intent

    fun getContext(): Context

    fun _setResult(res: Int, data: Intent?)

    fun _finish()

    fun _setTheme(isDark: Boolean)

    fun _startActionMode(callback: ActionMode.Callback)

    fun _startActivityForResult(i: Intent, r: Int)

    fun setShowHomeButton(b: Boolean)

    fun performBack()

    fun getRootView(): View
}
