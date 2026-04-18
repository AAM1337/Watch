package DataBase

import com.bignerdranch.android.watch.domain.model.MovieDetails
import com.bignerdranch.android.watch.domain.model.SavedMovie
import com.bignerdranch.android.watch.domain.model.SearchMovie
import com.bignerdranch.android.watch.domain.repository.MovieRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

class MovieRepositoryImpl(private val dao: MovieDao) : MovieRepository {

    override fun observeSavedMovies(): Flow<List<SavedMovie>> =
        dao.getAllMovies().map { movies -> movies.map { it.toDomain() } }

    override suspend fun addMovie(movie: SavedMovie) = dao.insert(movie.toEntity())

    override suspend fun deleteMovies(ids: List<String>) = dao.deleteByIds(ids)

    override suspend fun searchMovies(query: String, year: String?): List<SearchMovie>? = coroutineScope {
        try {
            val response = RetrofitInstance.api.searchMovies(query, year)
            if (response.Response != "True") {
                null
            } else {
                response.Search
                    ?.map { item ->
                        async {
                            val detail = getMovieDetails(item.imdbID)
                            item.copy(Genre = detail?.genre.orEmpty()).toDomain()
                        }
                    }
                    ?.awaitAll()
            }
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun getMovieDetails(imdbId: String): MovieDetails? = try {
        val detail = RetrofitInstance.api.getMovieById(imdbId)
        if (detail.Response == "True") detail.toDomain() else null
    } catch (e: Exception) {
        null
    }
}
