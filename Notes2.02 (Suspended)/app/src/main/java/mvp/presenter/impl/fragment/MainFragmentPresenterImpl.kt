/*
 * Copyright (C) 2018-2020 Vad Nik (http://github.com/vadniks). All rights reserved.
 *
 * This code is a part of proprietary software.
 * Usage, distribution, redistribution, modifying
 * and/or commercial use of this code,
 * without author's written permission, are strongly prohibited.
 */

package .mvp.presenter.impl.fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.view.*
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.appcompat.view.ActionMode
import androidx.appcompat.widget.SearchView
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.LiveData
import androidx.paging.PagedList
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.gms.ads.AdView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import .BuildConfig
import .R
import .common.*
import .mvp.model.CrossPresenterModel
import .mvp.model.Model
import .mvp.model.individual.IndividualModelFactory
import .mvp.model.individual.presenter.fragment.MainFragmentModel
import .mvp.presenter.commands.fragment.MainFragmentCommands
import .mvp.presenter.fragment.MainFragmentPresenter
import .mvp.presenter.viewbridge.fragment.MainFragmentViewBridge
import .mvp.presenter.viewstate.fragment.MainFragmentView
import .processing.common.ListAdapter
import .processing.common.Note

/**
 * @author Vad Nik
 * @version dated Jul 09, 2019.
 * @link https://github.com/vadniks
 */

fun getMainFragmentPresenter(view: MainFragmentView, vararg args: Any): MainFragmentPresenter =
    MainFragmentPresenterImpl(view, *args)

private class MainFragmentPresenterImpl(view: MainFragmentView, vararg args: Any) :
    MainFragmentPresenter(view, *args),
    MainFragmentCommands,
    MainFragmentViewBridge,
    View.OnClickListener,
    SwipeRefreshLayout.OnRefreshListener,
    CrossPresenterModel.OnMenuButtonClicked {
    
    private lateinit var adapter: ListAdapter
    private lateinit var context: Context
    private lateinit var recycler: RecyclerView
    private lateinit var emptyText: TextView
    private lateinit var fab: FloatingActionButton
    private lateinit var fabU: FloatingActionButton
    private lateinit var fabA: FloatingActionButton
    private lateinit var fabD: FloatingActionButton
    private lateinit var fabToTop: FloatingActionButton
    private lateinit var srl: SwipeRefreshLayout
    private lateinit var liveList: LiveData<PagedList<Note>>
    private val chosenItems = ArrayList<Pair<Note, View>>()
    private var isChoosingItems = false
    private var isFromWidget = false
    
    override val model: MainFragmentModel =
        IndividualModelFactory.imf.getPresenterModel(IndividualModelFactory.ID_MF, this) as MainFragmentModel

    init {
        model.init()
    }
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val v = inflater.inflate(R.layout.fragment_main, container, false)

        model.crossPresenterModel.setToolbar(v.findViewById(R.id.fragment_main_toolbar))
        model.crossPresenterModel.inflateMenu(R.menu.menu_main_actions)

        model.crossPresenterModel.setShowHomeButton(false)

        context = v.context

        initRecycler(v)

        initFabs(v)
        
        isFromWidget = model.isFromWidget()
        if (!isFromWidget)
            initSearch()

        fab.isEnabled = !isFromWidget
        fabToTop.isEnabled = !isFromWidget
        
        srl = v.findViewById(R.id.main_fragment_srl)
        srl.setOnRefreshListener(this)

        emptyText = v.findViewById(R.id.textNoNotes)
        
        onRefresh()
        
        model.crossPresenterModel.subscribeForMenuButton(this)

        v.findViewById<AdView>(R.id.fragment_main_ad_view).apply {
            doInCoroutine {
                val r = model.createAdRequest()
                if (r == null) {
                    this.visibility = visibleOrGone(false)
                    return@doInCoroutine
                }
                doInUI { loadAd(r) }
            }
        }

        model.onCreateView()

        return v
    }
    
    override fun onDestroy() {
        super.onDestroy()
        model.crossPresenterModel.unsubscribeOfMenuButton(this)
        model.onDestroyView()
    }
    
    override fun onClick(v: View?) {
        when (v?.id ?: return) {
            R.id.fabCreateUsual -> model.crossPresenterModel.initiateCreating(Model.MODE_NOTE_USUAL, null)
            R.id.fabCreateAudio -> model.crossPresenterModel.initiateCreating(Model.MODE_NOTE_AUDIO, null)
            R.id.fabCreateDrawn -> model.crossPresenterModel.initiateCreating(Model.MODE_NOTE_DRAWN, null)
            R.id.fabToTop -> recycler.scrollToPosition(0)
        }
    }
    
    override fun getArgs(): Bundle? = view.getArgs()
    
    @SuppressLint("SetTextI18n")
    override fun onMenuButtonClicked() {
        if (isFromWidget)
            return
        
        BottomSheetDialog(R.menu.menu_main, context, model.isDark(context)) { id, instance ->
            instance.dismiss()
        
            when (id) {
                R.id.menu_main_change_theme -> model.changeTheme(!model.isDark(view._getContext()), view._getContext())
                R.id.menu_main_db_encrypt -> model.encryptDB(view._getContext())
                R.id.menu_main_drive -> model.model.driveOperations()
                R.id.menu_main_remember_password -> model.model.rememberPassword()
                R.id.menu_main_buy -> model.buy(view._getContext() as Activity)
                R.id.menu_main_about -> model.crossPresenterModel.showCustomDialog(
                    context.getString(R.string.about),
                    TextView(context).apply {
                        gravity = Gravity.CENTER
                        typeface = Typeface.MONOSPACE
                        text = model.decodeHardcoded("") + "\nv. " + BuildConfig.VERSION_NAME +
                                '\n' + model.decodeHardcoded("")
                        // Created by the  2018-2019 //TODO: change to 2020
                    },
                    true)
                R.id.menu_main_debug -> showDebugActions()
                R.id.menu_main_export_db -> model.exportDB(view._getContext())
            }
        }.show()
    }

    private fun showDebugActions() =
        BottomSheetDialog(R.menu.menu_main_debug, context, model.isDark(context)) { id, instance ->
            instance.dismiss()

            when (id) {
                R.id.menu_main_debug_f_dism_attached -> doInCoroutine { model.forceDismissAttached(view._getContext()) }
                R.id.menu_main_debug_f_dism_scheduled -> doInCoroutine { model.forceDismissScheduled(view._getContext()) }
                R.id.menu_main_debug_reset_reminders -> doInCoroutine {
                    model.resetReminders(view._getContext())
                    model.resetWidgets(view._getContext())
                }
                R.id.menu_main_debug_f_remove_audios -> doInCoroutine { model.forceRemoveAudios(view._getContext()) }
                R.id.menu_main_debug_f_remove_drawns -> doInCoroutine { model.forceRemoveDrawns(view._getContext()) }
                R.id.menu_main_debug_restore_audios -> doInCoroutine { model.restoreAudios(view._getContext()) }
                R.id.menu_main_debug_restore_drawns -> doInCoroutine { model.restoreDrawns(view._getContext()) }
                R.id.menu_main_debug_rm_ext_data -> doInCoroutine { model.removeExtData() }
            }
        }.show()

    override fun onRefresh() {
        srl.isRefreshing = true
        refresh(null)
        srl.isRefreshing = false
    }

    private fun refresh(query: String?) {
        liveList =
            if (query != null)
                model.getNotesPagingLimited(query, context)
            else
                model.getNotesPagingLimited(context)
        liveList.observeForever {
            adapter.submitList(it)
            emptyText.visibility = visibleOrGone(it.isEmpty())
        }

        emptyText.visibility = visibleOrGone(adapter.itemCount == 0)
    }

    private fun initRecycler(v: View) {
        adapter = ListAdapter(
            { n, item ->
                if (onListItemClick(n, item))
                    return@ListAdapter
                model.onListItemClicked(n, context)
            },
            { n, vg ->
                if (model.isFromWidget())
                    return@ListAdapter
                
                startActionMode()
                onListItemClick(n, vg)
            })

        recycler = v.findViewById(R.id.recycler_main)
        recycler.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        recycler.layoutManager = LinearLayoutManager(context)
        
        initRecyclersScrollListener()
        
        recycler.adapter = adapter
    }

    private fun initRecyclersScrollListener() {
        recycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
    
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val a = (recyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                
                if (a != NUM_UNDEF && a != DEF_NUM) {
                    srl.isEnabled = false
                    fab.visibility = View.INVISIBLE
                    fabToTop.visibility = View.VISIBLE
                } else if (a != NUM_UNDEF) {
                    srl.isEnabled = true
                    fab.visibility = View.VISIBLE
                    fabToTop.visibility = View.INVISIBLE
                }
                
                super.onScrolled(recyclerView, dx, dy)
            }
        })
    }
    
    private fun initFabs(v: View) {
        fab = v.findViewById(R.id.fab)
        fabU = v.findViewById(R.id.fabCreateUsual)
        fabA = v.findViewById(R.id.fabCreateAudio)
        fabD = v.findViewById(R.id.fabCreateDrawn)
        fabToTop = v.findViewById(R.id.fabToTop)
        fabU.setOnClickListener(this)
        fabA.setOnClickListener(this)
        fabD.setOnClickListener(this)
        fabToTop.setOnClickListener(this)

        FabAnim()
    }

    private fun initSearch(): Unit = model.crossPresenterModel.queueForMenuCreation {
        val sv = (it?.findItem(R.id.menu_main_actions_search)?.actionView ?: return@queueForMenuCreation) as SearchView
        sv.isSubmitButtonEnabled = true
        sv.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextChange(newText: String): Boolean {
                refresh(newText)
                return true
            }

            override fun onQueryTextSubmit(query: String): Boolean {
                val n = doBlocking<Note?> { model.getSearchedNote(query, context) }

                if (n != null)
                    model.crossPresenterModel.initiateViewing(Model.MODE_NOTE_USUAL, n)
                else
                    toast(context.getString(R.string.noSearchedNote), context)
                return true
            }
        })
        sv.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {

            override fun onViewAttachedToWindow(p0: View?) = Unit

            override fun onViewDetachedFromWindow(p0: View?): Unit = onRefresh()
        })
    }

    private fun startActionMode() {
        model.crossPresenterModel.startActionMode(object : ActionMode.Callback {
            private val ID_DELETE = 0x00000002
            private val ID_SEND = 0x00000004

            override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
                menu.add(0, ID_DELETE, 0, R.string.delete).setIcon(R.drawable.delete)
                menu.add(1, ID_SEND, 0, R.string.send).setIcon(android.R.drawable.ic_menu_send)
                isChoosingItems = true
                return true
            }

            override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean = false

            override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
                if (item.itemId == ID_DELETE) {
                    model.crossPresenterModel.showWarningDialog(R.string.delete, R.string.warn_delete_selected) {
                        for (i in chosenItems) {
                            doInCoroutine {
                                if (model.isNotePureUsual(i.first.id))
                                    model.delete(i.first, context)
                                else
                                    model.notifyUserNoteIsNotPureUsual(context)
                            }
                        }
                        mode.finish()
                    }
                } else if (item.itemId == ID_SEND) {
                    if (chosenItems.size != 1)
                        toast(context.getString(R.string.choose_one_item), context)
                    else
                        model.send(chosenItems[0].first, context)

                    mode.finish()
                }
                return true
            }

            override fun onDestroyActionMode(mode: ActionMode?) {
                isChoosingItems = false

                for (i in chosenItems)
                    i.second.setBackgroundColorForChoosing(false)

                chosenItems.clear()
                onRefresh()
            }
        })
    }

    private fun onListItemClick(n: Note, v: View): Boolean {
        if (!isChoosingItems)
            return false

        val p = Pair(n, v)

        if (chosenItems.contains(p)) {
            chosenItems.remove(p)
            v.setBackgroundColorForChoosing(false)
        } else {
            chosenItems.add(p)
            v.setBackgroundColorForChoosing(true)
        }

        return true
    }

    private fun View.setBackgroundColorForChoosing(chosen: Boolean): Unit =
        this.setBackgroundColor(ResourcesCompat.getColor(
            context.resources,
            when {
                chosen -> R.color.colCyan
                model.isDark(context) -> R.color.darkThemeBackground
                else -> R.color.colWhite
            },
            context.theme))
    
    private inner class FabAnim {
        private val fabClose = AnimationUtils.loadAnimation(context, R.anim.fab_close)
        private val fabOpen = AnimationUtils.loadAnimation(context, R.anim.fab_open)
        private val fabRot1 = AnimationUtils.loadAnimation(context, R.anim.fab_rotate_1)
        private val fabRot2 = AnimationUtils.loadAnimation(context, R.anim.fab_rotate_2)
        private var isOpen = true

        init {
            fab.setOnClickListener(this::animate)
            animate()
        }

        private fun animate() {
            fabU.visibility = if (isOpen) View.INVISIBLE else View.VISIBLE
            fabA.visibility = if (isOpen) View.INVISIBLE else View.VISIBLE
            fabD.visibility = if (isOpen) View.INVISIBLE else View.VISIBLE
            fabU.startAnimation(if (isOpen) fabClose else fabOpen)
            fabA.startAnimation(if (isOpen) fabClose else fabOpen)
            fabD.startAnimation(if (isOpen) fabClose else fabOpen)
            fab.startAnimation(if (isOpen) fabRot1 else fabRot2)
            fabU.isClickable = !isOpen
            fabA.isClickable = !isOpen
            fabD.isClickable = !isOpen
            isOpen = !isOpen
        }
    }
}
