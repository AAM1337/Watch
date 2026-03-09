package DataBase

class MovieRepository(private val dao: MovieDao) {

    val allMovies = dao.getAllMovies()

    suspend fun addMovie(movie: Movie) = dao.insert(movie)

    suspend fun deleteChecked(ids: List<String>) = dao.deleteByIds(ids)

    suspend fun searchOnline(query: String, year: String?): List<SearchItem>? {
        return try {
            val response = RetrofitInstance.api.searchMovies(query, year)
            if (response.Response == "True") response.Search else null
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