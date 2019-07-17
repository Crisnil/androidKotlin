package com.design4web.tripii

import android.content.Context
import android.support.multidex.MultiDex
import android.support.multidex.MultiDexApplication


/**
 * Created by jasvi on 4/4/2018.
 */

class MyApplication : MultiDexApplication() {


    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    override fun onCreate() {
        super.onCreate()


    }
}
