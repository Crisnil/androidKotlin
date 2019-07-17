package com.design4web.tripii

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.webkit.WebView
import android.widget.Button

class PrivacyPolicyActivity : AppCompatActivity() {

    internal lateinit var wv_privacy: WebView
    internal lateinit var btn_accept: Button
    internal lateinit var btn_reject: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_privacy_policy)


        wv_privacy = findViewById(R.id.wv_privacy) as WebView
        btn_accept = findViewById(R.id.btn_accept) as Button
        btn_reject = findViewById(R.id.btn_reject) as Button

        btn_accept.setOnClickListener {
            val intent = Intent(this@PrivacyPolicyActivity, Dashboard::class.java)
            startActivity(intent)
        }

        btn_reject.setOnClickListener {
            val alert = AlertDialog.Builder(this@PrivacyPolicyActivity)
            alert.setTitle("Besked")
            alert.setMessage("Du kan ikke benytte TRIPII uden at acceptere vores betingelser.")
            alert.setPositiveButton("Okay", null)
            alert.show()
        }



        wv_privacy.loadUrl("file:///android_asset/tripiiprivacy.html")

    }
}
