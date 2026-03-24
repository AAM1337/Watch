package com.bignerdranch.android.watch

import DataBase.Movie
import DataBase.MovieRepository
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow

data class MainScreenState(
    val movies: List<Movie> = emptyList(),
    val isEmpty: Boolean = true,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class MainViewModel(private val repository: MovieRepository) : ViewModel() {

    private val checkedMovieIds = MutableStateFlow<Set<String>>(emptySet())

    val uiState: StateFlow<MainScreenState> = combine(
        repository.allMovies,
        checkedMovieIds
    ) { movies, checkedIds ->
        val updatedMovies = movies.map { movie ->
            movie.copy(isChecked = movie.imdbID in checkedIds)
        }
        MainScreenState(
            movies = updatedMovies,
            isEmpty = updatedMovies.isEmpty()
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = MainScreenState(isLoading = true)
    )

    fun onMovieCheckedChanged(imdbId: String, checked: Boolean) {
        checkedMovieIds.update { currentIds ->
            if (checked) currentIds + imdbId else currentIds - imdbId
        }
    }

    fun deleteCheckedMovies() = viewModelScope.launch {
        val idsToDelete = checkedMovieIds.value.toList()
        if (idsToDelete.isEmpty()) return@launch
        repository.deleteChecked(idsToDelete)
        checkedMovieIds.update { currentIds -> currentIds - idsToDelete.toSet() }
    }

    class Factory(private val repository: MovieRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repository) as T
        }
    }
}
