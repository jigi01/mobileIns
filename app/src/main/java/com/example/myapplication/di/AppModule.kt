package com.example.myapplication.di

import com.example.myapplication.database.GameDatabase
import com.example.myapplication.viewmodel.GameViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { GameDatabase.getDatabase(androidContext()) }
    
    viewModel { GameViewModel(androidContext().applicationContext, get()) }
}
