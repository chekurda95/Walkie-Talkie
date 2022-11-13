package com.chekurda.common.base_list

interface ComparableItem<T> {

    fun areItemsTheSame(anotherItem: T): Boolean

    fun areContentsTheSame(anotherItem: T): Boolean
}