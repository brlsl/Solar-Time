package com.example.solartime

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.android.material.snackbar.Snackbar
import pub.devrel.easypermissions.EasyPermissions
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity(){

    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private lateinit var mConstraintLayout: ConstraintLayout
    private lateinit var mSharedPreferences: SharedPreferences

    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    private lateinit var mLegalTimeTxtView : TextView
    private lateinit var mSolarTimeTxtView : TextView
    private lateinit var mPositionTxtView: TextView

    private lateinit var mLocalisationButton : Button

    private companion object val PREFERENCES = "PREFERENCES"
    private val PREFS_LONGITUDE = "PREFERENCES_LONGITUDE"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mLegalTimeTxtView = findViewById(R.id.hourTextView)
        mSolarTimeTxtView = findViewById(R.id.solarTimeHourTextView)
        mPositionTxtView = findViewById(R.id.longitudeTextView)

        mLocalisationButton = findViewById(R.id.button_localisation)

        configureLegalTime(mLegalTimeTxtView)

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mConstraintLayout = findViewById(R.id.mainActivityConstraintLayout)

        mSharedPreferences = baseContext.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)

        askGPSPosition(mLegalTimeTxtView, mSolarTimeTxtView)

        mLocalisationButton.setOnClickListener {
            val lm: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            var isGpsEnabled = false
            try {
                isGpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
            } catch (e: Exception){
                e.printStackTrace()
            }

            if (!isGpsEnabled){
                Snackbar.make(mConstraintLayout,"Please connect your GPS", Snackbar.LENGTH_SHORT).show()
            }

            else{
                startLocationUpdates()
                Snackbar.make(mConstraintLayout,"Refresh position", Snackbar.LENGTH_SHORT).show()
                var longitude = mSharedPreferences.getFloat(PREFS_LONGITUDE, 0.0f)
                if (longitude != 0.0f)
                    configureSolarTime(mLegalTimeTxtView, mSolarTimeTxtView, longitude.toDouble() )
            }


        }

        // for location update
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                for (location in locationResult.locations){
                    mPositionTxtView.text = "Last Known Position: ${Utils.round(location.longitude,8)}"
                    mSharedPreferences
                        .edit()
                        .putFloat(PREFS_LONGITUDE, location.longitude.toFloat())
                        .apply()

                }
            }
        }



        if (mSharedPreferences.contains(PREFS_LONGITUDE)){
            var longitude = mSharedPreferences.getFloat(PREFS_LONGITUDE, 0.0f).toDouble()

            mPositionTxtView.text = "Last known device longitude:" + Utils.round(longitude, 6)
            configureSolarTime(mLegalTimeTxtView, mSolarTimeTxtView, longitude)

        } else {
            mPositionTxtView.text = "Unknown location, please reconnect"
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
                        mPositionTxtView.text = "Last Known device longitude: ${Utils.round(longitude,8)}"
                        configureSolarTime(legalTimeTxtView, solarTimeTxtView, longitude)

                        // register last known longitude
                        mSharedPreferences
                            .edit()
                            .putFloat(PREFS_LONGITUDE, longitude.toFloat())
                            .apply()
                    }
                }
        }else{
            EasyPermissions.requestPermissions(this,"SolarTime needs your device position to calculate", 123,*perms)
        }
    }

    private fun configureSolarTime(legalTimeTextView: TextView, solarTimeTextView: TextView, longitude:Double){
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
                configureSolarTime(legalTimeTextView, solarTimeTextView, longitude)
            }

        } }).start()
    }

    private fun configureLegalTime(legalTimeTextView: TextView){
        val currentLegalTime = Date()
        val formatter = SimpleDateFormat("HH:mm:ss", Locale.ENGLISH)


        val handler = Handler(Looper.myLooper()!!)
        Thread(Runnable { run {
            try{
                Thread.sleep(10)
            } catch (e: InterruptedException){
                e.printStackTrace()
            }
            handler.post {
                legalTimeTextView.text = formatter.format(currentLegalTime)
                configureLegalTime(legalTimeTextView)
            }

        } }).start()
    }



    private fun buildLocationRequest(){
        locationRequest = LocationRequest()
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        locationRequest.setInterval(5000)

    }

    private fun startLocationUpdates() {

        buildLocationRequest()
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        mFusedLocationClient.requestLocationUpdates(locationRequest,
            locationCallback,
            Looper.getMainLooper())
        Snackbar.make(mConstraintLayout, "Position updated!", Snackbar.LENGTH_LONG).show()
    }

}