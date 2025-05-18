package com.vn.uit.ui.history

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vn.uit.R
import com.vn.uit.ui.history.HistoryAdapter
import com.vn.uit.ui.history.PestAdapter
import com.vn.uit.ui.history.HistoryItem
import com.vn.uit.ui.history.PestItem
import java.util.UUID
import java.util.Date

class HistoryFragment : Fragment() {

    private lateinit var searchEditText: EditText
    private lateinit var recentRecyclerView: RecyclerView
    private lateinit var bestAccuracyRecyclerView: RecyclerView
    private lateinit var mostDetectedPestsRecyclerView: RecyclerView

    // Adapters
    private lateinit var recentAdapter: HistoryAdapter
    private lateinit var bestAccuracyAdapter: HistoryAdapter
    private lateinit var pestAdapter: PestAdapter

    // Mock data lists
    private val recentItems = mutableListOf<HistoryItem>()
    private val bestAccuracyItems = mutableListOf<HistoryItem>()
    private val pestItems = mutableListOf<PestItem>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_history, container, false)

        // Initialize views
        searchEditText = root.findViewById(R.id.searchEditText)
        recentRecyclerView = root.findViewById(R.id.recentRecyclerView)
        bestAccuracyRecyclerView = root.findViewById(R.id.bestAccuracyRecyclerView)
        mostDetectedPestsRecyclerView = root.findViewById(R.id.mostDetectedPestsRecyclerView)

        setupAdapters()
        setupSearchBar()
        loadMockData()

        return root
    }

    private fun setupAdapters() {
        // Set up Recent adapter
        recentAdapter = HistoryAdapter(recentItems) { item ->
            showImageDetailDialog(item)
        }
        recentRecyclerView.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = recentAdapter
        }

        // Set up Best Accuracy adapter
        bestAccuracyAdapter = HistoryAdapter(bestAccuracyItems) { item ->
            showImageDetailDialog(item)
        }
        bestAccuracyRecyclerView.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = bestAccuracyAdapter
        }

        // Set up Pest adapter
        pestAdapter = PestAdapter(pestItems) { item ->
            showPestDetailDialog(item)
        }
        mostDetectedPestsRecyclerView.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = pestAdapter
        }
    }

    private fun setupSearchBar() {
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Implement search functionality
                filterData(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun filterData(query: String) {
        // Filter recent items
        val filteredRecentItems = if (query.isEmpty()) {
            recentItems
        } else {
            recentItems.filter { it.originalName.contains(query, ignoreCase = true) }
        }
        recentAdapter.updateItems(filteredRecentItems)

        // Filter best accuracy items
        val filteredBestAccuracyItems = if (query.isEmpty()) {
            bestAccuracyItems
        } else {
            bestAccuracyItems.filter { it.originalName.contains(query, ignoreCase = true) }
        }
        bestAccuracyAdapter.updateItems(filteredBestAccuracyItems)

        // Filter pest items
        val filteredPestItems = if (query.isEmpty()) {
            pestItems
        } else {
            pestItems.filter { it.name.contains(query, ignoreCase = true) }
        }
        pestAdapter.updateItems(filteredPestItems)
    }

    private fun loadMockData() {
        // Mock data for Recent
        recentItems.addAll(
            listOf(
                HistoryItem(
                    UUID.randomUUID().toString(),
                    "https://placekitten.com/200/200",
                    Date(),
                    "image1.jpg"
                ),
                HistoryItem(
                    UUID.randomUUID().toString(),
                    "https://placekitten.com/201/201",
                    Date(),
                    "image2.jpg"
                ),
                HistoryItem(
                    UUID.randomUUID().toString(),
                    "https://placekitten.com/202/202",
                    Date(),
                    "image3.jpg"
                )
            )
        )
        recentAdapter.notifyDataSetChanged()

        // Mock data for Best Accuracy
        bestAccuracyItems.addAll(
            listOf(
                HistoryItem(
                    UUID.randomUUID().toString(),
                    "https://placekitten.com/203/203",
                    Date(),
                    "high_accuracy1.jpg",
                    0.98f
                ),
                HistoryItem(
                    UUID.randomUUID().toString(),
                    "https://placekitten.com/204/204",
                    Date(),
                    "high_accuracy2.jpg",
                    0.97f
                ),
                HistoryItem(
                    UUID.randomUUID().toString(),
                    "https://placekitten.com/205/205",
                    Date(),
                    "high_accuracy3.jpg",
                    0.95f
                )
            )
        )
        bestAccuracyAdapter.notifyDataSetChanged()

        // Mock data for Most Detected Pests
        pestItems.addAll(
            listOf(
                PestItem(
                    UUID.randomUUID().toString(),
                    "Aphid",
                    "Aphidoidea",
                    "https://placekitten.com/100/100",
                    15,
                    "Small sap-sucking insects often found on crops."
                ),
                PestItem(
                    UUID.randomUUID().toString(),
                    "Spider Mite",
                    "Tetranychidae",
                    "https://placekitten.com/101/101",
                    12,
                    "Tiny arachnids that feed on plant tissues."
                ),
                PestItem(
                    UUID.randomUUID().toString(),
                    "Whitefly",
                    "Aleyrodidae",
                    "https://placekitten.com/102/102",
                    10,
                    "Small white-winged insects that suck plant juices."
                ),
                PestItem(
                    UUID.randomUUID().toString(),
                    "Thrips",
                    "Thysanoptera",
                    "https://placekitten.com/103/103",
                    8,
                    "Slender insects that damage plants by feeding on them."
                ),
                PestItem(
                    UUID.randomUUID().toString(),
                    "Scale Insect",
                    "Coccoidea",
                    "https://placekitten.com/104/104",
                    7,
                    "Insects that attach to plant stems and suck sap."
                ),
                PestItem(
                    UUID.randomUUID().toString(),
                    "Mealybug",
                    "Pseudococcidae",
                    "https://placekitten.com/105/105",
                    5,
                    "Soft-bodied pests covered with a white waxy material."
                )
            )
        )
        pestAdapter.notifyDataSetChanged()
    }

    private fun showImageDetailDialog(item: HistoryItem) {
        val dialog = ImageDetailDialogFragment.newInstance(item.id)
        dialog.show(childFragmentManager, "ImageDetailDialog")
    }

    private fun showPestDetailDialog(item: PestItem) {
        val dialog = PestDetailDialogFragment.newInstance(item.id)
        dialog.show(childFragmentManager, "PestDetailDialog")
    }
}