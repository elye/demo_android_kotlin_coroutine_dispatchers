package com.example.coroutinerevise

import android.app.Application

class MainApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        System.setProperty("kotlinx.coroutines.debug", "on" )
    }
}