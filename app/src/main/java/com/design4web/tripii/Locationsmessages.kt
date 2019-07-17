package com.design4web.tripii

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.TextView
import com.android.volley.toolbox.ImageLoader
import com.android.volley.toolbox.NetworkImageView
import org.json.JSONArray
import org.json.JSONException

/**
 * Created by jasvi on 7/11/2018.
 */


class Locationsmessages(internal var context: Activity, internal var jsonArray: JSONArray) : BaseAdapter() {
    internal lateinit var imageLoader: ImageLoader

    override fun getCount(): Int {
        return jsonArray.length()
    }

    override fun getItem(i: Int): Any {
        return i
    }

    override fun getItemId(i: Int): Long {
        return i.toLong()
    }


    override fun getView(i: Int, view: View?, viewGroup: ViewGroup): View {


        var v = view
        if (view == null) {
            val inflater = context.layoutInflater
            v = inflater.inflate(R.layout.locations, null, true)
        }

        val username = v!!.findViewById(R.id.username) as TextView
        val userimage = v.findViewById(R.id.userimage) as NetworkImageView
        val sendmessage = v.findViewById(R.id.sendmessage) as Button

        try {
            username.text = jsonArray.getJSONObject(i).getString("username")


            val url = "http://trippr.aprosoftech.com/Resources/" + jsonArray.getJSONObject(i).getString("imageurl")


            imageLoader = CustomVolleyRequest.getInstance(context.applicationContext)
                    .imageLoader
            imageLoader.get(url, ImageLoader.getImageListener(userimage,
                    R.mipmap.ic_launcher, R.mipmap.ic_launcher))
            userimage.setImageUrl(url, imageLoader)


        } catch (e: JSONException) {
            e.printStackTrace()
        }

        sendmessage.tag = i

        sendmessage.setOnClickListener { }



        return v

    }
}
