package com.some.hw3a3.presenter.view

import android.graphics.Bitmap
import com.arellomobile.mvp.MvpView
import com.some.hw3a3.FragmentType
import com.some.hw3a3.view.fragment.feed.dummy.DummyContent
import java.util.*

/**
 * @author Vad Nik.
 * @version dated Sep 11, 2018.
 * @link http://github.com/vadniks
 */
interface MainView : MvpView {
    fun changeFragment(fragment: FragmentType, items: ArrayList<DummyContent.DummyItem>)
    fun startLoading()
    fun stopLoading()
    fun loadImage(url: String, onComplete: (b: Bitmap) -> Unit)
    fun showToast(msg: String)
    fun doPost(block: () -> Unit)
}
