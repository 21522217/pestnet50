package com.vn.uit.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.vn.uit.R
import com.vn.uit.model.InsecticideInfo

class InsecticidesAdapter : ListAdapter<InsecticideInfo, InsecticidesAdapter.InsecticideViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InsecticideViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_insecticides, parent, false)
        return InsecticideViewHolder(view)
    }

    override fun onBindViewHolder(holder: InsecticideViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class InsecticideViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.insecticideNameTextView)
        private val typeTextView: TextView = itemView.findViewById(R.id.insecticideTypeTextView)
        private val dosageTextView: TextView = itemView.findViewById(R.id.insecticideDosageTextView)

        fun bind(insecticide: InsecticideInfo) {
            nameTextView.text = insecticide.name
            typeTextView.text = insecticide.type
            dosageTextView.text = insecticide.dosage
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<InsecticideInfo>() {
        override fun areItemsTheSame(oldItem: InsecticideInfo, newItem: InsecticideInfo): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(oldItem: InsecticideInfo, newItem: InsecticideInfo): Boolean {
            return oldItem == newItem
        }
    }
}