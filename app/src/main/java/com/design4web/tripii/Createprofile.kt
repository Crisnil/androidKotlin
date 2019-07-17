package com.design4web.tripii

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Button

class Createprofile : AppCompatActivity() {

    internal lateinit var createteacherprofile: Button
    internal lateinit var createstudentprofile: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_createprofile)

        val actionBar = supportActionBar
        actionBar!!.hide()

        createteacherprofile = findViewById(R.id.signupteacher) as Button
        createstudentprofile = findViewById(R.id.signupstudent) as Button

        createteacherprofile.setOnClickListener {
            val intent = Intent(this@Createprofile, Createteacherprofile::class.java)
            startActivity(intent)
            this@Createprofile.finish()
        }
        createstudentprofile.setOnClickListener {
            val intent2 = Intent(this@Createprofile, Createstudentprofile::class.java)
            startActivity(intent2)
            this@Createprofile.finish()
        }


    }
}
