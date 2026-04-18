package com.bignerdranch.android.watch.domain.usecase

import com.bignerdranch.android.watch.domain.repository.MovieRepository

class DeleteCheckedMoviesUseCase(private val repository: MovieRepository) {
    suspend operator fun invoke(ids: List<String>) = repository.deleteMovies(ids)
}
