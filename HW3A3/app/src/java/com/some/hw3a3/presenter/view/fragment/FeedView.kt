package com.some.hw3a3.presenter.view.fragment

import com.arellomobile.mvp.MvpView
import com.some.hw3a3.view.fragment.feed.dummy.DummyContent

/**
 * @author Vad Nik.
 * @version dated Sep 11, 2018.
 * @link http://github.com/vadniks
 */
interface FeedView : MvpView {
    fun showAlert(title: String, msg: String)
}
