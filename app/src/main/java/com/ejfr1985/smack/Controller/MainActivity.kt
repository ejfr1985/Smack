package com.ejfr1985.smack.Controller

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Bundle
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v4.widget.DrawerLayout
import android.support.design.widget.NavigationView
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.EditText
import com.ejfr1985.smack.Adapters.MessageAdapter
import com.ejfr1985.smack.Model.Channel
import com.ejfr1985.smack.Model.Message
import com.ejfr1985.smack.R
import com.ejfr1985.smack.Services.AuthService
import com.ejfr1985.smack.Services.MessageService
import com.ejfr1985.smack.Services.UserDataService
import com.ejfr1985.smack.Utilities.BROADCAST_USER_DATA_CHANGE
import com.ejfr1985.smack.Utilities.SOCKET_URL
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.nav_header_main.*
import java.util.zip.Inflater

class MainActivity : AppCompatActivity() {

    val socket: Socket = IO.socket(SOCKET_URL)
    lateinit var channelAdapter: ArrayAdapter<Channel>
    lateinit var messageAdapter: MessageAdapter
    var selectedChannel: Channel? = null

    private fun setupAdapters() {
        channelAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, MessageService.channels)
        channel_list.adapter = channelAdapter
        messageAdapter = MessageAdapter(this, MessageService.messages)
        messageListView.adapter = messageAdapter
        val layoutManager = LinearLayoutManager(this)
        messageListView.layoutManager = layoutManager

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        socket.connect()
        socket.on("channelCreated", onNewChannel)
        socket.on("messageCreated", onNewMessage)

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        setupAdapters()
        LocalBroadcastManager.getInstance(this).registerReceiver(
            userDataChangeReceiver,
            IntentFilter(BROADCAST_USER_DATA_CHANGE)
        )

        channel_list.setOnItemClickListener { _, _, i, _ ->

            selectedChannel = MessageService.channels[i]
            drawer_layout.closeDrawer(GravityCompat.START)
            updateWithChannel()
        }

        if (App.prefs.isLoggedIn) {
            AuthService.findUserByEmail(this) {}
        }


    }

    override fun onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(userDataChangeReceiver)
        socket.disconnect()
        super.onDestroy()
    }

    private val userDataChangeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            if (App.prefs.isLoggedIn) {
                userNameNavBar.text = UserDataService.name
                userEmailNavBar.text = UserDataService.email
                val resourceId = resources.getIdentifier(UserDataService.avatarName, "drawable", packageName)
                userImageNavBar.setImageResource(resourceId)
                userImageNavBar.setBackgroundColor(UserDataService.returnAvatarColor(UserDataService.avatarColor))
                loginNavBarBtn.text = "Logout"

                MessageService.getChannels() { complete ->
                    if (complete) {

                        if (MessageService.channels.count() > 0) {
                            selectedChannel = MessageService.channels[0]
                            channelAdapter.notifyDataSetChanged()
                            updateWithChannel()
                        }
                    }
                }
            }
        }
    }

    fun updateWithChannel() {

        mainChannelName.text = "#${selectedChannel?.name}"
        // will download selected channel messages

        if (selectedChannel != null) {

            MessageService.getMessages(selectedChannel!!.id) { complete ->

                if (complete) {
                    messageAdapter.notifyDataSetChanged()
                    if (messageAdapter.itemCount > 0) {
                        messageListView.smoothScrollToPosition(messageAdapter.itemCount - 1)
                    }
                }
            }
        }
    }

    override fun onBackPressed() {
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    fun addChannelBtnClicked(view: View) {

        if (App.prefs.isLoggedIn) {

            val builder = AlertDialog.Builder(this)
            val dialogView = layoutInflater.inflate(R.layout.add_channel_dialog, null)

            builder.setView(dialogView)
                .setPositiveButton("Add") { _, _ ->

                    val nameTextField = dialogView.findViewById<EditText>(R.id.addChannelNameText)
                    val descTextField = dialogView.findViewById<EditText>(R.id.addChannelDescText)
                    val channelName = nameTextField.text.toString()
                    val channelDesc = descTextField.text.toString()


                    socket.emit("newChannel", channelName, channelDesc)

                }
                .setNegativeButton("Cancel") { _, _ ->

                }
                .show()
        }
    }

    private val onNewChannel = Emitter.Listener { args ->

        if (App.prefs.isLoggedIn) {
            runOnUiThread {
                val channelName = args[0].toString()
                val channelDescription = args[1].toString()
                val channelId = args[2].toString()
                val newChannel = Channel(channelName, channelDescription, channelId)
                MessageService.channels.add(newChannel)
                channelAdapter.notifyDataSetChanged()
            }

        }
    }

    private val onNewMessage = Emitter.Listener { args ->


        if (App.prefs.isLoggedIn) {
            runOnUiThread {
                val channelId = args[2].toString()
                if (channelId == selectedChannel?.id) {
                    val messageBody = args[0].toString()
                    val userId = args[1].toString()
                    val userName = args[3].toString()
                    val userAvatar = args[4].toString()
                    val userAvatarColor = args[5].toString()
                    val id = args[6].toString()
                    val timeStamp = args[7].toString()

                    val newMessage =
                        Message(messageBody, userName, channelId, userAvatar, userAvatarColor, id, timeStamp)
                    MessageService.messages.add(newMessage)
                    messageAdapter.notifyDataSetChanged()
                    messageListView.smoothScrollToPosition(messageAdapter.itemCount - 1)

                }

            }
        }


    }


    fun loginNavBarClicked(view: View) {
        if (App.prefs.isLoggedIn) {
            UserDataService.logout()
            channelAdapter.notifyDataSetChanged()
            messageAdapter.notifyDataSetChanged()

            userNameNavBar.text = ""
            userEmailNavBar.text = ""
            userImageNavBar.setImageResource(R.drawable.profiledefault)
            userImageNavBar.setBackgroundColor(Color.TRANSPARENT)
            loginNavBarBtn.text = "Login"
            mainChannelName.text = "Please Log In"


        } else {
            val loginIntent = Intent(this, LoginActivity::class.java)
            startActivity(loginIntent)
        }


    }

    fun sendMsgBtnClicked(view: View) {

        if (App.prefs.isLoggedIn && messageTextField.text.isNotEmpty() && selectedChannel != null) {

            val userId = UserDataService.id
            val channelId = selectedChannel!!.id
            socket.emit(
                "newMessage", messageTextField.text.toString(), userId, channelId,
                UserDataService.name, UserDataService.avatarName, UserDataService.avatarColor
            )
            messageTextField.text.clear()
            hideKeyboard()
        }


    }

    fun hideKeyboard() {
        val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if (inputManager.isAcceptingText) {
            inputManager.hideSoftInputFromWindow(currentFocus.windowToken, 0)
        }
    }

}
