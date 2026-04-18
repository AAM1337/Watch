package com.bignerdranch.android.watch

import DataBase.Movie
import DataBase.MovieDetail
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

data class AddViewState(
    val loading: Boolean = false,
    val title: String = "",
    val year: String = "",
    val selectedMovie: MovieDetail? = null,
    val error: String? = null,
    val titleError: String? = null
) {
    val canAddMovie: Boolean
        get() = selectedMovie != null && !loading
}

sealed interface AddUiEvent {
    data class NavigateToSearch(val query: String, val year: String) : AddUiEvent
    data object NavigateBackToMain : AddUiEvent
    data class ShowMessage(val message: String) : AddUiEvent
}

class AddController(
    private val repository: MovieRepository,
    private val scope: CoroutineScope
) {

    private val _state = MutableStateFlow(AddViewState())
    val state: StateFlow<AddViewState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<AddUiEvent>()
    val events: SharedFlow<AddUiEvent> = _events.asSharedFlow()

    fun initialize(imdbId: String?) {
        loadMovie(imdbId.orEmpty())
    }

    fun onTitleChanged(title: String) {
        _state.update { state ->
            state.copy(
                title = title,
                titleError = null,
                error = null
            )
        }
    }

    fun onYearChanged(year: String) {
        _state.update { state -> state.copy(year = year) }
    }

    fun onSearchClicked() {
        val currentState = _state.value
        val query = currentState.title.trim()
        if (query.isBlank()) {
            _state.update { state -> state.copy(titleError = "Введите название") }
            return
        }

        val year = currentState.year.trim()
        _state.update { state ->
            state.copy(
                title = query,
                year = year,
                titleError = null,
                error = null
            )
        }

        scope.launch {
            _events.emit(AddUiEvent.NavigateToSearch(query, year))
        }
    }

    fun onAddMovieClicked() {
        val detail = _state.value.selectedMovie ?: return
        scope.launch {
            repository.addMovie(
                Movie(
                    imdbID = detail.imdbID,
                    title = detail.Title,
                    year = detail.Year,
                    posterUrl = detail.Poster,
                    genre = detail.Genre
                )
            )
            _state.value = AddViewState()
            _events.emit(AddUiEvent.NavigateBackToMain)
        }
    }

    private fun loadMovie(imdbId: String) {
        if (imdbId.isBlank()) return
        scope.launch {
            _state.update { state -> state.copy(loading = true, error = null) }
            val detail = repository.getMovieDetail(imdbId)
            if (detail == null) {
                _state.update { state ->
                    state.copy(
                        loading = false,
                        error = "Не удалось загрузить фильм"
                    )
                }
                _events.emit(AddUiEvent.ShowMessage("Не удалось загрузить фильм"))
            } else {
                _state.update { state ->
                    state.copy(
                        loading = false,
                        title = detail.Title,
                        year = detail.Year,
                        selectedMovie = detail,
                        error = null,
                        titleError = null
                    )
                }
            }
        }
    }
}
