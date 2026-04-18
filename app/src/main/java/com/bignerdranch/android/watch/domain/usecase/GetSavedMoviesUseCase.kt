package com.bignerdranch.android.watch.domain.usecase

import com.bignerdranch.android.watch.domain.repository.MovieRepository

class GetSavedMoviesUseCase(private val repository: MovieRepository) {
    operator fun invoke() = repository.observeSavedMovies()
}
