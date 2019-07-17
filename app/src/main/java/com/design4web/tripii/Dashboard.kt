package com.design4web.tripii

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import cn.pedant.SweetAlert.SweetAlertDialog
import com.android.volley.Request
import com.android.volley.Response
import org.json.JSONException
import org.json.JSONObject
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class Dashboard : AppCompatActivity(), View.OnClickListener {


    internal lateinit var teacherlayout: LinearLayout
    internal lateinit var studentlayout: LinearLayout
    internal lateinit var logout: Button
    internal lateinit var upgrade: Button
    internal lateinit var studentlogout: Button
    internal lateinit var joinclassstrip: Button
    internal lateinit var createtrip: Button
    internal lateinit var seetrip: Button
    internal lateinit var profile: Button
    internal lateinit var messages: Button
    internal lateinit var jointrip: Button
    internal lateinit var joinedtrip: Button
    internal lateinit var studentprofile: Button
    internal lateinit var studentmessages: Button
    internal lateinit var userid: String
    internal lateinit var membership: String
    internal lateinit var usertype: String
    internal var preferences_name = "isFirstTime"
    internal lateinit var enddate: String
    internal lateinit var stlicencecode: String
    internal lateinit var licencecode: TextView
    internal lateinit var currentdate: String
    internal lateinit var trip_id :String
    internal var share : SharePref? =null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        val actionBar = supportActionBar
        actionBar!!.hide()

        if(share == null) {
            share = SharePref.getInstance(applicationContext)
        }
        teacherlayout = findViewById(R.id.teacherlayout) as LinearLayout
        studentlayout = findViewById(R.id.studentlayout) as LinearLayout

        createtrip = findViewById(R.id.createtrip) as Button
        joinclassstrip = findViewById(R.id.joinclasstrip) as Button
        seetrip = findViewById(R.id.seetrip) as Button
        profile = findViewById(R.id.profile) as Button

        studentlogout = findViewById(R.id.studentlogout) as Button
        logout = findViewById(R.id.logout) as Button
        upgrade = findViewById(R.id.upgrade) as Button

        licencecode = findViewById(R.id.licencecode) as TextView
        messages = findViewById(R.id.messages) as Button
        jointrip = findViewById(R.id.jointrip) as Button
        joinedtrip = findViewById(R.id.joinedtrip) as Button
        studentprofile = findViewById(R.id.studentprofile) as Button
        studentmessages = findViewById(R.id.studentmessages) as Button


        userid = share!!.getVal("userid")
        membership = share!!.getVal("membership")
        usertype = share!!.getVal("usertype")
        stlicencecode = share!!.getVal("license")
        enddate = share!!.getVal("enddate")

        Log.d("enddate", enddate)

        if (!stlicencecode.equals("", ignoreCase = true)) {
                       licencecode.setVisibility(View.VISIBLE)
                       licencecode.setText(stlicencecode)
        }


//        if (membership.equals("", ignoreCase = true)) {
//            studentlayout.visibility = View.GONE
//            teacherlayout.visibility = View.VISIBLE
//            logout.visibility = View.VISIBLE
//        }
//

        if (membership.equals("Student", ignoreCase = true)) {
            studentlayout.visibility = View.VISIBLE
            teacherlayout.visibility = View.GONE
            logout.visibility = View.GONE
        }else{
            studentlayout.visibility = View.GONE
            teacherlayout.visibility = View.VISIBLE
            logout.visibility = View.VISIBLE
        }


        createtrip.setOnClickListener(this)
        seetrip.setOnClickListener(this)
        profile.setOnClickListener(this)
        messages.setOnClickListener(this)
        jointrip.setOnClickListener(this)
        joinedtrip.setOnClickListener(this)
        studentprofile.setOnClickListener(this)
        studentmessages.setOnClickListener(this)
        joinclassstrip.setOnClickListener(this)

        upgrade.setOnClickListener(this)
        studentlogout.setOnClickListener(this)
        logout.setOnClickListener(this)


        firstTime()

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
           //insertlocation()
            startService()


        }

    }

    private fun insertlocation() {



        var urlapi = share!!.urlApi
        val url = urlapi+"/locations/create"
        var access_token = share!!.getVal("access_token")
        trip_id =share!!.getVal("trip_id")
       // val url = "http://trippr.aprosoftech.com/api/insertlocation/insert"
        val params = JSONObject()
        try {


            val c1 = Calendar.getInstance()

            Log.d("date", "" + c1.get(Calendar.DATE))

            val mYear1 = c1.get(Calendar.YEAR)
            val mMonth1 = c1.get(Calendar.MONTH) + 1
            val mDay1 = c1.get(Calendar.DAY_OF_MONTH)
            val strDate = DateFormat.getDateTimeInstance().format(Date())


            val gpsTracker = GPSTracker(this@Dashboard)

            val latitude = gpsTracker.getLatitude().toString()
            val longitude = gpsTracker.getLongitude().toString()
//
//            val forgroundService = ForegroundService()
//
//            val latitude = forgroundService.getLocation()?.latitude
//            val longitude = forgroundService.getLocation()?.longitude

            params.put("trip_id", trip_id)
            params.put("user_id", userid)
          //  params.put("date", strDate)
            params.put("latitude", latitude)
            params.put("longitude", longitude)

        } catch (e: JSONException) {
            e.printStackTrace()
        }

        val request = CustomJsonObjectRequestBasicAuth(Request.Method.POST,url,params,
                Response.Listener{ response->
                    //                            pDialog.dismiss()
                    Log.d("res",response.toString())

                },
                Response.ErrorListener{
                    Log.d("res",it.toString())
                    SweetAlertDialog(this@Dashboard, SweetAlertDialog.ERROR_TYPE)
                            .setTitleText(getString(R.string.oops))
                            .setContentText("Something went wrong on Insert Location!")
                            .show()
                },
                access_token
        )

        VolleySingleton.getInstance(this).addToRequestQueue(request)

    }


    fun compareDatesByCompareTo(df: DateFormat?, currentdate: Date?, enddate: Date?) {
       // how to check if date1 is equal to date2
//        if (currentdate.compareTo(enddate) > 0) {

//            SweetAlertDialog(this@Dashboard, SweetAlertDialog.ERROR_TYPE)
//                    .setTitleText(getString(R.string.oops))
//                    .setContentText(getString(R.string.upgrade))
//                    .setConfirmText("ok")
//                    .setConfirmClickListener { sweetAlertDialog ->
//                        sweetAlertDialog.dismiss()
//                        val intent = Intent(this@Dashboard, Upgradeplan::class.java)
//                        startActivity(intent)
//                    }
//                    .show()

//        } else {
//
            val intent = Intent(this@Dashboard, Createclasstrip::class.java)
            startActivity(intent)
//        }
    }


    override fun onClick(view: View) {

        if (view.id == R.id.createtrip) {

            Log.d("enddate", enddate)


            val df = SimpleDateFormat("dd-MM-yyyy")
            val c1 = Calendar.getInstance()

            Log.d("date", "" + c1.get(Calendar.DATE))

            val mYear1 = c1.get(Calendar.YEAR)
            val mMonth1 = c1.get(Calendar.MONTH) + 1
            val mDay1 = c1.get(Calendar.DAY_OF_MONTH)
            currentdate = "$mDay1-$mMonth1-$mYear1"

            try {
                compareDatesByCompareTo(df, null ,null)
                //  compareDatesByCompareTo1(df, df.parse(currentdate1), df.parse(enddate));

            } catch (e: ParseException) {
                e.printStackTrace()
            }


        }

        if (view.id == R.id.seetrip) {

            val intent = Intent(this@Dashboard, Alltrips::class.java)
            startActivity(intent)

        }
        if (view.id == R.id.profile) {

            val intent = Intent(this@Dashboard, Profile::class.java)
            startActivity(intent)

        }


        if (view.id == R.id.logout) {


            share!!.clearAll()


            val intent = Intent(this@Dashboard, Login::class.java)
            startActivity(intent)
            this.finish()

        }

        if (view.id == R.id.studentlogout) {


            share!!.clearAll()


            val intent = Intent(this@Dashboard, Login::class.java)
            startActivity(intent)
            this.finish()

        }
        if (view.id == R.id.upgrade) {

            val intent = Intent(this@Dashboard, Upgradeselect::class.java)
            startActivity(intent)

        }



        if (view.id == R.id.messages) {

            val intent = Intent(this@Dashboard, ChatActivity::class.java)
            startActivity(intent)

        }
        if (view.id == R.id.jointrip) {

            val intent = Intent(this@Dashboard, Joinclasstrip::class.java)
            startActivity(intent)

        }
        if (view.id == R.id.joinclasstrip) {

            val intent = Intent(this@Dashboard, Joinclasstrip::class.java)
            startActivity(intent)

        }
        if (view.id == R.id.joinedtrip) {

            val intent = Intent(this@Dashboard, StudentJoinedTrip::class.java)
            startActivity(intent)

        }
        if (view.id == R.id.studentprofile) {

            val intent = Intent(this@Dashboard, Profile::class.java)
            startActivity(intent)

        }
        if (view.id == R.id.studentmessages) {

            val intent = Intent(this@Dashboard, ChatActivity::class.java)
            startActivity(intent)

        }

    }
}
