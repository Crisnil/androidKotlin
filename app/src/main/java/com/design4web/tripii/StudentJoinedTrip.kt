package com.design4web.tripii

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import cn.pedant.SweetAlert.SweetAlertDialog
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.ImageLoader
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class StudentJoinedTrip : AppCompatActivity() {

    internal lateinit var listView: ListView
    internal var share:SharePref?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_joined_trip)

        if(share == null) {
            share = SharePref.getInstance(applicationContext)
        }
        val actionBar = supportActionBar
        actionBar!!.hide()

        listView = findViewById(R.id.listview) as ListView


        val pDialog = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
        pDialog.progressHelper.barColor = Color.parseColor("#A5DC86")
        pDialog.titleText = getString(R.string.loading)
        pDialog.setCancelable(false)
        pDialog.show()

        var userid  = share!!.getVal("userid")
        var request_url = share!!.urlApi
        var access_token = share!!.getVal("access_token")
        val url = request_url+"/trips/show_trips/"+userid
        //val url = "http://trippr.aprosoftech.com/api/studentjoinedtrips/alljointrips"
        val params = JSONObject()
        try {

            params.put("userid", userid)

        } catch (e: JSONException) {
            e.printStackTrace()
        }

        val request = CustomJsonObjectRequestBasicAuth(Request.Method.GET,url,null,
                Response.Listener{ response->
                    pDialog.dismiss()
                    Log.d("response trips", response.toString())
                    var jsonArray = response.getJSONObject("meta").getJSONArray("metadata")
                    //                            pDialog.dismiss()
                    val alltripsAdapter = JointripsAdapter(this@StudentJoinedTrip, jsonArray)
                    listView.adapter = alltripsAdapter

                },
                Response.ErrorListener{
                    Log.d("errresponse", it.toString())
                    pDialog.dismiss()
//
//                    var errorresponse = JSONObject(it.toString())
//                    var mess = errorresponse.getString("title")
                    Log.d("errresponse", it.toString())
                    SweetAlertDialog(this@StudentJoinedTrip, SweetAlertDialog.WARNING_TYPE)
                            .setTitleText(this@StudentJoinedTrip.getString(R.string.oops))
                            .setContentText("No Trips are found")
                            .show()
                },
                access_token
        )

        VolleySingleton.getInstance(this).addToRequestQueue(request)

//        Volley.newRequestQueue(this@StudentJoinedTrip)
//                .add(object : JsonRequest<JSONArray>(Request.Method.POST,
//                        url,
//                        params.toString(),
//                        Response.Listener { jsonArray ->
//                            pDialog.dismiss()
//
//                            Log.d("response", jsonArray.toString())
//                            val alltripsAdapter = JointripsAdapter(this@StudentJoinedTrip, jsonArray)
//                            listView.adapter = alltripsAdapter
//                        }, Response.ErrorListener { volleyError ->
//                    Log.d("error", volleyError.toString())
//                    pDialog.dismiss()
//
//                    SweetAlertDialog(this@StudentJoinedTrip, SweetAlertDialog.ERROR_TYPE)
//                            .setTitleText(getString(R.string.oops))
//                            .setContentText(getString(R.string.checkconnection))
//                            .show()
//                }) {
//
//
//                    override fun parseNetworkResponse(networkResponse: NetworkResponse): Response<JSONArray> {
//
//
//                        try {
////                            val jsonString = String(networkResponse.data,
////                                    HttpHeaderParser
////                                            .parseCharset(networkResponse.headers))
//                           var jsonString = String(networkResponse.data, Charset.defaultCharset())
//                            return Response.success(JSONArray(jsonString),
//                                    HttpHeaderParser
//                                            .parseCacheHeaders(networkResponse))
//                        } catch (e: UnsupportedEncodingException) {
//                            return Response.error(ParseError(e))
//                        } catch (je: JSONException) {
//                            return Response.error(ParseError(je))
//                        }
//
//                    }
//                }
//                )


    }
}


internal class JointripsAdapter(var context: Activity, var jsonArray: JSONArray) : BaseAdapter() {
    var imageLoader: ImageLoader? = null

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
            v = inflater.inflate(R.layout.alltripsxml, null, true)
        }

        val tripname = v!!.findViewById(R.id.tripname) as TextView
        val code = v.findViewById(R.id.code) as TextView

        val more = v.findViewById(R.id.more) as Button



        try {
            tripname.text = jsonArray.getJSONObject(i).getJSONObject("trip_details").getString("trip_name")
            code.text = jsonArray.getJSONObject(i).getJSONObject("trip_details").getString("trip_code")

        } catch (e: JSONException) {
            e.printStackTrace()
        }


        more.tag = i

        more.setOnClickListener { view ->
            val intent = Intent(context, Studenttripoverview::class.java)
            try {
                intent.putExtra("tripdetail", jsonArray.getJSONObject(view.tag as Int).toString())
                context.startActivity(intent)
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }

        return v

    }
}

