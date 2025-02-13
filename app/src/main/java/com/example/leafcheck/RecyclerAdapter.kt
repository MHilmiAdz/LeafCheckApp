package com.example.leafcheck

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
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

        val (treeTypeLabel, treeIconRes) = when (currentItem.treeType) {
            1 -> Pair("Apel", R.drawable.appleicon)
            2 -> Pair("Mangga", R.drawable.mangoicon)
            3 -> Pair("Jeruk", R.drawable.orangeicon)
            else -> Pair("Unknown", R.drawable.baseline_question_mark_24)
        }

        holder.treeName.text = currentItem.treeName
        holder.treeType.text = treeTypeLabel
        holder.treeCondition.text = currentItem.treeCond
        holder.treeIcon.setImageResource(treeIconRes) // Set the tree icon

        // Pass the tree's document ID to the TreeProfile activity
        holder.itemView.setOnClickListener {
            onItemClick(currentItem.treeId ?: "")
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val treeName: TextView = itemView.findViewById(R.id.treeName)
        val treeType: TextView = itemView.findViewById(R.id.treeType)
        val treeCondition: TextView = itemView.findViewById(R.id.treeCondition)
        val treeIcon: ImageView = itemView.findViewById(R.id.treeImg) // Add ImageView
    }
}