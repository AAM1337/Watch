package com.bignerdranch.android.watch

import DataBase.Movie
import DataBase.MovieDetail
import DataBase.MovieRepository
import DataBase.SearchItem
import androidx.lifecycle.*
import kotlinx.coroutines.launch

class MovieViewModel(private val repo: MovieRepository) : ViewModel() {

    val movies = repo.allMovies.asLiveData()
    private val checkedMovieIds = linkedSetOf<String>()

    private val _searchResults = MutableLiveData<List<SearchItem>?>()
    val searchResults: LiveData<List<SearchItem>?> = _searchResults

    private val _selectedMovie = MutableLiveData<MovieDetail?>()
    val selectedMovie: LiveData<MovieDetail?> = _selectedMovie

    fun addMovie(movie: Movie) = viewModelScope.launch {
        repo.addMovie(movie)
    }

    fun setMovieChecked(imdbId: String, checked: Boolean) {
        if (checked) {
            checkedMovieIds += imdbId
        } else {
            checkedMovieIds -= imdbId
        }
    }

    fun isMovieChecked(imdbId: String): Boolean = imdbId in checkedMovieIds

    fun deleteChecked() = viewModelScope.launch {
        val ids = checkedMovieIds.toList()
        if (ids.isEmpty()) return@launch
        repo.deleteChecked(ids)
        checkedMovieIds.removeAll(ids.toSet())
    }

    fun searchMovies(query: String, year: String?) = viewModelScope.launch {
        _searchResults.value = repo.searchOnline(query, year)
    }

    fun selectMovie(imdbId: String) = viewModelScope.launch {
        _selectedMovie.value = repo.getMovieDetail(imdbId)
    }

    fun clearSelectedMovie() {
        _selectedMovie.value = null
    }

    class Factory(private val repo: MovieRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return MovieViewModel(repo) as T
        }
    }
}
