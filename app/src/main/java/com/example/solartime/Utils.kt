package com.example.solartime

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

class Utils {
    companion object{ // like static in java
        fun correctLongitudeToLocalStandardTime(longitude: Double): Int {

            val timeZone: String = SimpleDateFormat("Z", Locale.getDefault()).format(System.currentTimeMillis()) // +0130
            val hours = timeZone.substring(0,3).toInt() // +01
            val minutes = timeZone.substring(4,5).toInt() // 00

            //  (in degree)
            val localStandardTimeMeridian = (hours*15) +  (0.25* minutes)

            val result:Double = (longitude - localStandardTimeMeridian) * 4
            val partsResult = result.toString().split(".")
            val resultMinutes = partsResult[0].toInt()
            val resultSeconds = (((("0."+ partsResult[1]).toDouble()) * 60)).roundToInt()

            // return time in milliseconds
            return if (result > 0)
                (resultMinutes * 60 *1000) + (resultSeconds * 1000)
            else
                (resultMinutes * 60 *1000) - (resultSeconds * 1000)
        }

        fun equationOfTime(dayOfYear: Double): Int {

            val b = 360 * (dayOfYear - 81) / 365

            val result = (9.87 * sin(Math.toRadians(2*b))) - (7.53 * cos(Math.toRadians(b))) - (1.5 * sin(Math.toRadians(b)))

            val partsResult = result.toString().split(".")

            val resultMinutes = partsResult[0].toInt()
            val resultSeconds = (((("0."+ partsResult[1]).toDouble()) * 60)).roundToInt()

            // return time in milliseconds
            return if (result > 0)
                (resultMinutes * 60 *1000) + (resultSeconds * 1000)
            else
                (resultMinutes * 60 *1000) - (resultSeconds * 1000)
        }

        fun round(value: Double, places: Int): Double {
            require(places >= 0)
            var bd: BigDecimal = BigDecimal.valueOf(value)
            bd = bd.setScale(places, RoundingMode.HALF_UP)
            return bd.toDouble()
        }

        fun calculateCurrentSolarTime(longitude: Double): String? {
            val formatter = SimpleDateFormat("HH:mm:ss", Locale.ENGLISH)
            val calendar = Calendar.getInstance() // find today date and hour
            val dayOfYear  = calendar.get(Calendar.DAY_OF_YEAR).toDouble() // get number day of the year

            return  formatter.format(Date().time.plus(correctLongitudeToLocalStandardTime(longitude) + equationOfTime(dayOfYear)))

        }

        fun calculateCurrentTimeDifference(longitude: Double): String{
            val formatter = SimpleDateFormat("HH:mm:ss", Locale.CHINA)
            formatter.timeZone = TimeZone.getTimeZone("GMT")
            val calendar = Calendar.getInstance() // find today date and hour
            val dayOfYear  = calendar.get(Calendar.DAY_OF_YEAR).toDouble() // get number day of the year

            var timeDifference = correctLongitudeToLocalStandardTime(longitude) + equationOfTime(dayOfYear)

            return if (timeDifference < 0){
                timeDifference *= -1
                "-"+formatter.format(timeDifference)
            }else
                "+"+formatter.format(timeDifference)

        }

        fun updateWidgetView(context: Context){
            val updateViews = buildUpdate(context)
            val widget = ComponentName(context, MainActivityClockWidget::class.java)
            val manager = AppWidgetManager.getInstance(context)

            manager.updateAppWidget(widget, updateViews)

        }
        private fun buildUpdate(context: Context?): RemoteViews {
            // build view for clock widget
            val updateViews = RemoteViews(context?.packageName, R.layout.clock_widget)

            // clock data
            val preferences = context?.getSharedPreferences("PREFERENCES", Context.MODE_PRIVATE)
            val longitude = preferences!!.getFloat("PREFERENCES_LONGITUDE", 999.0f)

            if (longitude != 999.0f){
                val time = calculateCurrentTimeDifference(longitude.toDouble())
                updateViews.setTextViewText(R.id.widgetTextViewHour, time)
                updateViews.setTextViewCompoundDrawables(R.id.widgetTextViewHour, 0,0,0,0)
                updateViews.setTextViewText(R.id.widgetTextViewTitle, "Time difference:")
            } else{
                updateViews.setTextViewText(R.id.widgetTextViewHour, "Locate the device")
                updateViews.setTextViewCompoundDrawables(R.id.widgetTextViewHour, 0,R.drawable.ic_gps,0,0)
            }

            return updateViews

        }

    }

}