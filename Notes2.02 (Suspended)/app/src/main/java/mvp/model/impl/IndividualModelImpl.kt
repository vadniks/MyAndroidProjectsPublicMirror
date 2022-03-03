/*
 * Copyright (C) 2018-2020 Vad Nik (http://github.com/vadniks). All rights reserved.
 *
 * This code is a part of proprietary software.
 * Usage, distribution, redistribution, modifying 
 * and/or commercial use of this code,
 * without author's written permission, are strongly prohibited. 
 */

package .mvp.model.impl

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import .R
import .common.NUM_UNDEF
import .common.doInUI
import .common.toast
import .mvp.model.Model
import .mvp.model.individual.ICommands
import .mvp.model.individual.IIndividualModel
import .mvp.model.individual.IndividualModelWrapper
import .mvp.view.MainActivity
import .processing.billing.Billing
import .processing.common.Note
import .processing.notifications.Notifications
import .processing.security.Security
import .processing.widgets.Widgets
import kotlin.system.exitProcess

/**
 * @author Vad Nik
 * @version dated Aug 14, 2019.
 * @link https://github.com/vadniks
 */

fun getIndividualModel(commands: ICommands, model: Model) : IIndividualModel =
    IndividualModelWrapper(IndividualModelImpl(commands, model))

private class IndividualModelImpl(
    override val commands: ICommands,
    override val model: Model) : IIndividualModel {
    
    override fun save(n: Note, context: Context) {
        if (n.title.isBlank() || n.text.isBlank())
            return

        if (doesNoteAlreadyExist(n.title))
            notifyUserNoteAlreadyExists(context)
        else
            model.noteDao().insert(n)
    }
    
    override fun update(n: Note) = model.noteDao().update(n)
    
    override fun delete(n: Note, context: Context) {
        if (doesNoteAlreadyExist(n.title))
            model.noteDao().delete(n)
        else
            notifyUserNoteDoesntExist(context)
    }
    
    override fun getNote(id: Int): Note? = model.noteDao().getNoteById(id)
    
    override fun isNotePureUsual(id: Int): Boolean {
        val n = getNote(id)!!
        return n.nid == NUM_UNDEF.toLong() &&
                n.rid == NUM_UNDEF.toLong() &&
                n.sid == NUM_UNDEF.toLong() &&
                n.sid2 == NUM_UNDEF.toLong() &&
                n.wid == NUM_UNDEF.toLong() &&
                !n.audio && !n.drawn
    }

    override fun isNoteWidgeted(id: Int): Boolean = getNote(id)?.wid != NUM_UNDEF.toLong()

    override fun notifyUserNoteIsNotPureUsual(context: Context) {
        doInUI { toast(context.getString(R.string.noteNotPureUsual), context) }
    }
    
    override fun doesNoteAlreadyExist(title: String): Boolean =
        model.noteDao().getNoteByTitle(title) != null
    
    override fun notifyUserNoteAlreadyExists(context: Context) {
        doInUI { toast(context.getString(R.string.noteExists), context) }
    }
    
    override fun restart(context: Context) {
        context.startActivity(context.packageManager.getLaunchIntentForPackage(context.packageName)
            ?.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK) ?: return)
        exitProcess(0)
    }
    
    override fun getBoolean(key: String, context: Context): Boolean =
        PreferenceManager.getDefaultSharedPreferences(context).getBoolean(key, false)
    
    @SuppressLint("ApplySharedPref")
    override fun setBoolean(key: String, context: Context, value: Boolean) {
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .putBoolean(key, value)
            .commit()
    }
    
    override fun isDBEncrypted(context: Context): Boolean = Security.get(context).isDBEncrypted()
    
    override fun isDBEmpty(context: Context): Boolean = model.noteDao().getSize() == 0
    
    override fun initiateOpening(context: Context, extras: Bundle) =
        context.startActivity(Intent(context, MainActivity::class.java)
            .putExtras(extras)
            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    
    override fun updateWidget(context: Context, id: Long, title: String, text: String, color: Int) =
        Widgets.getDelegate().updateWidget(context, id, title, text, color)

    override fun updateNotif(
        context: Context,
        mode: Int,
        id: Long,
        title: String,
        text: String,
        oldTitle: String,
        oldText: String) =
        Notifications.create(context).update(id, mode, title, text, oldTitle, oldText)

    override fun chooseOfNotifIds(item: Note): Long =
        when {
            item.nid != NUM_UNDEF.toLong() -> item.nid
            item.rid != NUM_UNDEF.toLong() -> item.rid
            item.sid != NUM_UNDEF.toLong() -> item.sid
            else -> throw IllegalArgumentException()
        }

    override fun determineNotifMode(item: Note): Int =
        when {
            item.nid != NUM_UNDEF.toLong() -> Notifications.MODE_NOTIF
            item.rid != NUM_UNDEF.toLong() -> Notifications.MODE_REM
            item.sid != NUM_UNDEF.toLong() -> Notifications.MODE_SCH
            else -> throw IllegalArgumentException()
        }

    override fun isNoteNotified(id: Int): Boolean {
        val n = model.noteDao().getNoteById(id)

        return n != null &&
               (n.nid != NUM_UNDEF.toLong() ||
                n.rid != NUM_UNDEF.toLong() ||
                n.sid != NUM_UNDEF.toLong())
    }

    override fun checkPassword(pass: String, context: Context): Boolean = Security.get(context).checkPassword(pass)
    
    override fun isDark(context: Context): Boolean = getBoolean(PREFERENCE_IS_DARK, context)
    
    override fun setDark(context: Context, isDark: Boolean) = setBoolean(PREFERENCE_IS_DARK, context, isDark)
    
    override fun notifyUserNoteDoesntExist(context: Context) {
        doInUI { toast(context.getString(R.string.note_doesnt_exist), context) }
    }

    override fun checkSelfPermission(context: Context, p: String): Int =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            ContextCompat.checkSelfPermission(context, p)
        else
            PackageManager.PERMISSION_GRANTED

    override fun checkSelfPermission2(context: Context, p: String): Boolean =
        checkSelfPermission(context, p) == PackageManager.PERMISSION_GRANTED

    override fun notifyUserPermissionsNeeded(context: Context) {
        doInUI { toast(context.getString(R.string.need_additional_permissions), context) }
    }

    override fun decodeHardcoded(e: String): String = ""

    override fun decodeHardcodedWithoutEquals(e: String): String = decodeHardcoded(e)

    override fun decodeHardcodedWithoutEqual(e: String): String = decodeHardcoded(e)

    override fun isAllAvailable(context: Context): Boolean = Billing.get(context).areAllEnabled()

    override fun areAdsDisabled(context: Context): Boolean = Billing.get(context).areAdsDisabled()

    override fun notifyUserThisIsPaid(context: Context) {
        doInUI { toast(context.getString(R.string.paid_feature), context) }
    }

    override fun checkIsAllAvailable(context: Context): Boolean =
        if (!isAllAvailable(context)) {
            notifyUserThisIsPaid(context)
            false
        } else
            true

    private companion object {
        private const val PREFERENCE_IS_DARK = 0x00000008.toString()
    }
}
