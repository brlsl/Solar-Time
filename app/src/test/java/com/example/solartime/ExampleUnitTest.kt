package com.example.solartime

import org.junit.Assert.assertEquals

import org.junit.Test


/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun timeCorrection_isCorrect() {
        assertEquals(-3052000, Utils.correctLongitudeToLocalStandardTime(2.2834308))
    }

    @Test
    fun equationOfTime_isCorrect(){
        assertEquals(-222000, Utils.equationOfTime(1.0))
        assertEquals(-185000, Utils.equationOfTime(180.0))
        assertEquals(-58000, Utils.equationOfTime(360.0))
    }

}