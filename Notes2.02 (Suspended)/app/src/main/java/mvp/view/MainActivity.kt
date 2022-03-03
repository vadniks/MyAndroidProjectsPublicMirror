/*
 * Copyright (C) 2018-2020 Vad Nik (http://github.com/vadniks). All rights reserved.
 *
 * This code is a part of proprietary software.
 * Usage, distribution, redistribution, modifying
 * and/or commercial use of this code,
 * without author's written permission, are strongly prohibited.
 */

package .mvp.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.FragmentManager
import androidx.navigation.Navigation
import .R
import .mvp.presenter.MainActivityPresenter
import .mvp.presenter.PresenterFactory
import .mvp.presenter.viewstate.MainActivityView

/**
 * @author Vad Nik
 * @version dated Jul 09, 2019.
 * @link https://github.com/vadniks
 */
class MainActivity : AppCompatActivity(), MainActivityView {
    private lateinit var presenter: MainActivityPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        presenter = PresenterFactory.get(PresenterFactory.ID_MA, this) as MainActivityPresenter
        
        _setTheme(presenter.getViewBridge().isDark(this))
        
        setContentView(R.layout.activity_main)
        
        presenter.getViewBridge().onSetContentViewCalled(
            Navigation.findNavController(this, R.id.nav_host_fragment))

        window.decorView.post(presenter.getViewBridge()::onUIRendered)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        presenter.getViewBridge().onCreateOptionsMenu(menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        presenter.getViewBridge().onOptionsItemSelected(item)
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        if (presenter.getViewBridge().onBackPressed())
            super.onBackPressed()
    }

    override fun onMenuOpened(featureId: Int, menu: Menu?): Boolean {
        presenter.getViewBridge().onMenuOpened()
        return super.onMenuOpened(featureId, menu ?: return false)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        presenter.getViewBridge().onActivityResult(requestCode, resultCode, data)
    }
    
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        presenter.getViewBridge().onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onPause() {
        super.onPause()
        presenter.getViewBridge().onPause()
    }

    override fun onResume() {
        super.onResume()
        presenter.getViewBridge().onResume()
    }

    override fun _getFragmentManager(): FragmentManager = super.getSupportFragmentManager()

    override fun setToolbar(t: Toolbar): Unit = super.setSupportActionBar(t)

    override fun _getMenuInflater(): MenuInflater = super.getMenuInflater()

    override fun _getIntent(): Intent = super.getIntent()

    override fun getContext(): Context = this

    override fun _setResult(res: Int, data: Intent?): Unit =
        if (data != null)
            super.setResult(res, data)
        else
            super.setResult(res)

    override fun _finish(): Unit = super.finish()

    override fun _setTheme(isDark: Boolean) =
        super.setTheme(if (isDark) R.style.DarkAppTheme else R.style.AppTheme)

    override fun _startActionMode(callback: ActionMode.Callback) {
        super.startSupportActionMode(callback)
    }

    override fun _startActivityForResult(i: Intent, r: Int): Unit = super.startActivityForResult(i, r)

    override fun setShowHomeButton(b: Boolean) {
        supportActionBar?.setDisplayShowHomeEnabled(b)
        supportActionBar?.setDisplayHomeAsUpEnabled(b)
    }

    override fun performBack() = this.onBackPressed()

    override fun getRootView(): View = findViewById(android.R.id.content)
}
