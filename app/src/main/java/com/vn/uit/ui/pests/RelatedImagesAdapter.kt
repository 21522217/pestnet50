package com.vn.uit.ui.pests

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.vn.uit.R
import com.vn.uit.databinding.ItemRelatedImageBinding

class RelatedImagesAdapter(
    private val images: List<String>,
    private val onImageClick: (String) -> Unit
) : RecyclerView.Adapter<RelatedImagesAdapter.ImageViewHolder>() {

    inner class ImageViewHolder(private val binding: ItemRelatedImageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(imageUrl: String) {
            binding.imageView.load(imageUrl) {
                placeholder(R.drawable.placeholder_image)
                error(R.drawable.image_placeholder)
                crossfade(true)
            }

            binding.root.setOnClickListener {
                onImageClick(imageUrl)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val binding = ItemRelatedImageBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ImageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.bind(images[position])
    }

    override fun getItemCount() = images.size
}