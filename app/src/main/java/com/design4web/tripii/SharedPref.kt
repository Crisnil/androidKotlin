package com.design4web.tripii

import android.content.Context
import android.content.SharedPreferences
import android.app.Activity


class SharePref() //prevent creating multiple instances by making the constructor private
{

    val urlApi: String
        get() = sharedPreferences!!.getString(URL_API, "http://tripii.design4web.dk/api")

    fun getVal(key: String):String{
        return sharedPreferences!!.getString(key,"")
    }

    fun save(key: String,keyVal :String) {
        editor!!.putString(key, keyVal)
        editor!!.commit()
    }

    fun remove(key: String) {
        editor!!.remove(key)
        editor!!.commit()
    }

    fun clearAll() {
        editor!!.clear()
        editor!!.commit()
    }

    companion object {
        private val sharePref = SharePref()
        private var sharedPreferences: SharedPreferences? = null
        private var editor: SharedPreferences.Editor? = null

        private val PLACE_OBJ = "place_obj"
        private val URL_API = "http://tripii.design4web.dk/api"
        private val USER_ID = "0"

        //The context passed into the getInstance should be application level context.
        fun getInstance(context: Context): SharePref {
            if (sharedPreferences == null) {
                sharedPreferences = context.getSharedPreferences(context.packageName, Activity.MODE_PRIVATE)
                editor = sharedPreferences!!.edit()
            }
            return sharePref
        }
    }

}

