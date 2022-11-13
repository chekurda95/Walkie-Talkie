package com.chekurda.common.base_list

import androidx.recyclerview.widget.DiffUtil

fun <T : ComparableItem<T>>calculateDiff(old: List<T>, new: List<T>): DiffUtil.DiffResult =
    DiffUtil.calculateDiff(DiffUtilCallback(old, new))

private class DiffUtilCallback<T : ComparableItem<T>>(
    private val old: List<T>,
    private val new: List<T>
) : DiffUtil.Callback() {

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
        old[oldItemPosition].areItemsTheSame(new[newItemPosition])

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
        old[oldItemPosition].areItemsTheSame(new[newItemPosition])

    override fun getOldListSize(): Int = old.size
    override fun getNewListSize(): Int = new.size
}