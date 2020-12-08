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
    fun c_isCorrect() {


        assertEquals("0.1234", Utils.correctLongitudeToLocalStandardTime(2.2834308)  )


    }

}