package com.bignerdranch.android.watch

// ui/MovieAdapter.kt
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bignerdranch.android.watch.domain.model.SavedMovie
import com.bignerdranch.android.watch.databinding.ItemMovieBinding
import com.bumptech.glide.Glide

class MovieAdapter(
    private val onCheckedChange: (SavedMovie, Boolean) -> Unit
) : ListAdapter<SavedMovie, MovieAdapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(private val binding: ItemMovieBinding)
        : RecyclerView.ViewHolder(binding.root) {

        fun bind(movie: SavedMovie) {
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

    class DiffCallback : DiffUtil.ItemCallback<SavedMovie>() {
        override fun areItemsTheSame(old: SavedMovie, new: SavedMovie) = old.imdbId == new.imdbId
        override fun areContentsTheSame(old: SavedMovie, new: SavedMovie) = old == new
    }
}
