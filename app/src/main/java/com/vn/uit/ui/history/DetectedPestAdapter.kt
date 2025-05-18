package com.vn.uit.ui.history

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.vn.uit.R
import com.vn.uit.ui.history.DetectedPestItem
import java.text.NumberFormat

class DetectedPestAdapter(
    private var items: List<DetectedPestItem>
) : RecyclerView.Adapter<DetectedPestAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_detected_pest, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int = items.size

    fun updateItems(newItems: List<DetectedPestItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val pestThumbImageView: ImageView = itemView.findViewById(R.id.pestThumbImageView)
        private val pestNameTextView: TextView = itemView.findViewById(R.id.pestNameTextView)
        private val scientificNameTextView: TextView = itemView.findViewById(R.id.scientificNameTextView)
        private val confidenceTextView: TextView = itemView.findViewById(R.id.confidenceTextView)

        fun bind(item: DetectedPestItem) {
            // Load image using Glide
            Glide.with(itemView.context)
                .load(item.imageUrl)
                .centerCrop()
                .placeholder(android.R.color.darker_gray)
                .into(pestThumbImageView)

            // Set pest name
            pestNameTextView.text = item.name

            // Set scientific name
            scientificNameTextView.text = item.scientificName

            // Format and set confidence percentage
            val percentFormat = NumberFormat.getPercentInstance()
            confidenceTextView.text = percentFormat.format(item.confidence)

            // Set text color based on confidence level
            val confidenceColor = when {
                item.confidence >= 0.9f -> android.R.color.holo_green_dark
                item.confidence >= 0.7f -> android.R.color.holo_orange_dark
                else -> android.R.color.holo_red_dark
            }
            confidenceTextView.setTextColor(itemView.context.getColor(confidenceColor))
        }
    }
}