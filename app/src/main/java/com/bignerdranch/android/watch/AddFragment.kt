package com.bignerdranch.android.watch

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import DataBase.AppDatabase
import DataBase.Movie
import DataBase.MovieRepository
import com.bignerdranch.android.watch.databinding.FragmentAddBinding

import com.bumptech.glide.Glide

class AddFragment : Fragment() {

    private val viewModel: MovieViewModel by activityViewModels {
        val db = AppDatabase.getDatabase(requireContext())
        MovieViewModel.Factory(MovieRepository(db.movieDao()))
    }

    private var _binding: FragmentAddBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnAddMovie.isEnabled = viewModel.selectedMovie.value != null

        val savedStateHandle = findNavController().currentBackStackEntry?.savedStateHandle
        savedStateHandle?.getLiveData<String>("selected_imdb_id")
            ?.observe(viewLifecycleOwner) { imdbId ->
                if (imdbId.isNullOrEmpty()) return@observe
                viewModel.selectMovie(imdbId)
                savedStateHandle.remove<String>("selected_imdb_id")
            }

        val imdbId = arguments?.getString("imdbId").orEmpty()
        if (imdbId.isNotEmpty()) {
            viewModel.selectMovie(imdbId)
        }

        viewModel.selectedMovie.observe(viewLifecycleOwner) { detail ->
            binding.btnAddMovie.isEnabled = detail != null
            if (detail == null) {
                binding.etTitle.text?.clear()
                binding.etYear.text?.clear()
                binding.imgPoster.setImageDrawable(null)
                return@observe
            }
            binding.etTitle.setText(detail.Title)
            binding.etYear.setText(detail.Year)
            Glide.with(this)
                .load(detail.Poster)
                .placeholder(R.drawable.ic_empty_frame)
                .into(binding.imgPoster)
        }

        binding.btnSearch.setOnClickListener {
            val query = binding.etTitle.text.toString()
            if (query.isBlank()) {
                binding.etTitle.error = "Введите название"
                return@setOnClickListener
            }
            val year = binding.etYear.text.toString()
            val args = Bundle().apply {
                putString("query", query)
                putString("year", year)
            }
            findNavController().navigate(R.id.action_add_to_search, args)
        }

        binding.btnAddMovie.setOnClickListener {
            val detail = viewModel.selectedMovie.value ?: return@setOnClickListener
            val movie = Movie(
                imdbID = detail.imdbID,
                title = detail.Title,
                year = detail.Year,
                posterUrl = detail.Poster,
                genre = detail.Genre
            )
            viewModel.addMovie(movie)
            viewModel.clearSelectedMovie()
            savedStateHandle?.remove<String>("selected_imdb_id")
            findNavController().popBackStack(R.id.mainFragment, false)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
