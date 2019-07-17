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
import android.widget.RelativeLayout
import android.widget.TextView
import cn.pedant.SweetAlert.SweetAlertDialog
import com.android.volley.NetworkResponse
import com.android.volley.ParseError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.HttpHeaderParser
import com.android.volley.toolbox.JsonRequest
import com.android.volley.toolbox.Volley
import com.stripe.android.Stripe
import com.stripe.android.TokenCallback
import com.stripe.android.model.Card
import com.stripe.android.model.Token
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.UnsupportedEncodingException
import java.nio.charset.Charset
import java.util.*

class Upgradeplan : AppCompatActivity() {


    internal lateinit var name: String
    internal var email: String? = null
    internal var password: String? = null
    internal lateinit var school: String
    internal var membership: String?=null
    internal var imageurl: String?=null
    internal lateinit var pay: Button
    internal var licencecode: String?=null
    internal var userid: String?=null
    internal var usertype: String?=null
    internal lateinit var code: TextView
    internal lateinit var congratslayout: RelativeLayout
    internal lateinit var cardholdername: EditText
    internal lateinit var cvv: EditText
    internal lateinit var expiremonth: EditText
    internal lateinit var expireyear: EditText
    internal lateinit var cardnumber: EditText


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upgradeplan)

        val actionBar = supportActionBar
        actionBar!!.hide()

        val editor = getSharedPreferences("userid", Context.MODE_PRIVATE)

        name = editor.getString("username", "")
        school = editor.getString("schoolname", "")
        imageurl = editor.getString("imageurl", "")
        licencecode = editor.getString("licence", "")
        userid = editor.getString("userid", "")
        membership = editor.getString("membership", "")
        usertype = editor.getString("usertype", "")

        congratslayout = findViewById(R.id.congratslayout) as RelativeLayout

        code = findViewById(R.id.code) as TextView

        cardholdername = findViewById(R.id.name) as EditText
        cvv = findViewById(R.id.cvv) as EditText
        expiremonth = findViewById(R.id.expiremonth) as EditText
        expireyear = findViewById(R.id.expireyear) as EditText
        cardnumber = findViewById(R.id.cardnumber) as EditText

        pay = findViewById(R.id.pay) as Button

        pay.setOnClickListener(View.OnClickListener { view ->
            if (cardholdername.text.toString().equals("", ignoreCase = true)) {
                Snackbar.make(view, R.string.fillallthefields, Snackbar.LENGTH_LONG).show()
                return@OnClickListener
            }
            if (cvv.text.toString().equals("", ignoreCase = true)) {
                Snackbar.make(view, R.string.fillallthefields, Snackbar.LENGTH_LONG).show()
                return@OnClickListener
            }
            if (expiremonth.text.toString().equals("", ignoreCase = true)) {
                Snackbar.make(view, R.string.fillallthefields, Snackbar.LENGTH_LONG).show()
                return@OnClickListener
            }
            if (expireyear.text.toString().equals("", ignoreCase = true)) {
                Snackbar.make(view, R.string.fillallthefields, Snackbar.LENGTH_LONG).show()
                return@OnClickListener
            }
            if (cardnumber.text.toString().equals("", ignoreCase = true)) {
                Snackbar.make(view, R.string.fillallthefields, Snackbar.LENGTH_LONG).show()
                return@OnClickListener
            }


            val pDialog = SweetAlertDialog(this@Upgradeplan, SweetAlertDialog.PROGRESS_TYPE)
            pDialog.progressHelper.barColor = Color.parseColor("#A5DC86")
            pDialog.titleText = "Loading"
            pDialog.setCancelable(false)
            pDialog.show()

            val month = Integer.parseInt(expiremonth.text.toString())
            val year = Integer.parseInt(expireyear.text.toString())


            val card = Card(cardnumber.text.toString(), month, year, cvv.text.toString())

            if (!card.validateCard()) {

                Snackbar.make(view, R.string.wronginfo, Snackbar.LENGTH_LONG).show()
                return@OnClickListener
            }

            val stripe = Stripe(this@Upgradeplan, "pk_test_eKRuSJjtelOKCwrBLRAZI9gP")
            stripe.createToken(
                    card,
                    object : TokenCallback {
                        override fun onSuccess(token: Token) {

                            Log.d("token", "" + token.id)


                            pDialog.dismiss()

                            val params = JSONObject()
                            try {
                                params.put("Token", token.id)
                                params.put("Email", email)

                            } catch (e: JSONException) {
                                e.printStackTrace()
                            }

                            val requestQueue = Volley.newRequestQueue(this@Upgradeplan)
                            val url = "http://trippr.aprosoftech.com/api/RegisterAndPay/Register"

                            val jsonRequest = object : JsonRequest<JSONArray>(Request.Method.POST, url, params.toString(), Response.Listener { response ->
                                Log.d("Response", response.toString())


                                try {
                                    if (response.getJSONObject(0).getString("Msg").equals("success", ignoreCase = true)) {

                                        if (licencecode.equals("", ignoreCase = true)) {
                                            val cvvtoint = Integer.parseInt(cvv.text.toString()) + 157
                                            licencecode = expiremonth.toString() + "TRI" + cvvtoint + "PPR"

                                            updateplan()

                                        } else {
                                            updateplan()
                                        }


                                    } else {
                                        SweetAlertDialog(this@Upgradeplan, SweetAlertDialog.ERROR_TYPE)
                                                .setTitleText(getString(R.string.oops))
                                                .setContentText(getString(R.string.wronginfo))
                                                .show()
                                    }

                                } catch (e: JSONException) {
                                    e.printStackTrace()
                                    SweetAlertDialog(this@Upgradeplan, SweetAlertDialog.ERROR_TYPE)
                                            .setTitleText(getString(R.string.oops))
                                            .setContentText(getString(R.string.checkconnection))
                                            .show()

                                }
                            }, Response.ErrorListener { error ->
                                error.printStackTrace()
                                SweetAlertDialog(this@Upgradeplan, SweetAlertDialog.ERROR_TYPE)
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



                            requestQueue.add(jsonRequest)

                        }

                        override fun onError(error: Exception) {


                            SweetAlertDialog(this@Upgradeplan, SweetAlertDialog.ERROR_TYPE)
                                    .setTitleText(getString(R.string.oops))
                                    .setContentText(getString(R.string.wronginfo))
                                    .show()


                        }
                    }
            )
        })


    }


    fun updateplan() {


        val pDialog = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
        pDialog.progressHelper.barColor = Color.parseColor("#A5DC86")
        pDialog.titleText = getString(R.string.loading)
        pDialog.setCancelable(false)
        pDialog.show()


        val requestQueue = Volley.newRequestQueue(this@Upgradeplan)
        val url = "http://trippr.aprosoftech.com/api/updgradeplan/updgrade"


        val c1 = Calendar.getInstance()

        Log.d("date", "" + c1.get(Calendar.DATE))

        val mYear1 = c1.get(Calendar.YEAR)
        val mMonth1 = c1.get(Calendar.MONTH) + 1
        val mMonth2 = c1.get(Calendar.MONTH) + 2
        val mDay1 = c1.get(Calendar.DAY_OF_MONTH)
        val strDate = "$mDay1-$mMonth1-$mYear1"
        val endDate = "$mDay1-$mMonth2-$mYear1"


        val params = JSONObject()
        try {

            params.put("userid", userid)
            params.put("licence", licencecode)
            params.put("enddate", endDate)
            params.put("membership", "fullaccess")

        } catch (e: JSONException) {
            e.printStackTrace()
        }

        Volley.newRequestQueue(this@Upgradeplan)
                .add(object : JsonRequest<JSONArray>(Request.Method.POST,
                        url,
                        params.toString(),
                        Response.Listener { jsonArray ->
                            pDialog.dismiss()

                            Log.d("response", jsonArray.toString())


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


                            val intent = Intent(this@Upgradeplan, Dashboard::class.java)
                            startActivity(intent)
                        }, Response.ErrorListener { volleyError ->
                    Log.d("error", volleyError.toString())
                    pDialog.dismiss()


                    SweetAlertDialog(this@Upgradeplan, SweetAlertDialog.ERROR_TYPE)
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
