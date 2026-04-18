package com.bignerdranch.android.watch

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bignerdranch.android.watch.domain.model.SearchMovie
import com.bignerdranch.android.watch.databinding.ItemSearchBinding
import com.bumptech.glide.Glide

class SearchAdapter(
    private val onClick: (SearchMovie) -> Unit
) : ListAdapter<SearchMovie, SearchAdapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(private val binding: ItemSearchBinding)
        : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: SearchMovie) {
            binding.tvTitle.text = item.title
            binding.tvYear.text = item.year
            binding.tvGenre.text = item.genre.ifBlank { "Жанр неизвестен" }

            Glide.with(binding.imgPoster.context)
                .load(item.posterUrl)
                .placeholder(R.drawable.ic_empty_frame)
                .into(binding.imgPoster)

            binding.root.setOnClickListener { onClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemSearchBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        ))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(getItem(position))

    class DiffCallback : DiffUtil.ItemCallback<SearchMovie>() {
        override fun areItemsTheSame(a: SearchMovie, b: SearchMovie) = a.imdbId == b.imdbId
        override fun areContentsTheSame(a: SearchMovie, b: SearchMovie) = a == b
    }
}
