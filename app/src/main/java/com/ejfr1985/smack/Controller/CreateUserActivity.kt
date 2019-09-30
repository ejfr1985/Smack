package com.ejfr1985.smack.Controller

import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.ejfr1985.smack.R
import com.ejfr1985.smack.Services.AuthService
import kotlinx.android.synthetic.main.activity_create_user.*
import java.util.*

class CreateUserActivity : AppCompatActivity() {

    var userAvatar = "profileDefault"
    var avatarColor = "[0.5, 0.5, 0.5, 1]"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_user)
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

        AuthService.registerUser(this, "E@E.com", "123456") { complete ->
            println(complete)
        }

    }
}
