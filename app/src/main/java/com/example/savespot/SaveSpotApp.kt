package com.example.savespot

import android.app.Application

class SaveSpotApp:Application() {
    val db by lazy {
        SaveSpotDatabase.getInstance(this)
    }
}