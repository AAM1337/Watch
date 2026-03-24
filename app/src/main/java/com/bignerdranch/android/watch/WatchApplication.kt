package com.bignerdranch.android.watch

import DataBase.AppDatabase
import DataBase.MovieRepository
import android.app.Application

class WatchApplication : Application() {

    private val database by lazy { AppDatabase.getDatabase(this) }
    private val repository by lazy { MovieRepository(database.movieDao()) }

    fun mainViewModelFactory(): MainViewModel.Factory = MainViewModel.Factory(repository)

    fun addViewModelFactory(): AddViewModel.Factory = AddViewModel.Factory(repository)

    fun searchViewModelFactory(): SearchViewModel.Factory = SearchViewModel.Factory(repository)
}
