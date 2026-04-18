package com.bignerdranch.android.watch

import com.bignerdranch.android.watch.domain.model.MovieDetails
import com.bignerdranch.android.watch.domain.model.SavedMovie
import com.bignerdranch.android.watch.domain.usecase.AddMovieUseCase
import com.bignerdranch.android.watch.domain.usecase.GetMovieDetailsUseCase
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

sealed interface AddIntent {
    data class Initialize(val imdbId: String?) : AddIntent
    data class TitleChanged(val title: String) : AddIntent
    data class YearChanged(val year: String) : AddIntent
    data object SearchClicked : AddIntent
    data object AddMovieClicked : AddIntent
}

data class AddState(
    val loading: Boolean = false,
    val title: String = "",
    val year: String = "",
    val selectedMovie: MovieDetails? = null,
    val error: String? = null,
    val titleError: String? = null
) {
    val canAddMovie: Boolean
        get() = selectedMovie != null && !loading
}

sealed interface AddEffect {
    data class NavigateToSearch(val query: String, val year: String) : AddEffect
    data object NavigateBackToMain : AddEffect
    data class ShowMessage(val message: String) : AddEffect
}

class AddViewModel(
    private val getMovieDetailsUseCase: GetMovieDetailsUseCase,
    private val addMovieUseCase: AddMovieUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(AddState())
    val state: StateFlow<AddState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<AddEffect>()
    val effect = _effect.asSharedFlow()

    fun handleIntent(intent: AddIntent) {
        when (intent) {
            is AddIntent.Initialize -> loadMovie(intent.imdbId.orEmpty())
            is AddIntent.TitleChanged -> {
                _state.update { state ->
                    state.copy(title = intent.title, titleError = null, error = null)
                }
            }

            is AddIntent.YearChanged -> {
                _state.update { state -> state.copy(year = intent.year) }
            }

            AddIntent.SearchClicked -> onSearchClicked()
            AddIntent.AddMovieClicked -> addMovie()
        }
    }

    private fun loadMovie(imdbId: String) {
        if (imdbId.isBlank()) return
        viewModelScope.launch {
            _state.update { state -> state.copy(loading = true, error = null) }
            val detail = getMovieDetailsUseCase(imdbId)
            if (detail == null) {
                _state.update { state -> state.copy(loading = false, error = "Не удалось загрузить фильм") }
                _effect.emit(AddEffect.ShowMessage("Не удалось загрузить фильм"))
            } else {
                _state.update { state ->
                    state.copy(
                        loading = false,
                        title = detail.title,
                        year = detail.year,
                        selectedMovie = detail,
                        error = null,
                        titleError = null
                    )
                }
            }
        }
    }

    private fun onSearchClicked() {
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

        viewModelScope.launch {
            _effect.emit(AddEffect.NavigateToSearch(query, year))
        }
    }

    private fun addMovie() {
        val detail = _state.value.selectedMovie ?: return
        viewModelScope.launch {
            addMovieUseCase(
                SavedMovie(
                    imdbId = detail.imdbId,
                    title = detail.title,
                    year = detail.year,
                    posterUrl = detail.posterUrl,
                    genre = detail.genre
                )
            )
            _state.value = AddState()
            _effect.emit(AddEffect.NavigateBackToMain)
        }
    }

    class Factory(
        private val getMovieDetailsUseCase: GetMovieDetailsUseCase,
        private val addMovieUseCase: AddMovieUseCase
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return AddViewModel(getMovieDetailsUseCase, addMovieUseCase) as T
        }
    }
}
