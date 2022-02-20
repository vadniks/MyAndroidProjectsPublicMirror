package com.some.hw3a3.view.fragment.feed

import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.arellomobile.mvp.MvpAppCompatFragment
import com.arellomobile.mvp.presenter.InjectPresenter

import com.some.hw3a3.R
import com.some.hw3a3.presenter.fragment.FeedPresenter
import com.some.hw3a3.presenter.view.fragment.FeedView
import com.some.hw3a3.view.fragment.feed.dummy.DummyContent.DummyItem

/**
 * @author Vad Nik.
 * @version dated Sep 11, 2018.
 * @link http://github.com/vadniks
 */
class FeedFragment : MvpAppCompatFragment(), FeedView {
    @InjectPresenter
    lateinit var presenter: FeedPresenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_feed_list, container, false)

        val b = (arguments!!.getSerializable(ITEMS) as ArrayList<DummyItem>)
        val a = ListAdapter(b)

        if (view is RecyclerView) {
            with(view) {
                layoutManager = LinearLayoutManager(context)
                adapter = a
            }
        }

        presenter.attachView(this)

        presenter.addItems2(b)

        FeedPresenter.Return.getItem = { id: Int -> a.getItemAt(id) }
        FeedPresenter.Return.getSize = { a.itemCount }

        a.mOnClickListener =  {
            presenter.oOnItemClick(a.getItemAt(it).title)
        }

        presenter.setAdapter(a)

        return view
    }

    override fun onStart() {
        super.onStart()
        instance = this
    }

    override fun showAlert(title: String, msg: String) {
        AlertDialog.Builder(activity!!).apply {
            setCancelable(true)
            setTitle(title)
            setMessage(msg)
        }.show()
    }

    companion object {
        private lateinit var instance: FeedFragment

        fun getInstance() = instance

        const val ITEMS = "items"

        @JvmStatic
        fun newInstance(items: ArrayList<DummyItem>) =
                FeedFragment().apply {
                    arguments = Bundle().apply {
                        putSerializable(ITEMS, items)
                    }
                }
    }
}
