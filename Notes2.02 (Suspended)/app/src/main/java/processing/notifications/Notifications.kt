/*
 * Copyright (C) 2018-2020 Vad Nik (http://github.com/vadniks). All rights reserved.
 *
 * This code is a part of proprietary software.
 * Usage, distribution, redistribution, modifying
 * and/or commercial use of this code,
 * without author's written permission, are strongly prohibited.
 */

package .processing.notifications

import android.content.Context
import android.content.Intent
import androidx.annotation.WorkerThread
import .common.NUM_UNDEF
import .mvp.model.individual.IIndividualModel
import .mvp.model.individual.IndividualModelWrapper
import .processing.common.Note

/**
 * @author Vad Nik
 * @version dated Jul 21, 2019.
 * @link https://github.com/vadniks
 */
abstract class Notifications : IndividualModelWrapper {
    protected val context: Context
    protected lateinit var title: String
    protected lateinit var text: String
    protected var mode = NUM_UNDEF
    protected var onInsert: ((mode: Int, extras: LongArray) -> Unit)? = null

    protected val ACTION_DISM_NOTIF = this.decodeHardcodedWithoutEqual("") // ACTION_DISM_NOTIF
    protected val ACTION_DISM_SCH = this.decodeHardcoded("") // ACTION_DISM_SCH
    protected val ACTION_SEND_REM = this.decodeHardcoded("") // ACTION_SEND_REM
    protected val ACTION_SEND_SCH = this.decodeHardcoded("") // ACTION_SEND_SCH
    protected val ACTION_REM_SWAP_OFF = this.decodeHardcodedWithoutEquals("") // ACTION_REM_SWAP_OFF

    @Suppress("UNUSED")
    protected var extraId = NUM_UNDEF.toLong()

    constructor(
        context: Context,
        title: String,
        text: String,
        mode: Int,
        im: IIndividualModel,
        onInsert: (mode: Int, extras: LongArray) -> Unit) : this(context, im) {
        this.title = title
        this.text = text
        this.mode = mode
        this.onInsert = onInsert
    }
    
    constructor(
        context: Context,
        n: Note,
        mode: Int,
        im: IIndividualModel,
        onInsert: (mode: Int, extras: LongArray) -> Unit) : this(context, n.title, n.text, mode, im, onInsert)
    
    constructor(context: Context, @Suppress("UNUSED") intent: Intent, im: IIndividualModel) : this(context, im)
    
    constructor(context: Context, im: IIndividualModel) : super(im) {
        this.context = context
    }

    @WorkerThread
    abstract fun forceDismissAttached()

    @WorkerThread
    abstract fun forceDismissScheduled()

    abstract fun resetReminder(mode: Int, vararg extras: Any)

    abstract fun notifyMessage()

    @WorkerThread
    abstract fun getNoteByNotificationsId(id: Long, which: Int): Note?

    abstract fun dismiss(id: Long)

    abstract fun update(
        id: Long,
        mode: Int,
        title: String,
        text: String,
        oldTitle: String,
        oldText: String)

    fun setVals(title: String, text: String, mode: Int, onInsert: (mode: Int, extras: LongArray) -> Unit): Notifications {
        this.title = title
        this.text = text
        this.mode = mode
        this.onInsert = onInsert

        return this
    }

    companion object {
        const val MODE_NOTIF = 0x00000002
        const val MODE_REM = 0x00000004
        const val MODE_SCH = 0x00000006
        const val MODE_ONCE = 0x00000008

        const val EXTRA_TITLE = 0x00000020.toString()
        const val EXTRA_TEXT = 0x00000022.toString()
        const val EXTRA_PEN_ID = 0x00000026.toString()
        
        fun create(
            context: Context,
            title: String,
            text: String,
            mode: Int,
            onInsert: (mode: Int, extras: LongArray) -> Unit): Notifications =
                NotificationsGetter.create(context, title, text, mode, onInsert)

        fun create(
            context: Context,
            n: Note,
            mode: Int,
            onInsert: (mode: Int, extras: LongArray) -> Unit): Notifications =
            NotificationsGetter.create(context, n, mode, onInsert)
        
        fun create(context: Context, intent: Intent): Notifications =
            NotificationsGetter.create(context, intent)

        fun create(context: Context): Notifications = NotificationsGetter.create(context)
    }
}
