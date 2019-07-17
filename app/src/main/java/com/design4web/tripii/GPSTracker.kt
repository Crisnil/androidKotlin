package com.design4web.tripii

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.util.Log


/**
 * Created by jasvi on 9/12/2017.
 */

class GPSTracker(private val mContext: Context) : Service(), LocationListener {

    //  for GPS status
    internal var isGPSEnabled = false

    //  for network status
    internal var isNetworkEnabled = false

    // for GPS status
    internal var canGetLocation = false

    internal var location: Location? = null // location
    internal var latitude: Double = 0.toDouble() // latitude
    internal var longitude: Double = 0.toDouble() // longitude
    internal var criteria: Criteria


    // Declaring a Location Manager
    private var locationManager: LocationManager? = null
    private var time: Long = 0
    private var bestProvider: String? = null

    init {
        criteria = Criteria()
        getLocation()
    }

    @SuppressLint("MissingPermission")
    fun getLocation(): Location? {
        try {
            locationManager = mContext
                    .getSystemService(Context.LOCATION_SERVICE) as LocationManager
            bestProvider = locationManager!!.getBestProvider(criteria, true)
            // getting GPS status
            isGPSEnabled = locationManager!!
                    .isProviderEnabled(LocationManager.GPS_PROVIDER)

            // getting network status
            isNetworkEnabled = locationManager!!
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            //            if (!isGPSEnabled) {
            //                final AlertDialog.Builder builder =  new AlertDialog.Builder(mContext);
            //                final String action = Settings.ACTION_LOCATION_SOURCE_SETTINGS;
            //                final String message = "For more accuracy. Please enable your GPS.";
            //
            //                builder.setMessage(message)
            //                        .setPositiveButton("OK",
            //                                new DialogInterface.OnClickListener() {
            //                                    public void onClick(DialogInterface d, int id) {
            //                                        mContext.startActivity(new Intent(action));
            //                                        d.dismiss();
            //                                    }
            //                                })
            //                        .setNegativeButton("Cancel",
            //                                new DialogInterface.OnClickListener() {
            //                                    public void onClick(DialogInterface d, int id) {
            //                                        d.cancel();
            //                                    }
            //                                });
            //                builder.create().show();
            //            }

            if (!isGPSEnabled && !isNetworkEnabled) {
                // no network provider is enabled
                //                final AlertDialog.Builder builder =  new AlertDialog.Builder(mContext);
                //                final String action = Settings.ACTION_LOCATION_SOURCE_SETTINGS;
                //                final String message = "Do you want open GPS setting?";
                //
                //                builder.setMessage(message)
                //                        .setPositiveButton("OK",
                //                                new DialogInterface.OnClickListener() {
                //                                    public void onClick(DialogInterface d, int id) {
                //                                        mContext.startActivity(new Intent(action));
                //                                        d.dismiss();
                //                                    }
                //                                })
                //                        .setNegativeButton("Cancel",
                //                                new DialogInterface.OnClickListener() {
                //                                    public void onClick(DialogInterface d, int id) {
                //                                        d.cancel();
                //                                    }
                //                                });
                //                builder.create().show();

                val builder1 = AlertDialog.Builder(mContext)
                builder1.setMessage("Do you want open GPS setting?")
                builder1.setCancelable(true)
                builder1.setPositiveButton(
                        "Yes"
                ) { dialog, id ->
                    Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    dialog.dismiss() }

                builder1.setNegativeButton(
                        "No"
                ) { dialog, id -> dialog.cancel() }

                val alert11 = builder1.create()
                alert11.show()


            } else {
                this.canGetLocation = true

                // if GPS Enabled get lat/long using GPS Services
                if(!isGPSEnabled){
                    val builder1 = AlertDialog.Builder(mContext)
                    builder1.setMessage("Do you want open GPS setting?")
                    builder1.setCancelable(true)
                    builder1.setPositiveButton(
                            "Yes"
                    ) { dialog, id ->
                        Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                        dialog.dismiss() }

                    builder1.setNegativeButton(
                            "No"
                    ) { dialog, id -> dialog.cancel() }

                    val alert11 = builder1.create()
                    alert11.show()
                }
                else{

                    if (location == null) {
                        //if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
                            //return TODO;
                            locationManager!!.requestLocationUpdates(
                                    bestProvider,
                                    MIN_TIME_BW_UPDATES,
                                    MIN_DISTANCE_CHANGE_FOR_UPDATES.toFloat(), this)
                            Log.d("GPS Enabled", "GPS Enabled")
                            if (locationManager != null) {

                                location = locationManager!!
                                        .getLastKnownLocation(LocationManager.GPS_PROVIDER)
                                if (location != null) {
                                    //								Toast.makeText(mContext,"Location Accuracy for GPS_PROVIDER"+location.getAccuracy(),Toast.LENGTH_LONG).show();
                                    latitude = location!!.latitude
                                    longitude = location!!.longitude
                                }
                            }
                        //}

                    }
                }


                if (isNetworkEnabled) {
                    if (location == null) {

                        locationManager!!.requestLocationUpdates(
                                bestProvider,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES.toFloat(), this)
                        Log.d("Network", "Network")
                        if (locationManager != null) {
                            //                            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            //                                // TODO: Consider calling
                            //                                //    ActivityCompat#requestPermissions
                            //                                // here to request the missing permissions, and then overriding
                            //                                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                //                                          int[] grantResults)
                            //                                // to handle the case where the user grants the permission. See the documentation
                            //                                // for ActivityCompat#requestPermissions for more details.
                            //                                return null;
                            //                            }

                            location = locationManager!!
                                    .getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                            if (location != null) {
                                //								Toast.makeText(mContext, "Location Accuracy for NETWORK_PROVIDER" + location.getAccuracy(), Toast.LENGTH_LONG).show();

                                latitude = location!!.latitude
                                longitude = location!!.longitude
                            }
                        }
                    }
                }

            }

        } catch (e: Exception) {
            e.printStackTrace()
        }

        return location
    }

    /**
     * Stop using GPS listener
     * Calling this function will stop using GPS in your app
     */
    fun stopUsingGPS() {
        if (locationManager != null) {
            locationManager!!.removeUpdates(this@GPSTracker)
        }
    }

    /**
     * Function to get latitude
     */
    fun getLatitude(): Double {
        if (location != null) {
            latitude = location!!.latitude
        }
         return latitude
       // return getLocation()?.latitude !!
    }

    /**
     * Function to get longitude
     */
    fun getLongitude(): Double {
        if (location != null) {
            longitude = location!!.longitude
        }

        // return longitude
        return longitude
    }

    /**
     * Function to get time
     */
    fun getTime(): Long {
        if (location != null) {
            time = location!!.time
        }
        return time
    }


    /**
     * Function to check GPS/wifi enabled
     * @return boolean
     */
    fun canGetLocation(): Boolean {
        return this.canGetLocation
    }

    /**
     * Function to show settings alert dialog
     * On pressing Settings button will lauch Settings Options
     */
    fun showSettingsAlert(context: Context) {
        //        Context context =  this.getApplicationContext();
        val alertDialog = AlertDialog.Builder(context)

        // Setting Dialog Title
        alertDialog.setTitle("GPS is settings")

        // Setting Dialog Message
        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?")

        // On pressing Settings button
        alertDialog.setPositiveButton("Settings") { dialog, which ->
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            mContext.startActivity(intent)
        }

        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel") { dialog, which -> dialog.cancel() }

        // Showing Alert Message
        alertDialog.show()
    }

    override fun onLocationChanged(location: Location) {}

    override fun onProviderDisabled(provider: String) {}

    override fun onProviderEnabled(provider: String) {}

    override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}

    override fun onBind(arg0: Intent): IBinder? {
        return null
    }

    companion object {
        //= new Criteria();

        // The minimum distance to change Updates in meters
        private val MIN_DISTANCE_CHANGE_FOR_UPDATES: Long = 0 // 1 meters

        // The minimum time between updates in milliseconds
        private val MIN_TIME_BW_UPDATES = (1000 * 20 * 1).toLong() // 1 minute
    }

}

