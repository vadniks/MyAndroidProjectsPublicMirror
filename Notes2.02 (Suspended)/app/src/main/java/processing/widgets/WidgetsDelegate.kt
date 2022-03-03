/*
 * Copyright (C) 2018-2020 Vad Nik (http://github.com/vadniks). All rights reserved.
 *
 * This code is a part of proprietary software.
 * Usage, distribution, redistribution, modifying
 * and/or commercial use of this code,
 * without author's written permission, are strongly prohibited.
 */

package .processing.widgets

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import androidx.annotation.WorkerThread
import .mvp.model.individual.IIndividualModel
import .processing.common.Note
import .processing.widgets.WidgetsDelegateGetter.getWidgetsDelegate

/**
 * @author Vad Nik
 * @version dated Jul 30, 2019.
 * @link https://github.com/vadniks
 */
interface WidgetsDelegate : IIndividualModel {

    fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray)

    fun onDeleted(context: Context, ids: IntArray)

    fun onReceive(context: Context, intent: Intent)

    override fun updateWidget(
        context: Context,
        id: Long,
        title: String,
        text: String,
        color: Int)
    
    @WorkerThread
    fun getNoteByWidgetId(id: Long): Note?
    
    companion object {

        fun getDelegate(): WidgetsDelegate = getWidgetsDelegate()
    }
}
