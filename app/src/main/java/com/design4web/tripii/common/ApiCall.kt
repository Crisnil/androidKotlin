package com.design4web.tripii.common

import android.R.string
import android.os.AsyncTask.execute
import okhttp3.*
import org.json.JSONObject
import java.io.IOException


object ApiCall {

    //GET network request
    @Throws(IOException::class)
    fun GET(client: OkHttpClient, url: HttpUrl): String {
        val request = Request.Builder()
                .url(url)
                .build()
        val response = client.newCall(request).execute()
        return response.body()!!.string()
    }

    //POST network request
    @Throws(IOException::class)
    fun POST(client: OkHttpClient, url: HttpUrl, body: RequestBody): Response {
        val request = Request.Builder()
                .url(url)
                .post(body)
                .build()
        return  client.newCall(request).execute()

    }
}