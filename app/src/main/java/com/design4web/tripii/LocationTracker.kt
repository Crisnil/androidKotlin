package com.design4web.tripii

import android.location.Location
import android.os.Bundle
import android.annotation.SuppressLint
import android.location.LocationManager
import android.app.Activity
import android.content.Context
import android.location.LocationListener
import android.util.Log
import com.android.volley.NetworkResponse
import com.android.volley.ParseError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.HttpHeaderParser
import com.android.volley.toolbox.JsonRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.UnsupportedEncodingException
import java.nio.charset.Charset
import java.text.DateFormat
import java.util.*


class LocationTracker : Activity(), LocationListener {
    protected lateinit var locationManager: LocationManager
    protected  var locationListener: LocationListener? = null
    protected var context: Context? = null
    internal var lat: String? = null
    internal var provider: String? = null
    protected var latitude: String? = null
    protected var longitude: String? = null
    protected var gps_enabled: Boolean = false
    protected var network_enabled: Boolean = false
    internal var loc :Location?=null



    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {

       var  locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this)
//
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
//            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0f, locationListener)
//
//            val locationProvider: String = LocationManager.NETWORK_PROVIDER
//            val lastKnownLocation: Location = locationManager.getLastKnownLocation(locationProvider)
//            loc = lastKnownLocation
//        }
        super.onCreate(savedInstanceState)

    }

    override fun onLocationChanged(location: Location) {
        Log.d("Location", location.provider)

        val url = "http://trippr.aprosoftech.com/api/updatelocation/update"
        val params = JSONObject()
        try {


            val c1 = Calendar.getInstance()

            Log.d("date", "" + c1.get(Calendar.DATE))

            val mYear1 = c1.get(Calendar.YEAR)
            val mMonth1 = c1.get(Calendar.MONTH) + 1
            val mDay1 = c1.get(Calendar.DAY_OF_MONTH)
            val strDate = DateFormat.getDateTimeInstance().format(Date())


            val editor1 = getSharedPreferences("userid", Context.MODE_PRIVATE)
            val userid = editor1.getString("userid", "")



            val latitude = location.latitude.toString()
            val longitude = location.longitude.toString()


            params.put("userid", userid)
            params.put("date", strDate)
            params.put("latitude", latitude)
            params.put("longitude", longitude)


        } catch (e: JSONException) {
            e.printStackTrace()
        }

        Volley.newRequestQueue(this)
                .add(object : JsonRequest<JSONArray>(Request.Method.POST,
                        url,
                        params.toString(),
                        Response.Listener { jsonArray -> Log.d("response", jsonArray.toString()) }, Response.ErrorListener { volleyError -> Log.d("error", volleyError.toString()) }) {


                    override fun parseNetworkResponse(networkResponse: NetworkResponse): Response<JSONArray> {


                        try {
//                            val jsonString = String(networkResponse.data,
//                                    HttpHeaderParser
//                                            .parseCharset(networkResponse.headers))
                            var jsonString = String(networkResponse.data, Charset.defaultCharset())
                            return Response.success(JSONArray(jsonString),
                                    HttpHeaderParser
                                            .parseCacheHeaders(networkResponse))
                        } catch (e: UnsupportedEncodingException) {
                            return Response.error(ParseError(e))
                        } catch (je: JSONException) {
                            return Response.error(ParseError(je))
                        }

                    }
                }
                )


    }

    override fun onProviderDisabled(provider: String) {
        Log.d("Latitude", "disable")
    }

    override fun onProviderEnabled(provider: String) {
        Log.d("Latitude", "enable")
    }

    override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {
        Log.d("Latitude", "status")
    }
}

private fun LocationManager.requestLocationUpdates(gpS_PROVIDER: String, i: Int, i1: Int, locationTracker: LocationTracker) {

}
