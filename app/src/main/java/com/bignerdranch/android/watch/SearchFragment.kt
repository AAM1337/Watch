package com.bignerdranch.android.watch

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import DataBase.AppDatabase
import DataBase.MovieRepository
import com.bignerdranch.android.watch.databinding.FragmentSearchBinding


class SearchFragment : Fragment() {

    private val viewModel: MovieViewModel by activityViewModels {
        val db = AppDatabase.getDatabase(requireContext())
        MovieViewModel.Factory(MovieRepository(db.movieDao()))
    }

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    // Без этого метода FragmentSearchBinding не создастся
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val query = arguments?.getString("query") ?: ""
        val year = arguments?.getString("year")
        viewModel.searchMovies(query, year)

        val adapter = SearchAdapter { searchItem ->
            findNavController().previousBackStackEntry
                ?.savedStateHandle
                ?.set("selected_imdb_id", searchItem.imdbID)
            findNavController().popBackStack()
        }

        binding.recyclerView.adapter = adapter

        viewModel.searchResults.observe(viewLifecycleOwner) { results ->
            if (results.isNullOrEmpty()) {
                adapter.submitList(emptyList())
                binding.tvError.visibility = View.VISIBLE
                binding.tvError.text = "Ничего не найдено"
                binding.recyclerView.visibility = View.GONE
            } else {
                binding.tvError.visibility = View.GONE
                binding.recyclerView.visibility = View.VISIBLE
                adapter.submitList(results)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
