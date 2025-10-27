package com.example.myapplication.di

import android.util.Log
import com.example.myapplication.database.GameDatabase
import com.example.myapplication.viewmodel.GameViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { 
        Log.d("AppModule", "Creating GameDatabase")
        GameDatabase.getDatabase(androidContext()) 
    }
    
    viewModel { 
        Log.d("AppModule", "Creating GameViewModel with context: ${androidContext().applicationContext::class.simpleName}")
        GameViewModel(androidContext().applicationContext, get()) 
    }
}
