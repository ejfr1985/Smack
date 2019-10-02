package com.ejfr1985.smack.Controller

import android.content.Intent
import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import android.view.View
import android.widget.Toast
import com.ejfr1985.smack.R
import com.ejfr1985.smack.Services.AuthService
import com.ejfr1985.smack.Services.UserDataService
import com.ejfr1985.smack.Utilities.BROADCAST_USER_DATA_CHANGE
import kotlinx.android.synthetic.main.activity_create_user.*
import java.util.*

class CreateUserActivity : AppCompatActivity() {

    var userAvatar = "profileDefault"
    var avatarColor = "[0.5, 0.5, 0.5, 1]"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_user)
        createSpinner.visibility = View.INVISIBLE
    }

    fun createAvatarImageViewClicked(view: View) {

        val random = Random()
        val color = random.nextInt(2)
        val avatar = random.nextInt(28)

        when (color) {
            0 -> userAvatar = "light$avatar"
            1 -> userAvatar = "dark$avatar"
        }

        val resourceId = this.resources.getIdentifier(userAvatar, "drawable", packageName)

        createAvatarImageView.setImageResource(resourceId)


    }

    fun createGenerateBackgroundBtnClicked(view: View) {

        val random = Random()
        val r = random.nextInt(255)
        val g = random.nextInt(255)
        val b = random.nextInt(255)

        createAvatarImageView.setBackgroundColor(Color.rgb(r, g, b))

        val savedR = r.toDouble() / 255
        val savedG = g.toDouble() / 255
        val savedB = b.toDouble() / 255
        avatarColor = "[$savedR, $savedG, $savedB, 1]"


    }

    fun createUserBtnClicked(view: View) {

        enableSpinner(true)

        val userName = createUserNameText.text.toString()
        val email = createEmailText.text.toString()
        val password = createPasswordText.text.toString()

        if (userName.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {

            AuthService.registerUser(this, email, password) { registerSuccess ->

                if (registerSuccess) {

                    AuthService.loginUser(this, email, password) { loginSuccess ->

                        if (loginSuccess) {

                            AuthService.createUser(this, userName, email, userAvatar, avatarColor) { createSuccess ->
                                if (createSuccess) {

                                    val userDataChange = Intent(BROADCAST_USER_DATA_CHANGE)
                                    LocalBroadcastManager.getInstance(this).sendBroadcast(userDataChange)
                                    enableSpinner(false)
                                    finish()
                                } else {
                                    errorToast("An error occurred while creating your user")
                                }
                            }

                        } else {
                            errorToast("An error occurred while logging in your user")
                        }
                    }

                } else {
                    errorToast("An error occurred while registering in your user")
                }
            }
        } else {
            errorToast("All fields are required, please fill them in")
        }


    }


    fun errorToast(msg: String) {
        enableSpinner(false)
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }

    fun enableSpinner(enable: Boolean) {

        if (enable) {
            createSpinner.visibility = View.VISIBLE
        } else {
            createSpinner.visibility = View.INVISIBLE
        }

        createUserNameText.isEnabled = !enable
        createEmailText.isEnabled = !enable
        createPasswordText.isEnabled = !enable

    }


}
