package com.bignerdranch.android.watch.domain.usecase

import com.bignerdranch.android.watch.domain.model.SavedMovie
import com.bignerdranch.android.watch.domain.repository.MovieRepository

class AddMovieUseCase(private val repository: MovieRepository) {
    suspend operator fun invoke(movie: SavedMovie) = repository.addMovie(movie)
}
