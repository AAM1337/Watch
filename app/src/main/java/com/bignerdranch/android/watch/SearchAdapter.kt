package com.bignerdranch.android.watch

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import DataBase.SearchItem
import com.bignerdranch.android.watch.databinding.ItemSearchBinding
import com.bumptech.glide.Glide

class SearchAdapter(
    private val onClick: (SearchItem) -> Unit
) : ListAdapter<SearchItem, SearchAdapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(private val binding: ItemSearchBinding)
        : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: SearchItem) {
            binding.tvTitle.text = item.Title
            binding.tvYear.text = item.Year
            binding.tvGenre.text = item.Type

            Glide.with(binding.imgPoster.context)
                .load(item.Poster)
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

    class DiffCallback : DiffUtil.ItemCallback<SearchItem>() {
        override fun areItemsTheSame(a: SearchItem, b: SearchItem) = a.imdbID == b.imdbID
        override fun areContentsTheSame(a: SearchItem, b: SearchItem) = a == b
    }
}