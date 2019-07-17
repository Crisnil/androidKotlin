package com.design4web.tripii

import android.app.ActivityManager
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
import android.widget.RelativeLayout
import android.widget.TextView
import cn.pedant.SweetAlert.SweetAlertDialog
import com.android.volley.Request
import com.android.volley.Response
import org.json.JSONException
import org.json.JSONObject
import java.nio.charset.Charset
import java.util.*

class Joinclasstrip : AppCompatActivity(), View.OnClickListener {


    internal lateinit var tripcode: EditText
    internal lateinit var trippassword: EditText
    internal lateinit var schoolname: TextView
    internal lateinit var studenttriprome: TextView
    internal lateinit var join: Button
    internal lateinit var search: Button
    internal lateinit var tripinfo: RelativeLayout
    internal lateinit var tripid: String
    internal var password = ""
    internal lateinit var tripdetail: String
    internal var tripmembership = ""
    internal lateinit var logintype: String
    internal lateinit var usertype: String
    internal var preferences_name = "isFirstTime"
    internal var share :SharePref?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_joinclasstrip)

        val actionBar = supportActionBar
        actionBar!!.hide()


        schoolname = findViewById(R.id.schoolname) as TextView
        studenttriprome = findViewById(R.id.studenttriprome) as TextView

        tripinfo = findViewById(R.id.tripinfo) as RelativeLayout
        tripcode = findViewById(R.id.tripname) as EditText
        trippassword = findViewById(R.id.trippassword) as EditText

        search = findViewById(R.id.search) as Button




        join = findViewById(R.id.join) as Button
        join.setOnClickListener(this)
        search.setOnClickListener(this)

        //  firstTime();
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

    //    public  void  firstTime() {
    //
    //
    //        SharedPreferences sharedTime = getSharedPreferences(preferences_name, 0);
    //        if (sharedTime.getBoolean("firstTime", true)) {
    //            insertlocation();
    //            sharedTime.edit().putBoolean("firstTime", false).apply();
    //        } else {
    //
    //            startService();
    //
    //
    //        }
    //
    //    }


    override fun onClick(view: View) {

        if (view.id == R.id.search) {

            if (tripcode.text.toString().equals("", ignoreCase = true)) {

                Snackbar.make(view, R.string.entertripcode, Snackbar.LENGTH_LONG).show()
                return

            }

            if(share == null){
                share = SharePref.getInstance(applicationContext)
            }

            val pDialog = SweetAlertDialog(this@Joinclasstrip, SweetAlertDialog.PROGRESS_TYPE)
            pDialog.progressHelper.barColor = Color.parseColor("#A5DC86")
            pDialog.titleText = getString(R.string.loading)
            pDialog.setCancelable(false)
            pDialog.show()

            var url = share!!.urlApi+"/trips/availability/"+tripcode.text.toString()
            //val url = "http://trippr.aprosoftech.com/api/checktrip/availability"
            var access_token = share!!.getVal("access_token")

            val params = JSONObject()
            try {


                params.put("tripcode", tripcode.text.toString())

            } catch (e: JSONException) {
                e.printStackTrace()
            }
            val request = CustomJsonObjectRequestBasicAuth(Request.Method.GET,url,null,
                    Response.Listener{ response->
                        pDialog.dismiss()
                        Log.d("response", response.toString())
                       //var jsonArray = response.getJSONObject("meta").getJSONArray("metadata")
                        try {
                                    if (response.getInt("code") == 200) {

                                        studenttriprome.text = response.getJSONObject("meta").getJSONObject("metadata").getString("trip_name")
                                        schoolname.text = response.getJSONObject("meta").getJSONObject("metadata").getString("description")

                                        password = response.getJSONObject("meta").getJSONObject("metadata").getString("password")
                                        tripid = response.getJSONObject("meta").getJSONObject("metadata").getString("id")

                                       //tripmembership = jsonArray.getJSONObject(0).getString("membership")

                                        tripdetail = response.getJSONObject("meta").getJSONObject("metadata").toString()

                                        SweetAlertDialog(this@Joinclasstrip, SweetAlertDialog.SUCCESS_TYPE)
                                                .setTitleText(getString(R.string.congrats))
                                                .setContentText(getString(R.string.tripcodeverified))
                                                .setConfirmText("ok")
                                                .show()

                                        tripinfo.visibility = View.VISIBLE

                                    } else {
                                        Snackbar.make(view, R.string.invalidcode, Snackbar.LENGTH_LONG).show()

                                    }


                                } catch (e: JSONException) {
                                    e.printStackTrace()
                                }

                    },
                    Response.ErrorListener{
                        pDialog.dismiss()
                        var json = JSONObject (String(it.networkResponse.data , Charset.defaultCharset()))
                        SweetAlertDialog(this@Joinclasstrip, SweetAlertDialog.ERROR_TYPE)
                                .setTitleText(json.getString("code"))
                                .setContentText(json.getString("title"))
                                .show()
                    },
                    access_token
            )

            VolleySingleton.getInstance(this).addToRequestQueue(request)


        }

        if (view.id == R.id.join) {

            if (password.equals("", ignoreCase = true)) {
                Snackbar.make(view, R.string.entertripcode, Snackbar.LENGTH_LONG).show()
                return
            }

            if (password == trippassword.text.toString()) {

                val pDialog = SweetAlertDialog(this@Joinclasstrip, SweetAlertDialog.PROGRESS_TYPE)
                pDialog.progressHelper.barColor = Color.parseColor("#A5DC86")
                pDialog.titleText = getString(R.string.loading)
                pDialog.setCancelable(false)
                pDialog.show()

                var request_url = share!!.urlApi
                var access_token = share!!.getVal("access_token")

                var userid  = share!!.getVal("userid")
                //val url = "http://trippr.aprosoftech.com/api/Addjoinedtrips/joinedtrips"
                val params = JSONObject()
                try {


                    val c1 = Calendar.getInstance()

                    Log.d("date", "" + c1.get(Calendar.DATE))

                    val mYear1 = c1.get(Calendar.YEAR)
                    val mMonth1 = c1.get(Calendar.MONTH) + 1
                    val mDay1 = c1.get(Calendar.DAY_OF_MONTH)
                    val strDate = "$mDay1-$mMonth1-$mYear1"

//                    val editor = getSharedPreferences("userid", Context.MODE_PRIVATE)
                    logintype = share!!.getVal("membership")
//                    if (logintype.equals("", ignoreCase = true)) {
//                        usertype = "student"
//                    } else {
//                        usertype = "teacher"
//                    }

                    params.put("trip_id", tripid)
                    params.put("user_id", userid)
                    params.put("password",password)

//                    params.put("tripid", tripid)
//                    params.put("userid", userid)
//                    params.put("date", strDate)
//                    params.put("usertype", usertype)
//                    params.put("membership", tripmembership)

                } catch (e: JSONException) {
                    e.printStackTrace()
                }
                val request = CustomJsonObjectRequestBasicAuth(Request.Method.POST,request_url+"/trips/join_trip",params,
                        Response.Listener{ response->
                            Log.d("response", response.toString())
                            SweetAlertDialog(this@Joinclasstrip, SweetAlertDialog.SUCCESS_TYPE)
                                                    .setTitleText(getString(R.string.congrats))
                                                    .setContentText(getString(R.string.joinedtrip))
                                                    .setConfirmText("ok")
                                                    .setConfirmClickListener { sweetAlertDialog ->
                                                        sweetAlertDialog.dismiss()

                                                        if (logintype.equals("student", ignoreCase = true)) {
                                                            val intent = Intent(this@Joinclasstrip, Studenttripoverview::class.java)
                                                            intent.putExtra("tripdetail", tripdetail)
                                                            startActivity(intent)
                                                        } else {
                                                            val intent = Intent(this@Joinclasstrip, Alltrips::class.java)
                                                            //    intent.putExtra("tripdetail", tripdetail);
                                                            startActivity(intent)
                                                        }
                                                    }
                                                    .show()

                        },
                        Response.ErrorListener{
                            Log.d("error", it.toString())
                           var json = JSONObject (String(it.networkResponse.data , Charset.defaultCharset()))
                            pDialog.dismiss()
                            SweetAlertDialog(this@Joinclasstrip, SweetAlertDialog.ERROR_TYPE)
                                    .setTitleText(json.getString("title"))
                                    .setContentText(json.getString("description"))
                                    .show()
                        },
                        access_token
                )

                VolleySingleton.getInstance(this).addToRequestQueue(request)

//                Volley.newRequestQueue(this@Joinclasstrip)
//                        .add(object : JsonRequest<JSONArray>(Request.Method.POST,
//                                url,
//                                params.toString(),
//                                Response.Listener { jsonArray ->
//                                    pDialog.dismiss()
//
//
//                                    Log.d("response", jsonArray.toString())
//
//
//                                    try {
//                                        if (jsonArray.getJSONObject(0).getString("Msg").equals("success", ignoreCase = true)) {
//
//                                            SweetAlertDialog(this@Joinclasstrip, SweetAlertDialog.SUCCESS_TYPE)
//                                                    .setTitleText(getString(R.string.congrats))
//                                                    .setContentText(getString(R.string.joinedtrip))
//                                                    .setConfirmText("ok")
//                                                    .setConfirmClickListener { sweetAlertDialog ->
//                                                        sweetAlertDialog.dismiss()
//
//                                                        if (usertype.equals("student", ignoreCase = true)) {
//                                                            val intent = Intent(this@Joinclasstrip, Studenttripoverview::class.java)
//                                                            intent.putExtra("tripdetail", tripdetail)
//                                                            startActivity(intent)
//                                                        } else {
//                                                            val intent = Intent(this@Joinclasstrip, Alltrips::class.java)
//                                                            //    intent.putExtra("tripdetail", tripdetail);
//                                                            startActivity(intent)
//                                                        }
//                                                    }
//                                                    .show()
//
//                                        } else if (jsonArray.getJSONObject(0).getString("Msg").equals("No more seat", ignoreCase = true)) {
//                                            SweetAlertDialog(this@Joinclasstrip, SweetAlertDialog.ERROR_TYPE)
//                                                    .setTitleText(getString(R.string.oops))
//                                                    .setContentText(getString(R.string.tripfull))
//                                                    .setConfirmText("ok")
//                                                    .show()
//                                        } else {
//
//                                            SweetAlertDialog(this@Joinclasstrip, SweetAlertDialog.ERROR_TYPE)
//                                                    .setTitleText(getString(R.string.oops))
//                                                    .setContentText(getString(R.string.alreadyjoined))
//                                                    .setConfirmText("ok")
//                                                    .show()
//                                        }
//                                    } catch (e: JSONException) {
//                                        e.printStackTrace()
//                                    }
//                                }, Response.ErrorListener { volleyError ->
//                            Log.d("error", volleyError.toString())
//
//                            pDialog.dismiss()
//
//                            SweetAlertDialog(this@Joinclasstrip, SweetAlertDialog.ERROR_TYPE)
//                                    .setTitleText(getString(R.string.oops))
//                                    .setContentText(getString(R.string.checkconnection))
//                                    .show()
//                        }) {
//
//
//                            override fun parseNetworkResponse(networkResponse: NetworkResponse): Response<JSONArray> {
//
//
//                                try {
////                                    val jsonString = String(networkResponse.data,
////                                            HttpHeaderParser
////                                                    .parseCharset(networkResponse.headers))
//                                   var jsonString = String(networkResponse.data, Charset.defaultCharset())
//                                    return Response.success(JSONArray(jsonString),
//                                            HttpHeaderParser
//                                                    .parseCacheHeaders(networkResponse))
//                                } catch (e: UnsupportedEncodingException) {
//                                    return Response.error(ParseError(e))
//                                } catch (je: JSONException) {
//                                    return Response.error(ParseError(je))
//                                }
//
//                            }
//                        }
//                        )


            } else {
                Snackbar.make(view, R.string.invalidpassword, Snackbar.LENGTH_LONG).show()

            }

        }

    }
}
