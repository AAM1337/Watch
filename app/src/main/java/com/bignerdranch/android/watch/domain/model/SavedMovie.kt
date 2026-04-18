package com.bignerdranch.android.watch.domain.model

data class SavedMovie(
    val imdbId: String,
    val title: String,
    val year: String,
    val posterUrl: String,
    val genre: String = "",
    val isChecked: Boolean = false
)
