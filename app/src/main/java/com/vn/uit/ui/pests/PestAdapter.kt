package com.vn.uit.ui.pests

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.vn.uit.databinding.ItemPestBinding
import com.vn.uit.model.Pest

class PestAdapter(
    private val onItemClick: (Pest) -> Unit
) : ListAdapter<Pest, PestAdapter.PestViewHolder>(PestDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PestViewHolder {
        val binding = ItemPestBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PestViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PestViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PestViewHolder(
        private val binding: ItemPestBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(pest: Pest) {
            binding.pestNameTextView.text = pest.name
            binding.occurrenceTextView.text = pest.occurrenceCount.toString()
            binding.pestImageView.load(pest.pestUrl)
            binding.root.setOnClickListener { onItemClick(pest) }
        }
    }

    private object PestDiffCallback : DiffUtil.ItemCallback<Pest>() {
        override fun areItemsTheSame(oldItem: Pest, newItem: Pest): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Pest, newItem: Pest): Boolean = oldItem == newItem
    }
}
