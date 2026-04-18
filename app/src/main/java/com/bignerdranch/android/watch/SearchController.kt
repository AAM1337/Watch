package com.bignerdranch.android.watch

import DataBase.MovieRepository
import DataBase.SearchItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SearchViewState(
    val loading: Boolean = false,
    val query: String = "",
    val year: String = "",
    val results: List<SearchItem> = emptyList(),
    val selectedMovieId: String? = null,
    val error: String? = null
)

sealed interface SearchUiEvent {
    data class ReturnSelectedMovie(val imdbId: String) : SearchUiEvent
}

class SearchController(
    private val repository: MovieRepository,
    private val scope: CoroutineScope
) {

    private val _state = MutableStateFlow(SearchViewState())
    val state: StateFlow<SearchViewState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<SearchUiEvent>()
    val events: SharedFlow<SearchUiEvent> = _events.asSharedFlow()

    fun initialize(query: String, year: String?) {
        scope.launch {
            _state.update { state ->
                state.copy(
                    loading = true,
                    query = query,
                    year = year.orEmpty(),
                    results = emptyList(),
                    selectedMovieId = null,
                    error = null
                )
            }

            val results = repository.searchOnline(query, year?.takeIf { it.isNotBlank() })
            _state.update { state ->
                if (results.isNullOrEmpty()) {
                    state.copy(
                        loading = false,
                        results = emptyList(),
                        error = "Ничего не найдено"
                    )
                } else {
                    state.copy(
                        loading = false,
                        results = results,
                        error = null
                    )
                }
            }
        }
    }

    fun onMovieClicked(imdbId: String) {
        _state.update { state -> state.copy(selectedMovieId = imdbId) }
        scope.launch {
            _events.emit(SearchUiEvent.ReturnSelectedMovie(imdbId))
        }
    }
}
