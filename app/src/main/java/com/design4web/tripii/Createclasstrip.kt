package com.design4web.tripii

import android.app.ActivityManager
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import cn.pedant.SweetAlert.SweetAlertDialog
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
import java.util.*

class Createclasstrip : AppCompatActivity(), View.OnClickListener {


    internal lateinit var userid: String
    internal lateinit var tripcode: String
    internal lateinit var username: String
    internal lateinit var tripid: String
    internal lateinit var tripname: EditText
    internal lateinit var password: EditText
    internal lateinit var description: EditText
    internal lateinit var date: EditText
    internal lateinit var enddate: EditText
    internal lateinit var createtrip: Button
    internal lateinit var alltrip: Button
    internal lateinit var access_token :String
    private var mYear: Int = 0
    private var mMonth: Int = 0
    private var mDay: Int = 0
    private var date1: String? = null
    private var membership: String? = null
    internal var preferences_name = "isFirstTime"
    internal lateinit var share : SharePref

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_createclasstrip)

        val actionBar = supportActionBar
        actionBar!!.hide()

        share = SharePref.getInstance(applicationContext)
        userid = share.getVal("userid")
        username = share.getVal("username")
        membership = share.getVal("membership")
        access_token = share.getVal("access_token")


        tripname = findViewById(R.id.name) as EditText
        password = findViewById(R.id.trippassword) as EditText
        description = findViewById(R.id.description) as EditText
        date = findViewById(R.id.date) as EditText
        enddate = findViewById(R.id.enddate) as EditText


        createtrip = findViewById(R.id.createtrip) as Button
        alltrip = findViewById(R.id.seealltrip) as Button

        createtrip.setOnClickListener(this)
        alltrip.setOnClickListener(this)
        date.setOnClickListener(this)
        enddate.setOnClickListener(this)


        //    firstTime();


    }


    fun startService() {
        if (isMyServiceRunning(ForegroundService::class.java)) {
            Log.d("Service Status", "ALREADY RUNNING")
        } else {
            Log.d("Service Status", "STARTED RUNNING")
            val startIntent = Intent(this, ForegroundService::class.java)
            startIntent.action = Constants.ACTION.STARTFOREGROUND_ACTION
            startService(startIntent)
        }
    }

    private fun isMyServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }


    fun firstTime() {


        val sharedTime = getSharedPreferences(preferences_name, 0)
        if (sharedTime.getBoolean("firstTime", true)) {
            insertlocation()
            sharedTime.edit().putBoolean("firstTime", false).apply()
        } else {


            startService()


        }

    }

    private fun insertlocation() {

        var urlapi = share.urlApi
        val url = urlapi+"/locations/create"
        //val url = "http://trippr.aprosoftech.com/api/insertlocation/insert"
        var trip_id =share.getVal("trip_id")
        val params = JSONObject()
        try {


            val c1 = Calendar.getInstance()

            Log.d("date", "" + c1.get(Calendar.DATE))

            val mYear1 = c1.get(Calendar.YEAR)
            val mMonth1 = c1.get(Calendar.MONTH) + 1
            val mDay1 = c1.get(Calendar.DAY_OF_MONTH)
            val strDate = "$mDay1-$mMonth1-$mYear1"


            val gpsTracker = GPSTracker(this@Createclasstrip)

            val latitude = gpsTracker.getLatitude().toString()
            val longitude = gpsTracker.getLongitude().toString()

            params.put("trip_id", trip_id)
            params.put("user_id", userid)
            //  params.put("date", strDate)
            params.put("latitude", latitude)
            params.put("longitude", longitude)


        } catch (e: JSONException) {
            e.printStackTrace()
        }

        Volley.newRequestQueue(this@Createclasstrip)
                .add(object : JsonRequest<JSONArray>(Request.Method.POST,
                        url,
                        params.toString(),
                        Response.Listener { jsonArray -> Log.d("response", jsonArray.toString()) }, Response.ErrorListener { volleyError ->
                    Log.d("error", volleyError.toString())


                    SweetAlertDialog(this@Createclasstrip, SweetAlertDialog.ERROR_TYPE)
                            .setTitleText(getString(R.string.oops))
                            .setContentText(getString(R.string.checkconnection))
                            .show()
                }) {


                    override fun parseNetworkResponse(networkResponse: NetworkResponse): Response<JSONArray> {


                        try {
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

                    override fun getHeaders(): MutableMap<String, String> {
                        var  params =  HashMap<String, String>()
                              params.put("Accept","application/json")
                              params.put("Authorization","Bearer "+ access_token)
                        return params
                    }
                }
                )


    }


    override fun onClick(view: View) {

        if (view.id == R.id.seealltrip) {
            val intent = Intent(this@Createclasstrip, Alltrips::class.java)
            startActivity(intent)
        }
        if (view.id == R.id.createtrip) {


            if (tripname.text.toString().equals("", ignoreCase = true)) {
                Snackbar.make(view, R.string.fillallthefields, Snackbar.LENGTH_LONG).show()
                return
            }

            if (password.text.toString().equals("", ignoreCase = true)) {
                Snackbar.make(view, R.string.fillallthefields, Snackbar.LENGTH_LONG).show()
                return
            }

            if (description.text.toString().equals("", ignoreCase = true)) {
                Snackbar.make(view, R.string.fillallthefields, Snackbar.LENGTH_LONG).show()
                return
            }
            if (date.text.toString().equals("", ignoreCase = true)) {
                Snackbar.make(view, R.string.fillallthefields, Snackbar.LENGTH_LONG).show()
                return
            }
            if (enddate.text.toString().equals("", ignoreCase = true)) {
                Snackbar.make(view, R.string.fillallthefields, Snackbar.LENGTH_LONG).show()
                return
            }


            val pDialog = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
            pDialog.progressHelper.barColor = Color.parseColor("#A5DC86")
            pDialog.titleText = getString(R.string.loading)
            pDialog.setCancelable(false)
            pDialog.show()

            var urlapi = share.urlApi
            val url = urlapi+"/trips/create"

            val params = JSONObject()
            try {


                val c1 = Calendar.getInstance()

                Log.d("date", "" + c1.get(Calendar.DATE))

                val mYear1 = c1.get(Calendar.YEAR)
                val mMonth1 = c1.get(Calendar.MONTH) + 1
                val mDay1 = c1.get(Calendar.DAY_OF_MONTH)
                val strDate = "$mDay1-$mMonth1-$mYear1"

                val useridsplit = userid.split("-".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
                tripcode = useridsplit[0] + "" + c1.get(Calendar.MILLISECOND) + "" + c1.get(Calendar.DAY_OF_MONTH)
               // params.put("createby", userid)
                params.put("trip_name", tripname.text.toString())
                params.put("description", description.text.toString())
                params.put("start_date", date.text.toString())
                params.put("end_date", enddate.text.toString())
                params.put("password", password.text.toString())
                params.put("trip_code", tripcode)
                params.put("createdate", strDate)
                params.put("address", "")
               // params.put("username", username)
               // params.put("membership", membership)

            } catch (e: JSONException) {
                e.printStackTrace()
            }


            val request = CustomJsonObjectRequestBasicAuth(Request.Method.POST,url,params,
                    Response.Listener{ response->
                        pDialog.dismiss()
                        Log.d("response", response.toString())
                        try {
                            tripid = response.getJSONObject("meta").getJSONObject("data").getString("id")
                            if (!tripid.equals("", ignoreCase = true)) {
                                joinedtrips()
                            }

                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }

                    },
                    Response.ErrorListener {

                        Log.d("error", it.toString())
                        pDialog.dismiss()
                        var json = JSONObject (String(it.networkResponse.data , Charset.defaultCharset()))
                        SweetAlertDialog(this@Createclasstrip, SweetAlertDialog.ERROR_TYPE)
                                .setTitleText(getString(R.string.oops))
                                .setContentText(getString(R.string.checkconnection))
                                .show()
                    },
                    access_token
            )

            VolleySingleton.getInstance(this).addToRequestQueue(request)

        }

        if (view.id == R.id.date) {


            val c = Calendar.getInstance()
            mYear = c.get(Calendar.YEAR)
            mMonth = c.get(Calendar.MONTH)
            mDay = c.get(Calendar.DAY_OF_MONTH)


            val datePickerDialog = DatePickerDialog(this@Createclasstrip,
                    DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                        if (dayOfMonth < 10 && monthOfYear < 10) {
                            date1 = "0" + dayOfMonth + "-0" + (monthOfYear + 1) + "-" + year
                        } else if (dayOfMonth < 10) {
                            date1 = "0" + dayOfMonth + "-" + (monthOfYear + 1) + "-" + year

                        } else if (monthOfYear < 10) {
                            date1 = dayOfMonth.toString() + "-0" + (monthOfYear + 1) + "-" + year

                        } else {
                            date1 = dayOfMonth.toString() + "-" + (monthOfYear + 1) + "-" + year
                        }
                        date.setText(date1)
                    }, mYear, mMonth, mDay)
            datePickerDialog.datePicker.minDate = c.timeInMillis
            datePickerDialog.show()


        }





        if (view.id == R.id.enddate) {


            val c = Calendar.getInstance()
            mYear = c.get(Calendar.YEAR)
            mMonth = c.get(Calendar.MONTH)
            mDay = c.get(Calendar.DAY_OF_MONTH)


            val datePickerDialog = DatePickerDialog(this@Createclasstrip,
                    DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                        if (dayOfMonth < 10 && monthOfYear < 10) {
                            date1 = "0" + dayOfMonth + "-0" + (monthOfYear + 1) + "-" + year
                        } else if (dayOfMonth < 10) {
                            date1 = "0" + dayOfMonth + "-" + (monthOfYear + 1) + "-" + year

                        } else if (monthOfYear < 10) {
                            date1 = dayOfMonth.toString() + "-0" + (monthOfYear + 1) + "-" + year

                        } else {
                            date1 = dayOfMonth.toString() + "-" + (monthOfYear + 1) + "-" + year
                        }
                        enddate.setText(date1)
                    }, mYear, mMonth, mDay)
            datePickerDialog.datePicker.minDate = c.timeInMillis
            datePickerDialog.show()


        }


    }

    private fun joinedtrips() {

        var request_url = share.urlApi
        var access_token = share.getVal("access_token")

        var userid  = share.getVal("userid")
        val params = JSONObject()
        try {


            val c1 = Calendar.getInstance()

            Log.d("date", "" + c1.get(Calendar.DATE))

            val mYear1 = c1.get(Calendar.YEAR)
            val mMonth1 = c1.get(Calendar.MONTH) + 1
            val mDay1 = c1.get(Calendar.DAY_OF_MONTH)
            val strDate = "$mDay1-$mMonth1-$mYear1"

            params.put("trip_id", tripid)
            params.put("user_id", userid)
            params.put("password",password)


        } catch (e: JSONException) {
            e.printStackTrace()
        }

        val request = CustomJsonObjectRequestBasicAuth(Request.Method.POST,request_url+"/trips/join_trip",params,
                Response.Listener{ response->
                    Log.d("error", response.toString())
                    SweetAlertDialog(this@Createclasstrip, SweetAlertDialog.SUCCESS_TYPE)
                            .setTitleText(getString(R.string.congrats))
                            .setContentText(getString(R.string.tripadded))
                            .setConfirmText("ok")
                            .setConfirmClickListener { sweetAlertDialog ->
                                sweetAlertDialog.dismiss()
                                val intent = Intent(this@Createclasstrip, Alltrips::class.java)
                                startActivity(intent)
                            }
                            .show()

                },
                Response.ErrorListener{

                    Log.d("error", it.toString())

                    var json = JSONObject (String(it.networkResponse.data , Charset.defaultCharset()))
                    SweetAlertDialog(this@Createclasstrip, SweetAlertDialog.ERROR_TYPE)
                            .setTitleText(getString(R.string.oops))
                            .setContentText(getString(R.string.checkconnection))
                            .show()
                },
                access_token
        )

        VolleySingleton.getInstance(this).addToRequestQueue(request)

    }


}
