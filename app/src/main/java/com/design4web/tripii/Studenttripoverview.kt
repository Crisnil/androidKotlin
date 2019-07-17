package com.design4web.tripii

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.*
import cn.pedant.SweetAlert.SweetAlertDialog
import com.android.volley.Request
import com.android.volley.Response
import org.json.JSONException
import org.json.JSONObject
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class Studenttripoverview : AppCompatActivity(), View.OnClickListener {


    internal lateinit var tripdetail: String
    internal lateinit var description: EditText
    internal lateinit var writecomment: EditText
    internal lateinit var submitcomment: ImageButton
    internal lateinit var findteacher: Button
    internal lateinit var listView: ListView
    internal lateinit var tripid: String
    internal lateinit var username: String
    internal lateinit var startdate: String
    internal lateinit var enddate: String
    internal lateinit var duration: TextView
    internal var share :SharePref? =null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_studenttripoverview)

        val actionBar = supportActionBar
        actionBar!!.hide()

        if(share == null){
            share = SharePref.getInstance(applicationContext)
        }
        description = findViewById(R.id.description) as EditText
        writecomment = findViewById(R.id.writecomment) as EditText
        listView = findViewById(R.id.listview) as ListView

        submitcomment = findViewById(R.id.submitcomment) as ImageButton
        findteacher = findViewById(R.id.findteacher) as Button
        duration = findViewById(R.id.duration) as TextView

        tripdetail = intent.extras.getString("tripdetail")
        Log.d("tripdetail",tripdetail)
        try {
            val jsonObject = JSONObject(tripdetail).getJSONObject("trip_details")

            description.setText(jsonObject.getString("description"))
            tripid = jsonObject.getString("id")
            startdate = jsonObject.getString("start_date")
            enddate = jsonObject.getString("end_date")
           // status.setText(jsonObject.getString("status"))
            duration.text = getString(R.string.duration) + " : " + startdate + " " + getString(R.string.to) + " " + enddate


        } catch (e: JSONException) {
            e.printStackTrace()
        }

        allcomments()
        submitcomment.setOnClickListener(this)
        findteacher.setOnClickListener(this)
    }


    private fun allcomments() {


        val pDialog = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
        pDialog.progressHelper.barColor = Color.parseColor("#A5DC86")
        pDialog.titleText = getString(R.string.loading)
        pDialog.setCancelable(false)
        pDialog.show()


        val url = share!!.urlApi+"/comments/trip/"+tripid
        val access_token = share!!.getVal("access_token")

        val params = JSONObject()
        try {

            params.put("trip_id", tripid)

        } catch (e: JSONException) {
            e.printStackTrace()
        }
        val request = CustomJsonObjectRequestBasicAuth(Request.Method.GET,url,null,
                Response.Listener{ response->
                    pDialog.dismiss()

                    var jsonArray = response.getJSONObject("meta").getJSONObject("metadata").getJSONArray("comment")
                    Log.d("response", response.toString())

                    val commentsAdapter = CommentsAdapter(this@Studenttripoverview, jsonArray)
                    listView.adapter = commentsAdapter
                },
                Response.ErrorListener{
                    pDialog.dismiss()
                    Log.d("error response", it.toString())
                },
                access_token
        )

        VolleySingleton.getInstance(this).addToRequestQueue(request)


    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onClick(view: View) {
        if (view.id == R.id.submitcomment) {

            if (writecomment.text.toString().equals("", ignoreCase = true)) {

                Snackbar.make(view, R.string.entercomment, Snackbar.LENGTH_LONG).show()
                return

            }


            val pDialog = SweetAlertDialog(this@Studenttripoverview, SweetAlertDialog.PROGRESS_TYPE)
            pDialog.progressHelper.barColor = Color.parseColor("#A5DC86")
            pDialog.titleText = getString(R.string.loading)
            pDialog.setCancelable(false)
            pDialog.show()


            val url = share!!.urlApi+"/comments/create"
            //val url = "http://trippr.aprosoftech.com/api/Addcomment/comments"
            val access_token = share!!.getVal("access_token")

            val params = JSONObject()
            try {


                val c1 = Calendar.getInstance()

                Log.d("date", "" + c1.get(Calendar.DATE))

                val mYear1 = c1.get(Calendar.YEAR)
                val mMonth1 = c1.get(Calendar.MONTH) + 1
                val mDay1 = c1.get(Calendar.DAY_OF_MONTH)
                val strDate = "$mDay1-$mMonth1-$mYear1"

//                val editor = getSharedPreferences("userid", Context.MODE_PRIVATE)
                val userid = share!!.getVal("userid")

                username = share!!.getVal("username")


                params.put("trip_id", tripid)
                params.put("user_id", userid)
                params.put("date", strDate)
                params.put("usertype", "student")
                params.put("comment", writecomment.text.toString())
                params.put("username", username)

            } catch (e: JSONException) {
                e.printStackTrace()
            }

            val request = CustomJsonObjectRequestBasicAuth(Request.Method.POST,url,params,
                    Response.Listener{ response->
                        pDialog.dismiss()
                        Log.d("response", response.toString())
                        writecomment.setText("")
                        allcomments()
                    },
                    Response.ErrorListener{
                        pDialog.dismiss()
                        Log.d("error response", it.toString())
                    },
                    access_token
            )

            VolleySingleton.getInstance(this).addToRequestQueue(request)


        }

        if (view.id == R.id.findteacher) {

            val df = SimpleDateFormat("dd-MM-yyyy")
            val c1 = Calendar.getInstance()
            var start = Datetryparse().tryParse(startdate)
            Log.d("date", "" + c1.get(Calendar.DATE))

            val mYear1 = c1.get(Calendar.YEAR)
            val mMonth1 = c1.get(Calendar.MONTH) + 1
            val mDay1 = c1.get(Calendar.DAY_OF_MONTH)

            val currentdate = "$mDay1-$mMonth1-$mYear1"
            //comparing date using compareTo method in Java
            println("Comparing two Date in Java using CompareTo method")


            try {
                compareDatesByCompareTo(df.parse(currentdate), start!!)
            } catch (e: ParseException) {
                e.printStackTrace()
            }

        }
    }

    fun compareDatesByCompareTo( currentdate: Date, startdate: Date) {
        //how to check if date1 is equal to date2

        if (currentdate.compareTo(startdate) < 0) {

            Toast.makeText(this@Studenttripoverview, R.string.tripnotstart, Toast.LENGTH_LONG).show()
        } else {

            val df1 = SimpleDateFormat("dd-MM-yyyy")


            val c1 = Calendar.getInstance()

            Log.d("date", "" + c1.get(Calendar.DATE))

            val mYear1 = c1.get(Calendar.YEAR)
            val mMonth1 = c1.get(Calendar.MONTH) + 1
            val mDay1 = c1.get(Calendar.DAY_OF_MONTH)
            val currentdate1 = "$mDay1-$mMonth1-$mYear1"

            var end = Datetryparse().tryParse(enddate)
            try {
                compareDatesByCompareTo1( df1.parse(currentdate1),end!!)
            } catch (e: ParseException) {
                e.printStackTrace()
            }

        }
    }


    fun compareDatesByCompareTo1( currentdate: Date, enddate: Date) {
        //how to check if date1 is equal to date2
        if (currentdate.compareTo(enddate) > 0) {

            Toast.makeText(this@Studenttripoverview, R.string.tripover, Toast.LENGTH_LONG).show()

        } else {
            val intent = Intent(this@Studenttripoverview, Teacherlocation::class.java)
            intent.putExtra("tripid", tripid)
            startActivity(intent)
        }
    }

}
