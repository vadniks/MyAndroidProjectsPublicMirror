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
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent

/**
 * @author Vad Nik
 * @version dated Jul 30, 2019.
 * @link https://github.com/vadniks
 */
class Widgets : AppWidgetProvider() {
    private var delegate: WidgetsDelegate = WidgetsDelegate.getDelegate()

    override fun onUpdate(context: Context?, appWidgetManager: AppWidgetManager?, appWidgetIds: IntArray?) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)

        if (context != null && appWidgetManager != null && appWidgetIds != null)
            delegate.onUpdate(context, appWidgetManager, appWidgetIds)
    }

    override fun onDeleted(context: Context?, appWidgetIds: IntArray?) {
        super.onDeleted(context, appWidgetIds)

        if (context != null && appWidgetIds != null)
            delegate.onDeleted(context, appWidgetIds)
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)

        if (context != null && intent != null)
            delegate.onReceive(context, intent)
    }

    @Deprecated("use delegate's fun directly instead")
    fun updateWidget(
        context: Context,
        id: Long,
        title: String,
        text: String,
        color: Int): Unit = delegate.updateWidget(context, id, title, text, color)

    companion object {
        private val delegate = WidgetsDelegate.getDelegate()

        const val MODE_WIDGET = 0x00000008
        const val MODE_WIDGET_CONFIGURE = 0x000000010

        val ACTION_OPEN_WIDGET = delegate.decodeHardcoded("") // ACTION_OPEN_WIDGET
        const val EXTRA_WIDGET_ID = AppWidgetManager.EXTRA_APPWIDGET_ID
        const val ACTION_WIDGET_CONFIGURE = AppWidgetManager.ACTION_APPWIDGET_CONFIGURE

        fun getDelegate(): WidgetsDelegate = delegate
    }
}
