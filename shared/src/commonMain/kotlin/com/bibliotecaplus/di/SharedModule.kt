package com.bibliotecaplus.di

import com.bibliotecaplus.data.api.BibliotecaApiClient
import com.bibliotecaplus.data.repository.AuthRepository
import com.bibliotecaplus.presentation.auth.LoginViewModel
import com.bibliotecaplus.presentation.books.BookViewModel
import com.bibliotecaplus.presentation.home.HomeViewModel
import com.bibliotecaplus.presentation.loans.LoansViewModel
import com.bibliotecaplus.presentation.notifications.NotificationsViewModel
import com.bibliotecaplus.presentation.profile.EditProfileViewModel
import com.bibliotecaplus.presentation.profile.ProfileViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

fun sharedModule(baseUrl: String) = module {
    single { BibliotecaApiClient(baseUrl, get()) }
    single { AuthRepository(get(), get()) }

    viewModelOf(::LoginViewModel)
    viewModelOf(::HomeViewModel)
    viewModelOf(::BookViewModel)
    viewModelOf(::LoansViewModel)
    viewModelOf(::ProfileViewModel)
    viewModelOf(::EditProfileViewModel)
    viewModelOf(::NotificationsViewModel)
}
