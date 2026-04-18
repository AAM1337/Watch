package com.bignerdranch.android.watch.domain.usecase

import com.bignerdranch.android.watch.domain.repository.MovieRepository

class GetMovieDetailsUseCase(private val repository: MovieRepository) {
    suspend operator fun invoke(imdbId: String) = repository.getMovieDetails(imdbId)
}
