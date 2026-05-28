package com.bibliotecaplus

import com.bibliotecaplus.di.iosModule
import com.bibliotecaplus.di.sharedModule
import org.koin.core.context.startKoin

fun initKoin(baseUrl: String) {
    startKoin {
        modules(iosModule, sharedModule(baseUrl))
    }
}
