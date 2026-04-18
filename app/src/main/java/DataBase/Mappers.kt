package DataBase

import com.bignerdranch.android.watch.domain.model.MovieDetails
import com.bignerdranch.android.watch.domain.model.SavedMovie
import com.bignerdranch.android.watch.domain.model.SearchMovie

fun Movie.toDomain(): SavedMovie = SavedMovie(
    imdbId = imdbID,
    title = title,
    year = year,
    posterUrl = posterUrl,
    genre = genre,
    isChecked = isChecked
)

fun SavedMovie.toEntity(): Movie = Movie(
    imdbID = imdbId,
    title = title,
    year = year,
    posterUrl = posterUrl,
    genre = genre,
    isChecked = isChecked
)

fun SearchItem.toDomain(): SearchMovie = SearchMovie(
    imdbId = imdbID,
    title = Title,
    year = Year,
    posterUrl = Poster,
    type = Type,
    genre = Genre
)

fun MovieDetail.toDomain(): MovieDetails = MovieDetails(
    imdbId = imdbID,
    title = Title,
    year = Year,
    posterUrl = Poster,
    genre = Genre
)
