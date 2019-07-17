package com.design4web.tripii

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import cn.pedant.SweetAlert.SweetAlertDialog
import com.android.volley.NetworkResponse
import com.android.volley.ParseError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.HttpHeaderParser
import com.android.volley.toolbox.ImageLoader
import com.android.volley.toolbox.JsonRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.UnsupportedEncodingException
import java.nio.charset.Charset
import java.util.*

class Tripoverview : AppCompatActivity() {

    internal lateinit var tripdetail: String
    internal lateinit var tripid: String
    internal lateinit var membership: String

    internal lateinit var description: EditText
    internal lateinit var jointrip: Button
    internal lateinit var listView: ListView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tripoverview)

        val actionBar = supportActionBar
        actionBar!!.hide()

        tripdetail = intent.extras.getString("tripdetail")



        try {
            val jsonObject = JSONObject(tripdetail)

            description = findViewById(R.id.description) as EditText
            jointrip = findViewById(R.id.jointrip) as Button
            listView = findViewById(R.id.listview) as ListView

            description.setText(jsonObject.getString("description"))
            tripid = jsonObject.getString("tripid")
            membership = jsonObject.getString("membership")

            allcomments()



            jointrip.setOnClickListener {
                val pDialog = SweetAlertDialog(this@Tripoverview, SweetAlertDialog.PROGRESS_TYPE)
                pDialog.progressHelper.barColor = Color.parseColor("#A5DC86")
                pDialog.titleText = getString(R.string.loading)
                pDialog.setCancelable(false)
                pDialog.show()


                val url = "http://trippr.aprosoftech.com/api/Addjoinedtrips/joinedtrips"
                val params = JSONObject()
                try {


                    val c1 = Calendar.getInstance()

                    Log.d("date", "" + c1.get(Calendar.DATE))

                    val mYear1 = c1.get(Calendar.YEAR)
                    val mMonth1 = c1.get(Calendar.MONTH) + 1
                    val mDay1 = c1.get(Calendar.DAY_OF_MONTH)
                    val strDate = "$mDay1-$mMonth1-$mYear1"

                    val editor = getSharedPreferences("userid", Context.MODE_PRIVATE)
                    val userid = editor.getString("userid", "")


                    params.put("tripid", tripid)
                    params.put("userid", userid)
                    params.put("date", strDate)
                    params.put("usertype", "teacher")
                    params.put("membership", membership)

                } catch (e: JSONException) {
                    e.printStackTrace()
                }

                Volley.newRequestQueue(this@Tripoverview)
                        .add(object : JsonRequest<JSONArray>(Request.Method.POST,
                                url,
                                params.toString(),
                                Response.Listener { jsonArray ->
                                    pDialog.dismiss()


                                    Log.d("response", jsonArray.toString())



                                    try {
                                        if (jsonArray.getJSONObject(0).getString("Msg").equals("success", ignoreCase = true)) {

                                            SweetAlertDialog(this@Tripoverview, SweetAlertDialog.SUCCESS_TYPE)
                                                    .setTitleText(getString(R.string.congrats))
                                                    .setContentText(getString(R.string.joinedtrip))
                                                    .setConfirmText("ok")
                                                    .setConfirmClickListener { sweetAlertDialog ->
                                                        sweetAlertDialog.dismiss()
                                                        val intent = Intent(this@Tripoverview, Tripcomment::class.java)
                                                        intent.putExtra("tripdetail", tripdetail)
                                                        startActivity(intent)
                                                    }
                                                    .show()

                                        } else if (jsonArray.getJSONObject(0).getString("Msg").equals("No more seat", ignoreCase = true)) {
                                            SweetAlertDialog(this@Tripoverview, SweetAlertDialog.ERROR_TYPE)
                                                    .setTitleText(getString(R.string.oops))
                                                    .setContentText(getString(R.string.tripfull))
                                                    .setConfirmText("ok")
                                                    .show()
                                        } else {

                                            SweetAlertDialog(this@Tripoverview, SweetAlertDialog.ERROR_TYPE)
                                                    .setTitleText(getString(R.string.oops))
                                                    .setContentText(getString(R.string.alreadyjoined))
                                                    .setConfirmText("ok")
                                                    .show()
                                        }
                                    } catch (e: JSONException) {
                                        e.printStackTrace()
                                    }


                                    //                                                 new SweetAlertDialog(Tripoverview.this, SweetAlertDialog.SUCCESS_TYPE)
                                    //                                                         .setTitleText(getString(R.string.congrats))
                                    //                                                         .setContentText(getString(R.string.joinedtrip))
                                    //                                                         .setConfirmText("ok")
                                    //                                                         .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                    //                                                             @Override
                                    //                                                             public void onClick(SweetAlertDialog sweetAlertDialog) {
                                    //
                                    //                                                                 sweetAlertDialog.dismiss();
                                    //                                                                 Intent intent = new Intent(Tripoverview.this,Tripcomment.class);
                                    //                                                                  intent.putExtra("tripdetail",tripdetail);
                                    //                                                                 startActivity(intent);
                                    //                                                             }
                                    //                                                         })
                                    //                                                         .show();
                                }, Response.ErrorListener { volleyError ->
                            Log.d("error", volleyError.toString())

                            pDialog.dismiss()

                            SweetAlertDialog(this@Tripoverview, SweetAlertDialog.ERROR_TYPE)
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
                        }
                        )
            }


        } catch (e: JSONException) {
            e.printStackTrace()
        }


    }

    private fun allcomments() {

        val pDialog = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
        pDialog.progressHelper.barColor = Color.parseColor("#A5DC86")
        pDialog.titleText = getString(R.string.loading)
        pDialog.setCancelable(false)
        pDialog.show()


        val requestQueue = Volley.newRequestQueue(this@Tripoverview)
        val url = "http://trippr.aprosoftech.com/api/comments/showcomment"


        val params = JSONObject()
        try {

            params.put("tripid", tripid)

        } catch (e: JSONException) {
            e.printStackTrace()
        }

        Volley.newRequestQueue(this@Tripoverview)
                .add(object : JsonRequest<JSONArray>(Request.Method.POST,
                        url,
                        params.toString(),
                        Response.Listener { jsonArray ->
                            pDialog.dismiss()

                            Log.d("response", jsonArray.toString())

                            val commentsAdapter = CommentsAdapter(this@Tripoverview, jsonArray)
                            listView.adapter = commentsAdapter
                        }, Response.ErrorListener { volleyError ->
                    Log.d("error", volleyError.toString())
                    pDialog.dismiss()
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
                }
                )


    }
}


class CommentsAdapter(var context: Activity, var jsonArray: JSONArray) : BaseAdapter() {
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
            v = inflater.inflate(R.layout.tripoverviewcomment, null, true)
        }

        val username = v!!.findViewById(R.id.username) as TextView
        val comment = v.findViewById(R.id.comment) as TextView

        try {
            username.text = jsonArray.getJSONObject(i).getString("user_name")
            comment.text = jsonArray.getJSONObject(i).getString("comment")

        } catch (e: JSONException) {
            e.printStackTrace()
        }



        return v

    }
}
