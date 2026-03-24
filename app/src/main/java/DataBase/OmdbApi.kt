package DataBase

import retrofit2.http.GET
import retrofit2.http.Query

data class SearchResponse(
    val Search: List<SearchItem>?,
    val Response: String
)

data class SearchItem(
    val imdbID: String,
    val Title: String,
    val Year: String,
    val Poster: String,
    val Type: String,
    val Genre: String = ""
)

data class MovieDetail(
    val imdbID: String,
    val Title: String,
    val Year: String,
    val Poster: String,
    val Genre: String,
    val Response: String
)

interface OmdbApi {
    @GET(".")
    suspend fun searchMovies(
        @Query("s") query: String,
        @Query("y") year: String? = null,
        @Query("apikey") apiKey: String = "2a66aa0e"
    ): SearchResponse

    @GET(".")
    suspend fun getMovieById(
        @Query("i") imdbId: String,
        @Query("apikey") apiKey: String = "2a66aa0e"
    ): MovieDetail
}

object RetrofitInstance {
    val api: OmdbApi by lazy {
        retrofit2.Retrofit.Builder()
            .baseUrl("https://www.omdbapi.com/")
            .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
            .build()
            .create(OmdbApi::class.java)
    }
}
