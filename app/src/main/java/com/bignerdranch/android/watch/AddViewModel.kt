package com.bignerdranch.android.watch

import DataBase.Movie
import DataBase.MovieDetail
import DataBase.MovieRepository
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SearchRequest(
    val query: String,
    val year: String
)

data class AddScreenState(
    val title: String = "",
    val year: String = "",
    val selectedMovie: MovieDetail? = null,
    val isLoading: Boolean = false,
    val titleError: String? = null,
    val errorMessage: String? = null,
    val pendingSearch: SearchRequest? = null,
    val shouldNavigateBack: Boolean = false
) {
    val canAddMovie: Boolean
        get() = selectedMovie != null && !isLoading
}

class AddViewModel(private val repository: MovieRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(AddScreenState())
    val uiState: StateFlow<AddScreenState> = _uiState.asStateFlow()

    fun onTitleChanged(title: String) {
        _uiState.update { state ->
            state.copy(
                title = title,
                titleError = null
            )
        }
    }

    fun onYearChanged(year: String) {
        _uiState.update { state -> state.copy(year = year) }
    }

    fun loadMovie(imdbId: String) {
        if (imdbId.isBlank()) return
        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    isLoading = true,
                    errorMessage = null
                )
            }

            val detail = repository.getMovieDetail(imdbId)
            _uiState.update { state ->
                if (detail == null) {
                    state.copy(
                        isLoading = false,
                        errorMessage = "Не удалось загрузить фильм"
                    )
                } else {
                    state.copy(
                        title = detail.Title,
                        year = detail.Year,
                        selectedMovie = detail,
                        isLoading = false,
                        errorMessage = null,
                        titleError = null
                    )
                }
            }
        }
    }

    fun onSearchClicked() {
        val currentState = _uiState.value
        val query = currentState.title.trim()
        if (query.isBlank()) {
            _uiState.update { state -> state.copy(titleError = "Введите название") }
            return
        }

        _uiState.update { state ->
            state.copy(
                title = query,
                year = state.year.trim(),
                titleError = null,
                pendingSearch = SearchRequest(query = query, year = state.year.trim())
            )
        }
    }

    fun onSearchNavigated() {
        _uiState.update { state -> state.copy(pendingSearch = null) }
    }

    fun onErrorMessageShown() {
        _uiState.update { state -> state.copy(errorMessage = null) }
    }

    fun addMovie() {
        val detail = _uiState.value.selectedMovie ?: return
        viewModelScope.launch {
            repository.addMovie(
                Movie(
                    imdbID = detail.imdbID,
                    title = detail.Title,
                    year = detail.Year,
                    posterUrl = detail.Poster,
                    genre = detail.Genre
                )
            )
            _uiState.value = AddScreenState(shouldNavigateBack = true)
        }
    }

    fun onNavigationBackHandled() {
        _uiState.update { state -> state.copy(shouldNavigateBack = false) }
    }

    class Factory(private val repository: MovieRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return AddViewModel(repository) as T
        }
    }
}
