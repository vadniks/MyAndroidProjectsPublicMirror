package com.some.hw3a3.model

import com.some.hw3a3.App
import com.some.hw3a3.FragmentType
import com.some.hw3a3.processing.db.DatabaseClient
import com.some.hw3a3.view.fragment.feed.dummy.DummyContent
import javax.inject.Inject

/**
 * @author Vad Nik.
 * @version dated Sep 11, 2018.
 * @link http://github.com/vadniks
 */
//object (i.e jvm static fields) incompatible with Dagger.
class /*object*/ Model {
    companion object {
        val instance = Model()
    }

    init {
        App.getComponent().inject(this)
    }

    var onGlobalChanged: (newVal: ArrayList<DummyContent.DummyItem>) -> Unit = { }

    var currentFragment: FragmentType = FragmentType.FEED

    var currentFeedItems: ArrayList<DummyContent.DummyItem> = ArrayList()

    var globalFeedItems: ArrayList<DummyContent.DummyItem> = ArrayList()
        set(value) {
            field = value
            onGlobalChanged.invoke(value)
        }

    @Inject
    lateinit var dbc: DatabaseClient
}
