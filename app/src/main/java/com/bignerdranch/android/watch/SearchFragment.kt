package com.bignerdranch.android.watch

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.bignerdranch.android.watch.databinding.FragmentSearchBinding
import kotlinx.coroutines.launch


class SearchFragment : Fragment() {

    private val viewModel: SearchViewModel by viewModels {
        (requireActivity().application as WatchApplication).searchViewModelFactory()
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
        viewModel.handleIntent(SearchIntent.Initialize(query, year))

        val adapter = SearchAdapter { searchItem ->
            viewModel.handleIntent(SearchIntent.MovieClicked(searchItem.imdbID))
        }

        binding.recyclerView.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.state.collect { state ->
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
                    viewModel.effect.collect { effect ->
                        if (effect is SearchEffect.ReturnSelectedMovie) {
                            findNavController().previousBackStackEntry
                                ?.savedStateHandle
                                ?.set("selected_imdb_id", effect.imdbId)
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
