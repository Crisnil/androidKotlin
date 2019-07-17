package com.design4web.tripii

import android.content.Context
import android.graphics.Bitmap
import android.support.v4.util.LruCache
import com.android.volley.RequestQueue
import com.android.volley.toolbox.BasicNetwork
import com.android.volley.toolbox.DiskBasedCache
import com.android.volley.toolbox.HurlStack
import com.android.volley.toolbox.ImageLoader

/**
 * Created by jasvi on 10/30/2017.
 */

class CustomVolleyRequest private constructor(context: Context) {
    private var requestQueue: RequestQueue? = null
    val imageLoader: ImageLoader

    internal var context = context

    init {

        this.requestQueue = getRequestQueue()

        imageLoader = ImageLoader(requestQueue,
                object : ImageLoader.ImageCache {
                    private val cache = LruCache<String, Bitmap>(20)

                    override fun getBitmap(url: String?): Bitmap? {

                            return cache.get(url)
                    }
                    override fun putBitmap(url: String?, bitmap: Bitmap) {
                        cache.put(url, bitmap)
                    }
                })
    }

    fun getRequestQueue(): RequestQueue {
        if (requestQueue == null) {
            val cache = DiskBasedCache(context.cacheDir, 10 * 1024 * 1024)
            val network = BasicNetwork(HurlStack())
            requestQueue = RequestQueue(cache, network)
            requestQueue!!.start()
        }
        return requestQueue as RequestQueue
    }

    companion object {
        private var customVolleyRequest: CustomVolleyRequest? = null
        private val context: Context? = null

        @Synchronized
        fun getInstance(context: Context): CustomVolleyRequest {
            if (customVolleyRequest == null) {
                customVolleyRequest = CustomVolleyRequest(context)
            }
            return customVolleyRequest as CustomVolleyRequest
        }
    }


}
