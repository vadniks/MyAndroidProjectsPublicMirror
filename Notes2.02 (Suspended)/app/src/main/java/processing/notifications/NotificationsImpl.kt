/*
 * Copyright (C) 2018-2020 Vad Nik (http://github.com/vadniks). All rights reserved.
 *
 * This code is a part of proprietary software.
 * Usage, distribution, redistribution, modifying
 * and/or commercial use of this code,
 * without author's written permission, are strongly prohibited.
 */

package .processing.notifications

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.WorkerThread
import androidx.core.app.NotificationCompat
import .R
import .common.NUM_UNDEF
import .common.doInCoroutine
import .common.doInUI
import .common.toast
import .mvp.model.individual.IIndividualModel
import .processing.common.Note
import .processing.common.broadcastreceiver.BroadcastReceiver
import .processing.common.broadcastreceiver.BroadcastReceiver.Companion.ACTION_OPEN
import .processing.common.broadcastreceiver.BroadcastReceiver.Companion.EXTRA_ID
import .processing.common.broadcastreceiver.BroadcastReceiver.Companion.EXTRA_MODE
import .processing.common.broadcastreceiver.BroadcastReceiver.Companion.makeAction
import java.lang.System.currentTimeMillis
import java.util.*

/**
 * @author Vad Nik
 * @version dated Jul 21, 2019.
 * @link https://github.com/vadniks
 */
private class NotificationsImpl : Notifications {

    constructor(
        context: Context,
        title: String,
        text: String,
        mode: Int,
        im: IIndividualModel,
        onInsert: (mode: Int, extras: LongArray) -> Unit) : super(context, title, text, mode, im, onInsert) {
        when (mode) {
            MODE_NOTIF -> sendNotification()
            MODE_ONCE -> sendNotification()
            MODE_REM -> rem()
            MODE_SCH -> sch()
        }
    }
    
    constructor(
        context: Context,
        n: Note,
        mode: Int,
        im: IIndividualModel,
        onInsert: (mode: Int, extras: LongArray) -> Unit) : this(context, n.title, n.text, mode, im, onInsert)
    
    constructor(context: Context, intent: Intent, im: IIndividualModel) : super(context, intent, im) {
        val title = intent.getStringExtra(EXTRA_TITLE)
        val text = intent.getStringExtra(EXTRA_TEXT)

        if (title != null && text != null) {
            this.title = title
            this.text = text
        }

        when (intent.action ?: return) {
            makeAction(context, ACTION_DISM_NOTIF) -> dismissCons(intent.getLongExtra(EXTRA_ID, NUM_UNDEF.toLong()))
            makeAction(context, ACTION_DISM_SCH) -> dismissSch(
                intent.getLongExtra(EXTRA_ID, NUM_UNDEF.toLong()),
                intent.getIntExtra(EXTRA_PEN_ID, NUM_UNDEF))
            makeAction(context, ACTION_SEND_REM) -> {
                mode = MODE_REM
                sendNotification(intent.getLongExtra(EXTRA_ID, NUM_UNDEF.toLong()))
            }
            makeAction(context, ACTION_SEND_SCH) -> {
                mode = MODE_SCH
                sendNotification(
                    intent.getLongExtra(EXTRA_ID, NUM_UNDEF.toLong()),
                    intent.getIntExtra(EXTRA_PEN_ID, NUM_UNDEF))
            }
            makeAction(context, ACTION_REM_SWAP_OFF) -> doInCoroutine {
                val n = model.noteDao().getNoteByRid(intent.getLongExtra(EXTRA_ID, NUM_UNDEF.toLong())) ?: return@doInCoroutine
                n.rid = NUM_UNDEF.toLong()
                update(n)
            }
        }
    }

    constructor(context: Context, im: IIndividualModel) : super(context, im)

    override fun forceDismissAttached() {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager? ?: return

        for (i in getAllConsedNotes() ?: return) {
            nm.cancel(i.nid.toInt())
            update(i.apply { nid = NUM_UNDEF.toLong() })
        }
    }

    override fun forceDismissScheduled() {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager? ?: return

        for (i in getAllScheduledNotes() ?: return) {
            title = i.title
            text = i.text
            mode = MODE_SCH

            am.cancel(makePen(i.sid, i.sid.toInt() + 10))

            update(i.apply { sid = NUM_UNDEF.toLong() })
        }
    }

    override fun resetReminder(mode: Int, vararg extras: Any) {
        title = extras[0] as String
        text = extras[1] as String
        this.mode = mode

        when (mode) {
            MODE_NOTIF -> sendNotification(extras[2] as Long)
            MODE_REM -> performRem(extras[2] as Long)
            MODE_SCH -> performSch(extras[2] as Long, extras[3] as Long)
        }
    }

    private fun rem(): Unit = makeTimed { cal ->
        toast(context.getString(R.string.rems_at_same_time), context, true)
        
        if (cal < currentTimeMillis()) {
            toast(context.getString(R.string.non_future), context)
            return@makeTimed
        }

        onInsert!!.invoke(MODE_REM, longArrayOf(cal))

        performRem(cal)
    }

    private fun performRem(cal: Long) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager? ?: return
        am.set(AlarmManager.RTC_WAKEUP, cal, makePen(cal, cal.toInt() + 10, true))
    }

    @Suppress("UNCHECKED_CAST")
    private fun sch(): Unit = makeTimed { cal ->
        toast(context.getString(R.string.rems_at_same_time), context, true)

        if (cal < currentTimeMillis()) {
            toast(context.getString(R.string.non_future), context)
            return@makeTimed
        }

        makeTimed mk@ { _cal ->
            if (_cal < cal) {
                toast(context.getString(R.string.non_future), context)
                return@mk
            }

            val interval = _cal - cal
            onInsert!!.invoke(MODE_SCH, longArrayOf(cal, interval))

            performSch(cal, interval)
        }
    }

    private fun performSch(cal: Long, interval: Long) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager? ?: return
        am.setRepeating(AlarmManager.RTC_WAKEUP, cal, interval, makePen(cal, cal.toInt()+10))
    }

    private fun makePen(extraId: Long, penId: Int, isRem: Boolean = false): PendingIntent {
        val intent = Intent()
        intent.action = makeAction(context, if (!isRem) ACTION_SEND_SCH else ACTION_SEND_REM)
        intent.putExtra(EXTRA_ID, extraId)
        intent.putExtra(EXTRA_MODE, mode)
        intent.putExtra(EXTRA_TITLE, title)
        intent.putExtra(EXTRA_TEXT, text)

        if (!isRem)
            intent.putExtra(EXTRA_PEN_ID, penId)

        intent.setClass(context, BroadcastReceiver::class.java)

        return PendingIntent.getBroadcast(context, penId, intent, 0)
    }

    private fun makeTimed(onInitialDateChosen: (cal: Long) -> Unit) {
        var month: Int
        var day: Int
        var hour: Int
        var minute: Int

        createPicker(
            false,
            cpd@ { _, m, d ->
                month = m
                day = d

                createPicker(
                    true,
                    null,
                    cpt@ { h, _m ->
                        hour = h
                        minute = _m

                        val cal = Calendar.getInstance().apply {
                            set(Calendar.DAY_OF_MONTH, day)
                            set(Calendar.MONTH, month)
                            set(Calendar.HOUR_OF_DAY, hour)
                            set(Calendar.MINUTE, minute)
                            set(Calendar.SECOND, 0)
                        }.timeInMillis

                        onInitialDateChosen(cal)
                    })
            },
            null)
    }

    private fun createPicker(
        time: Boolean,
        onDoneDate: ((year: Int, month: Int, dayOfMonth: Int) -> Unit)?,
        onDoneTime: ((hourOfDay: Int, minute: Int) -> Unit)?) {
        val calendar = Calendar.getInstance()

        doInUI {
            if (!time)
                DatePickerDialog(
                    context,
                    DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                        onDoneDate?.invoke(year, month, dayOfMonth)
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)).show()
            else
                TimePickerDialog(
                    context,
                    TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                        onDoneTime?.invoke(hourOfDay, minute)
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    false).show()
        }
    }

    override fun notifyMessage() = sendNotification()

    @Suppress("DEPRECATION")
    private fun sendNotification(prevId: Long = NUM_UNDEF.toLong(), penId: Int = NUM_UNDEF) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager? ?: return
        val id = if (prevId == NUM_UNDEF.toLong()) currentTimeMillis() else prevId
        val pi = if (penId == NUM_UNDEF) id.toInt()+10 else penId

        onInsert?.invoke(mode, longArrayOf(id))

        var pen: PendingIntent? = null
        if (mode != MODE_REM) {
            val dism = Intent()
            dism.action = makeAction(context, if (mode == MODE_NOTIF) ACTION_DISM_NOTIF else ACTION_DISM_SCH)

            dism.setClass(context, BroadcastReceiver::class.java)
            dism.putExtra(EXTRA_ID, id)
            dism.putExtra(EXTRA_TITLE, title)
            dism.putExtra(EXTRA_TEXT, text)
            dism.putExtra(EXTRA_PEN_ID, pi)

            pen = PendingIntent.getBroadcast(context, pi, dism, 0)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            nm.createNotificationChannel(NotificationChannel(
                context.packageName,
                context.getString(R.string.app_name),
                NotificationManager.IMPORTANCE_HIGH))

        val cont = Intent()
        cont.action = makeAction(context, ACTION_OPEN)
        cont.putExtra(EXTRA_ID, id)
        cont.putExtra(EXTRA_MODE, mode)
        cont.putExtra(EXTRA_TITLE, title)
        cont.putExtra(EXTRA_TEXT, text)
        cont.putExtra(EXTRA_PEN_ID, pi)
        cont.setClass(context, BroadcastReceiver::class.java)

        val builder =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                NotificationCompat.Builder(context, context.packageName)
            else
                NotificationCompat.Builder(context)

        builder.setAutoCancel(mode != MODE_NOTIF)
        builder.setOngoing(mode == MODE_NOTIF)
        builder.setDefaults(Notification.DEFAULT_ALL)
        builder.priority = NotificationCompat.PRIORITY_MAX
        builder.setCategory(NotificationCompat.CATEGORY_REMINDER)
        builder.setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
        builder.setContentTitle(title)
        builder.setContentText(text)
        builder.setSmallIcon(R.drawable.notes_light)
        builder.setContentIntent(PendingIntent.getBroadcast(context, id.toInt()+10, cont, 0))

        if (mode != MODE_REM)
            builder.addAction(android.R.drawable.ic_menu_close_clear_cancel, context.getString(R.string.dismiss), pen)
        else {
            val swpOff = Intent()
            swpOff.action = makeAction(context, ACTION_REM_SWAP_OFF)

            swpOff.setClass(context, BroadcastReceiver::class.java)
            swpOff.putExtra(EXTRA_ID, id)
            swpOff.putExtra(EXTRA_TITLE, title)
            swpOff.putExtra(EXTRA_TEXT, text)

            builder.setDeleteIntent(PendingIntent.getBroadcast(context, id.toInt()+20, swpOff, 0))
        }

        nm.notify(id.toInt(), builder.build())
    }

    private fun dismissCons(id: Long) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
        nm?.cancel(id.toInt())

        doInCoroutine {
            val n = getNoteByNotificationsId(id, MODE_NOTIF)

            if (n == null) {
                toast(context.getString(R.string.noteNotFound), context)
                return@doInCoroutine
            }

            n.nid = NUM_UNDEF.toLong()

            update(n)
        }
    }

    private fun dismissSch(id: Long, penId: Int, isIdeResetNeeded: Boolean = true) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.cancel(makePen(id, penId))

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
        nm?.cancel(id.toInt())

        if (!isIdeResetNeeded)
            return

        doInCoroutine {
            val n = getNoteByNotificationsId(id, MODE_SCH)

            if (n == null) {
                toast(context.getString(R.string.noteNotFound), context)
                return@doInCoroutine
            }

            n.sid = NUM_UNDEF.toLong()
            n.sid2 = NUM_UNDEF.toLong()

            update(n)
        }
    }
    
    @WorkerThread
    private fun getAllConsedNotes(): List<Note>? = model.noteDao().getAllConstedNotes()
    
    @WorkerThread
    private fun getAllScheduledNotes(): List<Note>? = model.noteDao().getAllScheduledNotes()
    
    override fun getNoteByNotificationsId(id: Long, which: Int): Note? =
        when (which) {
            MODE_NOTIF -> model.noteDao().getNoteByNid(id)
            MODE_REM -> model.noteDao().getNoteByRid(id)
            MODE_SCH -> model.noteDao().getNoteBySid(id)
            else -> throw IllegalArgumentException()
        }

    override fun dismiss(id: Long) =
        when (mode) {
            MODE_NOTIF -> dismissCons(id)
            MODE_REM -> dismissRem(id)
            MODE_SCH -> dismissSch(id, id.toInt() + 10)
            else -> throw IllegalArgumentException()
        }

    private fun dismissRem(id: Long, needIdResetting: Boolean = true) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.cancel(makePen(id, id.toInt() + 10, true))

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
        nm?.cancel(id.toInt())

        if (!needIdResetting)
            return

        doInCoroutine {
            val n = getNoteByNotificationsId(id, MODE_REM)

            if (n == null) {
                toast(context.getString(R.string.noteNotFound), context)
                return@doInCoroutine
            }

            n.rid = NUM_UNDEF.toLong()

            update(n)
        }
    }

    @Suppress("name_shadowing")
    override fun update(
        id: Long,
        mode: Int,
        title: String,
        text: String,
        oldTitle: String,
        oldText: String) {
        this.mode = mode

        this.title = oldTitle
        this.text = oldText

        doInCoroutine {
            when (mode) {
                MODE_NOTIF -> {
                    this.title = title
                    this.text = text

                    sendNotification(id)
                }
                MODE_REM -> {
                    if (id < currentTimeMillis())
                        return@doInCoroutine

                    dismissRem(id, false)

                    this.title = title
                    this.text = text

                    val n = model.noteDao().getNoteByRid(id)
                    val id = id + Random().nextInt(100).let { if (it == 0) 1 else it }

                    n?.rid = id
                    update(n ?: return@doInCoroutine)

                    performRem(id)
                }
                MODE_SCH -> {
                    val interval = model.noteDao().getSid2BySid(id) ?: return@doInCoroutine
                    dismissSch(id, id.toInt()+10, false)
                    
                    this.title = title
                    this.text = text

                    val n = model.noteDao().getNoteBySid(id)
                    val id = id + Random().nextInt(100).let { if (it == 0) 1 else it }
                    
                    n?.sid = id
                    update(n ?: return@doInCoroutine)
                    
                    performSch(id, interval)
                }
            }
        }
    }
}
