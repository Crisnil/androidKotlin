package com.design4web.tripii

import android.annotation.SuppressLint
import com.android.volley.*
import com.android.volley.toolbox.HttpHeaderParser
import com.android.volley.toolbox.JsonObjectRequest
import org.json.JSONException
import org.json.JSONObject
import java.io.UnsupportedEncodingException
import java.nio.charset.Charset


class CustomJsonObjectRequestBasicAuth(
        method:Int, url: String,
        jsonObject: JSONObject?,
        listener: Response.Listener<JSONObject>,
        errorListener: Response.ErrorListener,
        credentials:String?
)
    : JsonObjectRequest(method,url, jsonObject, listener, errorListener) {


    private var mCredentials:String? = credentials

    @Throws(AuthFailureError::class)

    override fun getHeaders(): Map<String, String> {
        val headers = HashMap<String, String>()
        headers["Accept"] = "application/json"
        //val credentials:String = "username:password"
        if (mCredentials != null) {
//            val auth = "Bearer " + Base64.encodeToString(mCredentials!!.toByteArray(),
//                    Base64.NO_WRAP)
            val auth = "Bearer "+ mCredentials
        headers["Authorization"] = auth
        }

        return headers
    }

 @SuppressLint("NewApi")
 override fun parseNetworkResponse(response: NetworkResponse): Response<JSONObject> {
    try {
        var cacheEntry: Cache.Entry? = HttpHeaderParser.parseCacheHeaders(response)
        if (cacheEntry == null) {
            cacheEntry = Cache.Entry()
        }
        val cacheHitButRefreshed = (3 * 60 * 1000).toLong() // in 3 minutes cache will be hit, but also refreshed on background
        val cacheExpired = (24 * 60 * 60 * 1000).toLong() // in 24 hours this cache entry expires completely
        val now = System.currentTimeMillis()
        val softExpire = now + cacheHitButRefreshed
        val ttl = now + cacheExpired
        cacheEntry!!.data = response.data
        cacheEntry!!.softTtl = softExpire
        cacheEntry!!.ttl = ttl
        var headerValue: String?

        headerValue = response.headers["Date"]
        if (headerValue != null) {
            cacheEntry!!.serverDate = HttpHeaderParser.parseDateAsEpoch(headerValue)
        }
        headerValue = response.headers["Last-Modified"]
        if (headerValue != null) {
            cacheEntry!!.lastModified = HttpHeaderParser.parseDateAsEpoch(headerValue)
        }

        response.headers["Expires"] = headerValue

        cacheEntry!!.responseHeaders = response.headers

//        val now = System.currentTimeMillis()
//
//        val headers = response.headers
//        var serverDate: Long = 0
//        var serverEtag: String? = null
//        val headerValue: String?
//
//        headerValue = headers["Date"]
//        if (headerValue != null) {
//            serverDate = HttpHeaderParser.parseDateAsEpoch(headerValue)
//        }
//
//        serverEtag = headers["ETag"]
//
//        val cacheHitButRefreshed = (3 * 60 * 1000).toLong() // in 3 minutes cache will be hit, but also refreshed on background
//        val cacheExpired = (24 * 60 * 60 * 1000).toLong() // in 24 hours this cache entry expires completely
//        val softExpire = now + cacheHitButRefreshed
//        val ttl = now + cacheExpired
//
//        val entry = Cache.Entry()
//
//        entry.data = response.data
//        entry.etag = serverEtag
//        entry.softTtl = softExpire
//        entry.ttl = ttl
//        entry.serverDate = serverDate
//        entry.responseHeaders = headers

        var jsonString = String(response!!.data, Charset.defaultCharset())
            return Response.success(JSONObject(jsonString),
                    HttpHeaderParser
                            .parseCacheHeaders(response))

        return Response.success(JSONObject(jsonString),cacheEntry)
    } catch (e: UnsupportedEncodingException) {
        return Response.error(ParseError(e))
    } catch (e: JSONException) {
        return Response.error(ParseError(e))
    }

}

    override fun deliverResponse(response: JSONObject) {
        super.deliverResponse(response)
    }

    override fun deliverError(error: VolleyError) {
        super.deliverError(error)
    }

    override fun parseNetworkError(volleyError: VolleyError): VolleyError? {
        return super.parseNetworkError(volleyError)

    }

}
