package com.moodstudy.ui.reko

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.moodstudy.ai.RekomendasiItem
import com.moodstudy.databinding.ItemRekomendasiBinding

class RekomendasiAdapter(
    private val items: List<RekomendasiItem>
) : RecyclerView.Adapter<RekomendasiAdapter.VH>() {

    inner class VH(val binding: ItemRekomendasiBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemRekomendasiBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        with(holder.binding) {
            tvIcon.text  = item.icon
            tvTitle.text = item.title
            tvDesc.text  = item.desc
        }
    }
}