package com.example.solartime

import android.location.Location
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

class Utils {
    fun localSolarTime(longitude : Location){

        val localStandardTime = Calendar.getInstance() // find today date and hour

        var dayOfYear:Int  = localStandardTime.get(Calendar.DAY_OF_YEAR) // get number of the year

        //val solarTime =  localStandardTime  + 4 minutes * (longitude - heure locale méridienne) + EquationDuTemps

        val format = SimpleDateFormat("HH:mm:ss", Locale.FRENCH)


    }

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

    }

}