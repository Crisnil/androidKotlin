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

class Payment : AppCompatActivity() {


    internal lateinit var name: String
    internal lateinit var email: String
    internal lateinit var password: String
    internal lateinit var school: String
    internal lateinit var membership: String
    internal lateinit var pay: Button
    internal lateinit var licencecode: String
    internal lateinit var code: TextView

    internal lateinit var congratslayout: RelativeLayout

    internal lateinit var cardholdername: EditText
    internal lateinit var cvv: EditText
    internal lateinit var expiremonth: EditText
    internal lateinit var expireyear: EditText
    internal lateinit var cardnumber: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)


        val actionBar = supportActionBar
        actionBar!!.hide()


        name = intent.extras.getString("name", "")
        password = intent.extras.getString("password", "")
        membership = intent.extras.getString("membership", "")
        email = intent.extras.getString("email", "")
        school = intent.extras.getString("school", "")

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


            val pDialog = SweetAlertDialog(this@Payment, SweetAlertDialog.PROGRESS_TYPE)
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

            val stripe = Stripe(this@Payment, "pk_test_eKRuSJjtelOKCwrBLRAZI9gP")
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

                            val requestQueue = Volley.newRequestQueue(this@Payment)
                            val url = "http://trippr.aprosoftech.com/api/RegisterAndPay/Register"

                            val jsonRequest = object : JsonRequest<JSONArray>(Request.Method.POST, url, params.toString(), Response.Listener { response ->
                                Log.d("Response", response.toString())


                                try {
                                    if (response.getJSONObject(0).getString("Msg").equals("success", ignoreCase = true)) {

                                        val cvvtoint = Integer.parseInt(cvv.text.toString()) + 157
                                        licencecode = expiremonth.toString() + "TRI" + cvvtoint + "PPR"

                                        insertuser()

                                    } else {
                                        SweetAlertDialog(this@Payment, SweetAlertDialog.ERROR_TYPE)
                                                .setTitleText(getString(R.string.oops))
                                                .setContentText(getString(R.string.wronginfo))
                                                .show()
                                    }

                                } catch (e: JSONException) {
                                    e.printStackTrace()
                                    SweetAlertDialog(this@Payment, SweetAlertDialog.ERROR_TYPE)
                                            .setTitleText(getString(R.string.oops))
                                            .setContentText(getString(R.string.checkconnection))
                                            .show()

                                }
                            }, Response.ErrorListener { error ->
                                error.printStackTrace()
                                SweetAlertDialog(this@Payment, SweetAlertDialog.ERROR_TYPE)
                                        .setTitleText(getString(R.string.oops))
                                        .setContentText(getString(R.string.checkconnection))
                                        .show()
                            }) {
                                override fun parseNetworkResponse(networkResponse: NetworkResponse): Response<JSONArray> {
                                    try {
//                                        val jsonString = String(networkResponse.data,
//                                                HttpHeaderParser
//                                                        .parseCharset(networkResponse.headers))
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


                            SweetAlertDialog(this@Payment, SweetAlertDialog.ERROR_TYPE)
                                    .setTitleText(getString(R.string.oops))
                                    .setContentText(getString(R.string.wronginfo))
                                    .show()


                        }
                    }
            )
        })

    }

    fun insertuser() {


        val pDialog = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
        pDialog.progressHelper.barColor = Color.parseColor("#A5DC86")
        pDialog.titleText = getString(R.string.loading)
        pDialog.setCancelable(false)
        pDialog.show()


        val requestQueue = Volley.newRequestQueue(this@Payment)
        val url = "http://trippr.aprosoftech.com/api/useradd/SaveUser"


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
            params.put("name", name)
            params.put("password", password)
            params.put("email", email)
            params.put("imageurl", "")
            params.put("token", "")
            params.put("logintype", "email")
            params.put("licence", licencecode)
            params.put("school", school)
            params.put("membership", membership)
            params.put("date", strDate)
            params.put("enddate", endDate)

        } catch (e: JSONException) {
            e.printStackTrace()
        }

        Volley.newRequestQueue(this@Payment)
                .add(object : JsonRequest<JSONArray>(Request.Method.POST,
                        url,
                        params.toString(),
                        Response.Listener { jsonArray ->
                            pDialog.dismiss()

                            Log.d("response", jsonArray.toString())


                            try {
                                if (jsonArray.getJSONObject(0).getString("userid").equals("0", ignoreCase = true)) {
                                    SweetAlertDialog(this@Payment, SweetAlertDialog.ERROR_TYPE)
                                            .setTitleText(getString(R.string.oops))
                                            .setContentText(getString(R.string.emailexist))
                                            .show()

                                } else {


                                    Log.d("response", jsonArray.toString())
                                    val editor = getSharedPreferences("userid", Context.MODE_PRIVATE).edit()
                                    editor.putString("membership", jsonArray.getJSONObject(0).getString("membership"))
                                    editor.putString("userid", jsonArray.getJSONObject(0).getString("userid"))
                                    editor.putString("username", jsonArray.getJSONObject(0).getString("name"))
                                    editor.putString("schoolname", jsonArray.getJSONObject(0).getString("school"))
                                    editor.putString("imageurl", jsonArray.getJSONObject(0).getString("imageurl"))
                                    editor.putString("licence", jsonArray.getJSONObject(0).getString("licence"))
                                    editor.putString("date", jsonArray.getJSONObject(0).getString("date"))
                                    editor.putString("enddate", jsonArray.getJSONObject(0).getString("enddate"))

                                    editor.apply()


                                    congratslayout.visibility = View.VISIBLE


                                    val intent = Intent(this@Payment, Dashboard::class.java)
                                    startActivity(intent)

                                }

                            } catch (e: JSONException) {
                                e.printStackTrace()
                            }
                        }, Response.ErrorListener { volleyError ->
                    Log.d("error", volleyError.toString())
                    pDialog.dismiss()


                    SweetAlertDialog(this@Payment, SweetAlertDialog.ERROR_TYPE)
                            .setTitleText(getString(R.string.oops))
                            .setContentText(getString(R.string.checkconnection))
                            .show()
                }) {


                    override fun parseNetworkResponse(networkResponse: NetworkResponse): Response<JSONArray> {


                        try {
//                            val jsonString = String(networkResponse.data,
//                                    HttpHeaderParser
//                                            .parseCharset(networkResponse.headers))
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
