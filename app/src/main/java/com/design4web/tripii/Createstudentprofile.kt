package com.design4web.tripii

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.*
import cn.pedant.SweetAlert.SweetAlertDialog
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.Volley
import com.facebook.*
import com.facebook.login.LoginResult
import com.facebook.login.widget.LoginButton
import com.onesignal.OneSignal
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.net.MalformedURLException
import java.net.URL
import java.nio.charset.Charset
import java.util.*

class Createstudentprofile : AppCompatActivity(), View.OnClickListener {

    internal lateinit var name: EditText
    internal lateinit var email: EditText
    internal lateinit var password: EditText
    internal lateinit var school: EditText
    internal lateinit var createprofile: Button
    internal lateinit var createfacebook: Button
    internal lateinit var loginButton: LoginButton
    internal lateinit var callbackManager: CallbackManager
    internal var logintype = "email"
    internal lateinit var playerid: Array<String?>
    internal lateinit var spinner :Spinner
    internal lateinit var schools : JSONArray
    internal var share :SharePref? = null
    internal var school_id :Int?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_createstudentprofile)

        val actionBar = supportActionBar
        actionBar!!.hide()


        createfacebook = findViewById(R.id.createfacebook) as Button
        name = findViewById(R.id.name) as EditText
        email = findViewById(R.id.email) as EditText
        password = findViewById(R.id.password) as EditText
//        school = findViewById(R.id.schoolname) as EditText
        spinner = findViewById(R.id.spinner) as Spinner

        OneSignal.startInit(this)
                .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                .unsubscribeWhenNotificationsAreDisabled(true)
                .init()
        createfacebook.visibility = View.GONE
        playerid = arrayOfNulls(1)
        OneSignal.idsAvailable { userId, registrationId ->
            Log.d("debug", "User:$userId")
            playerid[0] = userId
            if (registrationId != null)

                Log.d("debug", "registrationId:$registrationId")
        }



        createprofile = findViewById(R.id.signup) as Button
        createprofile.setOnClickListener(this)
        createfacebook.setOnClickListener(this)


        callbackManager = CallbackManager.Factory.create()
        loginButton = findViewById(R.id.login_button) as LoginButton

        loginButton.setReadPermissions(Arrays.asList("basic_info", "email"))

        val requestQueue = Volley.newRequestQueue(this@Createstudentprofile)
        if(share == null){
            share = SharePref.getInstance(applicationContext)
        }
        var request_url = share?.urlApi

        val request = CustomJsonObjectRequestBasicAuth(Request.Method.GET,request_url+"/public/schools",null,
                Response.Listener{ response->
                    this.schools = response.getJSONObject("meta").getJSONArray("metadata")
                    val spinnerMap = HashMap<Int, String>()
                    var spinnerArray = arrayOfNulls<String>(this.schools.length())

                    for(obj in 0..this.schools.length() -1){
                        spinnerMap.put(obj,this.schools.getString(obj))
                        spinnerArray[obj]= this.schools.getJSONObject(obj).getString("description")
                    }

                    val aa = ArrayAdapter(this, android.R.layout.simple_spinner_item, spinnerArray)
                    // Set layout to use when the list of choices appear
                    aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    // Set Adapter to Spinner
                    spinner.setAdapter(aa)



                },
                Response.ErrorListener{
                    SweetAlertDialog(this@Createstudentprofile, SweetAlertDialog.ERROR_TYPE)
                            .setTitleText("opps")
                            .setContentText("failed")
                            .setConfirmClickListener { sweetAlertDialog ->
                                sweetAlertDialog.dismiss()
                            }
                            .show()
                    val intent = Intent(this@Createstudentprofile,Login::class.java)
                    startActivity(intent)

                },
               null
        )

        requestQueue.add(request)





        loginButton.registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                Log.d("Login Result", loginResult.toString())


                val request = GraphRequest.newMeRequest(loginResult.accessToken, object : GraphRequest.GraphJSONObjectCallback {

                    override fun onCompleted(`object`: JSONObject, response: GraphResponse) {
                        Log.i("LoginActivity", response.toString())
                        try {
                            Log.d("LoginActivity email", response.jsonObject.getString("email"))
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }

                        //                         Get facebook data from login
                        val bFacebookData = getFacebookData(`object`)
                        callFbAPI(bFacebookData)
                    }


                    private fun getFacebookData(`object`: JSONObject): Bundle? {

                        try {
                            val bundle = Bundle()
                            val id = `object`.getString("id")
                            try {
                                val profile_pic = URL("https://graph.facebook.com/$id/picture?width=200&height=150")
                                //   Log.i("profile_pic", profile_pic + "");
                                bundle.putString("profile_pic", profile_pic.toString())

                            } catch (e: MalformedURLException) {
                                e.printStackTrace()
                                return null
                            }

                            bundle.putString("idFacebook", id)
                            if (`object`.has("first_name"))
                                bundle.putString("first_name", `object`.getString("first_name"))
                            if (`object`.has("last_name"))
                                bundle.putString("last_name", `object`.getString("last_name"))
                            if (`object`.has("email"))
                                bundle.putString("email", `object`.getString("email"))
                            if (`object`.has("gender"))
                                bundle.putString("gender", `object`.getString("gender"))
                            if (`object`.has("birthday"))
                                bundle.putString("birthday", `object`.getString("birthday"))
                            if (`object`.has("location"))
                                bundle.putString("location", `object`.getJSONObject("location").getString("name"))
                            bundle.putString("source", "F")


                            Log.d("data", "" + `object`.getString("first_name") + " " + `object`.getString("email"))




                            return bundle
                        } catch (e: JSONException) {
                            Log.d("error", "Error parsing JSON")
                        }

                        return null
                    }
                })
                val parameters = Bundle()
                parameters.putString("fields", "first_name,last_name,email") // Parámetros que pedimos a facebook
                request.parameters = parameters
                request.executeAsync()


            }

            override fun onCancel() {
                Log.d("CANCEL", "ED00")
            }

            override fun onError(error: FacebookException) {
                error.printStackTrace()
            }
        })

        spinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent:AdapterView<*>, view: View, position: Int, id: Long){
                // Display the selected item text on text view
                school_id = position
            }

            override fun onNothingSelected(parent: AdapterView<*>){
                // Another interface callback
            }
        }

    }





    fun callFbAPI(facebookResult: Bundle?) {

        Log.d("facebook result", facebookResult!!.toString())


        logintype = "facebook"
        name.setText(facebookResult.getString("first_name") + " " + facebookResult.getString("last_name"))
        email.setText(if (facebookResult.containsKey("email")) facebookResult.getString("email") else "")
        email.isEnabled = false


    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        callbackManager.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }


    override fun onClick(view: View) {


        if (view.id == R.id.createfacebook) {

            loginButton.performClick()

        }

        if (view.id == R.id.signup) {

            if (name.text.toString().equals("", ignoreCase = true)) {
                Snackbar.make(view, R.string.fillallthefields, Snackbar.LENGTH_LONG).show()
                return
            }

//            if (school.text.toString().equals("", ignoreCase = true)) {
//                Snackbar.make(view, R.string.fillallthefields, Snackbar.LENGTH_LONG).show()
//                return
//            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email.text.toString()).matches()) {
                Snackbar.make(view, R.string.invalidmail, Snackbar.LENGTH_LONG).show()
                return
            }
            if (password.text.toString().equals("", ignoreCase = true)) {
                Snackbar.make(view, R.string.fillallthefields, Snackbar.LENGTH_LONG).show()
                return
            }

            val pDialog = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
            pDialog.progressHelper.barColor = Color.parseColor("#A5DC86")
            pDialog.titleText = getString(R.string.loading)
            pDialog.setCancelable(false)
            pDialog.show()

            if(share == null) {
                share = SharePref.getInstance(applicationContext)
            }
            var request_url = share!!.urlApi


            val params = JSONObject()

            try {
                params.put("name", name.text.toString())
                params.put("password", password.text.toString())
                params.put("email", email.text.toString())
                params.put("position_id", 4)
                params.put("school_id",school_id)

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

                        var json = JSONObject (String(it.networkResponse.data , Charset.defaultCharset()))
                        SweetAlertDialog(this@Createstudentprofile, SweetAlertDialog.ERROR_TYPE)
                                .setTitleText(json.getString("code"))
                                .setContentText(json.getString("title"))
                                .setConfirmClickListener { sweetAlertDialog ->
                                    sweetAlertDialog.dismiss()
                                }
                                .show()

                    }, null
            )

            VolleySingleton.getInstance(this).addToRequestQueue(request)

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
                         SweetAlertDialog(this@Createstudentprofile, SweetAlertDialog.SUCCESS_TYPE)
                                 .setTitleText("Congrats")
                                 .setContentText("Create Profile Success")
                                 .setConfirmClickListener { sweetAlertDialog ->
                                     val intent = Intent(this@Createstudentprofile, PrivacyPolicyActivity::class.java)
                                     startActivity(intent)
                                     this@Createstudentprofile.finish()
                                 }
                                 .show()


                     }
                 } catch (e: Exception) {
                     e.printStackTrace()

                     share?.clearAll()
                     var mess = response.get("message").toString()


                     SweetAlertDialog(this@Createstudentprofile, SweetAlertDialog.ERROR_TYPE)
                             .setTitleText(getString(R.string.oops))
                             .setContentText(mess)
                             .setConfirmClickListener { sweetAlertDialog ->
                                 sweetAlertDialog.dismiss()}
                             .show()

                 }
             },
             Response.ErrorListener{
                 share?.clearAll()
                 SweetAlertDialog(this@Createstudentprofile, SweetAlertDialog.ERROR_TYPE)
                         .setTitleText("opps")
                         .setContentText("failed")
                         .setConfirmClickListener { sweetAlertDialog ->
                             sweetAlertDialog.dismiss()
                             this@Createstudentprofile.finish()
                         }
                         .show()

                 val intent = Intent(this@Createstudentprofile,Login::class.java)
                 startActivity(intent)

             },
             null
     )

     VolleySingleton.getInstance(this).addToRequestQueue(request)
 }
}
