package com.bibliotecaplus.di

import android.content.Context
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.dsl.module

val androidModule = module {
    single<Settings> {
        val prefs = get<Context>().getSharedPreferences("bibliotecaplus_prefs", Context.MODE_PRIVATE)
        SharedPreferencesSettings(prefs)
    }
}

fun initKoinAndroid(context: Context, baseUrl: String) {
    startKoin {
        androidContext(context)
        modules(androidModule, sharedModule(baseUrl))
    }
}
