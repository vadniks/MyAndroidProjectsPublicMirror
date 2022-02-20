package com.some.hw3a3.presenter

import android.graphics.Bitmap
import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import com.some.hw3a3.FragmentType
import com.some.hw3a3.model.Model
import com.some.hw3a3.presenter.view.MainView
import com.some.hw3a3.processing.model.GithubUser
import com.some.hw3a3.processing.data.rest.NetApiClient
import com.some.hw3a3.processing.db.DatabaseClient
import com.some.hw3a3.view.fragment.feed.dummy.DummyContent
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking
import java.util.*
import kotlin.collections.ArrayList

/**
 * @author Vad Nik.
 * @version dated Sep 11, 2018.
 * @link http://github.com/vadniks
 */
@InjectViewState
class MainPresenter : MvpPresenter<MainView>() {
    @Deprecated("")
    val values = HashMap<Int, Bitmap>()
    private var usersCount = 0
    private var userCounter = 0

    fun changeFragment(fragment: FragmentType, items: ArrayList<DummyContent.DummyItem>) {
        Model.instance.currentFragment = fragment
        viewState.changeFragment(fragment, items)
    }

    fun getSeacrhed(query: String): ArrayList<DummyContent.DummyItem> {
        val arr = ArrayList<DummyContent.DummyItem>()

        runBlocking {
            for (o in Model.instance.currentFeedItems) {
                if (o.title.contains(query, true) || o.details.contains(query, true))
                    arr.add(o)
            }
        }
        return arr
    }

    fun onSearch(items: ArrayList<DummyContent.DummyItem>) {
        Model.instance.currentFragment = FragmentType.FEED
        viewState.changeFragment(FragmentType.FEED, items)
    }

    private var uc = 0

    fun loadFromNet(usr: String, userCount: Int) {
        viewState.startLoading()

        val arr = ArrayList<DummyContent.DummyItem>()

        NetApiClient.getUser(usr).subscribe(object : Observer<GithubUser> {
            private var counter = 0

            override fun onComplete() {
                println("testo compl $arr")
                changeFragment(FragmentType.FEED, Model.instance.globalFeedItems)
            }

            override fun onSubscribe(d: Disposable) {}

            override fun onNext(t: GithubUser) {
                println("testo next")
                runBlocking {
                    viewState.loadImage(t.avatar_url) { b: Bitmap ->
                        Model.instance.globalFeedItems.add(DummyContent.DummyItem(counter, t.login, b, "Details"))
                        changeFragment(FragmentType.FEED, Model.instance.globalFeedItems)
                        println("testo l_i lmb")
                        uc++

                        println("testo co $uc $userCount")
                        if (uc == userCount)
                            launch(CommonPool) {
                                viewState.stopLoading()
                            }
                    }
                }

                println("testo post end")

                println("testo o_n $t ${arr.size}")
                counter++
            }

            override fun onError(e: Throwable) {
                viewState.stopLoading()
                viewState.showToast(e.message!!)
                e.printStackTrace()
            }
        })
    }

    fun loadUsers(users: Array<String>) {
        for (i in users)
            loadFromNet(i, 2)
    }

    @Deprecated("")
    fun loadFromNet(/*usr: Array<String>*/) {
        viewState.startLoading()

        usersCount = 2

        NetApiClient.getUsers(arrayOf("", "")).subscribe(object : Observer<GithubUser> {
            private var counter = 0

            override fun onComplete() {
                this@MainPresenter.viewState.stopLoading()
                changeFragment(FragmentType.FEED, Model.instance.globalFeedItems)
            }

            override fun onSubscribe(d: Disposable) {}

            override fun onNext(t: GithubUser) {
                viewState.loadImage(t.avatar_url) { b: Bitmap ->

                    values[userCounter] = b

                    Model.instance.globalFeedItems.add(DummyContent.DummyItem(counter, t.login, b, details = "Details"))
                    println("testo onNext")

                    userCounter++
                }
                counter++
            }

            override fun onError(e: Throwable) {
                this@MainPresenter.viewState.stopLoading()
                viewState.showToast(e.message!!)
                e.printStackTrace()
            }
        })
    }

    fun loadReposFromNet(users: Array<String>) {
        for (user in users) {
            NetApiClient.getRepos(user).apply {
                subscribe { onNext -> Model.instance.dbc.write(onNext) }
            }
        }
    }

    fun onActivityDestroy(): Unit = Model.instance.dbc.close()

    var curFeedItems = Model.instance.currentFeedItems
        set(value) {
            Model.instance.currentFeedItems = value
        }

    var globalFeedItems = Model.instance.globalFeedItems
        set(value) {
            Model.instance.globalFeedItems = value
        }

    var onGlobalChanged = Model.instance.onGlobalChanged
        set(value) {
            Model.instance.onGlobalChanged = value
        }

    var currentFragment = Model.instance.currentFragment
        set(value) {
            Model.instance.currentFragment = value
        }
}
