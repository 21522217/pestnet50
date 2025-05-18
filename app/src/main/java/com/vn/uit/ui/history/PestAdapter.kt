package com.vn.uit.ui.history

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.vn.uit.R
import com.vn.uit.ui.history.PestItem

class PestAdapter(
    private var items: List<PestItem>,
    private val onItemClick: (PestItem) -> Unit
) : RecyclerView.Adapter<PestAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_pest, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int = items.size

    fun updateItems(newItems: List<PestItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val pestImageView: ImageView = itemView.findViewById(R.id.pestImageView)
        private val pestNameTextView: TextView = itemView.findViewById(R.id.pestNameTextView)
        private val occurrenceTextView: TextView = itemView.findViewById(R.id.occurrenceTextView)

        fun bind(item: PestItem) {
            // Load image using Glide
            Glide.with(itemView.context)
                .load(item.imageUrl)
                .centerCrop()
                .placeholder(android.R.color.darker_gray)
                .into(pestImageView)

            // Set pest name
            pestNameTextView.text = item.name

            // Set occurrence count
            occurrenceTextView.text = item.occurrenceCount.toString()

            // Set click listener
            itemView.setOnClickListener {
                onItemClick(item)
            }
        }
    }
}