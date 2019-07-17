package com.design4web.tripii

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
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.UnsupportedEncodingException
import java.nio.charset.Charset
import java.util.*

class Upgradeselect : AppCompatActivity(), View.OnClickListener {


    internal lateinit var fullaccess: LinearLayout
    internal lateinit var alreadymember: LinearLayout
    internal lateinit var licence: EditText

    internal lateinit var licencecode: String
    internal lateinit var endDate: String
    internal lateinit var userid: String
    internal lateinit var usertype: String
    internal lateinit var name: String
    internal var email: String? = null
    internal var password: String? = null
    internal lateinit  var school: String
    internal var membership: String? = null
    internal lateinit var imageurl: String

    internal lateinit var licencelayout: RelativeLayout
    internal lateinit var validate: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upgradeselect)


        val actionBar = supportActionBar
        actionBar!!.hide()

        fullaccess = findViewById(R.id.fullaccess) as LinearLayout
        alreadymember = findViewById(R.id.alreadymember) as LinearLayout
        licence = findViewById(R.id.licencecode) as EditText
        validate = findViewById(R.id.validate) as Button
        licencelayout = findViewById(R.id.licencelayout) as RelativeLayout


        val editor = getSharedPreferences("userid", Context.MODE_PRIVATE)
        name = editor.getString("username", "")
        school = editor.getString("schoolname", "")
        imageurl = editor.getString("imageurl", "")
        //  licencecode = editor.getString("licence","");
        userid = editor.getString("userid", "")
        //  membership =  editor.getString("membership","");
        usertype = editor.getString("usertype", "")


        validate.setOnClickListener(this)
        fullaccess.setOnClickListener(this)
        alreadymember.setOnClickListener(this)


    }

    override fun onClick(view: View) {

        if (view.id == R.id.fullaccess) {
            val intent = Intent(this@Upgradeselect, Upgradeplan::class.java)
            startActivity(intent)


        }
        if (view.id == R.id.alreadymember) {

            licencelayout.visibility = View.VISIBLE

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

            Volley.newRequestQueue(this@Upgradeselect)
                    .add(object : JsonRequest<JSONArray>(Request.Method.POST,
                            url,
                            params.toString(),
                            Response.Listener { jsonArray ->
                                pDialog.dismiss()

                                Log.d("licence response", jsonArray.toString())


                                try {
                                    if (jsonArray.getJSONObject(0).getString("userid").equals("0", ignoreCase = true)) {
                                        SweetAlertDialog(this@Upgradeselect, SweetAlertDialog.ERROR_TYPE)
                                                .setTitleText(getString(R.string.oops))
                                                .setContentText(getString(R.string.validcode))
                                                .show()


                                    } else {
                                        licencecode = licence.text.toString()
                                        endDate = jsonArray.getJSONObject(0).getString("enddate")


                                        licencelayout.visibility = View.INVISIBLE

                                        updateplan()


                                    }

                                } catch (e: JSONException) {
                                    e.printStackTrace()
                                }
                            }, Response.ErrorListener { volleyError ->
                        Log.d("error", volleyError.toString())
                        pDialog.dismiss()

                        SweetAlertDialog(this@Upgradeselect, SweetAlertDialog.ERROR_TYPE)
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


    }


    fun updateplan() {


        val pDialog = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
        pDialog.progressHelper.barColor = Color.parseColor("#A5DC86")
        pDialog.titleText = getString(R.string.loading)
        pDialog.setCancelable(false)
        pDialog.show()


        val requestQueue = Volley.newRequestQueue(this@Upgradeselect)
        val url = "http://trippr.aprosoftech.com/api/updgradeplan/updgrade"


        val c1 = Calendar.getInstance()

        Log.d("date", "" + c1.get(Calendar.DATE))

        val mYear1 = c1.get(Calendar.YEAR)
        val mMonth1 = c1.get(Calendar.MONTH) + 1
        val mMonth2 = c1.get(Calendar.MONTH) + 2
        val mDay1 = c1.get(Calendar.DAY_OF_MONTH)
        val strDate = "$mDay1-$mMonth1-$mYear1"


        val params = JSONObject()
        try {

            params.put("userid", userid)
            params.put("licence", licence.text.toString())
            params.put("enddate", endDate)
            params.put("membership", "fullaccess")

        } catch (e: JSONException) {
            e.printStackTrace()
        }

        Volley.newRequestQueue(this@Upgradeselect)
                .add(object : JsonRequest<JSONArray>(Request.Method.POST,
                        url,
                        params.toString(),
                        Response.Listener { jsonArray ->
                            pDialog.dismiss()

                            Log.d("updaTE response", jsonArray.toString())


                            Log.d("response", jsonArray.toString())
                            val editor = getSharedPreferences("userid", Context.MODE_PRIVATE).edit()
                            editor.putString("membership", "fullaccess")
                            editor.putString("userid", userid)
                            editor.putString("username", name)
                            editor.putString("schoolname", school)
                            editor.putString("imageurl", imageurl)
                            editor.putString("licence", licencecode)
                            editor.putString("date", strDate)
                            editor.putString("enddate", endDate)

                            editor.apply()


                            //  congratslayout.setVisibility(View.VISIBLE);


                            val intent = Intent(this@Upgradeselect, Dashboard::class.java)
                            startActivity(intent)
                        }, Response.ErrorListener { volleyError ->
                    Log.d("error", volleyError.toString())
                    pDialog.dismiss()


                    SweetAlertDialog(this@Upgradeselect, SweetAlertDialog.ERROR_TYPE)
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


}
