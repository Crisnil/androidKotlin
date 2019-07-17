package com.design4web.tripii

import android.content.Context
import android.os.AsyncTask
import com.design4web.tripii.common.Loading
import okhttp3.*
import java.io.File
import java.util.concurrent.TimeUnit


class AsyncOkhttp (context :Context): AsyncTask<Void,Void,String>(){

    internal var context = context
    val pDialog = Loading().dialog(context, "Uploading")

    override fun onPostExecute(result: String?) {
        pDialog.show()
    }
    override fun doInBackground(vararg p0: Void?): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


    fun uploading(finalFile:File) {
        var shared = SharePref.getInstance(context)
        val access_token = shared.getVal("access_token")
        var request_url = shared.urlApi
        var user_id = shared.getVal("userid")
        val connection = finalFile.toURL().openConnection()
        val mimeType = connection.getContentType()

        val client = OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build()


        var MEDIA_TYPE_PNG = MediaType.parse(mimeType)

        var requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("image", finalFile.getName(), RequestBody.create(MEDIA_TYPE_PNG, finalFile))
                .build()

        var request = Request.Builder()
                .url(request_url + "/users/upload_pic/" + user_id)
                .header("Authorization", "Bearer " + access_token)
                .header("Accept", "application/json")
                .post(requestBody)
                .build()

        try {

            var response = client.newCall(request).execute()

        } catch (e: Exception) {

            e.printStackTrace()
        }finally {
            pDialog.dismiss()
        }
    }

}