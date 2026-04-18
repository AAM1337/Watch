package com.bignerdranch.android.watch

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.bignerdranch.android.watch.databinding.FragmentSearchBinding
import kotlinx.coroutines.launch


class SearchFragment : Fragment() {

    private lateinit var controller: SearchController

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

        controller = SearchController(
            repository = (requireActivity().application as WatchApplication).provideRepository(),
            scope = viewLifecycleOwner.lifecycleScope
        )

        val query = arguments?.getString("query") ?: ""
        val year = arguments?.getString("year")
        controller.initialize(query, year)

        val adapter = SearchAdapter { searchItem ->
            controller.onMovieClicked(searchItem.imdbID)
        }

        binding.recyclerView.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    controller.state.collect { state ->
                        binding.progressBar.visibility = if (state.loading) View.VISIBLE else View.GONE
                        adapter.submitList(state.results)

                        if (state.error != null) {
                            binding.tvError.visibility = View.VISIBLE
                            binding.tvError.text = state.error
                            binding.recyclerView.visibility = View.GONE
                        } else {
                            binding.tvError.visibility = View.GONE
                            binding.recyclerView.visibility = View.VISIBLE
                        }
                    }
                }

                launch {
                    controller.events.collect { event ->
                        if (event is SearchUiEvent.ReturnSelectedMovie) {
                            findNavController().previousBackStackEntry
                                ?.savedStateHandle
                                ?.set("selected_imdb_id", event.imdbId)
                            findNavController().popBackStack()
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
