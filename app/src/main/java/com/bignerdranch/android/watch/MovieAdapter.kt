package com.bignerdranch.android.watch

// ui/MovieAdapter.kt
import DataBase.Movie
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bignerdranch.android.watch.databinding.ItemMovieBinding
import com.bumptech.glide.Glide

class MovieAdapter(
    private val onCheckedChange: (Movie, Boolean) -> Unit
) : ListAdapter<Movie, MovieAdapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(private val binding: ItemMovieBinding)
        : RecyclerView.ViewHolder(binding.root) {

        fun bind(movie: Movie) {
            binding.tvTitle.text = movie.title
            binding.tvYear.text = movie.year
            binding.checkBox.setOnCheckedChangeListener(null)
            binding.checkBox.isChecked = movie.isChecked

            Glide.with(binding.imgPoster)
                .load(movie.posterUrl)
                .placeholder(R.drawable.ic_empty_frame)
                .into(binding.imgPoster)

            binding.checkBox.setOnCheckedChangeListener { _, checked ->
                onCheckedChange(movie, checked)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMovieBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<Movie>() {
        override fun areItemsTheSame(old: Movie, new: Movie) = old.imdbID == new.imdbID
        override fun areContentsTheSame(old: Movie, new: Movie) = old == new
    }
}
