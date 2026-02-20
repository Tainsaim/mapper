package com.keymapper.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.keymapper.R
import com.keymapper.data.KeyMapping

class MappingAdapter(
    private var items: List<KeyMapping>,
    private val onToggle: (String) -> Unit,
    private val onDelete: (String) -> Unit
) : RecyclerView.Adapter<MappingAdapter.VH>() {

    inner class VH(parent: ViewGroup) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_mapping, parent, false)
    ) {
        val switch: Switch = itemView.findViewById(R.id.switchEnabled)
        val tvInfo: TextView = itemView.findViewById(R.id.tvInfo)
        val btnDelete: Button = itemView.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(parent)

    override fun onBindViewHolder(holder: VH, position: Int) {
        val m = items[position]
        holder.tvInfo.text = "⌨ ${m.keyLabel}  →  📍 (${m.tapX.toInt()}, ${m.tapY.toInt()})"
        holder.switch.isChecked = m.isEnabled
        // Сбрасываем listener перед установкой нового, чтобы не было лишних вызовов
        holder.switch.setOnCheckedChangeListener(null)
        holder.switch.setOnCheckedChangeListener { _, _ -> onToggle(m.id) }
        holder.btnDelete.setOnClickListener { onDelete(m.id) }
    }

    override fun getItemCount() = items.size

    fun update(newItems: List<KeyMapping>) {
        items = newItems
        notifyDataSetChanged()
    }
}
