package com.bignerdranch.android.watch

import DataBase.AppDatabase
import DataBase.MovieRepositoryImpl
import android.app.Application
import com.bignerdranch.android.watch.domain.usecase.AddMovieUseCase
import com.bignerdranch.android.watch.domain.usecase.DeleteCheckedMoviesUseCase
import com.bignerdranch.android.watch.domain.usecase.GetMovieDetailsUseCase
import com.bignerdranch.android.watch.domain.usecase.GetSavedMoviesUseCase
import com.bignerdranch.android.watch.domain.usecase.SearchMoviesUseCase

class WatchApplication : Application() {

    private val database by lazy { AppDatabase.getDatabase(this) }
    private val repository by lazy { MovieRepositoryImpl(database.movieDao()) }
    private val getSavedMoviesUseCase by lazy { GetSavedMoviesUseCase(repository) }
    private val deleteCheckedMoviesUseCase by lazy { DeleteCheckedMoviesUseCase(repository) }
    private val searchMoviesUseCase by lazy { SearchMoviesUseCase(repository) }
    private val getMovieDetailsUseCase by lazy { GetMovieDetailsUseCase(repository) }
    private val addMovieUseCase by lazy { AddMovieUseCase(repository) }

    fun mainViewModelFactory(): MainViewModel.Factory =
        MainViewModel.Factory(getSavedMoviesUseCase, deleteCheckedMoviesUseCase)

    fun addViewModelFactory(): AddViewModel.Factory =
        AddViewModel.Factory(getMovieDetailsUseCase, addMovieUseCase)

    fun searchViewModelFactory(): SearchViewModel.Factory =
        SearchViewModel.Factory(searchMoviesUseCase)
}
