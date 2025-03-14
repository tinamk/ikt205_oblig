package com.example.applikasjons_avokadoene.adapters

import androidx.recyclerview.widget.DiffUtil
import com.example.applikasjons_avokadoene.models.Course

// CourseDiffCallback is used to calculate the difference between two lists of courses

class CourseDiffCallback(
    private val oldList: List<Course>,
    private val newList: List<Course>
) : DiffUtil.Callback() {


    override fun getOldListSize(): Int = oldList.size
    override fun getNewListSize(): Int = newList.size


    // Check if items are the same by comparing their IDs
    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].id == newList[newItemPosition].id
    }


    // Check if contents of items are the same
    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
}
