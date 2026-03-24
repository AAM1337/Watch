package com.bignerdranch.android.watch

import DataBase.MovieRepository
import DataBase.SearchItem
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface SearchIntent {
    data class Initialize(val query: String, val year: String?) : SearchIntent
    data class MovieClicked(val imdbId: String) : SearchIntent
}

data class SearchState(
    val loading: Boolean = false,
    val query: String = "",
    val year: String = "",
    val results: List<SearchItem> = emptyList(),
    val selectedMovieId: String? = null,
    val error: String? = null
)

sealed interface SearchEffect {
    data class ReturnSelectedMovie(val imdbId: String) : SearchEffect
}

class SearchViewModel(private val repository: MovieRepository) : ViewModel() {

    private val _state = MutableStateFlow(SearchState())
    val state: StateFlow<SearchState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<SearchEffect>()
    val effect = _effect.asSharedFlow()

    fun handleIntent(intent: SearchIntent) {
        when (intent) {
            is SearchIntent.Initialize -> searchMovies(intent.query, intent.year)
            is SearchIntent.MovieClicked -> {
                _state.update { state -> state.copy(selectedMovieId = intent.imdbId) }
                viewModelScope.launch {
                    _effect.emit(SearchEffect.ReturnSelectedMovie(intent.imdbId))
                }
            }
        }
    }

    private fun searchMovies(query: String, year: String?) {
        viewModelScope.launch {
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

    class Factory(private val repository: MovieRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return SearchViewModel(repository) as T
        }
    }
}
