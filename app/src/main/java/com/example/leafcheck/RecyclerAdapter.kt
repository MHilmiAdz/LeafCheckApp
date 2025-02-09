package com.example.leafcheck

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RecyclerAdapter(
    private val treeList: ArrayList<TreeData>,
    private val onItemClick: (String) -> Unit // Pass only the document ID
) : RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recycler_view, parent, false)
        return ViewHolder(itemView)
    }

    override fun getItemCount(): Int = treeList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = treeList[position]
        val treeTypeLabel = when (currentItem.treeType) {
            1 -> "Apel"
            2 -> "Mangga"
            3 -> "Jambu"
            else -> "Unknown"
        }
        holder.treeName.text = currentItem.treeName
        holder.treeType.text = treeTypeLabel
        holder.treeCondition.text = currentItem.treeCond

        // Pass the tree's document ID to the TreeProfile activity
        holder.itemView.setOnClickListener {
            onItemClick(currentItem.treeId ?: "")
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val treeName: TextView = itemView.findViewById(R.id.treeName)
        val treeType: TextView = itemView.findViewById(R.id.treeType)
        val treeCondition: TextView = itemView.findViewById(R.id.treeCondition)
    }
}
