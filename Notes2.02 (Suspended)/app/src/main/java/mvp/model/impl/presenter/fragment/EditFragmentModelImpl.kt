/*
 * Copyright (C) 2018-2020 Vad Nik (http://github.com/vadniks). All rights reserved.
 *
 * This code is a part of proprietary software.
 * Usage, distribution, redistribution, modifying
 * and/or commercial use of this code,
 * without author's written permission, are strongly prohibited.
 */

package .mvp.model.impl.presenter.fragment

import android.content.Context
import .R
import .common.BottomSheetDialog
import .common.NUM_UNDEF
import .common.STR_EMPTY
import .common.doInCoroutine
import .mvp.model.CrossPresenterModel
import .mvp.model.individual.presenter.fragment.EditFragmentModel
import .mvp.model.individual.presenter.fragment.FragmentIndividualModelWrapper
import .mvp.model.individual.presenter.fragment.IFragmentIndividualModel
import .mvp.presenter.commands.fragment.EditFragmentCommands
import .processing.common.Note
import .processing.notifications.Notifications

/**
 * @author Vad Nik
 * @version dated Aug 13, 2019.
 * @link https://github.com/vadniks
 */

fun getEditFragmentModel(
    fim: IFragmentIndividualModel,
    commands: EditFragmentCommands): EditFragmentModel =
    EditFragmentModelImpl(fim, commands)

private class EditFragmentModelImpl(
    fim: IFragmentIndividualModel,
    override val commands: EditFragmentCommands) :
    FragmentIndividualModelWrapper(fim), EditFragmentModel {
    
    override fun init(vararg args: Any) =
        crossPresenterModel.onEditFragmentPresenterInitialized(commands)
    
    override fun reminderChoose(n: Note, isView: Boolean, context: Context): Unit = BottomSheetDialog(R.menu.menu_reminder, context, isDark(context)) { id, instance ->
        instance.dismiss()
        crossPresenterModel.initiateMain(null)
        
        val mode =
            when (id) {
                R.id.menu_reminder_attach -> Notifications.MODE_NOTIF
                R.id.menu_reminder_timed -> Notifications.MODE_REM
                R.id.menu_reminder_sch -> Notifications.MODE_SCH
                else -> throw IllegalArgumentException()
            }
        
        doInCoroutine {
            setReminder(context, n, mode, isView) { _mode, extras ->
                doInCoroutine {
                    
                    when (_mode) {
                        Notifications.MODE_NOTIF -> n.nid = extras[0]
                        Notifications.MODE_REM -> n.rid = extras[0]
                        Notifications.MODE_SCH -> {
                            n.sid = extras[0]
                            n.sid2 = extras[1]
                        }
                    }
                    
                    if (isView)
                        update(n)
                    else
                        save(n, context)
                }
            }
        }
    }.show()
    
    private fun setReminder(
        context: Context,
        n: Note,
        mode: Int,
        isView: Boolean,
        onInsert: (mode: Int, extras: LongArray) -> Unit) {
        
        if (!isView && doesNoteAlreadyExist(n.title)) {
            notifyUserNoteAlreadyExists(context)
            return
        }
        
        if (isView && !isNotePureUsual(n.id)) {
            notifyUserNoteIsNotPureUsual(context)
            return
        }
        
        Notifications.create(context, n, mode, onInsert)
    }
    
    override fun getNote(): Note? = commands.getArgs()?.getSerializable(CrossPresenterModel.EXTRA_NOTE) as Note?

    override fun dismissNotifs(n: Note, context: Context) {
        when {
            n.nid != NUM_UNDEF.toLong() ->
                Notifications.create(context).setVals(n.title, n.text, Notifications.MODE_NOTIF) { _, _ -> }.dismiss(n.nid)
            n.rid != NUM_UNDEF.toLong() ->
                Notifications.create(context).setVals(n.title, n.text, Notifications.MODE_REM) { _, _ -> }.dismiss(n.rid)
            n.sid != NUM_UNDEF.toLong() ->
                Notifications.create(context).setVals(n.title, n.text, Notifications.MODE_SCH) { _, _ -> }.dismiss(n.sid)
            else -> return
        }
    }

    override fun onCreateView() {
        val args = commands.getArgs()

        if (args != null && getNote() == null)
            commands.setStrings(
                args.getString(CrossPresenterModel.EXTRA_RECEIVED_TITLE, STR_EMPTY),
                args.getString(CrossPresenterModel.EXTRA_RECEIVED_TEXT, STR_EMPTY))
    }
}
