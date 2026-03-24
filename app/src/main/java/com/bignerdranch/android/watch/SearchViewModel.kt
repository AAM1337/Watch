package com.bignerdranch.android.watch

import DataBase.MovieRepository
import DataBase.SearchItem
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SearchScreenState(
    val results: List<SearchItem> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class SearchViewModel(private val repository: MovieRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchScreenState())
    val uiState: StateFlow<SearchScreenState> = _uiState.asStateFlow()

    fun searchMovies(query: String, year: String?) {
        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    isLoading = true,
                    errorMessage = null,
                    results = emptyList()
                )
            }

            val results = repository.searchOnline(query, year?.takeIf { it.isNotBlank() })
            _uiState.update { state ->
                if (results.isNullOrEmpty()) {
                    state.copy(
                        isLoading = false,
                        errorMessage = "Ничего не найдено",
                        results = emptyList()
                    )
                } else {
                    state.copy(
                        isLoading = false,
                        errorMessage = null,
                        results = results
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
