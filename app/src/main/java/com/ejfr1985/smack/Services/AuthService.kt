package com.ejfr1985.smack.Services

import android.content.Context
import android.util.Log
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.ejfr1985.smack.Utilities.URL_LOGIN
import com.ejfr1985.smack.Utilities.URL_REGISTER
import org.json.JSONException
import org.json.JSONObject

object AuthService {

    var userEmail = ""
    var authToken = ""
    var isLoggedIn = false

    fun registerUser(context: Context, email: String, password: String, complete: (Boolean) -> Unit) {

        val jsonBody = JSONObject()
        jsonBody.put("email", email)
        jsonBody.put("password", password)
        val requestBody = jsonBody.toString()

        val registerRequest = object : StringRequest(Method.POST, URL_REGISTER, Response.Listener { response ->
            Log.d("Register-SUCCESS", response)
            complete(true)
        }, Response.ErrorListener { error ->
            Log.d("Register-ERROR", "Could not register user: $error")
            complete(false)
        }) {
            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }

            override fun getBody(): ByteArray {
                return requestBody.toByteArray()
            }
        }

        Volley.newRequestQueue(context).add(registerRequest)

    }

    fun loginUser(context: Context, email: String, password: String, complete: (Boolean) -> Unit) {

        val jsonBody = JSONObject()
        jsonBody.put("email", email)
        jsonBody.put("password", password)
        val requestBody = jsonBody.toString()

        val loginRequest = object : JsonObjectRequest(Method.POST, URL_LOGIN, null, Response.Listener { response ->


            try {

                userEmail = response.getString("user")
                authToken = response.getString("token")
                isLoggedIn = true

            } catch (e: JSONException) {

                Log.d("JSON-ERROR", "EXC-LOGIN: ${e.localizedMessage}")
                complete(false)
            }

            complete(true)

        }, Response.ErrorListener { error ->

            Log.d("Login-ERROR", "Could not login user: $error")
            complete(false)
        }) {

            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }

            override fun getBody(): ByteArray {
                return requestBody.toByteArray()
            }

        }

        Volley.newRequestQueue(context).add(loginRequest)
    }
}