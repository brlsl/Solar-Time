package com.example.solartime

import android.location.Location
import java.text.SimpleDateFormat
import java.util.*

class Utils {
    fun localSolarTime(longitude : Location){

        val localStandardTime = Calendar.getInstance() // find today date and hour

        var dayOfYear:Int  = localStandardTime.get(Calendar.DAY_OF_YEAR) // get number of the year

        //val solarTime =  localStandardTime  + 4 minutes * (longitude - heure locale méridienne) + EquationDuTemps

        val format = SimpleDateFormat("HH:mm:ss", Locale.FRENCH)


    }

    fun timeEquation() {
        1+1
    }

    companion object{ // like static in java
        fun correctLongitudeToLocalStandardTime(longitude: Double): Double {

            val timeZone: String = SimpleDateFormat("Z", Locale.getDefault()).format(System.currentTimeMillis()) // +0130
            val hours = timeZone.substring(0,3).toInt() // +01
            val minutes = timeZone.substring(4,5).toInt() // 00

            //  (in degree)
            val localStandardTimeMeridian = (hours*15) +  (0.25* minutes)

            val result:Double = (longitude - localStandardTimeMeridian) * 4
            val partsResult = result.toString().split(".")
            val resultMinutes = partsResult[0].toInt()
            val resultSeconds = (((("0."+ partsResult[1]).toDouble()) * 60))

            return if (result > 0)
                (resultMinutes * 60 *1000) + (resultSeconds * 1000)
            else
                (resultMinutes * 60 *1000) - (resultSeconds * 1000)

            /*
            val parts = longitude.toString().split(".")
            val right = parts[1]
            val cutResult = "0.$right"

             */
          //  return //cutResult.toDouble()
        }

    }

}