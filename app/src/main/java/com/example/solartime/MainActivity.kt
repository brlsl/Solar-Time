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
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import com.airbnb.lottie.LottieAnimationView
import com.google.android.gms.location.*
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import pub.devrel.easypermissions.EasyPermissions
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity(){

    // FOR UI
    private lateinit var mLegalTimeTextView: TextView
    private lateinit var mSolarTimeHourTextView: TextView
    private lateinit var mPositionTitleTextView: TextView
    private lateinit var mSolarTimeTitle: TextView
    private lateinit var mTimeDifference: TextView
    private lateinit var mTimeDifferenceTitle: TextView
    private lateinit var mLocalisationButton: Button
    private lateinit var mMapLocationAnim: LottieAnimationView
    private lateinit var mSunAnim: LottieAnimationView

    // FOR DATA
    private val TAG = "Main Activity"
    private val PREFERENCES = "PREFERENCES"
    private val PREFS_LONGITUDE = "PREFERENCES_LONGITUDE"
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private lateinit var mConstraintLayout: ConstraintLayout
    private lateinit var mSharedPreferences: SharedPreferences
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    // LIFE CYCLE
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Utils.updateWidgetView(this)

        configureViews()
        configureCallBackLocation()
        configureLegalTime(mLegalTimeTextView)
        askGPSPermission(mLegalTimeTextView, mSolarTimeHourTextView)

        configureLastKnownPosition()
        configureTextTitles()

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
                Snackbar.make(mConstraintLayout,"Refreshing position", Snackbar.LENGTH_LONG).show()
                mLocalisationButton.visibility = View.GONE
                mMapLocationAnim.visibility = View.VISIBLE
                mMapLocationAnim.playAnimation()
                mSolarTimeTitle.visibility = View.GONE
                mSolarTimeHourTextView.visibility = View.GONE
                mTimeDifference.visibility = View.GONE
                mTimeDifferenceTitle.visibility = View.GONE
                mSunAnim.pauseAnimation()
                mSunAnim.visibility = View.GONE


                val context = this
                GlobalScope.launch(Dispatchers.Main) {
                    delay(6000)

                    configureTextTitles()

                    configureLastKnownPosition()

                    Utils.updateWidgetView(context)

                    stopLocationUpdates()

                    mMapLocationAnim.cancelAnimation()
                    mMapLocationAnim.visibility = View.GONE
                    mLocalisationButton.visibility = View.VISIBLE
                    mSolarTimeTitle.visibility = View.VISIBLE
                    mSolarTimeHourTextView.visibility = View.VISIBLE
                    mTimeDifference.visibility = View.VISIBLE
                    mTimeDifferenceTitle.visibility = View.VISIBLE
                    mSunAnim.resumeAnimation()
                    mSunAnim.visibility = View.VISIBLE

                }
            }
        }
    }

    private fun configureLastKnownPosition() {
        if (mSharedPreferences.contains(PREFS_LONGITUDE)){
            val longitude = mSharedPreferences.getFloat(PREFS_LONGITUDE, 999.0f)
            if (longitude != 999f){
                mPositionTitleTextView.text = "Last known device longitude:" + Utils.round(longitude.toDouble(), 8)
                configureSolarTime(mSolarTimeHourTextView, longitude.toDouble())
                mTimeDifference.text = Utils.calculateCurrentTimeDifference(longitude.toDouble())
            }

        } else {
            mPositionTitleTextView.text = "Unknown location, please locate your device"
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Utils.updateWidgetView(this)
    }

    // ask permissions
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode,permissions,grantResults,this)
    }

    @SuppressLint("MissingPermission")
    fun askGPSPermission(legalTimeTxtView: TextView, solarTimeTxtView: TextView){
        val perms : Array<String> = arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
        if(EasyPermissions.hasPermissions(this, *perms)){
            mFusedLocationClient.lastLocation
                .addOnSuccessListener { location : Location? ->
                    // Got last known location. In some rare situations this can be null.
                    if (location != null){
                        var longitude = location.longitude
                       // mPositionTxtView.text = "Last known device longitude: ${Utils.round(longitude,8)}"
                        //configureSolarTime(solarTimeTxtView, longitude)

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

    // CONFIGURATION METHODS

    private fun configureCallBackLocation(){
        // for location update
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                for (location in locationResult.locations){

                    // register
                    mSharedPreferences
                        .edit()
                        .putFloat(PREFS_LONGITUDE, location.longitude.toFloat())
                        .apply()

                    //mPositionTxtView.text = "Last known device longitude: ${Utils.round(location.longitude,8)}"
                    //configureSolarTime(mSolarTimeTxtView, location.longitude)
                }
            }
        }

    }

    private fun configureSolarTime(solarTimeTextView: TextView, longitude:Double){
        solarTimeTextView.text = Utils.calculateCurrentSolarTime(longitude)

        val handler = Handler(Looper.myLooper()!!)
        val job = GlobalScope.launch (Dispatchers.IO){
            kotlin.run {
                handler.post{
                    configureSolarTime(solarTimeTextView, longitude)
                }
            }
        }

        if (longitude == mSharedPreferences.getFloat(PREFS_LONGITUDE, 999.0f).toDouble()){
            job.start()
        }

        else {
            job.cancel()
            Log.d(TAG, "Stop coroutine job")
        }

    }

    private fun configureLegalTime(legalTimeTextView: TextView){
        val currentLegalTime = Date()
        val formatter = SimpleDateFormat("HH:mm:ss", Locale.ENGLISH)

        val handler = Handler(Looper.myLooper()!!)
        GlobalScope.launch(Dispatchers.IO) {
            handler.post {
                legalTimeTextView.text = formatter.format(currentLegalTime)
                configureLegalTime(legalTimeTextView)
            }
        }.start()
    }

    private fun configureViews(){
        mSharedPreferences = baseContext.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mConstraintLayout = findViewById(R.id.mainActivityConstraintLayout)
        mLegalTimeTextView = findViewById(R.id.legal_time_textView)
        mSolarTimeHourTextView = findViewById(R.id.solar_time_hour_textView)
        mPositionTitleTextView = findViewById(R.id.longitude_textView)
        mSolarTimeTitle = findViewById(R.id.solar_time_title_textView)
        mTimeDifferenceTitle = findViewById(R.id.time_difference_title_textView)
        mTimeDifference = findViewById(R.id.time_difference_hour_textView)
        mLocalisationButton = findViewById(R.id.button_localisation)
        mMapLocationAnim = findViewById(R.id.map_location_anim)
        mSunAnim = findViewById(R.id.sun_anim)
    }


    private fun configureTextTitles(): CharSequence {
        val longitude = mSharedPreferences.getFloat(PREFS_LONGITUDE, 999f)
        if (longitude == 999f){
            mSolarTimeTitle.text = "Locate your device"
            mTimeDifferenceTitle.text = ""
        }
        else {
            mSolarTimeTitle.text = "Current Solar Time:"
            mTimeDifferenceTitle.text = "Time difference:"
        }
        return mSolarTimeTitle.text
    }



    // LOCATION METHODS

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
    }

    private fun buildLocationRequest(){
        locationRequest = LocationRequest()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 5000

    }

    private fun stopLocationUpdates() {
        mFusedLocationClient.removeLocationUpdates(locationCallback)
    }

}