package com.ejfr1985.smack.Services

import android.util.Log
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.ejfr1985.smack.Controller.App
import com.ejfr1985.smack.Model.Channel
import com.ejfr1985.smack.Model.Message
import com.ejfr1985.smack.Utilities.URL_GET_CHANNELS
import com.ejfr1985.smack.Utilities.URL_GET_MESSAGES
import org.json.JSONException

object MessageService {

    val channels = ArrayList<Channel>()
    val messages = ArrayList<Message>()

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

    fun getMessages(channelId: String, complete: (Boolean) -> Unit){

        val url = "$URL_GET_MESSAGES$channelId"
        val messageRequest = object : JsonArrayRequest(url, Response.Listener { response ->
            clearMessages()
            try {

                for (x in 0 until response.length()){
                    val message = response.getJSONObject(x)
                    val messageBody = message.getString("messageBody")
                    val channelId = message.getString("channelId")
                    val id = message.getString("_id")
                    val userName = message.getString("userName")
                    val userAvatar = message.getString("userAvatar")
                    val userAvatarColor = message.getString("userAvatarColor")
                    val timeStamp = message.getString("timeStamp")

                    val newMessage = Message(messageBody,userName, channelId, userAvatar, userAvatarColor, id, timeStamp)
                    this.messages.add(newMessage)
                }

                complete(true)


            } catch (e: JSONException) {

                Log.d("JSONChannels-Error", "Error: " + e.localizedMessage)
                complete(false)

            }

        }, Response.ErrorListener { error ->
            Log.d("Channels-Error", "Could not retrieve channel Messages")
        }){
            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }

            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers.put("Authorization", "Bearer ${App.prefs.authToken}")
                return headers
            }
        }

        App.prefs.requestQueue.add(messageRequest)
    }


    fun clearMessages(){
        messages.clear()
    }

    fun clearChannels(){
        channels.clear()
    }
}