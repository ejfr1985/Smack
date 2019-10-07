package com.ejfr1985.smack.Services

import android.content.Context
import android.util.Log
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.ejfr1985.smack.Controller.App
import com.ejfr1985.smack.Model.Channel
import com.ejfr1985.smack.Utilities.URL_GET_CHANNELS
import org.json.JSONException

object MessageService {

    val channels = ArrayList<Channel>()

    fun getChannels(complete: (Boolean) -> Unit) {
        val channelsRequest = object : JsonArrayRequest(URL_GET_CHANNELS,
            Response.Listener { response ->

                try {

                    for (x in 0 until response.length()){
                        val channel = response.getJSONObject(x)
                        val name = channel.getString("name")
                        val description = channel.getString("description")
                        val id = channel.getString("_id")

                        val newChannel = Channel(name, description, id)
                        this.channels.add(newChannel)
                    }

                    complete(true)


                } catch (e: JSONException) {

                    Log.d("JSONChannels-Error", "Error: " + e.localizedMessage)
                    complete(false)

                }
            },
            Response.ErrorListener { error ->
                Log.d("Channels-Error", "Could not retrieve channels")
            }) {

            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }

            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers.put("Authorization", "Bearer ${App.prefs.authToken}")
                return headers
            }
        }

        App.prefs.requestQueue.add(channelsRequest)
    }

}