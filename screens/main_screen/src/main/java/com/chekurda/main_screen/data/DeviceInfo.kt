package com.chekurda.main_screen.data

import com.chekurda.common.base_list.ComparableItem

internal data class DeviceInfo(
    val address: String,
    val name: String
) : ComparableItem<DeviceInfo> {

    override fun areItemsTheSame(anotherItem: DeviceInfo): Boolean =
        address == anotherItem.address

    override fun areContentsTheSame(anotherItem: DeviceInfo): Boolean =
        this == anotherItem
}