package com.example.solartime

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import android.widget.RemoteViews
import java.util.*


class MainActivityClockWidget : AppWidgetProvider() {

    private val tag = "MainActivityWidget"
    private val CLOCK_WIDGET_UPDATE = "com.example.solartime.MainActivityClockWidget.CLOCK_WIDGET_UPDATE"

    override fun onDisabled(context: Context?) {
        super.onDisabled(context)
        Log.d(tag, "onDisabled")

    }

    override fun onEnabled(context: Context?) {
        super.onEnabled(context)
        Log.d(tag, "onEnabled")

        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()

        Log.d(tag, "onEnabled " + calendar.timeInMillis.toString())
    }



    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)

        if(AppWidgetManager.ACTION_APPWIDGET_UPDATE == intent!!.action) {
            Utils.updateWidgetView(context!!)
            Log.d(tag, "onReceive")
        }

    }

    override fun onDeleted(context: Context?, appWidgetIds: IntArray?) {
        super.onDeleted(context, appWidgetIds)
        Log.d(tag, "onDeleted")

    }

    override fun onUpdate(
        context: Context?,
        appWidgetManager: AppWidgetManager?,
        appWidgetIds: IntArray?
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        //context?.startService(Intent(context, UpdateService::class.java))
        Utils.updateWidgetView(context!!)

        Log.d(tag, "onUpdate")


       /*
        if (appWidgetIds != null) {
            for (appWidgetId in appWidgetIds){
                mIntent = Intent(context, MainActivity::class.java)

                val pendingIntent = PendingIntent.getActivity(context,0, mIntent, PendingIntent.FLAG_UPDATE_CURRENT)

                val views = RemoteViews(context?.packageName, R.layout.clock_widget)

                views.setOnClickPendingIntent(R.id.solarTimeWidgetButton, pendingIntent)
/*
                val test = Utils.calculateCurrentSolarTime(54.41596603)

                views.setTextViewText(R.id.solarTimeWidgetButton, test)
*/

                appWidgetManager?.updateAppWidget(appWidgetId, views)
/*
                val handler = Handler(Looper.myLooper()!!)
                GlobalScope.launch(Dispatchers.Main) {
                    handler.post {

                    }
                }.start()

 */
/*
                    val time = Utils.calculateCurrentSolarTime(54.41596603)

                    views.setTextViewText(R.id.solarTimeWidgetButton, time)
*/
                Log.d(tag, "onUpdate")
            }
        }
        */
    }




/*
            val job = GlobalScope.launch(Dispatchers.IO){
                kotlin.run {
                   // delay(1000)

                    handler.post {
                   //     onStartCommand(intent, flags, startId)
                    }

                }
            }


            //job.start()

            if (isWidgetActivated){
                job.start()
            } else
                job.cancel()



 */


/*
        override fun onStart(intent: Intent?, startId: Int) {
            val updateViews = buildUpdate(this)
            val widget = ComponentName(this, MainActivityClockWidget::class.java)
            val manager = AppWidgetManager.getInstance(this)

            manager.updateAppWidget(widget, updateViews)

            Log.d("MainActivityWidget", "onStart")

        }


 */



}