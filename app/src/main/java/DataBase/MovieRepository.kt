package DataBase

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

class MovieRepository(private val dao: MovieDao) {

    val allMovies = dao.getAllMovies()

    suspend fun addMovie(movie: Movie) = dao.insert(movie)

    suspend fun deleteChecked(ids: List<String>) = dao.deleteByIds(ids)

    suspend fun searchOnline(query: String, year: String?): List<SearchItem>? = coroutineScope {
        try {
            val response = RetrofitInstance.api.searchMovies(query, year)
            if (response.Response != "True") {
                null
            } else {
                response.Search
                    ?.map { item ->
                        async {
                            val detail = getMovieDetail(item.imdbID)
                            item.copy(Genre = detail?.Genre.orEmpty())
                        }
                    }
                    ?.awaitAll()
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getMovieDetail(imdbId: String): MovieDetail? {
        return try {
            val detail = RetrofitInstance.api.getMovieById(imdbId)
            if (detail.Response == "True") detail else null
        } catch (e: Exception) {
            null
        }
    }
}
