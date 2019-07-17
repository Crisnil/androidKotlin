package com.design4web.tripii

import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.*
import cn.pedant.SweetAlert.SweetAlertDialog
import com.android.volley.*
import com.android.volley.toolbox.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.nio.charset.Charset
import java.util.*

class Teacherlocation : AppCompatActivity(), OnMapReadyCallback {

    internal lateinit var listView: ListView
    internal lateinit var tripid: String
    internal var usertype = "teachers"

    internal lateinit var mMapView: MapView
    internal lateinit var mMap: GoogleMap
    internal lateinit var gpsTracker: GPSTracker
    internal var options: MarkerOptions? = null
    internal var latitude: Double? = null
    internal var longitude: Double? = null
    internal var share :SharePref? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_teacherlocation)

        if(share == null) {
            share = SharePref.getInstance(applicationContext)
        }
        val actionBar = supportActionBar
        actionBar!!.hide()

        listView = findViewById<ListView>(R.id.listview)

        tripid = intent.extras.getString("tripid")

        showallteachers()


        mMapView = findViewById(R.id.mapview) as MapView

        mMapView.onCreate(savedInstanceState)
        mMapView.onResume()

        try {
            MapsInitializer.initialize(applicationContext)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        mMapView.getMapAsync(this)


    }

    private fun showallteachers() {


        val pDialog = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
        pDialog.progressHelper.barColor = Color.parseColor("#A5DC86")
        pDialog.titleText = getString(R.string.loading)
        pDialog.setCancelable(false)
        pDialog.show()

        var url = share!!.urlApi+"/trips/all_users/"+tripid
        var access_token = share!!.getVal("access_token")

        val params = JSONObject()
        try {

            params.put("tripid", tripid)
           // params.put("usertype", usertype)

        } catch (e: JSONException) {
            e.printStackTrace()
        }

        val request = CustomJsonObjectRequestBasicAuth(Request.Method.GET,url,params,
                Response.Listener{ response->
                    pDialog.dismiss()
                    Log.d("response trips", response.toString())
                    var jsonArray = response.getJSONObject("meta").getJSONArray("metadata").getJSONObject(0).getJSONArray(usertype)
                    //                            pDialog.dismiss()
                    val commentsAdapter = Allteacheradapter(this@Teacherlocation, jsonArray)
                            listView.adapter = commentsAdapter

                },
                Response.ErrorListener{
                    Log.d("errresponse", it.toString())
                    pDialog.dismiss()
                    Log.d("errresponse", it.toString())
                    SweetAlertDialog(this@Teacherlocation, SweetAlertDialog.WARNING_TYPE)
                            .setTitleText(this@Teacherlocation.getString(R.string.oops))
                            .setContentText("No Trips are found")
                            .show()
                },
                access_token
        )

        VolleySingleton.getInstance(this).addToRequestQueue(request)

    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        googleMap.uiSettings.isZoomControlsEnabled = true


        // val url = "http://trippr.aprosoftech.com/api/userlocation/locations"
        var url = share!!.urlApi+"/locations/trip/"+tripid
        var access_token = share!!.getVal("access_token")

        gpsTracker = GPSTracker(this@Teacherlocation)

        gpsTracker.getLocation()

        val params = JSONObject()
        try {

            params.put("tripid", tripid)
            params.put("usertype", usertype)

        } catch (e: JSONException) {
            e.printStackTrace()
        }

        val request = CustomJsonObjectRequestBasicAuth(Request.Method.GET,url,null,
                Response.Listener{ response->
                    Log.d("response", response.toString())
                    var jsonArray = response.getJSONObject("meta").getJSONArray("metadata").getJSONObject(0).getJSONArray("joined_users")


                    for (i in 0 until jsonArray.length()) {

                        val obj = jsonArray.getJSONObject(i).get("user_location")
                        //var obj = jsonArray.getJSONObject(i).getJSONObject("user_location")
                        if(obj.toString() != "null") {
                            try {
                                latitude = java.lang.Double.valueOf(jsonArray.getJSONObject(i).getJSONObject("user_location").getString("latitude"))
                                longitude = java.lang.Double.valueOf(jsonArray.getJSONObject(i).getJSONObject("user_location").getString("longitude"))
                                var title = jsonArray.getJSONObject(i).getString("name")

                                // calcDistance(latitude,longitude);

                                val sydney = LatLng(latitude!!, longitude!!)
                                val options1 = MarkerOptions()
                                        .position(sydney)
                                        .title(title)

                                mMap.addMarker(options1)
                                mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))


                                // mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(jsonArray.getJSONObject(i).getDouble("Latitude"), jsonArray.getJSONObject(i).getDouble("Longitude")), 12.0f));


                            } catch (e: JSONException) {
                                e.printStackTrace()
                            }

                        }
                    }
                    val sydney1 = LatLng(gpsTracker.getLatitude(), gpsTracker.getLongitude())
                    val options = MarkerOptions()
                            .position(sydney1)
                            .title("You are here")
                    if(sydney1 != null) {
                        mMap.addMarker(options)
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney1))

                        var displayMetrics = DisplayMetrics()
                        var metrics =  getWindowManager().getDefaultDisplay().getMetrics(displayMetrics)
                        var width = displayMetrics.widthPixels;

                        var zlevel = calculateZoomLevel(width)
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(gpsTracker.getLatitude(), gpsTracker.getLongitude()),zlevel.toFloat()))

                    }

                },
                Response.ErrorListener{

                    var json = JSONObject (String(it.networkResponse.data , Charset.defaultCharset()))
                    SweetAlertDialog(this@Teacherlocation, SweetAlertDialog.ERROR_TYPE)
                            .setTitleText(json.getString("title"))
                            .setContentText(json.getString("description"))
                            .show()
                },
                access_token
        )

        VolleySingleton.getInstance(this).addToRequestQueue(request)

    }

}

private fun calculateZoomLevel(screenWidth: Int): Int {
    val equatorLength = 40075004.0 // in meters
    val widthInPixels = screenWidth.toDouble()
    var metersPerPixel = equatorLength / 256
    var zoomLevel = 1
    while (metersPerPixel * widthInPixels > 2000) {
        metersPerPixel /= 2.0
        ++zoomLevel
    }
    Log.i("ADNAN", "zoom level = $zoomLevel")
    return zoomLevel
}

internal class Allteacheradapter(var context: Activity, var jsonArray: JSONArray) : BaseAdapter() {

    lateinit var imageLoader: ImageLoader
    lateinit var sendto: String
    lateinit var tripid: String
    lateinit var name: String
    lateinit var sendby: String
    lateinit var imageurl: String
    lateinit var sendbyname: String
    lateinit var sendtoname: String

    override fun getCount(): Int {
        return jsonArray.length()
    }

    override fun getItem(i: Int): Any {
        return i
    }

    override fun getItemId(i: Int): Long {
        return i.toLong()
    }


    override fun getView(i: Int, view: View?, viewGroup: ViewGroup): View {


        var v = view
        if (view == null) {
            val inflater = context.layoutInflater
            v = inflater.inflate(R.layout.locations, null, true)
        }

        val username = v!!.findViewById(R.id.username) as TextView
        val sendmessage = v.findViewById(R.id.sendmessage) as Button
        val userimage = v.findViewById(R.id.userimage) as NetworkImageView

        try {
            username.text = jsonArray.getJSONObject(i).getString("name")
            val imageurl = jsonArray.getJSONObject(i).getString("imageurl")


            //val url = "http://trippr.aprosoftech.com/Resources/$imageurl"
            if(imageurl != "null"){
//            require(imageurl!= "null" || imageurl != null) {
                imageLoader = CustomVolleyRequest.getInstance(context.applicationContext)
                        .imageLoader

                imageLoader.get(imageurl, ImageLoader.getImageListener(userimage,
                        R.mipmap.ic_launcher, R.mipmap.ic_launcher))
                userimage.setImageUrl(imageurl, imageLoader)
            }

        } catch (e: JSONException) {
            e.printStackTrace()
        }

        sendmessage.tag = i

        sendmessage.setOnClickListener(View.OnClickListener { view ->

            val dialog = Dialog(context)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setContentView(R.layout.sendmessagedialog)

            val writemessage = dialog.findViewById(R.id.writemessage) as EditText
            val btnsend = dialog.findViewById(R.id.btnsend) as Button
            val btncancel = dialog.findViewById(R.id.btncancel) as Button

            //val editor = context.getSharedPreferences("userid", MODE_PRIVATE)

            try {
                Log.d("jass","mao ni sila" +jsonArray.toString() )

                sendto = jsonArray.getJSONObject(view.tag as Int).getString("id")
                tripid = jsonArray.getJSONObject(view.tag as Int).getString("trip_id")

            } catch (e: JSONException) {
                e.printStackTrace()
            }


            btnsend.setOnClickListener(View.OnClickListener { view ->
                if (writemessage.text.toString().equals("", ignoreCase = true)) {
                    Snackbar.make(view, R.string.fillallthefields, Snackbar.LENGTH_LONG).show()
                    return@OnClickListener
                }


                val c1 = Calendar.getInstance()

                Log.d("date", "" + c1.get(Calendar.DATE))

                val mYear1 = c1.get(Calendar.YEAR)
                val mMonth1 = c1.get(Calendar.MONTH) + 1
                val mDay1 = c1.get(Calendar.DAY_OF_MONTH)
                val strDate = "$mDay1-$mMonth1-$mYear1"


                val pDialog = SweetAlertDialog(context, SweetAlertDialog.PROGRESS_TYPE)
                pDialog.progressHelper.barColor = Color.parseColor("#A5DC86")
                pDialog.titleText = context.getString(R.string.loading)
                pDialog.setCancelable(false)
                pDialog.show()

                var share = SharePref.getInstance(context.applicationContext)

                //val url = "http://trippr.aprosoftech.com/api/sendmessages/insertmessage"
                var userid  = share!!.getVal("userid")
                var request_url = share!!.urlApi+"/chat/create/"+sendto
                var access_token = share!!.getVal("access_token")

                val params = JSONObject()
                try {


                    params.put("trip_id", tripid)


                    params.put("message", writemessage.text.toString())

                } catch (e: JSONException) {
                    e.printStackTrace()
                }
                val request = CustomJsonObjectRequestBasicAuth(Request.Method.POST,request_url,params,
                        Response.Listener{ response ->
                            dialog.dismiss()
                            Log.d("userchat response", response.toString())

                            pDialog.dismiss()

                            SweetAlertDialog(context, SweetAlertDialog.SUCCESS_TYPE)
                                    .setTitleText(context.getString(R.string.congrats))
                                    .setContentText(context.getString(R.string.messagesent))
                                    .show()


                        },
                        Response.ErrorListener{
                            dialog.dismiss()
                            Log.d("userchat response", it.toString())
                            pDialog.dismiss()
                            var json = JSONObject (String(it.networkResponse.data , Charset.defaultCharset()))
                            SweetAlertDialog(context, SweetAlertDialog.ERROR_TYPE)
                                    .setTitleText(json.getString("title"))
                                    .setContentText(json.getString("description"))
                                    .setConfirmClickListener { sweetAlertDialog ->
                                        sweetAlertDialog.dismiss()
                                        pDialog.dismiss()
                                    }
                                    .show()
                        },
                        access_token
                )
                request.retryPolicy = DefaultRetryPolicy(10000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)

                VolleySingleton.getInstance(context).addToRequestQueue(request)

            })

            btncancel.setOnClickListener { dialog.dismiss() }

            dialog.show()
        })


        return v

    }
}
