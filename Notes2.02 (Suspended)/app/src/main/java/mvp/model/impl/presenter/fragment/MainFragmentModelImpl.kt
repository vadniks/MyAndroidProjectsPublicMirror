/*
 * Copyright (C) 2018-2020 Vad Nik (http://github.com/vadniks). All rights reserved.
 *
 * This code is a part of proprietary software.
 * Usage, distribution, redistribution, modifying
 * and/or commercial use of this code,
 * without author's written permission, are strongly prohibited.
 */

package .mvp.model.impl.presenter.fragment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import .R
import .common.*
import .mvp.model.CrossPresenterModel
import .mvp.model.Model
import .mvp.model.individual.presenter.fragment.FragmentIndividualModelWrapper
import .mvp.model.individual.presenter.fragment.IFragmentIndividualModel
import .mvp.model.individual.presenter.fragment.MainFragmentModel
import .mvp.presenter.commands.fragment.MainFragmentCommands
import .processing.audios.Audios
import .processing.billing.Billing
import .processing.common.Note
import .processing.common.THRESHOLD
import .processing.databaseExport.DatabaseExport
import .processing.draw.processing.Drawn
import .processing.notifications.Notifications
import .processing.security.Security
import .processing.widgets.Widgets
import kotlin.system.exitProcess

/**
 * @author Vad Nik
 * @version dated Aug 13, 2019.
 * @link https://github.com/vadniks
 */

fun getMainFragmentModel(
    fim: IFragmentIndividualModel,
    commands: MainFragmentCommands): MainFragmentModel =
    MainFragmentModelImpl(fim, commands)

private class MainFragmentModelImpl(
    fim: IFragmentIndividualModel,
    override val commands: MainFragmentCommands) :
    FragmentIndividualModelWrapper(fim), MainFragmentModel {

    override fun init(vararg args: Any) {
        crossPresenterModel.onMainFragmentPresenterInitialized(commands)
    }

    override fun getSearchedNote(query: String, context: Context): Note? = model.noteDao().getSearchedNote(query)
    
    override fun getNotesPagingLimited(context: Context): LiveData<PagedList<Note>> =
        performGetNotesPagged(model.noteDao()::getAll)
    
    override fun getNotesPagingLimited(query: String, context: Context): LiveData<PagedList<Note>> =
        performGetNotesPagged { model.noteDao().getAllForSearch(query) }
    
    private fun performGetNotesPagged(src: () -> DataSource.Factory<Int, Note>): LiveData<PagedList<Note>> =
        LivePagedListBuilder(
            src(),
            PagedList.Config.Builder()
                .setInitialLoadSizeHint(THRESHOLD)
                .setPageSize(THRESHOLD)
                .setEnablePlaceholders(false)
                .build()
        ).build()
    
    override fun onListItemClicked(n: Note, context: Context) {
        val widgetId = crossPresenterModel.getIntent().getIntExtra(Widgets.EXTRA_WIDGET_ID, DEF_NUM).toLong()
        
        if (widgetId == DEF_NUM.toLong()) {
            val m =
                when {
                    n.audio -> Model.MODE_NOTE_AUDIO
                    n.drawn -> Model.MODE_NOTE_DRAWN
                    else -> Model.MODE_NOTE_USUAL
                }
            crossPresenterModel.initiateViewing(m, n)
        } else {
            if (checkIsAllAvailable(context))
                selectForWidget(n, widgetId, context)
        }
    }
    
    private fun selectForWidget(n: Note, widgetId: Long, context: Context) {
        if (!doBlocking<Boolean> { isNotePureUsual(n.id) }) {
            notifyUserNoteIsNotPureUsual(context)
            return
        }
    
        val note = doBlocking<Note?> { getNoteByWidgetId(widgetId)?.apply { wid = NUM_UNDEF.toLong() } }
        if (note != null)
            doBlocking { update(note) }
    
        doBlocking { update(n.apply { wid = widgetId }) }
    
        updateWidget(context, widgetId, n.title, n.text, n.color)
        crossPresenterModel.setActivityResult(Activity.RESULT_OK, Intent().putExtra(Widgets.EXTRA_WIDGET_ID, widgetId))
        crossPresenterModel.finishActivity()
        exitProcess(0)
    }
    
    private fun getNoteByWidgetId(id: Long): Note? = model.noteDao().getNoteByWidgetId(id)
    
    override fun forceDismissAttached(context: Context) = Notifications.create(context).forceDismissAttached()
    
    override fun forceDismissScheduled(context: Context) = Notifications.create(context).forceDismissScheduled()
    
    override fun resetReminders(context: Context) {
        for (i in model.noteDao().getAllNonPureNonWidgetedNotes() ?: return) {
            if (i.nid != NUM_UNDEF.toLong())
                Notifications.create(context).resetReminder(Notifications.MODE_NOTIF, i.title, i.text, i.nid)
            else if (i.rid != NUM_UNDEF.toLong())
                Notifications.create(context).resetReminder(Notifications.MODE_REM, i.title, i.text, i.rid)
            else if (i.sid != NUM_UNDEF.toLong() && i.sid2 != NUM_UNDEF.toLong())
                Notifications.create(context).resetReminder(Notifications.MODE_SCH, i.title, i.text, i.sid, i.sid2)
        }
    }
    
    override fun resetWidgets(context: Context) {
        if (!checkIsAllAvailable(context))
            return

        for (i in model.noteDao().getAllWidgetedNotes() ?: return)
            Widgets.getDelegate().updateWidget(
                context,
                i.wid,
                i.title,
                i.text,
                i.color)
    }
    
    override fun isFromWidget(): Boolean =
        commands.getArgs()?.getInt(Widgets.EXTRA_WIDGET_ID, DEF_NUM) != null
    
    override fun encryptDB(context: Context) {
        if (!checkIsAllAvailable(context))
            return

        val s = Security.get(context)
        
        s.showPassDialog { pass, instance ->
            if (isDBEncrypted(context) && !checkPassword(pass, context)) {
                s.notifyUserPasswordWrong()
                return@showPassDialog
            }
            
            instance.dismiss()
            s.showEncryptionDialog(pass) { restart(context) }
        }
    }
    
    override fun forceRemoveAudios(context: Context) {
        if (checkIsAllAvailable(context))
            Audios.get(context, null).deleteAllAudios()
    }

    override fun forceRemoveDrawns(context: Context) {
        if (checkIsAllAvailable(context))
            Drawn.get(null, context).deleteAllDrawns()
    }

    override fun send(n: Note, context: Context) {
        val intent = Intent(Intent.ACTION_SEND)

        if (!n.audio && !n.drawn) {
            intent.putExtra(Intent.EXTRA_TITLE, n.title)
            intent.putExtra(Intent.EXTRA_TEXT, n.text)
            intent.type = "text/plain"

            context.startActivity(Intent.createChooser(intent, null))
        } else
            toast(context.getString(R.string.select_text_note), context)
    }

    override fun buy(activity: Activity) {
        doInCoroutine { Billing.get(activity).startForPurchasing(activity) }
    }

    override fun onCreateView() {
        val intent = crossPresenterModel.getIntent()
        if (intent.action == Intent.ACTION_SEND || intent.action == Intent.ACTION_SENDTO)
            handleSend(intent)
        else
            model.showInterstitial()
    }

    private fun handleSend(intent: Intent) {
        if (intent.type != "text/plain")
            return

        crossPresenterModel.initiateCreating(Model.MODE_NOTE_USUAL, Bundle().apply {
            putString(CrossPresenterModel.EXTRA_RECEIVED_TITLE, intent.getStringExtra(Intent.EXTRA_TITLE))
            putString(CrossPresenterModel.EXTRA_RECEIVED_TEXT, intent.getStringExtra(Intent.EXTRA_TEXT))
        })
    }

    override fun onDestroyView() = model.showInterstitial()

    override fun restoreAudios(context: Context) {
        if (checkIsAllAvailable(context))
            Audios.get(context, null).restoreAudios()
    }

    override fun restoreDrawns(context: Context) {
        if (checkIsAllAvailable(context))
            Drawn.get(null, context).restoreAllDrawns()
    }

    override fun removeExtData() {
        model.getExternalStorageFolder()?.deleteRecursively()
    }

    override fun exportDB(context: Context) {
        if (checkIsAllAvailable(context))
            DatabaseExport.get(context).showImportExportDialog()
    }
}
