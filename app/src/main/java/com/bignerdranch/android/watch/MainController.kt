package com.bignerdranch.android.watch

import DataBase.Movie
import DataBase.MovieRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MainViewState(
    val loading: Boolean = true,
    val movies: List<Movie> = emptyList(),
    val error: String? = null,
    val selectedMovieIds: Set<String> = emptySet()
) {
    val isEmpty: Boolean
        get() = !loading && movies.isEmpty()
}

sealed interface MainUiEvent {
    data object NavigateToAdd : MainUiEvent
}

class MainController(
    private val repository: MovieRepository,
    private val scope: CoroutineScope
) {

    private val _state = MutableStateFlow(MainViewState())
    val state: StateFlow<MainViewState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<MainUiEvent>()
    val events: SharedFlow<MainUiEvent> = _events.asSharedFlow()

    init {
        scope.launch {
            repository.allMovies.collect { movies ->
                _state.update { state ->
                    state.copy(
                        loading = false,
                        movies = movies.map { movie ->
                            movie.copy(isChecked = movie.imdbID in state.selectedMovieIds)
                        }
                    )
                }
            }
        }
    }

    fun onMovieCheckedChanged(imdbId: String, checked: Boolean) {
        _state.update { state ->
            val selectedIds = if (checked) {
                state.selectedMovieIds + imdbId
            } else {
                state.selectedMovieIds - imdbId
            }

            state.copy(
                selectedMovieIds = selectedIds,
                movies = state.movies.map { movie ->
                    if (movie.imdbID == imdbId) movie.copy(isChecked = checked) else movie
                }
            )
        }
    }

    fun onDeleteSelectedClicked() {
        scope.launch {
            val idsToDelete = state.value.selectedMovieIds.toList()
            if (idsToDelete.isEmpty()) return@launch
            repository.deleteChecked(idsToDelete)
            _state.update { state ->
                state.copy(selectedMovieIds = state.selectedMovieIds - idsToDelete.toSet())
            }
        }
    }

    fun onAddMovieClicked() {
        scope.launch {
            _events.emit(MainUiEvent.NavigateToAdd)
        }
    }
}
