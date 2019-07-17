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
import java.nio.charset.Charset

class Alltrips : AppCompatActivity() {

    internal lateinit var listView: ListView
    internal var share :SharePref?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alltrips)

        val actionBar = supportActionBar
        actionBar!!.hide()

        listView = findViewById(R.id.listview) as ListView


        val pDialog = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
        pDialog.progressHelper.barColor = Color.parseColor("#A5DC86")
        pDialog.titleText = getString(R.string.loading)
        pDialog.setCancelable(false)
        pDialog.show()

        if(share == null) {
            share = SharePref.getInstance(applicationContext)
        }

        var userid  = share!!.getVal("userid")
        var request_url = share!!.urlApi
        var access_token = share!!.getVal("access_token")

        //val requestQueue = Volley.newRequestQueue(this@Alltrips)
        //val url = "http://trippr.aprosoftech.com/api/Trips/SHowTrips"
        val url = request_url+"/trips/show_trips/"+userid

        val params = JSONObject()
        try {


            val editor = getSharedPreferences("userid", MODE_PRIVATE)
            val userid = editor.getString("userid", "")

            params.put("userid", userid)

        } catch (e: JSONException) {
            e.printStackTrace()
        }

        val request = CustomJsonObjectRequestBasicAuth(Request.Method.GET,url,null,
                Response.Listener{ response->
                    pDialog.dismiss()
                    Log.d("response", response.toString())
                    var jsonArray = response.getJSONObject("meta").getJSONArray("metadata")
                    //                            pDialog.dismiss()
                    val alltripsAdapter = AlltripsAdapter(this@Alltrips, jsonArray)
                            listView.adapter = alltripsAdapter

                },
                Response.ErrorListener{
                    pDialog.dismiss()
                    var json = JSONObject (String(it.networkResponse.data , Charset.defaultCharset()))
                    SweetAlertDialog(this@Alltrips, SweetAlertDialog.ERROR_TYPE)
                            .setTitleText(json.getString("title"))
                            .setContentText(json.getString("description"))
                            .show()
                },
                access_token
        )

        VolleySingleton.getInstance(this).addToRequestQueue(request)

    }


}


internal class AlltripsAdapter(var context: Activity, var jsonArray: JSONArray) : BaseAdapter() {
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
            val intent = Intent(context, Tripcomment::class.java)
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
