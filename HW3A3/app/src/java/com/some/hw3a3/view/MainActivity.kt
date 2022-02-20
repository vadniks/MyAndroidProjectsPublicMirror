package com.some.hw3a3.view

import android.app.ProgressDialog
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.PersistableBundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.widget.SearchView
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import com.arellomobile.mvp.MvpAppCompatActivity
import com.arellomobile.mvp.presenter.InjectPresenter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.some.hw3a3.Consts
import com.some.hw3a3.FragmentType
import com.some.hw3a3.R
import com.some.hw3a3.presenter.MainPresenter
import com.some.hw3a3.presenter.view.MainView
import com.some.hw3a3.processing.db.DatabaseClient
import com.some.hw3a3.view.fragment.feed.FeedFragment
import com.some.hw3a3.view.fragment.feed.dummy.DummyContent
import com.some.hw3a3.view.fragment.profile.ProfileFragment
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * @author Vad Nik.
 * @version dated Sep 11, 2018.
 * @link http://github.com/vadniks
 */
@Suppress("deprecation")
class MainActivity : MvpAppCompatActivity(), MainView {
    @InjectPresenter
    lateinit var presenter: MainPresenter

    private var menu: Menu? = null
    private lateinit var searchView: SearchView
    private var isSVExpanded = false
    private lateinit var pd: ProgressDialog

    @Deprecated("")
    override fun startLoading() {
//        pd = ProgressDialog(this)
//        pd.show()
    }

    //TODO: uncoment and see magic...

    @Deprecated("")
    override fun stopLoading() {
//        println("testo dism")
//        pd.dismiss()
    }

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_feed -> {
                presenter.changeFragment(FragmentType.FEED, presenter.globalFeedItems)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_profile -> {
                if (isSVExpanded)
                    menu?.findItem(R.id.search_view)?.collapseActionView()

                presenter.changeFragment(FragmentType.PROFILE, presenter.globalFeedItems)
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

        presenter.attachView(this)

        presenter.loadReposFromNet(arrayOf())

        var c: String? = null

        if (savedInstanceState == null)
            presenter.loadUsers(arrayOf())
        else
            c = savedInstanceState.getString(CUR_FRAG)

        presenter.onGlobalChanged = { _: ArrayList<DummyContent.DummyItem> ->
            launch(CommonPool) {
                println("testo arrd ${presenter.globalFeedItems}")

                presenter.changeFragment(if (c != null) FragmentType.valueOf(c) else FragmentType.FEED,
                        presenter.curFeedItems)
            }
        }
    }

    override fun changeFragment(fragment: FragmentType, items: ArrayList<DummyContent.DummyItem>) {
        val ft = supportFragmentManager.beginTransaction()

        when (fragment) {
            FragmentType.FEED -> {
                if (menu != null)
                    menu!!.findItem(R.id.search_view).isVisible = true

                println("testo ch cur ${presenter.curFeedItems} ${presenter.globalFeedItems}")

                ft.replace(R.id.frag_holder, FeedFragment.newInstance(items))
            }
            FragmentType.PROFILE -> {
                if (menu != null)
                    menu!!.findItem(R.id.search_view).isVisible = false

                ft.replace(R.id.frag_holder, ProfileFragment.newInstance(), Consts.PROFILE_FRAGMENT_TAG)
            }
        }
        ft.commit()
    }

    override fun onBackPressed() {
        if (presenter.currentFragment == FragmentType.FEED) {
            //Model.currentFeedItems = Model.globalFeedItems
            presenter.changeFragment(FragmentType.FEED, presenter.globalFeedItems)
        }
        super.onBackPressed()
    }

    @Deprecated("only for testing")
    private fun test(id: Int, t: String): ArrayList<DummyContent.DummyItem> {
        val arr = ArrayList<DummyContent.DummyItem>()
        arr.add(DummyContent.DummyItem(
                id,
                t,
                BitmapFactory.decodeResource(resources, R.drawable.photos),
                "Details"))
        return arr
    }

    override fun onDestroy() {
        presenter.onActivityDestroy()
        super.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle?) {
        outState.putString(CUR_FRAG, presenter.currentFragment.name)
        super.onSaveInstanceState(outState, outPersistentState)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)

        searchView = menu.findItem(R.id.search_view).actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(query: String?): Boolean {
                presenter.changeFragment(FragmentType.FEED, presenter.globalFeedItems)

                menu.findItem(R.id.search_view).collapseActionView()
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                Toast.makeText(this@MainActivity, newText, Toast.LENGTH_SHORT).show()

                presenter.onSearch(presenter.getSeacrhed(newText))

                return false
            }

        })
        searchView.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {

            override fun onViewDetachedFromWindow(v: View?) {
                try {
                    presenter.changeFragment(FragmentType.FEED, presenter.globalFeedItems)
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
                isSVExpanded = false
            }

            override fun onViewAttachedToWindow(v: View?) {
                isSVExpanded = true
            }

        })

        this.menu = menu

        menu.findItem(R.id.search_view).isVisible = presenter.currentFragment != FragmentType.PROFILE

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (presenter.currentFragment != FragmentType.FEED) {
            if (item.itemId == R.id.search_view)
                return true
        }

        return super.onOptionsItemSelected(item)
    }

    companion object {
        private const val CUR_FRAG = "cur_flag"
    }

    @Suppress("deprecation")
    override fun loadImage(url: String, onComplete: (b: Bitmap) -> Unit) {
        println("testo l_n start")

        val c = Glide.with(this@MainActivity)
                .asBitmap()
                .load(url)

        c.into(object : SimpleTarget<Bitmap>() {

            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                println("testo l_i")
                onComplete.invoke(resource)
            }
        })
        println("testo l_i end")
    }

    override fun showToast(msg: String): Unit = Toast.makeText(this, msg, Toast.LENGTH_LONG).show()

    override fun doPost(block: () -> Unit) {
        Handler(Looper.getMainLooper()).post(block)
    }
}
