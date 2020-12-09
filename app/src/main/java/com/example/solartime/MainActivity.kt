package com.example.solartime

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.gms.location.*
import com.google.android.material.snackbar.Snackbar
import pub.devrel.easypermissions.EasyPermissions
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity(){

    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private lateinit var mConstraintLayout: ConstraintLayout
    private lateinit var mSharedPreferences: SharedPreferences

    private companion object val PREFERENCES = "PREFERENCES"
    private val PREFS_LONGITUDE = "PREFERENCES_LONGITUDE"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val legalTimeTxtView : TextView = findViewById(R.id.hourTextView)
        val solarTimeTxtView : TextView = findViewById(R.id.solarTimeHourTextView)
        val positionTxtView: TextView = findViewById(R.id.longitudeTextView)

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mConstraintLayout = findViewById(R.id.mainActivityConstraintLayout)

        mSharedPreferences = baseContext.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)

        askGPSPosition(positionTxtView, legalTimeTxtView, solarTimeTxtView)

        if (mSharedPreferences.contains(PREFS_LONGITUDE)){
            val longitude = mSharedPreferences.getFloat(PREFS_LONGITUDE, 0.0f).toDouble()
            getTime(legalTimeTxtView, solarTimeTxtView, longitude)
            positionTxtView.text = "Longitude test:" + longitude
        }

    }

    // ask permissions
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode,permissions,grantResults,this)
    }

    @SuppressLint("MissingPermission")
    fun askGPSPosition(
        positionTextView: TextView,
        legalTimeTxtView: TextView,
        solarTimeTxtView: TextView
    ) {
        var perms : Array<String> = arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
        if(EasyPermissions.hasPermissions(this, *perms)){
            mFusedLocationClient.lastLocation
                .addOnSuccessListener { location : Location? ->
                    // Got last known location. In some rare situations this can be null.
                    if (location != null){
                        var longitude = location.longitude
                        println("Longitude valeur:" +longitude)
                        positionTextView.text = "Last Known Position: $longitude"
                        getTime(legalTimeTxtView, solarTimeTxtView, longitude)

                        mSharedPreferences
                            .edit()
                            .putFloat(PREFS_LONGITUDE, longitude.toFloat())
                            .apply()
                    }
                    else{
                        Snackbar.make(mConstraintLayout,"Could not get position" ,Snackbar.LENGTH_SHORT)
                    }

                }
        }else{
            EasyPermissions.requestPermissions(this,"SolarTime needs your position to calculate", 123,*perms)
        }
    }

    private fun getTime(legalTimeTextView: TextView, solarTimeTextView: TextView, longitude:Double){
        val currentLegalTime = Date()
        val formatter = SimpleDateFormat("HH:mm:ss", Locale.ENGLISH)

        val calendar = Calendar.getInstance() // find today date and hour
        val dayOfYear  = calendar.get(Calendar.DAY_OF_YEAR).toDouble() // get number of the year

        val currentSolarTime = Date().time.plus(Utils.correctLongitudeToLocalStandardTime(longitude) + Utils.equationOfTime(dayOfYear))

        val handler = Handler(Looper.myLooper()!!)
        Thread(Runnable { run {
            try{
                Thread.sleep(10)
            } catch (e: InterruptedException){
                e.printStackTrace()
            }
            handler.post {
                legalTimeTextView.text = formatter.format(currentLegalTime)
                solarTimeTextView.text = formatter.format(currentSolarTime)
                getTime(legalTimeTextView, solarTimeTextView, longitude)
            }

        } }).start()
    }

}