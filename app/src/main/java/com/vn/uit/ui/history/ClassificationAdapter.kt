package com.vn.uit.ui.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.vn.uit.R
import com.vn.uit.databinding.ItemHistoryBinding
import com.vn.uit.model.ClassificationResponse
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class ClassificationAdapter(
    private var items: List<ClassificationResponse>,
    private val onClick: (ClassificationResponse) -> Unit
) : RecyclerView.Adapter<ClassificationAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ClassificationResponse) {
            binding.pestName.text = item.pestName
            binding.modelName.text = item.modelName
            binding.confidence.text = "${"%.1f".format(item.confidence * 100)}%"
            val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
            val localDateTime = LocalDateTime.parse(item.classifiedAt, formatter)
            val localDate = localDateTime.atZone(ZoneId.systemDefault()).toLocalDate()
            binding.date.text = localDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
            binding.imageView.load(item.imageUrl) {
                crossfade(true)
                placeholder(R.drawable.image_placeholder)
                error(R.drawable.image_broken)
            }

            binding.root.setOnClickListener {
                onClick(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    fun submitList(newList: List<ClassificationResponse>) {
        items = newList
        notifyDataSetChanged()
    }
}
