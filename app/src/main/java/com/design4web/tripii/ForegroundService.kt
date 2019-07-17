package com.design4web.tripii

import android.Manifest
import android.app.ActivityManager
import android.app.PendingIntent
import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.support.v4.app.ActivityCompat
import android.support.v4.app.NotificationCompat
import android.util.Log
import android.widget.Toast
import cn.pedant.SweetAlert.SweetAlertDialog
import com.android.volley.Request
import com.android.volley.Response
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsStatusCodes
import org.json.JSONException
import org.json.JSONObject
import java.text.DateFormat
import java.util.*

class ForegroundService : Service(), GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    private lateinit var m_locationManager: LocationManager
    private val locationManager: LocationManager? = null
    private val provider: String? = null
    private var mActivityManager: ActivityManager? = null

    //
    private var prevLat = 0.0
    private var prevLng = 0.0
    private lateinit var mGoogleApiClient: GoogleApiClient

    private var mLastLocation: Location? = null


    private val isNetworkAvailable: Boolean
        get() {
            val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            return activeNetworkInfo != null && activeNetworkInfo.isConnected
        }


    private val foregroundApp: ActivityManager.RunningAppProcessInfo?
        get() {
            var result: ActivityManager.RunningAppProcessInfo? = null
            var info: ActivityManager.RunningAppProcessInfo? = null

            if (mActivityManager == null)
                mActivityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val l = mActivityManager!!.runningAppProcesses
            val i = l.iterator()
            while (i.hasNext()) {
                info = i.next()
                if (info!!.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND && !isRunningService(info.processName)) {
                    result = info
                    break
                }
            }
            return result
        }


    override fun onCreate() {
        super.onCreate()
        buildGoogleApiClient()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        if (intent == null) {
            Log.i(LOG_TAG, "Received Start Foreground Intent ")
            val notificationIntent = Intent(this, Login::class.java)
            notificationIntent.action = Constants.ACTION.MAIN_ACTION
            notificationIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            val pendingIntent = PendingIntent.getActivity(this, 0,
                    notificationIntent, 0)

            val previousIntent = Intent(this, ForegroundService::class.java)
            previousIntent.action = Constants.ACTION.PREV_ACTION
            val ppreviousIntent = PendingIntent.getService(this, 0,
                    previousIntent, 0)

            val playIntent = Intent(this, ForegroundService::class.java)
            playIntent.action = Constants.ACTION.PLAY_ACTION
            val pplayIntent = PendingIntent.getService(this, 0,
                    playIntent, 0)

            val nextIntent = Intent(this, ForegroundService::class.java)
            nextIntent.action = Constants.ACTION.NEXT_ACTION
            val pnextIntent = PendingIntent.getService(this, 0,
                    nextIntent, 0)

            val icon = BitmapFactory.decodeResource(resources,
                    R.mipmap.ic_launcher)

            val notification = NotificationCompat.Builder(this)
                    .setContentTitle("iJUJU is finding deals for you!")
                    .setTicker("iJUJU is finding deals for you in the background")
                    .setContentText("iJUJU")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentIntent(pendingIntent)
                    .setOngoing(true)
                    .addAction(android.R.drawable.ic_media_previous,
                            "Open App", ppreviousIntent).build()
            startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE,
                    notification)

//            buildGoogleApiClient()


            //  Here I offer two options: either you are using satellites or the Wi-Fi services to get user's location
            //          this.m_locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 3000, 0, this); //  User's location is retrieve every 3 seconds
            this.m_locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                //            requestPermission();
                return Service.START_STICKY
            }
            this.m_locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 50f, this)

            val locManager = this.m_locationManager
            val handler = Handler()
            val runnable = object : Runnable {

                override fun run() {

                    //Perform any task here which you want to do after time finish.
                    if (ActivityCompat.checkSelfPermission(this@ForegroundService.applicationContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this@ForegroundService.applicationContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return
                    }
                    //                Location loc = locManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    //                GPSTracker gpsTracker = new GPSTracker(ForegroundService.this);
                    getLocation()

                    //                if (gpsTracker.canGetLocation) {
                    Log.d("DDD", "Sending Juice to Server with 5 sec delay")
                    sendToServer(mLastLocation)
                    //                }
                    handler.postDelayed(this, 20000)

                }
            }
            handler.postDelayed(runnable, 5000)

            return Service.START_STICKY
        }

        if (intent.action == Constants.ACTION.STARTFOREGROUND_ACTION) {
            Log.i(LOG_TAG, "Received Start Foreground Intent ")
            val notificationIntent = Intent(this, Login::class.java)
            notificationIntent.action = Constants.ACTION.MAIN_ACTION
            notificationIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            val pendingIntent = PendingIntent.getActivity(this, 0,
                    notificationIntent, 0)

            val previousIntent = Intent(this, ForegroundService::class.java)
            previousIntent.action = Constants.ACTION.PREV_ACTION
            val ppreviousIntent = PendingIntent.getService(this, 0,
                    previousIntent, 0)

            val playIntent = Intent(this, ForegroundService::class.java)
            playIntent.action = Constants.ACTION.PLAY_ACTION
            val pplayIntent = PendingIntent.getService(this, 0,
                    playIntent, 0)

            val nextIntent = Intent(this, ForegroundService::class.java)
            nextIntent.action = Constants.ACTION.NEXT_ACTION
            val pnextIntent = PendingIntent.getService(this, 0,
                    nextIntent, 0)

            val icon = BitmapFactory.decodeResource(resources,
                    R.mipmap.ic_launcher)

            val notification = NotificationCompat.Builder(this)
                    .setContentTitle("iJUJU is finding deals for you!")
                    .setTicker("iJUJU is finding deals for you in the background")
                    .setContentText("iJUJU")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentIntent(pendingIntent)
                    .setOngoing(true)
                    .addAction(android.R.drawable.ic_media_previous,
                            "Open App", ppreviousIntent).build()
            startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE,
                    notification)
        } else if (intent.action == Constants.ACTION.PREV_ACTION) {
            Log.i(LOG_TAG, "Clicked Previous")
        } else if (intent.action == Constants.ACTION.PLAY_ACTION) {
            Log.i(LOG_TAG, "Clicked Play")
        } else if (intent.action == Constants.ACTION.NEXT_ACTION) {
            Log.i(LOG_TAG, "Clicked Next")
        } else if (intent.action == Constants.ACTION.STOPFOREGROUND_ACTION) {
            Log.i(LOG_TAG, "Received Stop Foreground Intent")
            stopForeground(true)
            stopSelf()
        }


        //        //GOOGLE API CLIENT
        //
        //
        //        mGoogleApiClient = new GoogleApiClient.Builder(this)
        //                .addConnectionCallbacks(this)
        //                .addOnConnectionFailedListener(this)
        //                .addApi(LocationServices.API).build();
        //
        //        mGoogleApiClient.connect();
        //        //END GOOGLE API CLIENT

        //buildGoogleApiClient()


        //  Here I offer two options: either you are using satellites or the Wi-Fi services to get user's location
        //          this.m_locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 3000, 0, this); //  User's location is retrieve every 3 seconds
        this.m_locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            //            requestPermission();
            return Service.START_STICKY
        }
        this.m_locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 50f, this)

        val locManager = this.m_locationManager
        val handler = Handler()
        val runnable = object : Runnable {

            override fun run() {

                //Perform any task here which you want to do after time finish.
                if (ActivityCompat.checkSelfPermission(this@ForegroundService.applicationContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this@ForegroundService.applicationContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return
                }
                //                Location loc = locManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                //                GPSTracker gpsTracker = new GPSTracker(ForegroundService.this);
                getLocation()

                //                if (gpsTracker.canGetLocation) {
                Log.d("DDD", "Sending Juice to Server with 5 sec delay")
                sendToServer(mLastLocation)
                //                }
                handler.postDelayed(this, 20000)

            }
        }
        handler.postDelayed(runnable, 5000)

        return Service.START_STICKY
    }

    //    @Override
    //    public void onLocationChanged(Location location) {
    //        int lat = (int) (location.getLatitude());
    //        int lng = (int) (location.getLongitude());
    //        sendToServer(location);
    //    }

    fun sendToServer(loc: Location?) {


        val sharedPreferences11 = getSharedPreferences("loginuser", Context.MODE_PRIVATE)
        val notification = sharedPreferences11.getBoolean("NOTIFICATIONS", true)
        if (!notification)
            return



        if (loc == null) {    //  Filtering out null values
            Toast.makeText(this@ForegroundService, "No Location Value", Toast.LENGTH_LONG).show()
            //buildGoogleApiClient()
            return
        }

        if (loc.latitude == 0.0) {     //Filtering out 0.0 values
            //buildGoogleApiClient()
            Toast.makeText(this@ForegroundService, "0.0 Location Value", Toast.LENGTH_LONG).show()
            return
        }

        val sharedPreferences = getSharedPreferences("TRACKING_PREFS", Context.MODE_PRIVATE)

        if (sharedPreferences.contains("prevLat")) {
            prevLat = java.lang.Double.parseDouble(sharedPreferences.getString("prevLat", "0.0"))
            prevLng = java.lang.Double.parseDouble(sharedPreferences.getString("prevLng", "0.0"))
        } else {
            prevLng = 0.0
            prevLat = 0.0
        }



        val lat = loc.latitude
        val lon = loc.longitude


        val distance = distanceBetween(prevLat, lat, prevLng, lon, 0.0, 0.0)


        Toast.makeText(this, "PrevLat:$prevLat\nPrevLng:$prevLng\nCurr Lat:$lat\nCurr Lng:$lon\n Distance:$distance", Toast.LENGTH_LONG).show()
        val editor = applicationContext.getSharedPreferences("loginuser", Context.MODE_PRIVATE)

        //        if (!isNetworkAvailable())
        //            return;
        //        Boolean showNotifications = true;
        //        if (editor.contains("NOTIFICATIONS")) {
        //            showNotifications = editor.getBoolean("NOTIFICATIONS", true);
        //        }
        //        if (!showNotifications)
        //            return;

        var share = SharePref.getInstance(applicationContext)

        var urlapi = share.urlApi
        var access_token = share.getVal("access_token")

        val url = urlapi+"/locations/create"
        //val url = "http://trippr.aprosoftech.com/api/updatelocation/update"

        val params = JSONObject()
        try {


            val c1 = Calendar.getInstance()

            Log.d("date", "" + c1.get(Calendar.DATE))

            val mYear1 = c1.get(Calendar.YEAR)
            val mMonth1 = c1.get(Calendar.MONTH) + 1
            val mDay1 = c1.get(Calendar.DAY_OF_MONTH)
            val strDate = DateFormat.getDateTimeInstance().format(Date())

            val userid = share.getVal("userid")

            val gpsTracker = GPSTracker(this@ForegroundService)

            val latitude = gpsTracker.getLatitude().toString()
            val longitude = gpsTracker.getLongitude().toString()

//            var latitude = loc.latitude
//            var longitude = loc.longitude

            params.put("trip_id", 1)
            params.put("user_id", userid)
         //   params.put("date", strDate)
            params.put("latitude", latitude)
            params.put("longitude", longitude)


        } catch (e: JSONException) {
            e.printStackTrace()
        }


        val request = CustomJsonObjectRequestBasicAuth(Request.Method.POST,url,params,
                Response.Listener{ response->
                    //                            pDialog.dismiss()

                },
                Response.ErrorListener{
                    SweetAlertDialog(this@ForegroundService, SweetAlertDialog.ERROR_TYPE)
                            .setTitleText(getString(R.string.oops))
                            .setContentText("Something went wrong on Foreground Service!!!")
                            .show()
                },
                access_token
        )

        VolleySingleton.getInstance(this).addToRequestQueue(request)

    }


    override fun onDestroy() {
        super.onDestroy()
        Log.i(LOG_TAG, "In onDestroy")

        val startIntent = Intent(this, ForegroundService::class.java)
        startIntent.action = Constants.ACTION.STARTFOREGROUND_ACTION
        startService(startIntent)
    }

    override fun onBind(intent: Intent): IBinder? {
        // Used only in case of bound services.
        return null
    }


    override fun onLocationChanged(loc: Location) {
        //        Toast.makeText(this.getApplicationContext(),"Location ready to be sent to server",Toast.LENGTH_LONG).show();
        sendToServer(loc)

        //Calling AsyncTask for upload latitude and longitude
    }

    override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {

    }

    override fun onProviderEnabled(provider: String) {

    }

    override fun onProviderDisabled(provider: String) {

    }


    /**
     * Calculate distance between two points in latitude and longitude taking
     * into account height difference using the Haversine method as its base.
     *
     *
     * Elevation should be in meters. If you are not interested in elevation, pass 0.
     *
     * @return Distance in meters
     */
    private fun distanceBetween(lat1: Double, lat2: Double, lon1: Double,
                                lon2: Double, el1: Double, el2: Double): Double {

        val R = 6371 // Radius of the earth

        val latDistance = Math.toRadians(lat2 - lat1)
        val lonDistance = Math.toRadians(lon2 - lon1)
        val a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) + (Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2))
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        var distance = R.toDouble() * c * 1000.0 // convert to meters

        val height = el1 - el2

        distance = Math.pow(distance, 2.0) + Math.pow(height, 2.0)

        return Math.sqrt(distance)
    }

    /**
     * Google api callback methods
     */
    override fun onConnectionFailed(result: ConnectionResult) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.errorCode)
    }

    override fun onConnected(arg0: Bundle?) {

        // Once connected with google api, get the location
        getLocation()
    }

    override fun onConnectionSuspended(arg0: Int) {
        mGoogleApiClient.connect()
    }


    @Synchronized
    protected fun buildGoogleApiClient() {
        mGoogleApiClient = GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build()

        mGoogleApiClient.connect()

        val mLocationRequest = LocationRequest.create()
        mLocationRequest.interval = 10000
        mLocationRequest.fastestInterval = 5000
        mLocationRequest.smallestDisplacement = 100f
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        val builder = LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest)

        val result = LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build())

        result.setResultCallback { locationSettingsResult ->
            val status = locationSettingsResult.status

            when (status.statusCode) {
                LocationSettingsStatusCodes.SUCCESS ->
                    // All location settings are satisfied. The client can initialize location requests here
                    getLocation()
                LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                }
                LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                }
            }
            // Show the dialog by calling startResolutionForResult(),
            // and check the result in onActivityResult().
            //                            status.startResolutionForResult(ForegroundService.this.getApplicationContext(), REQUEST_CHECK_SETTINGS);
            //                            ActivityManager am = (ActivityManager) AppService.this.getSystemService(ACTIVITY_SERVICE);
            // The first in the list of RunningTasks is always the foreground task.
            //                            ActivityManager.RunningTaskInfo foregroundTaskInfo = am.getRunningTasks(1).get(0);
            //                        PendingIntent pI = status.getResolution();
            //
            //                        getApplication().startActivity(new Intent(getApplicationContext(), Resolution.class)
            //                                .putExtra("resolution", pI).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            //                            MainActivity mainActivity = new MainActivity();
            //                            status.startResolutionForResult(mainActivity,REQUEST_CHECK_SETTINGS);
        }


    }

    private fun getActivityForApp(target: ActivityManager.RunningAppProcessInfo?): ComponentName? {
        var result: ComponentName? = null
        var info: ActivityManager.RunningTaskInfo

        if (target == null)
            return null

        if (mActivityManager == null)
            mActivityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val l = mActivityManager!!.getRunningTasks(9999)
        val i = l.iterator()

        while (i.hasNext()) {
            info = i.next()
            if (info.baseActivity.packageName == target.processName) {
                result = info.topActivity
                break
            }
        }

        return result
    }

    private fun isStillActive(process: ActivityManager.RunningAppProcessInfo?, activity: ComponentName?): Boolean {
        // activity can be null in cases, where one app starts another. for example, astro
        // starting rock player when a move file was clicked. we dont have an activity then,
        // but the package exits as soon as back is hit. so we can ignore the activity
        // in this case
        if (process == null)
            return false

        val currentFg = foregroundApp
        val currentActivity = getActivityForApp(currentFg)

        if (currentFg != null && currentFg.processName == process.processName &&
                (activity == null || currentActivity!!.compareTo(activity) == 0))
            return true

        Log.i(TAG, "isStillActive returns false - CallerProcess: " + process.processName + " CurrentProcess: "
                + (if (currentFg == null) "null" else currentFg.processName) + " CallerActivity:" + (activity?.toString()
                ?: "null")
                + " CurrentActivity: " + (currentActivity?.toString() ?: "null"))
        return false
    }

    private fun isRunningService(processname: String?): Boolean {
        if (processname == null || processname.isEmpty())
            return false

        var service: ActivityManager.RunningServiceInfo

        if (mActivityManager == null)
            mActivityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val l = mActivityManager!!.getRunningServices(9999)
        val i = l.iterator()
        while (i.hasNext()) {
            service = i.next()
            if (service.process == processname)
                return true
        }

        return false
    }


    /**
     * Method to verify google play services on the device
     */

    private fun checkPlayServices(): Boolean {

        val googleApiAvailability = GoogleApiAvailability.getInstance()

        val resultCode = googleApiAvailability.isGooglePlayServicesAvailable(this)

        if (resultCode != ConnectionResult.SUCCESS) {
            if (googleApiAvailability.isUserResolvableError(resultCode)) {
                googleApiAvailability.getErrorDialog(Login(), resultCode,
                        PLAY_SERVICES_REQUEST).show()
            } else {
                Toast.makeText(applicationContext,
                        "This device is not supported.", Toast.LENGTH_LONG)
                        .show()
                //                finish();
            }
            return false
        }
        return true
    }


    //    @Override
    //    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    //        final LocationSettingsStates states = LocationSettingsStates.fromIntent(data);
    //        switch (requestCode) {
    //            case REQUEST_CHECK_SETTINGS:
    //                switch (resultCode) {
    //                    case Activity.RESULT_OK:
    //                        // All required changes were successfully made
    //                        getLocation();
    //                        break;
    //                    case Activity.RESULT_CANCELED:
    //                        // The user was asked to change settings, but chose not to
    //                        break;
    //                    default:
    //                        break;
    //                }
    //                break;
    //        }
    //    }

    /**
     * Method to display the location on UI
     */

    private fun getLocation() {
        try {
            mLastLocation = LocationServices.FusedLocationApi
                    .getLastLocation(mGoogleApiClient)
            //                if (mLastLocation!=null)
            //                    Toast.makeText(this,"Locc "+mLastLocation.getLatitude(),Toast.LENGTH_LONG).show();
        } catch (e: SecurityException) {
            e.printStackTrace()
        }

    }

    companion object {
        private val LOG_TAG = "ForegroundService"
        private val TAG = "QWE"

        // LogCat tag
        //    private static final String TAG = ForegroundService.class.getSimpleName();

        private val PLAY_SERVICES_REQUEST = 1000
        private val REQUEST_CHECK_SETTINGS = 2000
    }


}
