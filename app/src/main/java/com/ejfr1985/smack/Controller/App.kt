package com.ejfr1985.smack.Controller

import android.app.Application
import com.ejfr1985.smack.Utilities.SharePrefs

class App : Application() {

    companion object {
        lateinit var prefs: SharePrefs
    }

    override fun onCreate() {

        prefs = SharePrefs(applicationContext)
        super.onCreate()
    }

}
