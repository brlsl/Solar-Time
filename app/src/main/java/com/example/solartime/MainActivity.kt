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
    private lateinit var mLegalTimeTxtView: TextView
    private lateinit var mSolarTimeTxtView: TextView
    private lateinit var mPositionTxtView: TextView
    private lateinit var mLocalisationButton: Button
    private lateinit var mapLocationAnim: LottieAnimationView

    // FOR DATA
    private val TAG = "Main Activity"
    private companion object val PREFERENCES = "PREFERENCES"
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

        configureViews()
        configureCallBackLocation()
        configureLegalTime(mLegalTimeTxtView)
        askGPSPermission(mLegalTimeTxtView, mSolarTimeTxtView)

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
                mapLocationAnim.visibility = View.VISIBLE
                mapLocationAnim.playAnimation()
                mSolarTimeTxtView.visibility = View.GONE

                GlobalScope.launch(Dispatchers.Main) {
                    delay(3000)
                    stopLocationUpdates()
                    mapLocationAnim.visibility = View.GONE

                    mLocalisationButton.visibility = View.VISIBLE
                    mSolarTimeTxtView.visibility = View.VISIBLE
                    val longitude = mSharedPreferences.getFloat(PREFS_LONGITUDE, 999f)
                    if (longitude != 999f)
                        configureSolarTime(mSolarTimeTxtView, longitude.toDouble())
                }
            }
        }



        if (mSharedPreferences.contains(PREFS_LONGITUDE)){
            val longitude = mSharedPreferences.getFloat(PREFS_LONGITUDE, 999.0f)
            if (longitude != 999f){
                mPositionTxtView.text = "Last known device longitude:" + Utils.round(longitude.toDouble(), 8)
                configureSolarTime(mSolarTimeTxtView, longitude.toDouble())
            }

        } else {
            mPositionTxtView.text = "Unknown location, please locate your device"
        }

    }

    override fun onResume() {
        super.onResume()
        val longitude = mSharedPreferences.getFloat(PREFS_LONGITUDE, 999.0f)

        if (longitude != 999f){
            mPositionTxtView.text = "Last known device longitude:" + Utils.round(longitude.toDouble(), 8)
            configureSolarTime(mSolarTimeTxtView, longitude.toDouble())
        }
        else {
            mPositionTxtView.text = "Unknown location, please locate your device"
        }
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
                        mPositionTxtView.text = "Last known device longitude: ${Utils.round(longitude,8)}"
                        configureSolarTime(solarTimeTxtView, longitude)

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

                    mPositionTxtView.text = "Last known device longitude: ${Utils.round(location.longitude,8)}"
                    configureSolarTime(mSolarTimeTxtView, location.longitude)
                }
            }
        }

    }

    private fun configureSolarTime(solarTimeTextView: TextView, longitude:Double){
        val formatter = SimpleDateFormat("HH:mm:ss", Locale.ENGLISH)
        val calendar = Calendar.getInstance() // find today date and hour
        val dayOfYear  = calendar.get(Calendar.DAY_OF_YEAR).toDouble() // get number day of the year

        val currentSolarTime = Date().time.plus(Utils.correctLongitudeToLocalStandardTime(longitude) + Utils.equationOfTime(dayOfYear))

        solarTimeTextView.text = formatter.format(currentSolarTime)

        val handler = Handler(Looper.myLooper()!!)
        val job = GlobalScope.launch (Dispatchers.IO){
            kotlin.run {
                handler.post{
                    configureSolarTime(solarTimeTextView, longitude)
                }
            }
        }

        if (longitude == mSharedPreferences.getFloat(PREFS_LONGITUDE, 0.0f).toDouble()){
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
        mLegalTimeTxtView = findViewById(R.id.hourTextView)
        mSolarTimeTxtView = findViewById(R.id.solarTimeHourTextView)
        mPositionTxtView = findViewById(R.id.longitudeTextView)
        mLocalisationButton = findViewById(R.id.button_localisation)
        mapLocationAnim = findViewById(R.id.map_location_anim)
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