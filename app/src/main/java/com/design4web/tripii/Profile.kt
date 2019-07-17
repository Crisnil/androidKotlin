package com.design4web.tripii

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.CursorLoader
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import cn.pedant.SweetAlert.SweetAlertDialog
import com.android.volley.Request
import com.android.volley.Request.Method.GET
import com.android.volley.Response
import com.android.volley.toolbox.ImageLoader
import com.android.volley.toolbox.NetworkImageView
import com.android.volley.toolbox.Volley
import com.design4web.tripii.common.Loading
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.charset.Charset


class Profile : AppCompatActivity(), View.OnClickListener {

    internal lateinit var emailname: EditText
    internal lateinit var schoolname: EditText
    internal lateinit var username: EditText
    internal lateinit var update: Button
    internal lateinit var userimage: NetworkImageView
    internal lateinit var imageLoader: ImageLoader
    internal  var userid: String? = null
    internal var imageurl: String?=null
    internal var images: ByteArray?=null
    internal lateinit var membership: String
    internal lateinit var licencecode: String
    internal lateinit var licence: TextView
    internal lateinit var membershiptext: TextView
    internal var finalFile: String?= null
    internal var share:SharePref? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val actionBar = supportActionBar
        actionBar!!.hide()

//        schoolname = findViewById(R.id.schoolname) as EditText
        emailname = findViewById(R.id.emailname) as EditText
        username = findViewById(R.id.username) as EditText
        userimage = findViewById(R.id.userimage) as NetworkImageView

        update = findViewById(R.id.update) as Button

        licence = findViewById(R.id.licence) as TextView
        membershiptext = findViewById(R.id.membershiptext) as TextView


//        val editor = getSharedPreferences("userid", Context.MODE_PRIVATE)
//        userid = editor.getString("userid", "")
//
//        schoolname.setText(editor.getString("schoolname", ""))
//        username.setText(editor.getString("username", ""))
//
//        imageurl = editor.getString("imageurl", "")
//
//        membership = editor.getString("membership", "")
//        licencecode = editor.getString("licence", "")
//
//        if (membership.equals("", ignoreCase = true)) {
//            membershiptext.visibility = View.GONE
//            licence.visibility = View.GONE
//        }
//        if (membership.equals("trial", ignoreCase = true)) {
//            membershiptext.setText(R.string.trial)
//            licence.visibility = View.GONE
//        }
//        if (membership.equals("fullaccess", ignoreCase = true)) {
//            membershiptext.setText(R.string.fullaccess)
//            licence.text = licencecode
//        }

        val requestQueue = Volley.newRequestQueue(this@Profile)
        if(share == null) {
            share = SharePref.getInstance(applicationContext)
        }
        var request_url = share!!.urlApi
        var access_token = share!!.getVal("access_token")

        userid = share!!.getVal("userid")
        imageurl = share!!.getVal("imageurl")

        if(imageurl!= "null"){
            imageLoader = CustomVolleyRequest.getInstance(applicationContext)
                    .imageLoader
            imageLoader.get(imageurl, ImageLoader.getImageListener(userimage,
                    R.mipmap.ic_launcher, R.mipmap.ic_launcher))
            userimage.setImageUrl(imageurl, imageLoader)
        }


        val request = CustomJsonObjectRequestBasicAuth(GET,request_url+"/users/"+userid,null,
                Response.Listener{ response->
                    Log.d("respnse","profile fetch : " +response.toString())
                    username.setText((response.getJSONObject("meta").getJSONArray("metadata").get(0) as JSONObject).getString("name"))
                    membershiptext.setText((response.getJSONObject("meta").getJSONArray("metadata").get(0) as JSONObject).getString("access"))
                    imageurl = response.getJSONObject("meta").getJSONArray("metadata").getJSONObject(0).getString("imageurl")
                    emailname.setText((response.getJSONObject("meta").getJSONArray("metadata").get(0) as JSONObject).getString("email"))
                    //TODO: Consider using String utils null or empty string
                    if(imageurl!= "null"){
                        imageLoader = CustomVolleyRequest.getInstance(applicationContext)
                                .imageLoader
                        imageLoader.get(imageurl, ImageLoader.getImageListener(userimage,
                                R.mipmap.ic_launcher, R.mipmap.ic_launcher))
                        userimage.setImageUrl(imageurl, imageLoader)
                    }
                },
                Response.ErrorListener{
                    SweetAlertDialog(this@Profile, SweetAlertDialog.ERROR_TYPE)
                            .setTitleText("opps")
                            .setContentText("failed")
                            .setConfirmClickListener { sweetAlertDialog ->
                                sweetAlertDialog.dismiss()
                                val intent = Intent(this@Profile,Login::class.java)
                                startActivity(intent)
                                this@Profile.finish()
                            }
                            .show()

                  },
                access_token
                )

        requestQueue.add(request)

            update.setOnClickListener(this)
            userimage.setOnClickListener(this)


    }


    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && requestCode == 1) {

            val bitmap = data.extras.get("data") as Bitmap
            var tempUri = getImageUri(getApplicationContext(), bitmap)
             finalFile = getPath(tempUri)
            userimage.setImageBitmap(bitmap)
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            images = baos.toByteArray()
//            imageurl = Base64.encodeToString(images, Base64.DEFAULT)

            updateimage()

        } else if (resultCode == Activity.RESULT_OK && requestCode == 2) {
            val selectedImage = data.data

            //imagePath = getPath(selectedImage)
            val filePath = arrayOf(MediaStore.Images.Media.DATA)
            val c = this.contentResolver.query(selectedImage, filePath, null, null, null)
            c.moveToFirst()
            val columnIndex = c.getColumnIndex(filePath[0])
            val picturePath = c.getString(columnIndex)
            c.close()
            val thumbnail = BitmapFactory.decodeFile(picturePath)
             finalFile = picturePath
            
            val nh = (thumbnail.height * (512.0 / thumbnail.width)).toInt()
            val scaled = Bitmap.createScaledBitmap(thumbnail, 512, nh, true)
            userimage.setImageBitmap(scaled)
            val baos = ByteArrayOutputStream()
            scaled.compress(Bitmap.CompressFormat.JPEG, 100, baos)
             images = baos.toByteArray()
//            imageurl = Base64.encodeToString(images, Base64.DEFAULT)
            updateimage()
        }
    }

    private fun updateimage() {

//        if (username.text.toString().equals("", ignoreCase = true)) {
//            Toast.makeText(this@Profile, R.string.fillallthefields, Toast.LENGTH_LONG).show()
//            return
//        }
//
//        if (schoolname.text.toString().equals("", ignoreCase = true)) {
//            Toast.makeText(this@Profile, R.string.fillallthefields, Toast.LENGTH_LONG).show()
//            return
//        }

        val pDialog = Loading().dialog(this, "Uploading")
        pDialog.show()
            try {
                AsyncOkhttp(applicationContext).uploading(File(finalFile))
            } catch (e: Exception) {
                e.printStackTrace()

            }
        pDialog.dismiss()
    }

    override fun onClick(view: View) {


        if (view.id == R.id.userimage) {
            val options = arrayOf<CharSequence>("Take Photo", "Choose from Gallery", "Cancel")
            val builder = AlertDialog.Builder(this@Profile)
            builder.setTitle("Add Photo!")
            builder.setItems(options) { dialog, item ->
                if (options[item] == "Take Photo") {
                    val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    startActivityForResult(intent, 1)
                } else if (options[item] == "Choose from Gallery") {
                    val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    startActivityForResult(intent, 2)

                } else if (options[item] == "Cancel") {
                    dialog.dismiss()
                }
            }
            builder.show()


        }

        if (view.id == R.id.update) {

            if (update.text.toString().equals("edit profile", ignoreCase = true)) {
                username.isEnabled = true
                emailname.isEnabled = true
               // schoolname.isEnabled = true
                update.setText(R.string.updateprofile)
            } else {

                if (username.text.toString().equals("", ignoreCase = true)) {
                    Snackbar.make(view, R.string.fillallthefields, Snackbar.LENGTH_LONG).show()
                    return
                }
//
//                if (schoolname.text.toString().equals("", ignoreCase = true)) {
//                    Snackbar.make(view, R.string.fillallthefields, Snackbar.LENGTH_LONG).show()
//                    return
//                }


                val pDialog = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
                pDialog.progressHelper.barColor = Color.parseColor("#A5DC86")
                pDialog.titleText = "Loading"
                pDialog.setCancelable(false)
                pDialog.show()

                var request_url = share!!.urlApi
                var access_token = share!!.getVal("access_token")

                val params = JSONObject()
                try {

                    params.put("name", username.text.toString())
//                    params.put("school_id", username.text.toString())

                }catch (e:Exception){
                    e.printStackTrace()
                }

                    val request = CustomJsonObjectRequestBasicAuth(Request.Method.POST,request_url+"/users/update/"+userid,params,
                        Response.Listener{ response->
                                 pDialog.dismiss()
                            update.setText(R.string.editprofile)
                            username.isEnabled = false
                            emailname.isEnabled = false
                            //schoolname.isEnabled = false
                            try {
                                share!!.save("userid", response.getJSONObject("meta").getJSONObject("data").getString("id"))
                                share!!.save("username", response.getJSONObject("meta").getJSONObject("data").getString("name"))
                                share!!.save("imageurl", response.getJSONObject("meta").getJSONObject("data").getString("imageurl"))

                                imageurl = response.getJSONObject("meta").getJSONObject("data").getString("imageurl")


                            }catch (e: Exception){
                                e.printStackTrace()
                              Log.d("exemption ",e.printStackTrace().toString())

                                SweetAlertDialog(this@Profile, SweetAlertDialog.ERROR_TYPE)
                                        .setTitleText(getString(R.string.oops))
                                        .setContentText("Something went wrong")
                                        .show()
                            }


                            imageLoader = CustomVolleyRequest.getInstance(applicationContext)
                                    .imageLoader
                            imageLoader.get(imageurl, ImageLoader.getImageListener(userimage,
                                    R.mipmap.ic_launcher, R.mipmap.ic_launcher))
                            userimage.setImageUrl(imageurl, imageLoader)

                            SweetAlertDialog(this@Profile, SweetAlertDialog.SUCCESS_TYPE)
                                    .setTitleText(getString(R.string.congrats))
                                    .setContentText(getString(R.string.profileupdate))
                                    .show()
                        },
                        Response.ErrorListener{
                            update.setText(R.string.editprofile)
                            username.isEnabled = false
                            emailname.isEnabled = false
                           // schoolname.isEnabled = false
                            pDialog.dismiss()
                            var json = JSONObject (String(it.networkResponse.data , Charset.defaultCharset()))
                            SweetAlertDialog(this@Profile, SweetAlertDialog.ERROR_TYPE)
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

        }

    }

    private fun getImageUri(inContext:Context,inImage:Bitmap): Uri {
        var bytes = ByteArrayOutputStream()
    inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
    var path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null)
    return Uri.parse(path)
}

    private fun getPath(contentUri: Uri): String {
        val proj = arrayOf(MediaStore.Images.Media.DATA)
        val loader = CursorLoader(applicationContext, contentUri, proj, null, null, null)
        val cursor = loader.loadInBackground()
        val column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        cursor.moveToFirst()
        val result = cursor.getString(column_index)
        cursor.close()
        return result
    }
}











