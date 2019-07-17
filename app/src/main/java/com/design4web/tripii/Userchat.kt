package com.design4web.tripii

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import cn.pedant.SweetAlert.SweetAlertDialog
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.ImageLoader
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.nio.charset.Charset
import java.util.*

class Userchat : AppCompatActivity() {

    internal lateinit var listView: ListView
    internal lateinit var username: TextView
    internal lateinit var sendto: String
    internal lateinit var sendby: String
    internal lateinit var sendbyname: String
    internal lateinit var imageurl: String
    internal lateinit var tripid: String
    internal lateinit var writecomment: EditText
    internal lateinit var submitcomment: ImageButton
    internal  var share : SharePref? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_userchat)

        val actionBar = supportActionBar
        actionBar!!.hide()
        if(share == null ) {
            share = SharePref.getInstance(applicationContext)
        }

        listView = findViewById(R.id.listview)
        username = findViewById(R.id.username) as TextView


        writecomment = findViewById(R.id.writecomment) as EditText
        submitcomment = findViewById(R.id.submitcomment) as ImageButton


        sendto = intent.extras.getString("sendto")
        username.text = intent.extras.getString("sendtoname")

//        val editor = getSharedPreferences("userid", MODE_PRIVATE)

        sendby = share!!.getVal("userid")


        sendbyname = share!!.getVal("username")

        imageurl = share!!.getVal("imageurl")



        Log.d("id sendby sendto", "$sendby $sendto")

        allmessages()


        submitcomment.setOnClickListener(View.OnClickListener { view ->
            //                SharedPreferences editor=getSharedPreferences("userid",MODE_PRIVATE);
            //
            //                try {
            //                    sendto = jsonArray.getJSONObject((Integer) view.getTag()).getString("userid");
            //                    tripid = jsonArray.getJSONObject((Integer) view.getTag()).getString("tripid");
            //                    sendby =  editor.getString("userid","");
            //                    imageurl =  editor.getString("imageurl","");
            //                    name =  editor.getString("username","");
            //                    sendbyname =  editor.getString("username","");
            //                    sendtoname =  jsonArray.getJSONObject((Integer) view.getTag()).getString("name");
            //                } catch (JSONException e) {
            //                    e.printStackTrace();
            //                }


            if (writecomment.text.toString().equals("", ignoreCase = true)) {
                Snackbar.make(view, R.string.fillallthefields, Snackbar.LENGTH_LONG).show()
                return@OnClickListener
            }


            val c1 = Calendar.getInstance()

            Log.d("date", "" + c1.get(Calendar.DATE))

            val mYear1 = c1.get(Calendar.YEAR)
            val mMonth1 = c1.get(Calendar.MONTH) + 1
            val mDay1 = c1.get(Calendar.DAY_OF_MONTH)
            val strDate = "$mDay1-$mMonth1-$mYear1"


            val pDialog = SweetAlertDialog(this@Userchat, SweetAlertDialog.PROGRESS_TYPE)
            pDialog.progressHelper.barColor = Color.parseColor("#A5DC86")
            pDialog.titleText = this@Userchat.getString(R.string.loading)
            pDialog.setCancelable(false)
            pDialog.show()

            var userid  = share!!.getVal("userid")
            var request_url = share!!.urlApi
            var access_token = share!!.getVal("access_token")


//            val url = "http://trippr.aprosoftech.com/api/sendmessages/insertmessage"
            val params = JSONObject()
            try {


                params.put("trip_id", tripid)
//                params.put("sendby", sendby)
//                params.put("username", sendbyname)
//                params.put("imageurl", imageurl)
//                params.put("senddate", strDate)
//                params.put("sendto", sendto)
//                params.put("sendbyname", sendbyname)
//                params.put("sendtoname", username)

                params.put("message", writecomment.text.toString())

            } catch (e: JSONException) {
                e.printStackTrace()
            }

            val request = CustomJsonObjectRequestBasicAuth(Request.Method.POST,request_url+"/chat/create/"+sendto,params,
                    Response.Listener{ response ->
                        pDialog.dismiss()

                        Log.d("userchat response", response.toString())

                        writecomment.setText("")
                                allmessages()


                    },
                    Response.ErrorListener{

                        var json = JSONObject (String(it.networkResponse.data , Charset.defaultCharset()))
                        SweetAlertDialog(this@Userchat, SweetAlertDialog.ERROR_TYPE)
                                .setTitleText(json.getString("title"))
                                .setContentText(json.getString("description"))
                                .setConfirmClickListener { sweetAlertDialog ->
                                    sweetAlertDialog.dismiss()
                                }
                                .show()
                    },
                    access_token
            )

            VolleySingleton.getInstance(this).addToRequestQueue(request)

//            Volley.newRequestQueue(this@Userchat)
//                    .add(object : JsonRequest<JSONArray>(Request.Method.POST,
//                            url,
//                            params.toString(),
//                            Response.Listener { jsonArray ->
//                                pDialog.dismiss()
//
//
//                                Log.d("response", jsonArray.toString())
//
//
//
//
//
//                                writecomment.setText("")
//
//                                allmessages()
//
//
//                                //
//                                //                                             new SweetAlertDialog(Userchat.this, SweetAlertDialog.SUCCESS_TYPE)
//                                //                                                     .setTitleText(Userchat.this.getString(R.string.congrats))
//                                //                                                     .setContentText(Userchat.this.getString(R.string.messagesent))
//                                //                                                     .show();
//                                //
//                                //                                             pDialog.dismiss();
//                            }, Response.ErrorListener { volleyError ->
//                        Log.d("error", volleyError.toString())
//
//                        pDialog.dismiss()
//
//                        SweetAlertDialog(this@Userchat, SweetAlertDialog.ERROR_TYPE)
//                                .setTitleText(this@Userchat.getString(R.string.oops))
//                                .setContentText(this@Userchat.getString(R.string.checkconnection))
//                                .show()
//                    }) {
//
//
//                        override fun parseNetworkResponse(networkResponse: NetworkResponse): Response<JSONArray> {
//
//                            try {
//                               var jsonString = String(networkResponse.data, Charset.defaultCharset())
//                                return Response.success(JSONArray(jsonString),
//                                        HttpHeaderParser
//                                                .parseCacheHeaders(networkResponse))
//                            } catch (e: UnsupportedEncodingException) {
//                                return Response.error(ParseError(e))
//                            } catch (je: JSONException) {
//                                return Response.error(ParseError(je))
//                            }
//
//                        }
//                    }
//                    )

        })


    }


    fun allmessages() {
        val pDialog = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
        pDialog.progressHelper.barColor = Color.parseColor("#A5DC86")
        pDialog.titleText = getString(R.string.loading)
        pDialog.setCancelable(false)
        pDialog.show()


        var userid  = share!!.getVal("userid")
        var request_url = share!!.urlApi
        var access_token = share!!.getVal("access_token")


        val params = JSONObject()
        try {


            params.put("sendby", sendby)
            params.put("sendto", sendto)

        } catch (e: JSONException) {
            e.printStackTrace()
        }

        val request = CustomJsonObjectRequestBasicAuth(Request.Method.GET,request_url+"/chat/userMessages/"+userid,null,
                Response.Listener{ response ->
                    pDialog.dismiss()

                            Log.d("userchat response", response.toString())
                    var jsonArray = response.getJSONObject("meta").getJSONArray("metadata")
                            val commentsAdapter = AllmessagesAdapter(this@Userchat, jsonArray)
                            listView.adapter = commentsAdapter

                },
                Response.ErrorListener{

                    var json = JSONObject (String(it.networkResponse.data , Charset.defaultCharset()))
                    SweetAlertDialog(this@Userchat, SweetAlertDialog.ERROR_TYPE)
                            .setTitleText(json.getString("title"))
                            .setContentText(json.getString("description"))
                            .setConfirmClickListener { sweetAlertDialog ->
                                sweetAlertDialog.dismiss()
                            }
                            .show()
                },
                access_token
        )

        VolleySingleton.getInstance(this).addToRequestQueue(request)

//        Volley.newRequestQueue(this@Userchat)
//                .add(object : JsonRequest<JSONArray>(Request.Method.POST,
//                        url,
//                        params.toString(),
//                        Response.Listener { jsonArray ->
//                            pDialog.dismiss()
//
//                            Log.d("userchat response", jsonArray.toString())
//
//                            val commentsAdapter = AllmessagesAdapter(this@Userchat, jsonArray)
//                            listView.adapter = commentsAdapter
//
//
//                            try {
//                                tripid = jsonArray.getJSONObject(0).getString("tripid")
//                            } catch (e: JSONException) {
//                                e.printStackTrace()
//                            }
//                        }, Response.ErrorListener { volleyError ->
//                    Log.d("error", volleyError.toString())
//                    pDialog.dismiss()
//                }) {
//
//
//                    override fun parseNetworkResponse(networkResponse: NetworkResponse): Response<JSONArray> {
//
//
//                        try {
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


internal class AllmessagesAdapter(var context: Activity, var jsonArray: JSONArray) : BaseAdapter() {
    var imageLoader: ImageLoader? = null
    var sendto: String? = null
    var tripid: String? = null
    var name: String? = null
    var sendby: String? = null
    var imageurl: String? = null
    var stusername: String? = null
    var stuserid: String? = null


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
            v = inflater.inflate(R.layout.messagesxml, null, true)
        }

        val sendbymessage = v!!.findViewById(R.id.sendbymessage) as TextView
        val sendtomessage = v.findViewById(R.id.sendtomessage) as TextView

        //  NetworkImageView userimage = (NetworkImageView) v.findViewById(R.id.userimage);

//        val editor = context.getSharedPreferences("userid", MODE_PRIVATE)
//        if(sharedpref == null){
//            sharedpref = SharePref.getInstance
//        }
//
//        sendby = editor.getString("userid", "")



        try {

//            if (sendby.equals(jsonArray.getJSONObject(i).getString("send_by"), ignoreCase = true)) {
//                sendbymessage.text = jsonArray.getJSONObject(i).getString("message")
//            } else {
                sendtomessage.text = jsonArray.getJSONObject(i).getString("message")
            //}
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        return v

    }
}
