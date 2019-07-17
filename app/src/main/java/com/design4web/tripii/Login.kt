package com.design4web.tripii

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.os.StrictMode
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import cn.pedant.SweetAlert.SweetAlertDialog
import com.android.volley.Request
import com.android.volley.Request.Method.POST
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.facebook.login.widget.LoginButton
import com.onesignal.OneSignal
import org.json.JSONException
import org.json.JSONObject
import java.net.MalformedURLException
import java.net.URL
import java.nio.charset.Charset
import java.util.*

class Login : AppCompatActivity(), View.OnClickListener {


    internal lateinit var email: EditText
    internal lateinit var password: EditText
    internal lateinit var login :Button
    internal lateinit var logo : ImageView
   // var login: Button by Delegates.notNull()
   internal lateinit var signup: Button
    internal lateinit var loginviafacebook: Button
    internal lateinit var callbackManager :CallbackManager
    internal var token = ""
    internal var logintype = "email"
    lateinit var playerid: String
    internal lateinit var loginButton: LoginButton
    internal var share : SharePref? = null

    //We are calling this method to check the permission status
    private//Getting the permission status
    //If permission is granted returning true
    //If permission is not granted returning false
    val isReadStorageAllowed: Boolean
        get() {
            val result = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            val result1 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            val result2 = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            val result3 = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            val result4 = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
            return if (result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED && result2 == PackageManager.PERMISSION_GRANTED && result == PackageManager.PERMISSION_GRANTED && result4 == PackageManager.PERMISSION_GRANTED) true else false


        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val actionBar = supportActionBar
        actionBar!!.hide()

        if(share == null) {
            share = SharePref.getInstance(applicationContext)
        }


        if (android.os.Build.VERSION.SDK_INT > 9)
        {
           var policy =  StrictMode.ThreadPolicy.Builder().permitAll().build()
            StrictMode.setThreadPolicy(policy)
        }


        OneSignal.startInit(this)
                .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                .unsubscribeWhenNotificationsAreDisabled(true)
                .init()


        OneSignal.idsAvailable { userId, registrationId ->
            Log.d("debug", "User:$userId")
            playerid = userId
            if (registrationId != null)

                Log.d("debug", "registrationId:$registrationId")
        }




//        var share = SharePref.getInstance(applicationContext)

        var userid  = share!!.getVal("userid")
            if(userid != ""){

              //  val editor = getSharedPreferences("params", Context.MODE_PRIVATE)
                var request_url = share!!.urlApi
                var access_token = share!!.getVal("access_token")

                val request = CustomJsonObjectRequestBasicAuth(Request.Method.GET,request_url+"/users/"+userid,null,
                        Response.Listener{ response->

                            updateOnesignal()
//                            pDialog.dismiss()

                        },
                        Response.ErrorListener{
//                            var json = JSONObject (String(it.networkResponse.data , Charset.defaultCharset()))
                            SweetAlertDialog(this@Login, SweetAlertDialog.ERROR_TYPE)
                                    .setTitleText("opps")
                                    .setContentText("please check internet")
                                    .setConfirmClickListener { sweetAlertDialog ->
                                        sweetAlertDialog.dismiss()
                                    }
                                    .show()

                        },
                        access_token
                )

                VolleySingleton.getInstance(this).addToRequestQueue(request)



            }

        logo = findViewById(R.id.logo)
        email = findViewById(R.id.email) as EditText
        password = findViewById(R.id.password) as EditText
        login = findViewById(R.id.login)
        login.setOnClickListener(this)
        logo.setOnClickListener(this)
        signup = findViewById(R.id.signup) as Button
        loginviafacebook = findViewById(R.id.loginviafacebook) as Button

        loginviafacebook.visibility= View.GONE
        signup.setOnClickListener(this)
        loginviafacebook.setOnClickListener(this)
        callbackManager = CallbackManager.Factory.create()
        loginButton = findViewById(R.id.login_button) as LoginButton
        loginButton.setReadPermissions(Arrays.asList("basic_info", "email"))

        loginButton.registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                //  Log.d("Login Result", loginResult.getAccessToken().getToken());
                token = loginResult.accessToken.token


                val request = GraphRequest.newMeRequest(loginResult.accessToken, object : GraphRequest.GraphJSONObjectCallback {

                    override fun onCompleted(`object`: JSONObject, response: GraphResponse) {
                        //   Log.i("LoginActivity", response.toString());
                        // Get facebook data from login
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
                            //  Log.d("data",""+object.getString("first_name")+" "+ object.getString("email"));


                            return bundle
                        } catch (e: JSONException) {
                            Log.d("error", "Error parsing JSON")
                        }

                        return null
                    }
                })
                val parameters = Bundle()
                parameters.putString("fields", "id, first_name, last_name, email,gender, birthday, location") // Parámetros que pedimos a facebook
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


        if (!isReadStorageAllowed) {
            requestStoragePermission()
        }


    }


    fun updateOnesignal(){

        var userid  = share!!.getVal("userid")
        var request_url = share!!.urlApi
        var access_token = share!!.getVal("access_token")

        val params = JSONObject()
        try {


            params.put("player_id", playerid)

        } catch (e: JSONException) {
            e.printStackTrace()
        }

        val request = CustomJsonObjectRequestBasicAuth(Request.Method.POST,request_url+"/users/add_player_id/"+userid,params,
                Response.Listener{ response->

                    val intent = Intent(this@Login,Dashboard::class.java)
                    startActivity(intent)
                    this@Login.finish()
                },
                Response.ErrorListener{

                    var json = JSONObject (String(it.networkResponse.data , Charset.defaultCharset()))
                    SweetAlertDialog(this@Login, SweetAlertDialog.ERROR_TYPE)
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
    }

    fun callFbAPI(facebookResult: Bundle?) {

        Log.d("facebook result", facebookResult!!.toString())

        logintype = "facebook"


        val pDialog = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
        pDialog.progressHelper.barColor = Color.parseColor("#A5DC86")
        pDialog.titleText = getString(R.string.loading)
        pDialog.setCancelable(false)
        pDialog.show()


//        var share = SharePref.getInstance(applicationContext)
        var url = share!!.urlApi

        val params = JSONObject()
        try {

            params.put("password", password.text.toString())
            params.put("logintype", logintype)
//            params.put("playerid", playerid)

            params.put("email", if (facebookResult.containsKey("email")) facebookResult.getString("email") else "")

        } catch (e: JSONException) {
            e.printStackTrace()
        }

        val request = JsonObjectRequest(POST, url + "/auth/login", params,
                Response.Listener { jsonArray ->
                    var res = jsonArray
                    pDialog.dismiss()
                    Log.d("response", jsonArray.toString())

                    try {
                        var userinfo = res.getJSONObject("user").getInt("id").toString()
                        if (userinfo != "0") {
                           // share.save("USER_ID", jsonArray.getJSONObject("user").getInt("id").toString())
                            share!!.save("access_token", jsonArray.getString("access_token"))
                            share!!.save("membership", jsonArray.getJSONObject("user").getString("access"))
                            share!!.save("userid", jsonArray.getJSONObject("user").getString("userid"))
                            share!!.save("username", jsonArray.getJSONObject("user").getString("name"))
                            share!!.save("schoolname", jsonArray.getJSONObject("user").getString("school"))
                            share!!.save("imageurl", jsonArray.getJSONObject("user").getString("imageurl"))
                            share!!.save("licence", jsonArray.getJSONObject("user").getString("license"))
                            share!!.save("date", jsonArray.getJSONObject("user").getString("date"))
                            share!!.save("enddate", jsonArray.getJSONObject("user").getString("enddate"))

                            LoginManager.getInstance().logOut()

                            val intent = Intent(this@Login, Dashboard::class.java)
                            startActivity(intent)
                            this@Login.finish()

                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        share!!.clearAll()
                        var mess = res.get("message").toString()

                        SweetAlertDialog(this@Login, SweetAlertDialog.ERROR_TYPE)
                                .setTitleText(getString(R.string.oops))
                                .setContentText(mess)
                                .show()

                    }
                },

                Response.ErrorListener { error ->
                    Log.d("errorresponse", error.toString())
                    share!!.clearAll()
                    pDialog.dismiss()
                    SweetAlertDialog(this@Login, SweetAlertDialog.ERROR_TYPE)
                            .setTitleText(getString(R.string.oops))
                            .setContentText(getString(R.string.checkconnection))
                            .show()
                }
        )

        VolleySingleton.getInstance(this).addToRequestQueue(request)

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        callbackManager.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }

    //Requesting permission
    private fun requestStoragePermission() {

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE) && ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) && ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA) && ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION) && ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
            //If the user has denied the permission previously your code will come to this block
            //Here you can explain why you need this permission
            //Explain here why you need this permission
        }

        //And finally ask for the permission
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), 1002)

    }

    //This method will be called when the user will tap on allow or deny
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {

        //Checking the request code of our request
        if (requestCode == 1002) {

            //If permission is granted
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                //Displaying a toast
                Toast.makeText(this, "Permission granted now you can read the storage", Toast.LENGTH_LONG).show()
            } else {
                //Displaying another toast if permission is not granted
                Toast.makeText(this, "Oops you just denied the permission", Toast.LENGTH_LONG).show()
            }
        }
    }


    override fun onClick(view: View) {
        println("click log in")
        if (view.id == R.id.signup) {
            val intent = Intent(this@Login, Createprofile::class.java)
            startActivity(intent)
            this@Login.finish()

        }
        if (view.id == R.id.loginviafacebook) {
            loginButton.performClick()
        }

        if (view.id == R.id.login) {


            if (email.text.toString().equals("", ignoreCase = true)) {
                Snackbar.make(view, R.string.fillallthefields, Snackbar.LENGTH_LONG).show()
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


//            var share = SharePref.getInstance(applicationContext)
            var url = share!!.urlApi

            val params = JSONObject()
            try {
                params.put("email", email.text.toString())
                params.put("password", password.text.toString())
            } catch (e: JSONException) {
                e.printStackTrace()
            }

            val request = CustomJsonObjectRequestBasicAuth(POST, url +"/auth/login", params,
                    Response.Listener { res ->

                        pDialog.dismiss()
                        Log.d("response", res.toString())

                        try {
                            var userinfo = res.getJSONObject("user").getInt("id").toString()

                            if (userinfo != null) {
                               // share.save("USER_ID", jsonArray.getJSONObject("user").getInt("id").toString())
                                share!!.save("access_token", res.getString("access_token"))
                                share!!.save("membership", res.getJSONObject("user").getString("access"))
                                share!!.save("userid", res.getJSONObject("user").getInt("id").toString())
                                share!!.save("email", res.getJSONObject("user").getString("email"))
                                //share.save("schoolname", jsonArray.getJSONObject("user").getString("school")?:"")
                                share!!.save("imageurl", res.getJSONObject("user").getString("imageurl"))
                                share!!.save("licence", res.getJSONObject("user").getString("license"))
                                share!!.save("date", res.getJSONObject("user").getString("date"))
                                share!!.save("enddate", res.getJSONObject("user").getString("enddate"))
                                share!!.save("trip_id", res.getJSONObject("user").getString("trip_id"))

//                                val intent = Intent(this@Login, Dashboard::class.java)
//                                startActivity(intent)
                               // this@Login.finish()
                                updateOnesignal()

                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            share!!.clearAll()
                            var mess = res.get("message").toString()

                            SweetAlertDialog(this@Login, SweetAlertDialog.ERROR_TYPE)
                                    .setTitleText(getString(R.string.oops))
                                    .setContentText(mess)
                                    .show()

                        }
                    },

                    Response.ErrorListener { error ->
                        Log.d("errorresponse", error.toString())
                        share!!.clearAll()
                        pDialog.dismiss()
                        SweetAlertDialog(this@Login, SweetAlertDialog.ERROR_TYPE)
                                .setTitleText(getString(R.string.oops))
                                .setContentText(getString(R.string.checkconnection))
                                .show()
                    },null
            )

            VolleySingleton.getInstance(this).addToRequestQueue(request)

        }
    }


}


