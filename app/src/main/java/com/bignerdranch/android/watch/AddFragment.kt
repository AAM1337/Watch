package com.bignerdranch.android.watch

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.bignerdranch.android.watch.databinding.FragmentAddBinding
import com.bumptech.glide.Glide
import kotlinx.coroutines.launch

class AddFragment : Fragment() {

    private val viewModel: AddViewModel by viewModels {
        (requireActivity().application as WatchApplication).addViewModelFactory()
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

        val savedStateHandle = findNavController().currentBackStackEntry?.savedStateHandle
        savedStateHandle?.getLiveData<String>("selected_imdb_id")
            ?.observe(viewLifecycleOwner) { imdbId ->
                if (imdbId.isNullOrEmpty()) return@observe
                viewModel.loadMovie(imdbId)
                savedStateHandle.remove<String>("selected_imdb_id")
            }

        val imdbId = arguments?.getString("imdbId").orEmpty()
        if (imdbId.isNotEmpty()) {
            viewModel.loadMovie(imdbId)
        }

        binding.etTitle.doAfterTextChanged { editable ->
            val text = editable?.toString().orEmpty()
            if (text != viewModel.uiState.value.title) {
                viewModel.onTitleChanged(text)
            }
        }

        binding.etYear.doAfterTextChanged { editable ->
            val text = editable?.toString().orEmpty()
            if (text != viewModel.uiState.value.year) {
                viewModel.onYearChanged(text)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    render(state)

                    state.pendingSearch?.let { request ->
                        val args = Bundle().apply {
                            putString("query", request.query)
                            putString("year", request.year)
                        }
                        viewModel.onSearchNavigated()
                        findNavController().navigate(R.id.action_add_to_search, args)
                    }

                    if (state.shouldNavigateBack) {
                        viewModel.onNavigationBackHandled()
                        savedStateHandle?.remove<String>("selected_imdb_id")
                        findNavController().popBackStack(R.id.mainFragment, false)
                    }
                }
            }
        }

        binding.btnSearch.setOnClickListener {
            viewModel.onSearchClicked()
        }

        binding.btnAddMovie.setOnClickListener {
            viewModel.addMovie()
        }
    }

    private fun render(state: AddScreenState) {
        if (binding.etTitle.text?.toString() != state.title) {
            binding.etTitle.setText(state.title)
            binding.etTitle.setSelection(binding.etTitle.text?.length ?: 0)
        }
        if (binding.etYear.text?.toString() != state.year) {
            binding.etYear.setText(state.year)
            binding.etYear.setSelection(binding.etYear.text?.length ?: 0)
        }

        binding.etTitle.error = state.titleError
        binding.progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE
        binding.btnAddMovie.isEnabled = state.canAddMovie

        val selectedMovie = state.selectedMovie
        if (selectedMovie == null) {
            binding.imgPoster.setImageDrawable(null)
        } else {
            Glide.with(this)
                .load(selectedMovie.Poster)
                .placeholder(R.drawable.ic_empty_frame)
                .into(binding.imgPoster)
        }

        state.errorMessage?.let { message ->
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            viewModel.onErrorMessageShown()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
