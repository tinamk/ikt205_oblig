package com.example.applikasjons_avokadoene.adapters

import androidx.recyclerview.widget.DiffUtil
import com.example.applikasjons_avokadoene.models.Course

class CourseDiffCallback(
    private val oldList: List<Course>,
    private val newList: List<Course>
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldList.size
    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].id == newList[newItemPosition].id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
}
