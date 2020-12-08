package com.example.solartime

import android.Manifest
import android.annotation.SuppressLint
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

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var constraintLayout: ConstraintLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        constraintLayout = findViewById(R.id.mainActivityConstraintLayout)

        val hoursTextView : TextView = findViewById(R.id.hourTextView)
        val positionTextView: TextView = findViewById(R.id.longitudeTextView)

        askGPSPosition(positionTextView)
        getTime(hoursTextView)
    }

    // ask permissions
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode,permissions,grantResults,this)
    }

    @SuppressLint("MissingPermission")
    fun askGPSPosition(positionTextView: TextView) {
        var perms : Array<String> = arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
        if(EasyPermissions.hasPermissions(this, *perms)){
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location : Location? ->
                    // Got last known location. In some rare situations this can be null.
                    if (location != null){
                        var longitude = location.longitude
                        println("Longitude valeur:" +longitude)
                        positionTextView.text = "Last Known Position: $longitude"
                    }
                    else{
                        Snackbar.make(constraintLayout,"Could not get position" ,Snackbar.LENGTH_SHORT)
                    }

                }
        }else{
            EasyPermissions.requestPermissions(this,"SolarTime needs your position to calculate", 123,*perms)
        }
    }

    private fun getTime(textView: TextView){
        val today = Date()
        val formatter = SimpleDateFormat("HH:mm:ss",Locale.ENGLISH)

        var handler = Handler(Looper.myLooper()!!)
        Thread(Runnable { run {
            try{
                Thread.sleep(10)
            } catch (e: InterruptedException){
                e.printStackTrace()
            }
            handler.post {
                textView.text = formatter.format(today)
                getTime(textView)
            }

        } }).start()
    }

}