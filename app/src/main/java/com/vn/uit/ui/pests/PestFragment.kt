package com.vn.uit.ui.pests

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.vn.uit.data.remote.ApiClient
import com.vn.uit.databinding.FragmentPestsViewBinding
import com.vn.uit.repository.PestRepository
import com.vn.uit.viewmodel.PestViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class PestsFragment : Fragment() {

    private var _binding: FragmentPestsViewBinding? = null
    private val binding get() = _binding!!

    private val pestRepository: PestRepository by lazy {
        PestRepository(ApiClient.pestApi)
    }

    private val viewModel: PestViewModel by viewModels {
        PestViewModelFactory(pestRepository)
    }

    private lateinit var adapter: PestAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPestsViewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupSearch()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        adapter = PestAdapter { pest -> viewModel.showPestDetails(pest) }
        binding.pestRecyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.pestRecyclerView.adapter = adapter
    }

    private fun setupSearch() {
        binding.searchEditText.addTextChangedListener { text ->
            viewModel.searchPests(text.toString())
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.uiState.collectLatest { state ->
                adapter.submitList(state.filteredPests)

                binding.progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE

                if (state.showDialog && state.selectedPest != null) {
                    val dialog = PestDetailDialog.newInstance(state.selectedPest)
                    if (!dialog.isAdded) {
                        dialog.show(childFragmentManager, "PestDetailDialog")
                    }
                    viewModel.hidePestDetails()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}