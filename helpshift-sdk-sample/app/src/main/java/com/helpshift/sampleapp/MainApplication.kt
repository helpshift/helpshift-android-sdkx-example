package com.helpshift.sampleapp

import android.app.Application
import android.os.Build
import android.util.Log
import com.helpshift.Helpshift
import com.helpshift.UnsupportedOSVersionException

class MainApplication: Application() {

    override fun onCreate() {
        super.onCreate()

        try {
            Helpshift.install(
                this,
                "platform_id",
                "<your-domain>.helpshift.com",
                getHelpshiftInstallConfig()
            )
        } catch (e: UnsupportedOSVersionException) {
            Log.e(
                "MainApp",
                "install() called on the OS version: " + Build.VERSION.SDK_INT + " is not supported"
            )
        }
    }

    fun getHelpshiftInstallConfig(): Map<String, Any>{
        val config = mutableMapOf<String, Any>()
        config["enableLogging"] = true
        config["enableInAppNotification"] = true
        // Replace with your own values
//        config["notificationSoundId"] = R.raw.custom_notification
//        config["notificationIcon"] = R.drawable.hs__chat_icon
//        config["notificationChannelId"] = CHANNEL_ID
//        config["notificationLargeIcon"] = R.drawable.airplane
        return config
    }
}