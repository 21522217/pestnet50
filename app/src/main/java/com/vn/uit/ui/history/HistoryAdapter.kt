package com.vn.uit.ui.history

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.vn.uit.R
import com.vn.uit.ui.history.HistoryItem
import java.text.SimpleDateFormat
import java.util.Locale

class HistoryAdapter(
    private var items: List<HistoryItem>,
    private val onItemClick: (HistoryItem) -> Unit
) : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_history, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int = items.size

    fun updateItems(newItems: List<HistoryItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.imageView)
        private val dateTextView: TextView = itemView.findViewById(R.id.dateTextView)

        fun bind(item: HistoryItem) {
            // Load image using Glide
            Glide.with(itemView.context)
                .load(item.imageUrl)
                .centerCrop()
                .placeholder(android.R.color.darker_gray)
                .into(imageView)

            // Set date
            dateTextView.text = dateFormat.format(item.uploadedAt)

            // Set click listener
            itemView.setOnClickListener {
                onItemClick(item)
            }
        }
    }
}