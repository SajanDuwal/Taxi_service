package com.sajan.taxiservice.controller

import android.os.AsyncTask
import com.sajan.taxiservice.protocols.OnPostResponseListener
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class InfoPostController(
    private val url: String,
    private val onPostResponseListener: OnPostResponseListener
) : AsyncTask<String, Void, String>() {


    override fun onPreExecute() {
        onPostResponseListener.onStarted(url)
    }

    override fun doInBackground(vararg params: String): String {
        return try {
            val urlToConnect = URL(url)
            val httpURLConnection = urlToConnect.openConnection() as HttpURLConnection
            httpURLConnection.requestMethod = "POST"
            httpURLConnection.addRequestProperty("Accept", "application/json")
            httpURLConnection.addRequestProperty(
                "Content-type",
                "application/x-www-form-urlencoded"
            )
            httpURLConnection.doOutput = true
            httpURLConnection.doInput = true

            val dataOutputStream = DataOutputStream(httpURLConnection.outputStream)
            dataOutputStream.writeBytes(params[0])
            dataOutputStream.flush()
            dataOutputStream.close()

            httpURLConnection.connect()

            return if (httpURLConnection.responseCode == 200) {
                val reader = BufferedReader(InputStreamReader(httpURLConnection.inputStream))
                val builder = StringBuilder()
                var line: String?
                do {
                    line = reader.readLine()
                    if (line != null) {
                        builder.append(line)
                    }
                } while (line != null)
                builder.toString()
            } else {
                val reader = BufferedReader(InputStreamReader(httpURLConnection.errorStream))
                val builder = StringBuilder()
                var line: String?
                do {
                    line = reader.readLine()
                    if (line != null) {
                        builder.append(line)
                    }
                } while (line != null)
                builder.toString()
            }
        } catch (e: Exception) {
            "Error: ${e.localizedMessage}"
        }
    }

    override fun onPostExecute(result: String?) {
        if (result != null && !result.startsWith("Error")) {
            onPostResponseListener.onComplete(result)
        } else {
            onPostResponseListener.onError(result)
        }
    }
}