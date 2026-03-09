package DataBase

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "movies")
data class Movie(
    @PrimaryKey val imdbID: String,
    val title: String,
    val year: String,
    val posterUrl: String,
    val genre: String = "",
    var isChecked: Boolean = false
)