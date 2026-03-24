package com.bignerdranch.android.watch

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.bignerdranch.android.watch.databinding.FragmentMainBinding
import kotlinx.coroutines.launch

class MainFragment : Fragment() {

    private val viewModel: MainViewModel by viewModels {
        (requireActivity().application as WatchApplication).mainViewModelFactory()
    }
    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val adapter = MovieAdapter { movie, checked ->
            viewModel.handleIntent(MainIntent.ToggleMovieSelection(movie.imdbID, checked))
        }

        binding.recyclerView.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.state.collect { state ->
                        adapter.submitList(state.movies)
                        binding.emptyView.visibility = if (state.isEmpty) View.VISIBLE else View.GONE
                        binding.recyclerView.visibility = if (state.isEmpty) View.GONE else View.VISIBLE
                    }
                }

                launch {
                    viewModel.effect.collect { effect ->
                        if (effect is MainEffect.NavigateToAdd) {
                            findNavController().navigate(R.id.action_main_to_add)
                        }
                    }
                }
            }
        }

        binding.fabAdd.setOnClickListener {
            viewModel.handleIntent(MainIntent.AddMovieClicked)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_main, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_delete -> {
                viewModel.handleIntent(MainIntent.DeleteSelectedMovies)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
