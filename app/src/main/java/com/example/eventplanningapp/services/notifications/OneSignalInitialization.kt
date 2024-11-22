package com.example.eventplanningapp.services.notifications

import android.app.Application
import com.onesignal.OneSignal
import com.onesignal.debug.LogLevel


class OneSignalInitialization: Application(){
    override fun onCreate() {
        super.onCreate()
        // Initialize OneSignal
        OneSignal.initWithContext(this)
        OneSignal.Debug.logLevel = LogLevel.VERBOSE

        // OneSignal Initialization
        OneSignal.initWithContext(this, "05e7aef1-a5a7-493c-bc65-f092d7253bff")

    }
    }

