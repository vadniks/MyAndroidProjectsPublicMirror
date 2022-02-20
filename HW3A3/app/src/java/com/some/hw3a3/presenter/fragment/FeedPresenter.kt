package com.some.hw3a3.presenter.fragment

import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import com.some.hw3a3.model.Model

import com.some.hw3a3.presenter.view.fragment.FeedView
import com.some.hw3a3.processing.model.Repo
import com.some.hw3a3.view.fragment.feed.ListAdapter
import com.some.hw3a3.view.fragment.feed.dummy.DummyContent

/**
 * @author Vad Nik.
 * @version dated Sep 11, 2018.
 * @link http://github.com/vadniks
 */
@InjectViewState
class FeedPresenter : MvpPresenter<FeedView>() {
    private lateinit var adaper: ListAdapter

    fun setAdapter(adapter: ListAdapter) {
        this.adaper = adapter
    }

    fun addItem(item: DummyContent.DummyItem) {
        Model.instance.currentFeedItems.add(item)
        adaper.addItem(item)
    }

    fun addItems(items: ArrayList<DummyContent.DummyItem>) {
        Model.instance.currentFeedItems.addAll(items)
        adaper.addItems(items)
    }

    fun addItems2(items: ArrayList<DummyContent.DummyItem>) {
        flush()
        Model.instance.currentFeedItems.addAll(items)
    }

    fun flush() {
        Model.instance.currentFeedItems = ArrayList()
    }

    fun replace(index: Int, item: DummyContent.DummyItem) {
        Model.instance.currentFeedItems[index] = item
        adaper.replace(index, item)
    }

    fun getItem(id: Int): DummyContent.DummyItem = adaper.getItemAt(id)

    fun getSize(): Int = adaper.itemCount

    fun oOnItemClick(owner: String): Unit = viewState.showAlert("Repositories:",
            getStringedNames(getRepos(owner)) ?: "<none>")

    private fun getRepos(owner: String, name: String = ""): ArrayList<Repo>? {
        val arr = ArrayList<Repo>()

        for (r in Model.instance.dbc.read()) {
            if (name != "") {
                if (r.owner.login == owner && r.name == name)
                    arr.add(r)
            } else {
                if (r.owner.login == owner)
                    arr.add(r)
            }
        }
        return if (arr.isEmpty()) null else arr
    }

    private fun getStringedNames(arr: ArrayList<Repo>?): String? {
        var s: String? = null

        if (arr == null)
            return null

        for (a in arr) {
            if (s?.contains(a.name)/* == true*/)
                continue

            s += "${a.name}, "
        }
        return s?.substringAfterLast("null")
    }

    /**
     * Class holds non-void(non-unit in kotlin) methods,
     * to add possibility to use them against moxy (mvp) needs.
     */
    object Return {
        lateinit var getItem: (id: Int) -> DummyContent.DummyItem
        lateinit var getSize: () -> Int
    }
}
