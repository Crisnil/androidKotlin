package com.design4web.tripii.common

//import com.sun.xml.internal.ws.streaming.XMLStreamWriterUtil.getOutputStream
import android.content.Context
import com.design4web.tripii.SharePref
import java.io.*
import java.net.HttpURLConnection
import java.net.URL


class Uploader {

    @Throws(IOException::class)

    fun upload(ctx: Context,selectedPath: String) {

        var shared = SharePref.getInstance(ctx)

        var connection: HttpURLConnection? = null
        var outputStream: DataOutputStream? = null
        val inputStream: DataInputStream? = null

        val urlServer = shared.urlApi
        val access_token = shared.getVal("ACCESS_TOKEN")
        val lineEnd = "\r\n"
        val twoHyphens = "--"
        val boundary = "*****"

        var bytesRead: Int
        var bytesAvailable: Int
        var bufferSize: Int
        val buffer: ByteArray
        val maxBufferSize = 1 * 1024 * 1024

        try {
            val fileInputStream = FileInputStream(File(
                    selectedPath))

            val url = URL(urlServer)
            connection = url.openConnection() as HttpURLConnection

            connection!!.setDoInput(true)
            connection!!.setDoOutput(true)
            connection!!.setUseCaches(false)

            connection!!.setRequestMethod("POST")
            connection.setRequestProperty("Accept","application/json")
            connection.setRequestProperty("Authorization","Bearer "+access_token)
            connection!!.setRequestProperty("Connection", "Keep-Alive")
            connection!!.setRequestProperty("Content-Type",
                    "multipart/form-data;boundary=$boundary")

            outputStream = DataOutputStream(connection!!.getOutputStream())
            outputStream!!.writeBytes(twoHyphens + boundary + lineEnd)
            outputStream!!
                    .writeBytes("Content-Disposition: form-data; name=\"uploadedfile\";filename=\""
                            + selectedPath + "\"" + lineEnd)
            outputStream!!.writeBytes(lineEnd)

            bytesAvailable = fileInputStream.available()
            bufferSize = Math.min(bytesAvailable, maxBufferSize)
            buffer = ByteArray(bufferSize)

            bytesRead = fileInputStream.read(buffer, 0, bufferSize)

            while (bytesRead > 0) {
                outputStream!!.write(buffer, 0, bufferSize)
                bytesAvailable = fileInputStream.available()
                bufferSize = Math.min(bytesAvailable, maxBufferSize)
                bytesRead = fileInputStream.read(buffer, 0, bufferSize)
            }

            outputStream!!.writeBytes(lineEnd)
            outputStream!!.writeBytes(twoHyphens + boundary + twoHyphens
                    + lineEnd)

            val serverResponseCode = connection!!.getResponseCode()
            val serverResponseMessage = connection!!.getResponseMessage()

            fileInputStream.close()
            outputStream!!.flush()
            outputStream!!.close()
        } catch (ex: Exception) {
        }

    }
}