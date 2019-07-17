package com.design4web.tripii

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RelativeLayout
import cn.pedant.SweetAlert.SweetAlertDialog
import com.android.volley.NetworkResponse
import com.android.volley.ParseError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.HttpHeaderParser
import com.android.volley.toolbox.JsonRequest
import com.android.volley.toolbox.Volley
import com.onesignal.OneSignal
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.UnsupportedEncodingException
import java.nio.charset.Charset
import java.util.*

class Createteacherprofile : AppCompatActivity(), View.OnClickListener {


    internal lateinit var name: EditText
    internal lateinit var email: EditText
    internal lateinit var password: EditText
    internal lateinit var school: EditText
    internal lateinit var licence: EditText

    internal var imageurl: String? = null
    internal var logintype: String? = null
    internal var token: String? = null
    internal lateinit var membership: String
    internal var licencecode = ""

    internal lateinit var freetrail: LinearLayout
    internal lateinit var fullaccess: LinearLayout
    internal lateinit var alreadymember: LinearLayout
    internal lateinit var playerid: Array<String?>
    internal lateinit var endDate: String
    internal lateinit var strDate: String

    internal lateinit var createprofile: Button
    internal lateinit var validate: Button

    internal lateinit var licencelayout: RelativeLayout
    internal lateinit var relfree: RelativeLayout
    internal lateinit var relfullaccess: RelativeLayout
    internal lateinit var relalreadymember: RelativeLayout
    internal var share :SharePref? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_createteacherprofile)

        if(share == null){
            share = SharePref.getInstance(applicationContext)
        }

        val actionBar = supportActionBar
        actionBar!!.hide()

        OneSignal.startInit(this)
                .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                .unsubscribeWhenNotificationsAreDisabled(true)
                .init()

        playerid = arrayOfNulls(1)
        OneSignal.idsAvailable { userId, registrationId ->
            Log.d("debug", "User:$userId")
            playerid[0] = userId
            if (registrationId != null)

                Log.d("debug", "registrationId:$registrationId")
        }


        val c1 = Calendar.getInstance()

        Log.d("date", "" + c1.get(Calendar.DATE))

        val mYear1 = c1.get(Calendar.YEAR)
        val mMonth1 = c1.get(Calendar.MONTH) + 1
        val mMonth2 = c1.get(Calendar.MONTH) + 2
        val mDay1 = c1.get(Calendar.DAY_OF_MONTH)
        strDate = "$mDay1-$mMonth1-$mYear1"
        endDate = "" + mDay1 + "-" + mMonth2 + "-" + (mYear1 + 200)



        freetrail = findViewById(R.id.freetrial) as LinearLayout
        fullaccess = findViewById(R.id.fullaccess) as LinearLayout
        alreadymember = findViewById(R.id.alreadymember) as LinearLayout


        name = findViewById(R.id.name) as EditText
        email = findViewById(R.id.email) as EditText
        password = findViewById(R.id.password) as EditText
        school = findViewById(R.id.schoolname) as EditText
        licence = findViewById(R.id.licencecode) as EditText

        createprofile = findViewById(R.id.createprofile) as Button
        validate = findViewById(R.id.validate) as Button
        licencelayout = findViewById(R.id.licencelayout) as RelativeLayout
        relfree = findViewById(R.id.relfree) as RelativeLayout
        relfullaccess = findViewById(R.id.relfull) as RelativeLayout
        relalreadymember = findViewById(R.id.relalreadymember) as RelativeLayout


        createprofile.setOnClickListener(this)
        validate.setOnClickListener(this)
        freetrail.setOnClickListener(this)
        fullaccess.setOnClickListener(this)
        alreadymember.setOnClickListener(this)


    }

    @SuppressLint("ResourceAsColor")
    override fun onClick(view: View) {


        if (view.id == R.id.createprofile) {


            if (name.text.toString().equals("", ignoreCase = true)) {
                Snackbar.make(view, R.string.fillallthefields, Snackbar.LENGTH_LONG).show()
                return
            }

            if (school.text.toString().equals("", ignoreCase = true)) {
                Snackbar.make(view, R.string.fillallthefields, Snackbar.LENGTH_LONG).show()
                return
            }

            if (email.text.toString().equals("", ignoreCase = true)) {
                Snackbar.make(view, R.string.fillallthefields, Snackbar.LENGTH_LONG).show()
                return
            }
            if (password.text.toString().equals("", ignoreCase = true)) {
                Snackbar.make(view, R.string.fillallthefields, Snackbar.LENGTH_LONG).show()
                return
            }

            //            if (membership.equalsIgnoreCase(""))
            //            {
            //                Snackbar.make(view,R.string.membershipplan,Snackbar.LENGTH_LONG).show();
            //                return;
            //            }


            val pDialog = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
            pDialog.progressHelper.barColor = Color.parseColor("#A5DC86")
            pDialog.titleText = getString(R.string.loading)
            pDialog.setCancelable(false)
            pDialog.show()



            var request_url = share!!.urlApi


            val params = JSONObject()
            try {
                params.put("name", name.text.toString())
                params.put("password", password.text.toString())
                params.put("email", email.text.toString())
                params.put("school_name", school.text.toString())
                params.put("position_id", 3)

            } catch (e: JSONException) {
                e.printStackTrace()
            }

            val request = CustomJsonObjectRequestBasicAuth(Request.Method.POST, request_url + "/auth/register", params,
                    Response.Listener { response ->
                        pDialog.dismiss()

                        //  share.save("access_token", response.getString("access_token"))
                        //  share.save("membership", response.getJSONObject("user").getString("access"))
                        share?.save("userid", response.getJSONObject("meta").getJSONObject("data").getString("id"))
                        share?.save("email", response.getJSONObject("meta").getJSONObject("data").getString("email"))

                        autoLogin()
                    },
                    Response.ErrorListener {
                        pDialog.dismiss()
                        Log.d("error response",it.toString())
                        SweetAlertDialog(this@Createteacherprofile, SweetAlertDialog.ERROR_TYPE)
                                .setTitleText("opps")
                                .setContentText("failed")
                                .setConfirmClickListener { sweetAlertDialog ->
                                    sweetAlertDialog.dismiss()
                                }
                                .show()

                    }, null
            )

            VolleySingleton.getInstance(this).addToRequestQueue(request)

        }



        if (view.id == R.id.validate) {

            if (licence.text.toString().equals("", ignoreCase = true)) {
                Snackbar.make(view, R.string.validcode, Snackbar.LENGTH_LONG).show()
                return
            }


            val pDialog = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
            pDialog.progressHelper.barColor = Color.parseColor("#A5DC86")
            pDialog.titleText = getString(R.string.loading)
            pDialog.setCancelable(false)
            pDialog.show()


            val url = "http://trippr.aprosoftech.com/api/licencetest/licenceexist"


            val params = JSONObject()
            try {

                params.put("licence", licence.text.toString())

            } catch (e: JSONException) {
                e.printStackTrace()
            }

            Volley.newRequestQueue(this@Createteacherprofile)
                    .add(object : JsonRequest<JSONArray>(Request.Method.POST,
                            url,
                            params.toString(),
                            Response.Listener { jsonArray ->
                                pDialog.dismiss()

                                Log.d("response", jsonArray.toString())


                                try {
                                    if (jsonArray.getJSONObject(0).getString("userid").equals("0", ignoreCase = true)) {
                                        SweetAlertDialog(this@Createteacherprofile, SweetAlertDialog.ERROR_TYPE)
                                                .setTitleText(getString(R.string.oops))
                                                .setContentText(getString(R.string.validcode))
                                                .show()

                                        membership = ""

                                    } else {
                                        licencecode = licence.text.toString()
                                        endDate = jsonArray.getJSONObject(0).getString("enddate")
                                        membership = "fullaccess"
                                        SweetAlertDialog(this@Createteacherprofile, SweetAlertDialog.SUCCESS_TYPE)
                                                .setTitleText(getString(R.string.congrats))
                                                .setContentText(getString(R.string.codeverified))
                                                .setConfirmText("ok")
                                                .setConfirmClickListener { sweetAlertDialog ->
                                                    licencelayout.visibility = View.INVISIBLE
                                                    sweetAlertDialog.dismiss()
                                                }
                                                .show()

                                    }

                                } catch (e: JSONException) {
                                    e.printStackTrace()
                                }
                            }, Response.ErrorListener { volleyError ->
                        Log.d("error", volleyError.toString())
                        pDialog.dismiss()

                        SweetAlertDialog(this@Createteacherprofile, SweetAlertDialog.ERROR_TYPE)
                                .setTitleText(getString(R.string.oops))
                                .setContentText(getString(R.string.checkconnection))
                                .show()
                    }) {


                        override fun parseNetworkResponse(networkResponse: NetworkResponse): Response<JSONArray> {


                            try {
//                                val jsonString = String(networkResponse.data,
//                                        HttpHeaderParser
//                                                .parseCharset(networkResponse.headers))
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



        if (view.id == R.id.freetrial) {

            membership = "trial"
            relfree.setBackgroundResource(R.drawable.membershipborder)
            relfullaccess.setBackgroundColor(Color.parseColor("#00000000"))

            relalreadymember.setBackgroundColor(Color.parseColor("#00000000"))
            licencelayout.visibility = View.INVISIBLE
        }



        if (view.id == R.id.fullaccess) {


            if (name.text.toString().equals("", ignoreCase = true)) {
                Snackbar.make(view, R.string.fillallthefields, Snackbar.LENGTH_LONG).show()
                return
            }

            if (school.text.toString().equals("", ignoreCase = true)) {
                Snackbar.make(view, R.string.fillallthefields, Snackbar.LENGTH_LONG).show()
                return
            }

            if (email.text.toString().equals("", ignoreCase = true)) {
                Snackbar.make(view, R.string.fillallthefields, Snackbar.LENGTH_LONG).show()
                return
            }
            if (password.text.toString().equals("", ignoreCase = true)) {
                Snackbar.make(view, R.string.fillallthefields, Snackbar.LENGTH_LONG).show()
                return
            }



            relfullaccess.setBackgroundResource(R.drawable.membershipborder)
            relfree.setBackgroundColor(Color.parseColor("#00000000"))
            relalreadymember.setBackgroundColor(Color.parseColor("#00000000"))
            licencelayout.visibility = View.INVISIBLE


            val pDialog = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
            pDialog.progressHelper.barColor = Color.parseColor("#A5DC86")
            pDialog.titleText = getString(R.string.loading)
            pDialog.setCancelable(false)
            pDialog.show()


            val url = "http://trippr.aprosoftech.com/api/emailtest/emailexist"


            val params = JSONObject()
            try {

                params.put("email", email.text.toString())

            } catch (e: JSONException) {
                e.printStackTrace()
            }

            Volley.newRequestQueue(this@Createteacherprofile)
                    .add(object : JsonRequest<JSONArray>(Request.Method.POST,
                            url,
                            params.toString(),
                            Response.Listener { jsonArray ->
                                pDialog.dismiss()

                                Log.d("response", jsonArray.toString())


                                try {
                                    if (jsonArray.getJSONObject(0).getString("userid").equals("0", ignoreCase = true)) {
                                        SweetAlertDialog(this@Createteacherprofile, SweetAlertDialog.ERROR_TYPE)
                                                .setTitleText(getString(R.string.oops))
                                                .setContentText(getString(R.string.emailexist))
                                                .show()

                                    } else {
                                        val intent = Intent(this@Createteacherprofile, Payment::class.java)
                                        intent.putExtra("name", name.text.toString())
                                        intent.putExtra("password", password.text.toString())
                                        intent.putExtra("membership", "fullaccess")
                                        intent.putExtra("email", email.text.toString())
                                        intent.putExtra("school", school.text.toString())
                                        startActivity(intent)
                                    }

                                } catch (e: JSONException) {
                                    e.printStackTrace()
                                }
                            }, Response.ErrorListener { volleyError ->
                        Log.d("error", volleyError.toString())
                        pDialog.dismiss()

                        SweetAlertDialog(this@Createteacherprofile, SweetAlertDialog.ERROR_TYPE)
                                .setTitleText(getString(R.string.oops))
                                .setContentText(getString(R.string.checkconnection))
                                .show()
                    }) {


                        override fun parseNetworkResponse(networkResponse: NetworkResponse): Response<JSONArray> {


                            try {
//                                val jsonString = String(networkResponse.data,
//                                        HttpHeaderParser
//                                                .parseCharset(networkResponse.headers))
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


        if (view.id == R.id.alreadymember) {

            licencelayout.visibility = View.VISIBLE
            relalreadymember.setBackgroundResource(R.drawable.membershipborder)
            relfree.setBackgroundColor(Color.parseColor("#00000000"))
            relfullaccess.setBackgroundColor(Color.parseColor("#00000000"))


        }

    }
    private fun autoLogin () {


        var url = share?.urlApi
        var userid = share?.getVal("userid")

        val pDialog2 = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
        pDialog2.progressHelper.barColor = Color.parseColor("#A5DC86")
        pDialog2.titleText = getString(R.string.loading)
        pDialog2.setCancelable(false)
        pDialog2.show()

        val params = JSONObject()
        try {
            params.put("email", email.text.toString())
            params.put("password", password.text.toString())
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        val request = CustomJsonObjectRequestBasicAuth(Request.Method.POST,url + "/auth/login",params,
                Response.Listener{ response ->
                    pDialog2.dismiss()
                    try {
                        var userinfo = response.getJSONObject("user").getInt("id").toString()
                        if (userinfo != "0") {
                            try {
                                // share.save("USER_ID", jsonArray.getJSONObject("user").getInt("id").toString())
                                share!!.save("access_token", response.getString("access_token"))
                                share!!.save("membership", response.getJSONObject("user").getString("access"))
                                share!!.save("userid", response.getJSONObject("user").getInt("id").toString())
                                share!!.save("email", response.getJSONObject("user").getString("email"))
                                //share.save("schoolname", jsonArray.getJSONObject("user").getString("school")?:"")
                                share!!.save("imageurl", response.getJSONObject("user").getString("imageurl"))
                                share!!.save("licence", response.getJSONObject("user").getString("license"))
                                share!!.save("date", response.getJSONObject("user").getString("date"))
                                share!!.save("enddate", response.getJSONObject("user").getString("enddate"))
                                share!!.save("trip_id", response.getJSONObject("user").getString("trip_id"))
                            }
                            catch (e:Exception){e.printStackTrace()}
                            SweetAlertDialog(this@Createteacherprofile, SweetAlertDialog.SUCCESS_TYPE)
                                    .setTitleText("Congrats")
                                    .setContentText("Create Profile Success")
                                    .setConfirmClickListener { sweetAlertDialog ->
                                        val intent = Intent(this@Createteacherprofile, PrivacyPolicyActivity::class.java)
                                        startActivity(intent)
                                    }
                                    .show()


                        }
                    } catch (e: Exception) {
                        e.printStackTrace()

                        share?.clearAll()
                        var mess = response.get("message").toString()


                        SweetAlertDialog(this@Createteacherprofile, SweetAlertDialog.ERROR_TYPE)
                                .setTitleText(getString(R.string.oops))
                                .setContentText(mess)
                                .setConfirmClickListener { sweetAlertDialog ->
                                    sweetAlertDialog.dismiss()}
                                .show()

                    }
                },
                Response.ErrorListener{
                    share?.clearAll()
                    SweetAlertDialog(this@Createteacherprofile, SweetAlertDialog.ERROR_TYPE)
                            .setTitleText("opps")
                            .setContentText("failed")
                            .setConfirmClickListener { sweetAlertDialog ->
                                sweetAlertDialog.dismiss()
                                this@Createteacherprofile.finish()
                            }
                            .show()

                    val intent = Intent(this@Createteacherprofile,Login::class.java)
                    startActivity(intent)

                },
                null
        )

        VolleySingleton.getInstance(this).addToRequestQueue(request)
    }
}
