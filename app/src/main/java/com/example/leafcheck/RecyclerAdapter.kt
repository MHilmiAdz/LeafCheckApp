package com.example.leafcheck

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RecyclerAdapter(private val treeList:ArrayList<TreeData>, private val onItemClick: (TreeData) -> Unit) : RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_recycler_view, parent, false)
        return ViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return treeList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = treeList[position]
        val treeTypeLabel = when (currentItem.treeType) {
            1 -> "Apel"
            2 -> "Mangga"
            3 -> "Jambu"
            else -> "Unknown" // Handle cases where treeType is not 1, 2, or 3
        }
        holder.treeName.text = currentItem.treeName
        holder.treeType.text = treeTypeLabel
        holder.treeCondition.text = currentItem.treeCond
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val treeName: TextView = itemView.findViewById(R.id.treeName)
        var treeType: TextView = itemView.findViewById(R.id.treeType)
        val treeCondition: TextView = itemView.findViewById(R.id.treeCondition)
    }


}