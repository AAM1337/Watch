package com.bignerdranch.android.watch.domain.repository

import com.bignerdranch.android.watch.domain.model.MovieDetails
import com.bignerdranch.android.watch.domain.model.SavedMovie
import com.bignerdranch.android.watch.domain.model.SearchMovie
import kotlinx.coroutines.flow.Flow

interface MovieRepository {
    fun observeSavedMovies(): Flow<List<SavedMovie>>
    suspend fun addMovie(movie: SavedMovie)
    suspend fun deleteMovies(ids: List<String>)
    suspend fun searchMovies(query: String, year: String?): List<SearchMovie>?
    suspend fun getMovieDetails(imdbId: String): MovieDetails?
}
