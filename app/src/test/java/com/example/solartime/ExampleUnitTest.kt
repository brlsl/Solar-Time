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

    @Test
    fun roundDouble_isCorrect(){
        assertEquals(1.67, Utils.round(1.66666666666,2),0.0)
        assertEquals(1.123, Utils.round(1.123456,3),0.0)
        assertEquals(2.0, Utils.round(1.9111111,0),0.0)
        assertEquals(1.91, Utils.round(1.9111,2),0.0)
    }
}