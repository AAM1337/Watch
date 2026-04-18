package com.bignerdranch.android.watch.domain.usecase

import com.bignerdranch.android.watch.domain.repository.MovieRepository

class SearchMoviesUseCase(private val repository: MovieRepository) {
    suspend operator fun invoke(query: String, year: String?) = repository.searchMovies(query, year)
}
