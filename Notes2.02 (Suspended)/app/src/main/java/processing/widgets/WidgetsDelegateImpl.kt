/*
 * Copyright (C) 2018-2020 Vad Nik (http://github.com/vadniks). All rights reserved.
 *
 * This code is a part of proprietary software.
 * Usage, distribution, redistribution, modifying
 * and/or commercial use of this code,
 * without author's written permission, are strongly prohibited.
 */

package .processing.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.widget.RemoteViews
import .R
import .common.NUM_UNDEF
import .common.STR_UNDEF
import .common.doInCoroutine
import .mvp.model.individual.IIndividualModel
import .mvp.model.individual.IndividualModelWrapper
import .mvp.view.MainActivity
import .processing.common.Note
import .processing.common.broadcastreceiver.BroadcastReceiver
import .processing.common.broadcastreceiver.BroadcastReceiver.Companion.EXTRA_ID
import .processing.common.broadcastreceiver.BroadcastReceiver.Companion.EXTRA_MODE
import .processing.common.broadcastreceiver.BroadcastReceiver.Companion.makeAction
import .processing.widgets.Widgets.Companion.ACTION_OPEN_WIDGET
import .processing.widgets.Widgets.Companion.ACTION_WIDGET_CONFIGURE
import .processing.widgets.Widgets.Companion.EXTRA_WIDGET_ID
import .processing.widgets.Widgets.Companion.MODE_WIDGET
import .processing.widgets.Widgets.Companion.MODE_WIDGET_CONFIGURE
import java.lang.System.currentTimeMillis

/**
 * @author Vad Nik
 * @version dated Jul 30, 2019.
 * @link https://github.com/vadniks
 */
private class WidgetsDelegateImpl(im: IIndividualModel) : IndividualModelWrapper(im), WidgetsDelegate {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (i in appWidgetIds)
            updateWidget(context, i.toLong(), STR_UNDEF, STR_UNDEF, NUM_UNDEF)
    }

    override fun onDeleted(context: Context, ids: IntArray) {
        if (isDBEncrypted(context)) {
            model.onReceivedEventForDecryption(IIndividualModel.EVENT_DELETE_WIDGET,
                Intent().putExtra(EXTRA_WIDGET_ID, ids))
            return
        }

        doInCoroutine {
            for (i in ids)
                update(getNoteByWidgetId(i.toLong())?.apply { wid = NUM_UNDEF.toLong() } ?: continue)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            makeAction(context, ACTION_OPEN_WIDGET) -> BroadcastReceiver.open(context, intent)
        }
    }

    override fun updateWidget(
        context: Context,
        id: Long,
        title: String,
        text: String,
        color: Int) {

        val cont = Intent()
        cont.putExtra(EXTRA_ID, id)
        cont.putExtra(EXTRA_MODE, MODE_WIDGET)
        cont.action = makeAction(context, ACTION_OPEN_WIDGET)
        cont.setClass(context, Widgets::class.java)

        val conf = Intent()
        conf.putExtra(EXTRA_WIDGET_ID, id.toInt())
        conf.putExtra(EXTRA_MODE, MODE_WIDGET_CONFIGURE)
        conf.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
        conf.action = ACTION_WIDGET_CONFIGURE
        conf.setClass(context, MainActivity::class.java)

        val rv = RemoteViews(context.packageName, R.layout.widget)
        rv.setOnClickPendingIntent(R.id.widget,
            PendingIntent.getBroadcast(context, currentTimeMillis().toInt(), cont, 0))
        rv.setOnClickPendingIntent(R.id.changeNote,
            PendingIntent.getActivity(context, currentTimeMillis().toInt()+10, conf, PendingIntent.FLAG_UPDATE_CURRENT))
        rv.setTextViewText(R.id.widgetTitle, title)
        rv.setTextViewText(R.id.widgetText, text)

        rv.setInt(
            R.id.widget,
            decodeHardcoded(""),
            if (color != NUM_UNDEF) color else Color.WHITE)
        
        AppWidgetManager.getInstance(context).updateAppWidget(id.toInt(), rv)
    }
    
    override fun getNoteByWidgetId(id: Long): Note? = model.noteDao().getNoteByWidgetId(id)
}
