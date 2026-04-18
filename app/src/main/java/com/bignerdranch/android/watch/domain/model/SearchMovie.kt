package com.bignerdranch.android.watch.domain.model

data class SearchMovie(
    val imdbId: String,
    val title: String,
    val year: String,
    val posterUrl: String,
    val type: String,
    val genre: String = ""
)
