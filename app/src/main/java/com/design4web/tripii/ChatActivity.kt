package com.design4web.tripii

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageButton
import android.widget.ListView
import android.widget.TextView
import cn.pedant.SweetAlert.SweetAlertDialog
import com.android.volley.NetworkResponse
import com.android.volley.ParseError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.HttpHeaderParser
import com.android.volley.toolbox.ImageLoader
import com.android.volley.toolbox.JsonRequest
import com.android.volley.toolbox.Volley
import com.design4web.tripii.common.ApiCall
import okhttp3.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.nio.charset.Charset
import java.util.concurrent.TimeUnit

class ChatActivity : AppCompatActivity() {

    internal lateinit var listView: ListView
    internal lateinit var sendby: String
    internal var share :SharePref? = null
    internal var access_token :String? = null
    internal var url :String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        if(share == null){
            share = SharePref.getInstance(applicationContext)
        }

        val actionBar = supportActionBar
        actionBar!!.hide()


        listView = findViewById(R.id.listview)

//        val editor = getSharedPreferences("userid", MODE_PRIVATE)
        sendby = share!!.getVal("userid")



        url = "${share!!.urlApi}/chat/usermessages"

        access_token = share!!.getVal("access_token")

        allusers()

    }

    fun allusers() {
        val pDialog = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
        pDialog.progressHelper.barColor = Color.parseColor("#A5DC86")
        pDialog.titleText = getString(R.string.loading)
        pDialog.setCancelable(false)
        pDialog.show()


        //val requestQueue = Volley.newRequestQueue(this@ChatActivity)
        //val url = "http://trippr.aprosoftech.com/api/showusers/chatuser"


//        val params = JSONObject()
//        try {
//
//
//            params.put("send_to", sendby.toInt())
//
//        } catch (e: JSONException) {
//            e.printStackTrace()
//        }
        val JSON = MediaType.parse("application/json")

        val client = OkHttpClient()
//                .connectTimeout(10, TimeUnit.SECONDS)
//                .writeTimeout(10, TimeUnit.SECONDS)
//                .readTimeout(30, TimeUnit.SECONDS)
                //.build()

        var json = JSONObject()
                json.put("send_to",sendby)

        val body = RequestBody.create(JSON, json.toString())

        var request = okhttp3.Request.Builder()
                .url(url!!)
                .post(body)
                .header("Authorization", "Bearer " + access_token)
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .build()


        try {

            var response = client.newCall(request).execute()
            println(response.message())
        } catch (e: Exception) {

            e.printStackTrace()
        }finally {
            pDialog.dismiss()
        }

//
//        Volley.newRequestQueue(this@ChatActivity)
//                .add(object : JsonRequest<JSONObject>(Request.Method.POST,
//                        "http://tripii.design4web.dk/api/chat/usermessages",
//                       params.toString(),
//                        Response.Listener { res ->
//                            pDialog.dismiss()
//                            var jsonArray = res.getJSONObject("meta").getJSONArray("metadata")
//                            Log.d("teacherocationresponse", jsonArray.toString())
//
//                            val commentsAdapter = AllusersAdapter(this@ChatActivity, jsonArray)
//                            listView.adapter = commentsAdapter
//                        },
//                        Response.ErrorListener { volleyError ->
//
//                    Log.d("error", volleyError.toString())
//                    pDialog.dismiss()
//                }) {
//
//
//                    override fun parseNetworkResponse(networkResponse: NetworkResponse): Response<JSONObject> {
//
//
//                        try {
//                           var jsonString = String(networkResponse.data, Charset.defaultCharset())
//                            return Response.success(JSONObject(jsonString),
//                                    HttpHeaderParser
//                                            .parseCacheHeaders(networkResponse))
//                        } catch (e: UnsupportedEncodingException) {
//                            return Response.error(ParseError(e))
//                        } catch (je: JSONException) {
//                            return Response.error(ParseError(je))
//                        }
//
//                    }
//
//                    override fun getHeaders(): MutableMap<String, String> {
//                        var  params =  HashMap<String, String>()
//                        params.put("Accept","application/json")
//                        params.put("Authorization","Bearer "+ access_token)
//                        return params
//                    }
//                 }
//                )


    }

    internal class postApi(context:Context) :AsyncTask<Void,Void,String>(){

        override fun doInBackground(vararg p0: Void?): String {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }
        fun fetchMessage(): okhttp3.Response? {
            var response :okhttp3.Response? =null
            try {
                val JSON = MediaType.parse("application/json")
                val client = OkHttpClient()
                var json = JSONObject()
                json.put("send_to",sendby)

                val body = RequestBody.create(JSON, json.toString())

               var response = ApiCall.POST(
                        client,
                       url,
                       body)

                Log.d("Response", response.toString());
            } catch (e: IOException) {
                e.printStackTrace()
            }

            return response
        }
    }


}



internal class AllusersAdapter(var context: Activity, var jsonArray: JSONArray) : BaseAdapter() {
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
            v = inflater.inflate(R.layout.chatuserlistxml, null, true)
        }

        val username = v!!.findViewById(R.id.username) as TextView
        val next = v.findViewById(R.id.next) as ImageButton
        //  NetworkImageView userimage = (NetworkImageView) v.findViewById(R.id.userimage);

        //val editor = context.getSharedPreferences("userid", MODE_PRIVATE)
        var share = SharePref.getInstance(context)

        stusername = share.getVal("username")
        stuserid = share.getVal("userid")




        try {

//            if (stusername.equals(jsonArray.getJSONObject(i).getString("sendbyname"), ignoreCase = true)) {
//                username.text = jsonArray.getJSONObject(i).getString("sendtoname")
//            } else {
                username.text = jsonArray.getJSONObject(i).getString("send_by_name")
          //  }
        } catch (e: JSONException) {
            e.printStackTrace()
        }


        next.tag = i

        next.setOnClickListener { view ->
            Log.d("mao ni",view.tag.toString() )
            try {
                if (stuserid.equals(jsonArray.getJSONObject(view.tag as Int).get("send_by").toString(), ignoreCase = true)) {
                    val intent = Intent(context.applicationContext, Userchat::class.java)
                    intent.putExtra("sendto", jsonArray.getJSONObject(view.tag as Int).get("send_to").toString())
                    intent.putExtra("sendtoname", jsonArray.getJSONObject(view.tag as Int).get("send_to_name").toString())
                    context.startActivity(intent)
                } else {
                    val intent = Intent(context, Userchat::class.java)
                    intent.putExtra("sendto", jsonArray.getJSONObject(view.tag as Int).get("send_by").toString())
                    intent.putExtra("sendtoname", jsonArray.getJSONObject(view.tag as Int).get("send_to_name").toString())

                    context.startActivity(intent)
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }


        return v

    }
}
