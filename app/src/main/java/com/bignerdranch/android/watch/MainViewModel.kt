package com.bignerdranch.android.watch

import com.bignerdranch.android.watch.domain.model.SavedMovie
import com.bignerdranch.android.watch.domain.usecase.DeleteCheckedMoviesUseCase
import com.bignerdranch.android.watch.domain.usecase.GetSavedMoviesUseCase
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface MainIntent {
    data class ToggleMovieSelection(val imdbId: String, val checked: Boolean) : MainIntent
    data object DeleteSelectedMovies : MainIntent
    data object AddMovieClicked : MainIntent
}

data class MainState(
    val loading: Boolean = true,
    val movies: List<SavedMovie> = emptyList(),
    val error: String? = null,
    val selectedMovieIds: Set<String> = emptySet()
) {
    val isEmpty: Boolean
        get() = !loading && movies.isEmpty()
}

sealed interface MainEffect {
    data object NavigateToAdd : MainEffect
}

class MainViewModel(
    private val getSavedMoviesUseCase: GetSavedMoviesUseCase,
    private val deleteCheckedMoviesUseCase: DeleteCheckedMoviesUseCase
) : ViewModel() {

    private val selectedIds = MutableStateFlow<Set<String>>(emptySet())
    private val _effect = MutableSharedFlow<MainEffect>()
    val effect = _effect.asSharedFlow()

    val state: StateFlow<MainState> = combine(
        getSavedMoviesUseCase(),
        selectedIds
    ) { movies, checkedIds ->
        val updatedMovies = movies.map { movie ->
            movie.copy(isChecked = movie.imdbId in checkedIds)
        }
        MainState(
            loading = false,
            movies = updatedMovies,
            selectedMovieIds = checkedIds
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = MainState()
    )

    fun handleIntent(intent: MainIntent) {
        when (intent) {
            is MainIntent.ToggleMovieSelection -> {
                selectedIds.update { ids ->
                    if (intent.checked) ids + intent.imdbId else ids - intent.imdbId
                }
            }

            MainIntent.DeleteSelectedMovies -> {
                viewModelScope.launch {
                    val idsToDelete = selectedIds.value.toList()
                    if (idsToDelete.isEmpty()) return@launch
                    deleteCheckedMoviesUseCase(idsToDelete)
                    selectedIds.update { ids -> ids - idsToDelete.toSet() }
                }
            }

            MainIntent.AddMovieClicked -> {
                viewModelScope.launch {
                    _effect.emit(MainEffect.NavigateToAdd)
                }
            }
        }
    }

    class Factory(
        private val getSavedMoviesUseCase: GetSavedMoviesUseCase,
        private val deleteCheckedMoviesUseCase: DeleteCheckedMoviesUseCase
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(getSavedMoviesUseCase, deleteCheckedMoviesUseCase) as T
        }
    }
}
