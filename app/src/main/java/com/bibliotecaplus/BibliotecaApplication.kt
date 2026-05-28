package com.bibliotecaplus

import android.app.Application
import com.bibliotecaplus.di.initKoinAndroid

class BibliotecaApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoinAndroid(this, BuildConfig.BASE_URL)
    }
}
