package 

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import java.lang.System.currentTimeMillis

/**
 * @author Vad Nik.
 * @version dated July 8, 2018.
 * @link github.com/vadniks
 */
public final class FlashlightWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds)
            updateAppWidget(context, appWidgetManager, appWidgetId)
    }

    private fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        // Construct the RemoteViews object
        val views = RemoteViews(context.packageName, R.layout.flashlight_widget)
        //views.setTextViewText(R.id.appwidget_text, widgetText)

        views.setOnClickPendingIntent(R.id.widget_bt_flash, PendingIntent.getBroadcast(context, currentTimeMillis().toInt(),
                Intent(context, BR::class.java)
                        .setAction("FL_ON/OFF")
                        .setClass(context, BR::class.java), 0))

        views.setOnClickPendingIntent(R.id.widget_bt_screen, PendingIntent.getBroadcast(context, currentTimeMillis().toInt(),
                Intent(context, BR::class.java)
                        .setAction("LS_ON/OFF")
                        .setClass(context, BR::class.java), 0))

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        //TODO: check whether flashlight is activated, turn it off if it is, proceed otherwise.
    }

    //override fun onEnabled(context: Context) {}

    //override fun onDisabled(context: Context) {}
}
